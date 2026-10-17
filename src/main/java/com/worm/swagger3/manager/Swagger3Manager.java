package com.worm.swagger3.manager;

import com.worm.swagger3.properties.Swagger3Properties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootVersion;
import org.springframework.stereotype.Component;

/**
 * Swagger3 核心配置管理器（springdoc 版本）
 * <p>
 * 该类负责创建 springdoc {@link OpenAPI} Bean，配置 API 文档的基本信息和安全认证方案。
 * 替代原有的 springfox {@code DocumentationPluginsManager} 实现。
 * </p>
 *
 * @author 姚文兵
 * @version 2.0.0
 * @since 2019-05-20
 */
@Slf4j
@Component
public class Swagger3Manager {

    private static final String AUTHORIZATION = "Authorization";

    /**
     * Swagger3 配置属性
     */
    private final Swagger3Properties swagger3Properties;

    /**
     * 构造函数
     *
     * @param swagger3Properties Swagger3 配置属性
     */
    public Swagger3Manager(Swagger3Properties swagger3Properties) {
        this.swagger3Properties = swagger3Properties;
    }

    /**
     * 创建 OpenAPI 配置 Bean
     * <p>
     * 配置 API 文档基本信息（标题、版本、描述、联系人等）和安全认证方案（API Key）。
     * </p>
     *
     * @return OpenAPI 配置对象
     */
    public OpenAPI createOpenAPI() {
        log.debug("【Swagger管理器】创建 OpenAPI 配置");

        return new OpenAPI()
                .info(buildInfo())
                .components(new Components()
                        .addSecuritySchemes(AUTHORIZATION,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(AUTHORIZATION)))
                .addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION));
    }

    private Info buildInfo() {
        // 为什么：starter 不引入第三方字符串工具，标题与版本行以拼接实现同等格式 —— 干什么：拼接标题与版本描述
        return new Info()
                .title(swagger3Properties.getApplicationName() + " REST ful API 文档")
                .version("应用版本: " + swagger3Properties.getVersion()
                        + ", Spring Boot 版本: " + SpringBootVersion.getVersion())
                .description(swagger3Properties.getDescription())
                .contact(new Contact()
                        .name(swagger3Properties.getContactName())
                        .url(swagger3Properties.getContactUrl())
                        .email(swagger3Properties.getContactMail()))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }
}
