package com.forum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.PageResult;
import com.forum.entity.Post;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserBoardFollowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合信息流服务（P2-M5）
 *
 * 匿名/无关注 → 全站热门 Top N
 * 有关注 → 70% 关注板块最新 + 30% 全站热门（不足部分用热门补）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final PostMapper postMapper;
    private final UserBoardFollowMapper followMapper;
    private final PostService postService;

    private static final double FOLLOW_RATIO = 0.7;

    public PageResult<Map<String, Object>> getFeed(Long userId, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        // 匿名或无关注 → 全站热门
        if (userId == null) {
            return hotFeed(page, size);
        }

        List<Long> followedBoardIds = followMapper.selectBoardIdsByUserId(userId);
        if (followedBoardIds.isEmpty()) {
            return hotFeed(page, size);
        }

        // 有关注：70% 关注板块 + 30% 热门
        int followSize = (int) Math.ceil(size * FOLLOW_RATIO);
        int hotSize = size - followSize;

        // 关注板块帖子（多取一些用于去重后裁剪）
        IPage<Post> followPage = postMapper.selectByBoardIds(
                new Page<>(1, followSize + hotSize), followedBoardIds);
        List<Post> followPosts = followPage.getRecords();

        // 热门帖子
        IPage<Post> hotPage = postMapper.selectHotPosts(new Page<>(1, size + followPosts.size()));
        List<Post> hotPosts = hotPage.getRecords();

        // 去重：热门中排除关注已有的
        Set<Long> followIds = followPosts.stream().map(Post::getId).collect(Collectors.toSet());
        List<Post> hotFiltered = hotPosts.stream()
                .filter(p -> !followIds.contains(p.getId()))
                .collect(Collectors.toList());

        // 合并：关注取 followSize，热门取 hotSize
        List<Post> merged = new ArrayList<>();
        merged.addAll(followPosts.stream().limit(followSize).collect(Collectors.toList()));
        merged.addAll(hotFiltered.stream().limit(hotSize).collect(Collectors.toList()));

        List<Map<String, Object>> items = merged.stream()
                .map(postService::toListItem)
                .collect(Collectors.toList());

        // total 不精确（混合流无法精确计数），给一个示意值
        long total = followPage.getTotal() + hotPage.getTotal();
        return PageResult.of(total, page, size, items);
    }

    private PageResult<Map<String, Object>> hotFeed(int page, int size) {
        IPage<Post> p = postMapper.selectHotPosts(new Page<>(page, size));
        List<Map<String, Object>> items = p.getRecords().stream()
                .map(postService::toListItem)
                .collect(Collectors.toList());
        return PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), items);
    }
}
