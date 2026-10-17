package com.worm.swagger3.service.impl;

import com.worm.swagger3.service.IDocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.context.WebServerInitializedEvent;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 文档服务实现类
 * <p>
 * 该类实现了 {@link IDocService} 接口，用于构建 API 文档访问地址所需的参数。
 * 主要功能包括：
 * </p>
 *
 * <ul>
 *   <li>获取应用运行的协议类型（HTTP/HTTPS）</li>
 *   <li>自动获取本机 IP 地址</li>
 *   <li>获取应用配置的端口号</li>
 *   <li>获取应用的上下文路径</li>
 * </ul>
 *
 * <h3>使用说明：</h3>
 * <p>
 * 该类在 Web 服务器初始化完成后，通过 {@link WebServerInitializedEvent} 事件获取服务器信息，
 * 并构建包含协议、主机、端口、上下文的参数 Map，供 {@link com.worm.swagger3.runner.SwaggerApplicationRunner}
 * 和 {@link com.worm.swagger3.knife4j.Knife4jApplicationRunner} 使用。
 * </p>
 *
 * @author 姚文兵
 * @version 1.0.0
 * @see IDocService
 * @see ServerProperties
 * @see WebServerInitializedEvent
 * @since 2023-05-14
 */
@Slf4j
public class DocServiceImpl implements IDocService {

    /**
     * HTTP 协议常量
     * <p>
     * 用于标识使用 HTTP 协议访问。
     * </p>
     */
    private static final String PROTOCOL_HTTP = "http";

    /**
     * HTTPS 协议常量
     * <p>
     * 用于标识使用 HTTPS 协议访问。
     * </p>
     */
    private static final String PROTOCOL_HTTPS = "https";

    /**
     * SSL 端口号
     * <p>
     * HTTPS 协议的默认端口号，当应用使用此端口时，自动判断为 HTTPS 协议。
     * </p>
     */
    private static final int SSL_PORT = 443;

    /**
     * 本地回环地址
     * <p>
     * 当无法获取本机 IP 时使用的默认回环地址。
     * </p>
     */
    private static final String LOCALHOST = "127.0.0.1";

    /**
     * Spring Boot 服务器配置属性提供者（可选 Bean）。
     *
     * <p>为什么：ServerProperties 仅在 Web 自动装配场景下才是 Bean，非 Web 精简容器中缺失，
     * 字段注入会因 Bean 不存在导致容器启动失败 —— 干什么：经 ObjectProvider 延迟获取，
     * 缺失时端口退化为使用 Web 服务器事件实际端口、上下文路径退化为空串。</p>
     */
    private final ObjectProvider<ServerProperties> serverPropertiesProvider;

    /**
     * 构造函数
     *
     * @param serverPropertiesProvider Spring Boot 服务器配置属性提供者
     */
    public DocServiceImpl(ObjectProvider<ServerProperties> serverPropertiesProvider) {
        this.serverPropertiesProvider = serverPropertiesProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> buildArgs(WebServerInitializedEvent event) {
        // 记录方法开始执行的日志
        log.debug("【文档服务】开始构建文档访问地址参数");

        // 创建参数 Map 集合，用于存储最终返回的参数
        Map<String, Object> parameters = new HashMap<>(16);

        // ==================== 协议处理 ====================
        // 获取端口号，用于判断协议类型
        Integer port = getPort(event);
        // 根据端口号判断协议类型，SSL 端口使用 HTTPS，否则使用 HTTP
        String protocol = determineProtocol(port);
        // 将协议参数添加到 Map 中
        parameters.put("protocol", protocol);
        log.debug("【文档服务】协议类型: {}", protocol);

        // ==================== 主机 IP 处理 ====================
        // 获取本机 IP 地址
        String host = getHostAddress();
        // 将主机 IP 参数添加到 Map 中
        parameters.put("host", host);
        log.debug("【文档服务】主机地址: {}", host);

        // ==================== 端口处理 ====================
        // 将端口参数添加到 Map 中
        parameters.put("port", port);
        log.debug("【文档服务】端口号: {}", port);

        // ==================== 上下文路径处理 ====================
        // 获取应用的上下文路径
        String contextPath = getContextPath();
        // 将上下文路径参数添加到 Map 中
        parameters.put("contextPath", contextPath);
        log.debug("【文档服务】上下文路径: {}", contextPath);

        // 记录方法执行完成的日志
        log.debug("【文档服务】文档访问地址参数构建完成");

        // 返回包含所有参数的 Map
        return parameters;
    }

    /**
     * 根据端口号判断协议类型
     * <p>
     * 如果端口号为 443，则使用 HTTPS 协议；
     * 否则默认使用 HTTP 协议。
     * </p>
     *
     * @param port 服务端口号
     * @return 协议类型字符串，"http" 或 "https"
     */
    private String determineProtocol(Integer port) {
        // 判断端口是否为 SSL 端口（443）
        if (port != null && port.equals(SSL_PORT)) {
            // SSL 端口使用 HTTPS 协议
            return PROTOCOL_HTTPS;
        }
        // 其他端口默认使用 HTTP 协议
        return PROTOCOL_HTTP;
    }

    /**
     * 获取服务端口号
     * <p>
     * 优先使用 server.port 配置的端口，
     * 如果配置为空，则从 Web 服务器事件中获取实际端口。
     * </p>
     *
     * @param event Web 服务器初始化完成事件
     * @return 服务端口号
     */
    private Integer getPort(WebServerInitializedEvent event) {
        // 尝试从配置中获取端口号（ServerProperties 可能缺失）
        Integer port = null;
        ServerProperties serverProperties = serverPropertiesProvider.getIfAvailable();
        if (serverProperties != null) {
            port = serverProperties.getPort();
        }
        // 如果配置的端口为空，则从事件对象中获取实际端口
        if (port == null) {
            // 从 Web 服务器中获取实际监听的端口
            port = event.getWebServer().getPort();
            log.debug("【文档服务】使用服务器实际端口: {}", port);
        } else {
            log.debug("【文档服务】使用配置端口: {}", port);
        }
        // 返回端口号
        return port;
    }

    /**
     * 获取本机 IP 地址
     * <p>
     * 遍历网卡获取本机非回环 IPv4 地址（优先站点本地地址）。
     * 如果获取失败，则返回本地回环地址 127.0.0.1。
     * </p>
     *
     * @return 本机 IP 地址字符串
     */
    private String getHostAddress() {
        try {
            // 为什么：原实现依赖 Hutool NetUtil.getLocalhost()，为保持零非 Spring 体系依赖
            // —— 干什么：以 JDK NetworkInterface 枚举网卡，取首个非回环、非虚拟、站点本地 IPv4 地址
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress() && address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // 获取本机 IP 地址失败，记录警告日志
            log.warn("【文档服务】获取本机 IP 地址失败，将使用本地回环地址: {}", e.getMessage());
        }
        // 获取失败时返回本地回环地址
        return LOCALHOST;
    }

    /**
     * 获取应用上下文路径
     * <p>
     * 从 ServerProperties 中获取 server.servlet.context-path 配置。
     * 如果配置为空或 null，返回空字符串。
     * </p>
     *
     * @return 上下文路径字符串，为空时返回空字符串
     */
    private String getContextPath() {
        // 获取 Servlet 相关配置，可能为 null
        ServerProperties serverProperties = serverPropertiesProvider.getIfAvailable();
        if (serverProperties != null && serverProperties.getServlet() != null) {
            // 获取上下文路径配置
            String contextPath = serverProperties.getServlet().getContextPath();
            // 如果上下文路径不为空，则返回
            if (contextPath != null && !contextPath.trim().isEmpty()) {
                return contextPath;
            }
        }
        // 上下文路径为空时返回空字符串
        return "";
    }
}
