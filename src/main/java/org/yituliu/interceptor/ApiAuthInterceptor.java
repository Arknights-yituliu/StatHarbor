package org.yituliu.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.yituliu.common.enums.ResultCode;
import org.yituliu.common.exception.ServiceException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * API认证拦截器
 * 有Token：每分钟120次 / 每日10万次 / payload不超过16KB
 * 无Token：每分钟20次 / 每日1千次 / payload不超过4KB
 */
public class ApiAuthInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "Bearer ";                    // 请求头中Token的前缀标识
    private static final String REDIS_TOKEN_PREFIX = "api:token:";           // Redis中Token存储的key前缀
    private static final String RATE_LIMIT_MINUTE_PREFIX = "rate_limit:minute:"; // 每分钟限流Redis key前缀
    private static final String RATE_LIMIT_DAILY_PREFIX = "rate_limit:daily:";   // 每日限流Redis key前缀
    private static final long MAX_UNAUTH_PAYLOAD_BYTES = 4096L;              // 未认证用户最大payload：4KB
    private static final long MAX_AUTH_PAYLOAD_BYTES = 16384L;               // 已认证用户最大payload：16KB
    private static final long UNAUTH_PER_MINUTE = 20L;                       // 未认证用户每分钟最大请求数
    private static final long AUTH_PER_MINUTE = 120L;                        // 已认证用户每分钟最大请求数
    private static final long UNAUTH_PER_DAY = 1000L;                        // 未认证用户每日最大记录数
    private static final long AUTH_PER_DAY = 100000L;                        // 已认证用户每日最大记录数

    private final RedisTemplate<String, Object> redisTemplate;               // Redis操作模板，用于计数器缓存

    public ApiAuthInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;                                   // 注入RedisTemplate
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");              // 获取请求头中的Authorization
        boolean authenticated = false;                                        // 标记是否已认证，默认false
        String identifier = getClientIp(request);                            // 默认以客户端IP作为限流标识

        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {     // 检查是否携带Bearer Token
            String token = authHeader.substring(TOKEN_PREFIX.length()).trim(); // 提取Token值并去除首尾空格
            if (!token.isEmpty()) {                                           // Token不为空时进行Redis校验
                Object cachedToken = redisTemplate.opsForValue().get(REDIS_TOKEN_PREFIX + token); // 从Redis查询Token是否有效
                if (cachedToken != null) {                                    // Redis中存在该Token
                    authenticated = true;                                     // 标记为已认证
                    identifier = token;                                       // 以Token作为限流标识
                }
            }
        }

        checkMinuteRateLimit(identifier, authenticated);                     // ① 每分钟请求数限流检查
        checkDailyLimit(identifier, authenticated);                          // ② 每日记录数上限检查

        long maxPayload = authenticated ? MAX_AUTH_PAYLOAD_BYTES : MAX_UNAUTH_PAYLOAD_BYTES; // 根据认证状态选择不同的payload上限
        return checkPayloadSize(request, maxPayload);                        // ③ Payload大小检查
    }

    /**
     * 获取客户端IP
     * 优先从X-Forwarded-For头获取（支持反向代理），否则取直连IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");         // 获取反向代理传递的真实IP
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {             // 如果存在X-Forwarded-For头
            return xForwardedFor.split(",")[0].trim();                       // 取第一个IP（最原始的客户端IP）
        }
        return request.getRemoteAddr();                                      // 否则使用直连的远程地址
    }

    /**
     * 每分钟请求数限流
     * 使用Redis INCR实现滑动窗口计数器，首次请求设置60秒过期
     */
    private void checkMinuteRateLimit(String identifier, boolean authenticated) {
        String limitKey = RATE_LIMIT_MINUTE_PREFIX + identifier;             // 拼接限流key，如 rate_limit:minute:192.168.1.1
        long maxRequests = authenticated ? AUTH_PER_MINUTE : UNAUTH_PER_MINUTE; // 根据认证状态选择对应的每分钟上限

        Long count = redisTemplate.opsForValue().increment(limitKey);        // Redis原子自增，返回自增后的值
        if (count != null && count == 1) {                                   // 如果是该分钟内的第一次请求
            redisTemplate.expire(limitKey, 60, TimeUnit.SECONDS);            // 设置key在60秒后自动过期
        }
        if (count != null && count > maxRequests) {                          // 超过每分钟上限
            throw new ServiceException(ResultCode.TOO_MANY_REQUESTS);         // 抛出请求过于频繁异常
        }
    }

    /**
     * 每日记录数上限
     * 以北京时间（Asia/Shanghai）为基准，每天零点自动重置计数器
     */
    private void checkDailyLimit(String identifier, boolean authenticated) {
        String dateStr = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString(); // 获取当前北京时间日期字符串，如 2026-05-12
        String limitKey = RATE_LIMIT_DAILY_PREFIX + identifier + ":" + dateStr; // 拼接每日限流key，带上日期隔离
        long maxRecords = authenticated ? AUTH_PER_DAY : UNAUTH_PER_DAY;     // 根据认证状态选择对应的每日上限

        Long count = redisTemplate.opsForValue().increment(limitKey);        // Redis原子自增，返回自增后的值
        if (count != null && count == 1) {                                   // 如果是当天的第一次请求
            long secondsToEndOfDay = ChronoUnit.SECONDS.between(             // 计算距离当天24:00:00还剩多少秒
                    java.time.LocalDateTime.now(ZoneId.of("Asia/Shanghai")), // 当前北京时间
                    java.time.LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(1).atStartOfDay() // 次日00:00:00
            );
            redisTemplate.expire(limitKey, secondsToEndOfDay, TimeUnit.SECONDS); // key在零点自动过期，计数器重置
        }
        if (count != null && count > maxRecords) {                           // 超过每日上限
            throw new ServiceException(ResultCode.DAILY_RECORD_LIMIT_EXCEEDED); // 抛出每日记录数已达上限异常
        }
    }

    /**
     * 检查请求体大小，超过指定字节数则拒绝
     */
    private boolean checkPayloadSize(HttpServletRequest request, long maxBytes) {
        long contentLength = request.getContentLengthLong();                 // 从Content-Length头获取请求体字节数
        if (contentLength > maxBytes) {                                      // 超过上限
            throw new ServiceException(ResultCode.PAYLOAD_TOO_LARGE);         // 抛出请求体过大异常
        }
        return true;                                                          // 放行
    }
}
