package com.worm.swagger3.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Long 类型精度自动配置类
 * <p>
 * 解决 JavaScript 中 Long 类型超过 Number.MAX_SAFE_INTEGER 导致的精度丢失问题。
 * JavaScript 的 Number 类型最大安全整数是 9007199254740991 (2^53 - 1)，
 * Java 的 Long 类型最大值是 9223372036854775807 (2^63 - 1)，
 * 当后端返回的 Long 值超过 JavaScript 安全范围时，前端会丢失精度。
 * 本配置类向 Jackson 注册 Long → String 序列化模块，将所有 Long 类型值序列化为字符串，
 * 避免前端 JavaScript 精度丢失。Spring Boot 2.7 默认 JSON 框架为 Jackson 2。
 * </p>
 *
 * <h3>启用/禁用：</h3>
 * <pre>
 * worm:
 *   swagger3:
 *     long-to-string: false  # 禁用（默认启用）
 * </pre>
 *
 * @author 姚文兵
 * @since 1.0.9
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "worm.swagger3", name = "long-to-string",
        havingValue = "true", matchIfMissing = true)
public class LongPrecisionAutoConfiguration {

    /**
     * Long 转 String 的 Jackson 序列化模块
     * <p>
     * 将所有 Long 类型（含基本类型 long）序列化为字符串，避免前端精度丢失。
     * Spring Boot 会自动将该模块注册到容器内的 Jackson ObjectMapper。
     * </p>
     *
     * @return Long → String 序列化模块
     */
    @Bean
    public SimpleModule longToStringModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        log.info("【Long精度处理】已配置 Long 类型序列化为 String，避免前端精度丢失");
        return module;
    }
}
