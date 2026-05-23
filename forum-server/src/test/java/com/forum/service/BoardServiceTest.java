package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.entity.Board;
import com.forum.mapper.BoardMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BoardService 测试（M2-T3）
 */
@SpringBootTest
@ActiveProfiles("dev")
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardMapper boardMapper;

    @Test
    void listEnabled_shouldReturnSeededBoards() {
        List<Board> list = boardService.listEnabled();
        assertNotNull(list);
        assertTrue(list.size() >= 3, "种子至少应有 3 个版块");
        // 状态都应为 1
        list.forEach(b -> assertEquals(1, b.getStatus()));
        // 排序：sort_weight 倒序
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getSortWeight() >= list.get(i).getSortWeight(),
                    "应该按 sort_weight 倒序");
        }
    }

    @Test
    void getById_validId_shouldReturn() {
        Board b = boardService.getById(1L);
        assertNotNull(b);
        assertEquals(1L, b.getId());
    }

    @Test
    void getById_notExist_shouldThrow3003() {
        BizException ex = assertThrows(BizException.class, () -> boardService.getById(99999L));
        assertEquals(ErrorCode.BOARD_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void getById_disabled_shouldThrow3003() {
        Board b = boardMapper.selectById(1L);
        b.setStatus(0);
        boardMapper.updateById(b);
        // listEnabled 应该不返回它
        List<Board> list = boardService.listEnabled();
        assertTrue(list.stream().noneMatch(x -> x.getId().equals(1L)));
        // getById 也应该抛 3003
        BizException ex = assertThrows(BizException.class, () -> boardService.getById(1L));
        assertEquals(ErrorCode.BOARD_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @Transactional
    @Rollback
    void incrPostCount_shouldWork() {
        Board before = boardMapper.selectById(1L);
        int beforeCount = before.getPostCount();
        boardMapper.incrPostCount(1L);
        Board after = boardMapper.selectById(1L);
        assertEquals(beforeCount + 1, after.getPostCount());
    }
}
