package com.example.demo.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Double[].class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class DoubleArrayTypeHandler extends BaseTypeHandler<Double[]> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Double[] parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting Double[] to JSON", e);
        }
    }

    @Override
    public Double[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseDoubleArray(value);
    }

    @Override
    public Double[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseDoubleArray(value);
    }

    @Override
    public Double[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseDoubleArray(value);
    }

    private Double[] parseDoubleArray(String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, Double[].class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to Double[]", e);
        }
    }
}