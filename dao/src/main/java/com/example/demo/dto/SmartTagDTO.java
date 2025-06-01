package com.example.demo.dto;

/**
 * 智能标签DTO
 * 用于封装用户偏好标签和权重信息
 */
public class SmartTagDTO {

    private String name; // 标签名称
    private Double weight; // 标签权重（0-1之间）
    private String displayName; // 显示名称（可本地化）
    private String category; // 标签分类
    private String description; // 标签描述
    private String color; // 标签颜色（前端展示用）
    private Boolean isHot; // 是否为热门标签
    private Integer usageCount; // 使用次数

    /**
     * 默认构造函数
     */
    public SmartTagDTO() {
    }

    /**
     * 基础构造函数
     */
    public SmartTagDTO(String name, Double weight) {
        this.name = name;
        this.weight = weight;
        this.displayName = name; // 默认显示名称与标签名称相同
    }

    /**
     * 完整构造函数
     */
    public SmartTagDTO(String name, Double weight, String displayName, String category) {
        this.name = name;
        this.weight = weight;
        this.displayName = displayName;
        this.category = category;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsHot() {
        return isHot;
    }

    public void setIsHot(Boolean isHot) {
        this.isHot = isHot;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    /**
     * 获取格式化的权重百分比
     */
    public String getWeightPercentage() {
        if (weight == null) {
            return "0%";
        }
        return String.format("%.0f%%", weight * 100);
    }

    /**
     * 获取权重等级描述
     */
    public String getWeightLevel() {
        if (weight == null || weight < 0.2) {
            return "低";
        } else if (weight < 0.5) {
            return "中";
        } else if (weight < 0.8) {
            return "高";
        } else {
            return "极高";
        }
    }

    /**
     * 判断是否为高权重标签
     */
    public boolean isHighWeight() {
        return weight != null && weight >= 0.7;
    }

    /**
     * 判断是否为有效标签（权重大于阈值）
     */
    public boolean isValid() {
        return weight != null && weight > 0.1 && name != null && !name.trim().isEmpty();
    }

    /**
     * 获取标签的默认颜色（基于权重）
     */
    public String getDefaultColor() {
        if (weight == null) {
            return "#gray";
        } else if (weight >= 0.8) {
            return "#red"; // 高权重-红色
        } else if (weight >= 0.5) {
            return "#orange"; // 中高权重-橙色
        } else if (weight >= 0.3) {
            return "#yellow"; // 中权重-黄色
        } else {
            return "#blue"; // 低权重-蓝色
        }
    }

    /**
     * 创建热门标签
     */
    public static SmartTagDTO createHotTag(String name, Double weight) {
        SmartTagDTO tag = new SmartTagDTO(name, weight);
        tag.setIsHot(true);
        tag.setColor(tag.getDefaultColor());
        return tag;
    }

    /**
     * 创建分类标签
     */
    public static SmartTagDTO createCategoryTag(String name, Double weight, String category) {
        SmartTagDTO tag = new SmartTagDTO(name, weight);
        tag.setCategory(category);
        tag.setColor(tag.getDefaultColor());
        return tag;
    }

    @Override
    public String toString() {
        return "SmartTagDTO{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                ", displayName='" + displayName + '\'' +
                ", category='" + category + '\'' +
                ", isHot=" + isHot +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmartTagDTO that = (SmartTagDTO) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}