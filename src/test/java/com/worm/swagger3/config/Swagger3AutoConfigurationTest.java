package com.worm.swagger3.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.worm.swagger3.knife4j.Knife4jApplicationRunner;
import com.worm.swagger3.manager.Swagger3Manager;
import com.worm.swagger3.properties.Swagger3Properties;
import com.worm.swagger3.runner.SwaggerApplicationRunner;
import com.worm.swagger3.service.IDocService;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Swagger3 自动装配测试：开关语义与 Bean 注册。
 *
 * 为什么：starter 的开关（enabled / long-to-string）直接决定文档功能是否生效
 * —— 干什么：以 ApplicationContextRunner / WebApplicationContextRunner 轻量验证
 */
class Swagger3AutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Swagger3ConfigAutoConfiguration.class));

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LongPrecisionAutoConfiguration.class));

    @Test
    @DisplayName("默认：文档配置、OpenAPI、两个地址打印监听器与文档服务全部注册")
    void allBeansRegisteredByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Swagger3Properties.class);
            assertThat(context).hasSingleBean(Swagger3Manager.class);
            assertThat(context).hasSingleBean(OpenAPI.class);
            assertThat(context).hasSingleBean(SwaggerApplicationRunner.class);
            assertThat(context).hasSingleBean(Knife4jApplicationRunner.class);
            assertThat(context).hasSingleBean(IDocService.class);
        });
    }

    @Test
    @DisplayName("worm.swagger3.enabled=false：整个自动配置不生效")
    void disabledByProperty() {
        contextRunner.withPropertyValues("worm.swagger3.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(Swagger3Manager.class);
            assertThat(context).doesNotHaveBean(OpenAPI.class);
            assertThat(context).doesNotHaveBean(SwaggerApplicationRunner.class);
            assertThat(context).doesNotHaveBean(Knife4jApplicationRunner.class);
            assertThat(context).doesNotHaveBean(IDocService.class);
        });
    }

    @Test
    @DisplayName("LongPrecision：Web 环境下默认注册 Long 序列化模块")
    void longPrecisionModuleRegisteredByDefault() {
        webContextRunner.run(context -> assertThat(context).hasSingleBean(SimpleModule.class));
    }

    @Test
    @DisplayName("LongPrecision：long-to-string=false 时不注册")
    void longPrecisionModuleDisabledByProperty() {
        webContextRunner.withPropertyValues("worm.swagger3.long-to-string=false")
                .run(context -> assertThat(context).doesNotHaveBean(SimpleModule.class));
    }
}
