package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forum.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /**
     * 按作用域分页查询公告（前台只读，过滤隐藏+已删）
     * pinned=1 置顶优先，再按 sort_weight DESC, created_at DESC
     */
    @Select("<script>" +
            "SELECT * FROM announcement" +
            " WHERE scope = #{scope}" +
            " AND status = 1 AND deleted = 0" +
            "<if test='scope == \"board\"'> AND board_id = #{boardId}</if>" +
            "<if test='scope == \"site\"'> AND board_id IS NULL</if>" +
            " ORDER BY pinned DESC, sort_weight DESC, created_at DESC" +
            "</script>")
    IPage<Announcement> selectByScope(IPage<Announcement> page,
                                       @Param("scope") String scope,
                                       @Param("boardId") Long boardId);

    /**
     * 后台管理列表（含隐藏，含已删）
     */
    @Select("<script>" +
            "SELECT * FROM announcement WHERE 1=1" +
            "<if test='scope != null and scope != \"\"'> AND scope = #{scope}</if>" +
            "<if test='boardId != null'> AND board_id = #{boardId}</if>" +
            " ORDER BY created_at DESC" +
            "</script>")
    IPage<Announcement> adminList(IPage<Announcement> page,
                                   @Param("scope") String scope,
                                   @Param("boardId") Long boardId);
}
