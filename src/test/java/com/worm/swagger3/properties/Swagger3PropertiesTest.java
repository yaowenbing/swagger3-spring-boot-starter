package com.worm.swagger3.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Swagger3Properties 默认值测试。
 *
 * 为什么：默认值是消费方零配置接入的行为基线，默认联系人等直接出现在文档页上
 * —— 干什么：钉死各字段默认取值
 */
class Swagger3PropertiesTest {

    @Test
    @DisplayName("默认值：开关启用、默认应用名/版本/联系人/协议")
    void defaults() {
        Swagger3Properties properties = new Swagger3Properties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getApplicationName()).isEqualTo("项目");
        assertThat(properties.getVersion()).isEqualTo("V 1.0.0");
        assertThat(properties.getContactName()).isEqualTo("姚文兵");
        assertThat(properties.getProtocols()).containsExactly("https", "http");
        assertThat(Swagger3Properties.PREFIX).isEqualTo("worm.swagger3");
    }

    @Test
    @DisplayName("setter：各字段可覆盖")
    void settersOverrideDefaults() {
        Swagger3Properties properties = new Swagger3Properties();
        properties.setEnabled(false);
        properties.setApplicationName("订单服务");
        properties.setDescription("订单域接口文档");

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getApplicationName()).isEqualTo("订单服务");
        assertThat(properties.getDescription()).isEqualTo("订单域接口文档");
    }
}
