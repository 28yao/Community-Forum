package com.forum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Announcement;
import com.forum.entity.Board;
import com.forum.mapper.AnnouncementMapper;
import com.forum.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private static final int MAX_TITLE_LEN = 50;
    private static final int MAX_CONTENT_LEN = 1000;

    private final AnnouncementMapper announcementMapper;
    private final BoardMapper boardMapper;

    // ========== 前台只读 ==========

    /** 站点公告列表（前台） */
    public IPage<Announcement> listSite(int page, int size) {
        return announcementMapper.selectByScope(new Page<>(page, size), "site", null);
    }

    /** 板块公告列表（前台） */
    public IPage<Announcement> listBoard(Long boardId, int page, int size) {
        return announcementMapper.selectByScope(new Page<>(page, size), "board", boardId);
    }

    // ========== 后台管理（admin） ==========

    /** 后台公告列表（含隐藏） */
    public IPage<Announcement> adminList(String scope, Long boardId, int page, int size) {
        return announcementMapper.adminList(new Page<>(page, size), scope, boardId);
    }

    /** 创建公告 */
    @Transactional(rollbackFor = Exception.class)
    public Announcement create(String scope, Long boardId, String title, String content,
                                Integer pinned, Integer sortWeight, Long publisherId, boolean isAdmin) {
        validateScope(scope, boardId);
        assertCanManage(scope, boardId, publisherId, isAdmin);
        validateFields(title, content);

        Announcement a = new Announcement();
        a.setScope(scope);
        a.setBoardId("board".equals(scope) ? boardId : null);
        a.setTitle(title);
        a.setContent(content);
        a.setPinned(pinned == null ? 0 : pinned);
        a.setSortWeight(sortWeight == null ? 0 : sortWeight);
        a.setPublisherId(publisherId);
        a.setStatus(1);
        announcementMapper.insert(a);
        log.info("[ANNOUNCEMENT] created id={} scope={} board={} by={}", a.getId(), scope, boardId, publisherId);
        return a;
    }

    /** 编辑公告 */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, String title, String content, Integer pinned, Integer sortWeight, Integer status,
                        Long currentUserId, boolean isAdmin) {
        Announcement a = mustFind(id);
        assertCanManage(a.getScope(), a.getBoardId(), currentUserId, isAdmin);
        validateFields(title, content);

        if (title != null) a.setTitle(title);
        if (content != null) a.setContent(content);
        if (pinned != null) a.setPinned(pinned);
        if (sortWeight != null) a.setSortWeight(sortWeight);
        if (status != null) a.setStatus(status);
        announcementMapper.updateById(a);
        log.info("[ANNOUNCEMENT] updated id={} by={}", id, currentUserId);
    }

    /** 删除公告（软删除） */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        Announcement a = mustFind(id);
        assertCanManage(a.getScope(), a.getBoardId(), currentUserId, isAdmin);
        announcementMapper.deleteById(id);
        log.info("[ANNOUNCEMENT] deleted id={} by={}", id, currentUserId);
    }

    // ========== 内部方法 ==========

    private Announcement mustFind(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) {
            throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        return a;
    }

    private void validateScope(String scope, Long boardId) {
        if (!"site".equals(scope) && !"board".equals(scope)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "scope 必须为 site 或 board");
        }
        if ("board".equals(scope)) {
            if (boardId == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "scope=board 时 boardId 不能为空");
            }
            Board b = boardMapper.selectById(boardId);
            if (b == null) {
                throw new BizException(ErrorCode.BOARD_NOT_FOUND);
            }
        }
    }

    private void validateFields(String title, String content) {
        if (title != null && title.length() > MAX_TITLE_LEN) {
            throw new BizException(ErrorCode.PARAM_INVALID, "公告标题不能超过 " + MAX_TITLE_LEN + " 字");
        }
        if (content != null && content.length() > MAX_CONTENT_LEN) {
            throw new BizException(ErrorCode.PARAM_INVALID, "公告内容不能超过 " + MAX_CONTENT_LEN + " 字");
        }
    }

    /**
     * 权限校验：site 仅 admin；board 为 admin 或该板块吧主
     */
    private void assertCanManage(String scope, Long boardId, Long userId, boolean isAdmin) {
        if ("site".equals(scope)) {
            if (!isAdmin) {
                throw new BizException(ErrorCode.FORBIDDEN, "仅管理员可管理站点公告");
            }
        } else {
            // board scope: admin 或吧主
            if (!isAdmin) {
                Board b = boardMapper.selectById(boardId);
                if (b == null || !userId.equals(b.getOwnerUserId())) {
                    throw new BizException(ErrorCode.FORBIDDEN, "仅管理员或吧主可管理板块公告");
                }
            }
        }
    }
}
