package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND behavior_type = #{behaviorType} ORDER BY timestamp DESC LIMIT #{limit}")
    List<UserBehavior> findRecentBehaviors(@Param("userId") Long userId,
                                           @Param("behaviorType") String behaviorType,
                                           @Param("limit") int limit);

    @Select("SELECT room_id, COUNT(*) as click_count FROM user_behavior WHERE behavior_type = 'click' AND timestamp > #{since} GROUP BY room_id ORDER BY click_count DESC LIMIT #{limit}")
    List<Map<String, Object>> findPopularRooms(@Param("since") Timestamp since, @Param("limit") int limit);
}

