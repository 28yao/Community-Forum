package com.forum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.ErrorCode;
import com.forum.common.PageResult;
import com.forum.common.exception.BizException;
import com.forum.entity.Board;
import com.forum.entity.Post;
import com.forum.entity.PostImage;
import com.forum.entity.User;
import com.forum.mapper.BoardMapper;
import com.forum.mapper.PostImageMapper;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索服务（M5-T3）
 *
 * 基于 MySQL FULLTEXT + ngram 的关键词搜索。
 * 关键词需 ≥ 2 字符；BOOLEAN MODE 下需对特殊字符做转义防注入。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;

    /** BOOLEAN MODE 保留字符：+ - > < ( ) ~ * " @ */
    private static final String BOOLEAN_RESERVED = "+-><()~*\"@";

    private static final int MIN_KEYWORD_LENGTH = 2;
    private static final int MAX_PAGE_SIZE = 50;

    /**
     * 搜索帖子
     *
     * @param keyword 关键词
     * @param page    页码（从 1 起）
     * @param size    每页大小
     */
    public PageResult<Map<String, Object>> search(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请输入搜索关键词");
        }
        String trimmed = keyword.trim();
        if (trimmed.length() < MIN_KEYWORD_LENGTH) {
            throw new BizException(ErrorCode.PARAM_INVALID, "关键词至少 2 个字符");
        }
        if (page < 1) page = 1;
        if (size < 1 || size > MAX_PAGE_SIZE) size = 20;

        String escaped = escapeBoolean(trimmed);
        // 直接使用转义后的关键词；如要更强相关性也可拼前缀星号
        Page<Post> p = new Page<>(page, size);
        IPage<Post> result = postMapper.searchByKeyword(p, escaped);

        if (result.getRecords().isEmpty()) {
            return PageResult.empty(page, size);
        }

        // 批量取作者 + 版块 + 首图
        List<Long> userIds = result.getRecords().stream().map(Post::getUserId).distinct().collect(Collectors.toList());
        List<Long> boardIds = result.getRecords().stream().map(Post::getBoardId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, x -> x));
        Map<Long, Board> boardMap = boardMapper.selectBatchIds(boardIds).stream()
                .collect(Collectors.toMap(Board::getId, x -> x));

        List<Map<String, Object>> items = result.getRecords().stream().map(post -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", post.getId());
            m.put("boardId", post.getBoardId());
            m.put("title", post.getTitle());
            m.put("summary", buildSummary(post.getContent()));
            m.put("likeCount", post.getLikeCount());
            m.put("commentCount", post.getCommentCount());
            m.put("viewCount", post.getViewCount());
            m.put("isPinned", post.getIsPinned());
            m.put("isEdited", post.getIsEdited());
            m.put("createdAt", post.getCreatedAt());

            // 首图
            List<PostImage> imgs = postImageMapper.selectByPostId(post.getId());
            m.put("firstImage", imgs.isEmpty() ? null : imgs.get(0).getUrl());

            // 作者
            User u = userMap.get(post.getUserId());
            Map<String, Object> author = new HashMap<>();
            author.put("id", post.getUserId());
            author.put("nickname", u != null ? u.getNickname() : "已注销");
            author.put("avatar", u != null ? u.getAvatar() : null);
            m.put("author", author);

            // 版块
            Board b = boardMap.get(post.getBoardId());
            Map<String, Object> board = new HashMap<>();
            board.put("id", post.getBoardId());
            board.put("name", b != null ? b.getName() : "未知版块");
            m.put("board", board);

            return m;
        }).collect(Collectors.toList());

        log.info("[SEARCH] keyword='{}' page={} size={} total={}", trimmed, page, size, result.getTotal());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), items);
    }

    /** BOOLEAN MODE 关键字转义：去掉保留字符，避免 SQL 注入和操作符冲突 */
    private String escapeBoolean(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (BOOLEAN_RESERVED.indexOf(c) < 0) {
                sb.append(c);
            } else {
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    /** 简单摘要：去 HTML 标签后取前 120 字 */
    private String buildSummary(String content) {
        if (content == null) return "";
        String plain = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        return plain.length() > 120 ? plain.substring(0, 120) + "..." : plain;
    }
}
