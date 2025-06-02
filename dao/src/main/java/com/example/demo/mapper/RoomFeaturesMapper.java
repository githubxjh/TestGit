package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.RoomFeatures;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 房间特征数据访问层
 * 继承MyBatis-Plus的BaseMapper，提供基础CRUD操作
 */
@Mapper
@Repository
public interface RoomFeaturesMapper extends BaseMapper<RoomFeatures> {

    /**
     * 根据房间ID列表查询房间特征（字符串参数版本）
     */
    @Select("SELECT * FROM room_features WHERE room_id IN (${roomIds})")
    List<RoomFeatures> selectByRoomIdList(@Param("roomIds") String roomIds);

    /**
     * 根据单个房间ID查询房间特征
     */
    @Select("SELECT * FROM room_features WHERE room_id = #{roomId}")
    RoomFeatures selectByRoomId(@Param("roomId") String roomId);

    /**
     * 根据价格等级查询房间特征
     */
    @Select("SELECT * FROM room_features WHERE price_level = #{priceLevel}")
    List<RoomFeatures> selectByPriceLevel(@Param("priceLevel") Integer priceLevel);

    /**
     * 根据星级查询房间特征
     */
    @Select("SELECT * FROM room_features WHERE star_level = #{starLevel}")
    List<RoomFeatures> selectByStarLevel(@Param("starLevel") Integer starLevel);

    /**
     * 根据价格等级范围查询房间特征
     */
    @Select("SELECT * FROM room_features WHERE price_level BETWEEN #{minLevel} AND #{maxLevel}")
    List<RoomFeatures> selectByPriceLevelRange(@Param("minLevel") Integer minLevel,
                                               @Param("maxLevel") Integer maxLevel);

    /**
     * 根据地理位置范围查询房间特征
     */
    @Select("SELECT * FROM room_features WHERE " +
            "longitude BETWEEN #{minLng} AND #{maxLng} AND " +
            "latitude BETWEEN #{minLat} AND #{maxLat}")
    List<RoomFeatures> selectByLocationRange(@Param("minLng") Double minLng,
                                             @Param("maxLng") Double maxLng,
                                             @Param("minLat") Double minLat,
                                             @Param("maxLat") Double maxLat);

    /**
     * 根据标签查询房间特征（JSON包含查询）
     */
    @Select("SELECT * FROM room_features WHERE JSON_CONTAINS(tags, #{tag})")
    List<RoomFeatures> selectByTag(@Param("tag") String tag);

    /**
     * 查询所有有特征向量的房间
     */
    @Select("SELECT * FROM room_features WHERE feature_vector IS NOT NULL AND feature_vector != ''")
    List<RoomFeatures> selectAllWithFeatureVector();

    /**
     * 统计房间特征总数
     */
    @Select("SELECT COUNT(*) FROM room_features")
    long countAll();

    /**
     * 统计有特征向量的房间数量
     */
    @Select("SELECT COUNT(*) FROM room_features WHERE feature_vector IS NOT NULL AND feature_vector != ''")
    long countWithFeatureVector();

    /**
     * 批量插入房间特征（如果不存在则插入，存在则更新）
     * 修正：使用@Insert注解
     */
    @Insert("INSERT INTO room_features (room_id, feature_vector, tags, longitude, latitude, price_level, star_level) " +
            "VALUES (#{roomId}, #{featureVector}, #{tags}, #{longitude}, #{latitude}, #{priceLevel}, #{starLevel}) " +
            "ON DUPLICATE KEY UPDATE " +
            "feature_vector = VALUES(feature_vector), " +
            "tags = VALUES(tags), " +
            "longitude = VALUES(longitude), " +
            "latitude = VALUES(latitude), " +
            "price_level = VALUES(price_level), " +
            "star_level = VALUES(star_level), " +
            "updated_at = NOW()")
    boolean insertOrUpdate(RoomFeatures roomFeatures);

    /**
     * 删除没有对应房间记录的特征数据（清理孤立数据）
     * 修正：使用@Delete注解
     */
    @Delete("DELETE FROM room_features WHERE room_id NOT IN (SELECT room_id FROM room)")
    int deleteOrphanedFeatures();

    /**
     * 更新房间特征向量
     * 修正：使用@Update注解
     */
    @Update("UPDATE room_features SET feature_vector = #{featureVector}, updated_at = NOW() WHERE room_id = #{roomId}")
    int updateFeatureVector(@Param("roomId") String roomId, @Param("featureVector") String featureVector);

    /**
     * 更新房间标签
     * 修正：使用@Update注解
     */
    @Update("UPDATE room_features SET tags = #{tags}, updated_at = NOW() WHERE room_id = #{roomId}")
    int updateTags(@Param("roomId") String roomId, @Param("tags") String tags);

    /**
     * 批量查询房间特征的辅助方法
     */
    default List<RoomFeatures> selectByRoomIds(Collection<String> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return java.util.Collections.emptyList(); // 兼容Java 8
        }

        // 构建安全的IN查询参数
        String ids = roomIds.stream()
                .map(id -> "'" + id.replace("'", "''") + "'") // 防SQL注入
                .collect(java.util.stream.Collectors.joining(","));

        return selectByRoomIdList(ids);
    }
}