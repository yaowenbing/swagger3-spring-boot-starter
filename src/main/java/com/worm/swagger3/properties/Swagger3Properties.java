package com.worm.swagger3.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger3 配置属性类
 * <p>
 * 该类用于配置 Swagger3 API 文档生成器的各项参数，
 * 通过 Spring Boot 的配置属性绑定机制，可以在 application.yml 或 application.properties 中进行配置。
 * </p>
 *
 * <p>
 * 配置前缀：worm.swagger3
 * </p>
 *
 * <p>
 * 配置示例：
 * <pre>
 * worm:
 *   swagger3:
 *     enable: true
 *     application-name: 我的API项目
 *     description: API接口文档描述
 *     version: V 1.0.0
 *     contact-name: 开发者
 *     contact-url: <a href="https://www.example.com">...</a>
 *     contact-mail: developer@example.com
 *     protocols:
 *       - https
 *       - http
 * </pre>
 * </p>
 *
 * @author 姚文兵
 * @version 1.0.0
 * @since 2023-05-14
 */
@Data
@ConfigurationProperties(prefix = Swagger3Properties.PREFIX)
@NoArgsConstructor
public class Swagger3Properties {
    public static final String PREFIX = "worm.swagger3";

    /**
     * Swagger 文档开关
     * <p>
     * 控制是否启用 Swagger API 文档功能。
     * 建议在生产环境中关闭此开关，以避免 API 文档暴露。
     * </p>
     *
     * <p>默认值：true（开启）</p>
     */
    private boolean enabled = true;

    /**
     * 应用名称
     * <p>
     * 在 Swagger UI 页面标题中显示的应用名称，
     * 会显示为 "{应用名称} REST ful API 文档"。
     * </p>
     *
     * <p>默认值："项目"</p>
     */
    private String applicationName = "项目";

    /**
     * API 文档描述
     * <p>
     * 在 Swagger UI 页面中显示的 API 文档详细描述信息。
     * </p>
     *
     * <p>默认值："这是当前项目的接口API在线文档地址！！！（生产环境不能打开）"</p>
     */
    private String description = "这是当前项目的接口API在线文档地址！！！（生产环境不能打开）";

    /**
     * API 版本号
     * <p>
     * 在 Swagger UI 页面中显示的 API 版本号，
     * 格式通常为 "V x.x.x"。
     * </p>
     *
     * <p>默认值："V 1.0.0"</p>
     */
    private String version = "V 1.0.0";

    /**
     * 联系人姓名
     * <p>
     * API 文档中显示的开发者或维护人员姓名。
     * </p>
     *
     * <p>默认值："姚文兵"</p>
     */
    private String contactName = "姚文兵";

    /**
     * 联系人网址
     * <p>
     * API 文档中显示的开发者或团队的联系网址。
     * </p>
     *
     * <p>默认值："<a href="https://www.xxx.com">...</a>"</p>
     */
    private String contactUrl = "https://www.xxx.com";

    /**
     * 联系人邮箱
     * <p>
     * API 文档中显示的开发者或维护人员的联系邮箱地址。
     * </p>
     *
     * <p>默认值："379746421@qq.com"</p>
     */
    private String contactMail = "379746421@qq.com";

    /**
     * 支持的协议列表
     * <p>
     * API 文档支持的通讯协议列表，
     * 如 ["https", "http"]，Swagger UI 会根据此配置显示相应的协议信息。
     * </p>
     *
     * <p>默认值：["https", "http"]</p>
     */
    private List<String> protocols = Arrays.asList("https", "http");
}
