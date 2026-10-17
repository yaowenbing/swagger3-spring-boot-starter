package com.worm.swagger3.manager;

import com.worm.swagger3.properties.Swagger3Properties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Swagger3Manager 测试：OpenAPI 文档信息与安全方案装配。
 *
 * 为什么：OpenAPI Bean 是文档页的元数据来源，标题/版本拼接格式与 Authorization 安全方案
 * 直接决定文档页展示与 UI 鉴权输入框 —— 干什么：逐字段钉死装配结果
 */
class Swagger3ManagerTest {

    private Swagger3Manager manager;

    @BeforeEach
    void setUp() {
        Swagger3Properties properties = new Swagger3Properties();
        properties.setApplicationName("订单服务");
        properties.setVersion("V 2.0.0");
        properties.setDescription("订单域接口文档");
        properties.setContactName("张三");
        properties.setContactUrl("https://example.com");
        properties.setContactMail("zhangsan@example.com");
        manager = new Swagger3Manager(properties);
    }

    @Test
    @DisplayName("OpenAPI：标题拼接应用名，版本行含应用版本与 Spring Boot 版本")
    void openApiInfo() {
        OpenAPI openAPI = manager.createOpenAPI();

        Info info = openAPI.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("订单服务 REST ful API 文档");
        assertThat(info.getVersion()).contains("V 2.0.0").contains("Spring Boot 版本");
        assertThat(info.getDescription()).isEqualTo("订单域接口文档");

        Contact contact = info.getContact();
        assertThat(contact.getName()).isEqualTo("张三");
        assertThat(contact.getUrl()).isEqualTo("https://example.com");
        assertThat(contact.getEmail()).isEqualTo("zhangsan@example.com");

        License license = info.getLicense();
        assertThat(license.getName()).isEqualTo("Apache 2.0");
        assertThat(license.getUrl()).isEqualTo("https://www.apache.org/licenses/LICENSE-2.0");
    }

    @Test
    @DisplayName("OpenAPI：Authorization 安全方案（APIKEY + HEADER）并加入全局安全要求")
    void openApiSecurityScheme() {
        OpenAPI openAPI = manager.createOpenAPI();

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("Authorization");
        assertThat(scheme).isNotNull();
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.APIKEY);
        assertThat(scheme.getIn()).isEqualTo(SecurityScheme.In.HEADER);
        assertThat(scheme.getName()).isEqualTo("Authorization");

        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).anyMatch(requirement -> requirement.containsKey("Authorization"));
    }
}
