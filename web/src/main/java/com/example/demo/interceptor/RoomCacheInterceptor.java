package com.example.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


/**
 * 缓存状态拦截器 - 用于在页面上显示缓存状态
 */
@Component
public class RoomCacheInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ROOM_LIST_KEY = "room:list:status:";
    private static final String ROOM_DETAIL_KEY = "room:detail:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            // 处理房间列表页
            String path = request.getRequestURI();
            if (path.equals("/room-brand")) {
                String cacheKey = ROOM_LIST_KEY + "1"; // 活跃房间

                boolean fromCache = redisTemplate.hasKey(cacheKey);

                // 在模型中添加缓存状态信息
                modelAndView.addObject("fromCache", fromCache);
                modelAndView.addObject("cacheKey", cacheKey);
            }

            // 处理房间详情页
            else if (path.matches("/offlineroom/.*") || path.matches("/editroom/.*")) {
                // 从URL中提取房间ID
                String[] parts = path.split("/");
                String roomId = parts[parts.length - 1];

                // 直接使用字符串类型的roomId
                String cacheKey = ROOM_DETAIL_KEY + roomId;

                // 检查是否从缓存中获取
                boolean fromCache = redisTemplate.hasKey(cacheKey);

                // 在模型中添加缓存状态信息
                modelAndView.addObject("fromCache", fromCache);
                modelAndView.addObject("cacheKey", cacheKey);
            }
        }
    }
}