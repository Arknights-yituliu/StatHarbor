package org.yituliu.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 注册API认证拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RedisTemplate<String, Object> redisTemplate;

    public WebMvcConfig(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiAuthInterceptor(redisTemplate))
                .addPathPatterns("/v1/**");
    }
}
