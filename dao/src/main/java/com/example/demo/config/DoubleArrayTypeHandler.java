package com.example.demo.config;

import com.alibaba.fastjson2.JSON;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Double数组类型处理器
 * 用于处理MyBatis中Double[]与数据库VARCHAR/TEXT字段之间的转换
 */
@MappedTypes({Double[].class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR, JdbcType.CLOB})
public class DoubleArrayTypeHandler extends BaseTypeHandler<Double[]> {

    /**
     * 设置参数时将Double[]转换为JSON字符串存储到数据库
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Double[] parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.length == 0) {
            ps.setString(i, "[]");
        } else {
            ps.setString(i, JSON.toJSONString(parameter));
        }
    }

    /**
     * 从ResultSet中获取数据并转换为Double[]
     */
    @Override
    public Double[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String jsonStr = rs.getString(columnName);
        return parseJsonToDoubleArray(jsonStr);
    }

    /**
     * 从ResultSet中根据列索引获取数据并转换为Double[]
     */
    @Override
    public Double[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonStr = rs.getString(columnIndex);
        return parseJsonToDoubleArray(jsonStr);
    }

    /**
     * 从CallableStatement中获取数据并转换为Double[]
     */
    @Override
    public Double[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String jsonStr = cs.getString(columnIndex);
        return parseJsonToDoubleArray(jsonStr);
    }

    /**
     * 解析JSON字符串为Double数组
     */
    private Double[] parseJsonToDoubleArray(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return getDefaultDoubleArray();
        }

        try {
            // 处理空数组的情况
            if ("[]".equals(jsonStr.trim()) || "null".equals(jsonStr.trim())) {
                return getDefaultDoubleArray();
            }

            // 使用fastjson解析JSON字符串
            Double[] result = JSON.parseObject(jsonStr, Double[].class);

            // 如果解析结果为null或空数组，返回默认数组
            if (result == null || result.length == 0) {
                return getDefaultDoubleArray();
            }

            return result;

        } catch (Exception e) {
            // 解析失败时，记录警告并返回默认数组
            System.err.println("Warning: Failed to parse profileVector JSON: " + jsonStr + ", error: " + e.getMessage());
            return getDefaultDoubleArray();
        }
    }

    /**
     * 获取默认的Double数组（10维零向量）
     */
    private Double[] getDefaultDoubleArray() {
        Double[] defaultArray = new Double[10];
        for (int i = 0; i < 10; i++) {
            defaultArray[i] = 0.0;
        }
        return defaultArray;
    }
}
