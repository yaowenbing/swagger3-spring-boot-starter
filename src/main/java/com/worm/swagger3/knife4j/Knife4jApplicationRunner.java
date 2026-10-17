package com.worm.swagger3.knife4j;

import com.worm.swagger3.service.IDocService;
import com.worm.swagger3.properties.Swagger3Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Map;

/**
 * Knife4j UI 地址打印启动监听器
 * <p>
 * 该类实现了 {@link ApplicationListener} 接口，监听 {@link WebServerInitializedEvent} 事件。
 * 在 Web 服务器初始化完成后，自动在控制台打印 Knife4j UI 的访问地址。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>监听 Web 服务器初始化完成事件</li>
 *   <li>获取服务器地址信息（协议、主机、端口、上下文路径）</li>
 *   <li>在控制台打印 Knife4j UI 访问地址</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <p>
 * 应用启动后，控制台会输出类似以下信息：
 * <pre>
 * [knife4j-spring-boot-starter] Knife4j UI 访问地址: <a href="http://192.168.1.100:8080/doc.html">...</a>
 * </pre>
 * </p>
 *
 * <h3>Knife4j 简介：</h3>
 * <p>
 * Knife4j 是 Swagger 的增强 UI 实现，提供了更美观、更强大的 API 文档界面。
 * 相比原生 Swagger UI，Knife4j 提供了以下增强功能：
 * </p>
 * <ul>
 *   <li>更美观的界面设计</li>
 *   <li>支持离线文档导出（Markdown、Word 等）</li>
 *   <li>支持接口排序</li>
 *   <li>支持全局参数配置</li>
 *   <li>支持接口搜索过滤</li>
 * </ul>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>仅当 Swagger 功能启用时才会打印地址</li>
 *   <li>地址中的协议根据端口自动判断（443 端口使用 HTTPS）</li>
 *   <li>如果配置了上下文路径，地址中会包含该路径</li>
 * </ul>
 *
 * @author 姚文兵
 * @version 1.0.0
 * @since 2023-05-14
 * @see ApplicationListener
 * @see WebServerInitializedEvent
 * @see IDocService
 */
@Slf4j
public class Knife4jApplicationRunner implements ApplicationListener<WebServerInitializedEvent> {

    /**
     * Knife4j UI 地址模板：{protocol}://{host}:{port}{contextPath}/doc.html
     */
    private static final String KNIFE4J_UI_TEMPLATE = "{protocol}://{host}:{port}{contextPath}/doc.html";

    /**
     * 文档服务接口
     * <p>
     * 用于构建文档访问地址所需的参数（协议、主机、端口、上下文路径）。
     * </p>
     */
    private final IDocService iDocService;

    /**
     * Swagger3 配置属性
     * <p>
     * 用于判断 Swagger 是否启用，只有启用时才打印地址。
     * </p>
     */
    private final Swagger3Properties swagger3Properties;

    /**
     * 构造函数
     *
     * @param iDocService         文档服务
     * @param swagger3Properties  Swagger3 配置属性
     */
    public Knife4jApplicationRunner(IDocService iDocService, Swagger3Properties swagger3Properties) {
        this.iDocService = iDocService;
        this.swagger3Properties = swagger3Properties;
    }

    /**
     * 处理 Web 服务器初始化完成事件
     * <p>
     * 当 Web 服务器初始化完成后，该方法会被自动调用。
     * 主要功能是在控制台打印 Knife4j UI 的访问地址。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>检查 Swagger 是否启用</li>
     *   <li>如果启用，调用文档服务构建地址参数</li>
     *   <li>格式化并打印 Knife4j UI 访问地址</li>
     *   <li>如果未启用，打印提示信息</li>
     * </ol>
     *
     * @param event Web 服务器初始化完成事件，包含服务器相关信息
     */
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        // 检查 Swagger 是否启用
        if (swagger3Properties.isEnabled()) {
            // Swagger 已启用，打印 Knife4j UI 访问地址
            // 调用文档服务构建地址参数（协议、主机、端口、上下文路径）
            Map<String, Object> parameters = iDocService.buildArgs(event);

            // 格式化 Knife4j UI 访问地址
            // 格式为：{protocol}://{host}:{port}{contextPath}/doc.html
            String knife4jUrl = iDocService.formatUrl(KNIFE4J_UI_TEMPLATE, parameters);

            // 在控制台打印 Knife4j UI 访问地址
            log.info("【knife4j-spring-boot-starter】Knife4j UI 访问地址: {}", knife4jUrl);
        } else {
            // Swagger 未启用，打印提示信息
            log.info("【knife4j-spring-boot-starter】Swagger 已禁用，不打印访问地址");
        }
    }
}
