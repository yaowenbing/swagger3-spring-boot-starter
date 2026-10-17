package com.worm.swagger3.config;

import com.worm.swagger3.knife4j.Knife4jApplicationRunner;
import com.worm.swagger3.manager.Swagger3Manager;
import com.worm.swagger3.properties.Swagger3Properties;
import com.worm.swagger3.runner.SwaggerApplicationRunner;
import com.worm.swagger3.service.IDocService;
import com.worm.swagger3.service.impl.DocServiceImpl;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger3 自动配置类（springdoc 版本）
 * <p>
 * 该类是 Swagger3 Spring Boot Starter 的核心自动配置类，
 * 使用 springdoc-openapi 替代原有的 springfox 实现。
 * </p>
 *
 * @author 姚文兵
 * @version 2.0.0
 * @see Swagger3Properties
 * @see Swagger3Manager
 * @since 2019-05-20
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({Swagger3Properties.class})
@ConditionalOnProperty(
        prefix = "worm.swagger3",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class Swagger3ConfigAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Swagger3Manager swagger3Manager(Swagger3Properties swagger3Properties) {
        log.debug("【Swagger自动配置】创建 Swagger3Manager");
        return new Swagger3Manager(swagger3Properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI customOpenAPI(Swagger3Manager swagger3Manager) {
        log.debug("【Swagger自动配置】创建 OpenAPI 配置");
        return swagger3Manager.createOpenAPI();
    }

    @Bean
    public SwaggerApplicationRunner swaggerApplicationRunner(IDocService iDocService, Swagger3Properties swagger3Properties) {
        log.debug("【Swagger自动配置】注册 Swagger UI 地址打印监听器");
        return new SwaggerApplicationRunner(iDocService, swagger3Properties);
    }

    @Bean
    public Knife4jApplicationRunner knife4jApplicationRunner(IDocService iDocService, Swagger3Properties swagger3Properties) {
        log.debug("【Swagger自动配置】注册 Knife4j UI 地址打印监听器");
        return new Knife4jApplicationRunner(iDocService, swagger3Properties);
    }

    @Bean
    public IDocService iDocService(ObjectProvider<ServerProperties> serverPropertiesProvider) {
        // 为什么：ServerProperties 仅在 Web 自动装配场景下才是 Bean，非 Web 精简容器中缺失
        // —— 干什么：经 ObjectProvider 延迟获取，缺失时 DocServiceImpl 退化为仅使用事件实际端口
        log.debug("【Swagger自动配置】注册文档服务");
        return new DocServiceImpl(serverPropertiesProvider);
    }
}
