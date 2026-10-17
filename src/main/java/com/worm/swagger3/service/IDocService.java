package com.worm.swagger3.service;

import org.springframework.boot.web.context.WebServerInitializedEvent;

import java.util.Map;

/**
 * 文档服务接口
 * <p>
 * 该接口定义了构建 API 文档访问地址所需参数的方法。
 * 主要用于在应用启动后，获取服务器的协议、主机地址、端口和上下文路径等信息，
 * 以便在控制台打印 Swagger 和 Knife4j 的文档访问地址。
 * </p>
 *
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>获取应用运行的协议类型（HTTP/HTTPS）</li>
 *   <li>获取应用运行的主机 IP 地址</li>
 *   <li>获取应用运行的端口号</li>
 *   <li>获取应用的上下文路径（Context Path）</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>SwaggerApplicationRunner：在应用启动后打印 Swagger UI 访问地址</li>
 *   <li>Knife4jApplicationRunner：在应用启动后打印 Knife4j UI 访问地址</li>
 * </ul>
 *
 * @author 姚文兵
 * @version 1.0.0
 * @since 2023-05-14
 * @see com.worm.swagger3.service.impl.DocServiceImpl
 */
public interface IDocService {

    /**
     * 构建文档访问地址所需的参数列表
     * <p>
     * 该方法在 Web 服务器初始化完成后被调用，用于构建访问 API 文档所需的参数。
     * 返回的 Map 包含以下键值对：
     * </p>
     *
     * <table border="1">
     *   <tr>
     *     <th>键名</th>
     *     <th>值类型</th>
     *     <th>说明</th>
     *   </tr>
     *   <tr>
     *     <td>protocol</td>
     *     <td>String</td>
     *     <td>访问协议，如 "http" 或 "https"</td>
     *   </tr>
     *   <tr>
     *     <td>host</td>
     *     <td>String</td>
     *     <td>服务器主机 IP 地址</td>
     *   </tr>
     *   <tr>
     *     <td>port</td>
     *     <td>Integer</td>
     *     <td>服务端口号</td>
     *   </tr>
     *   <tr>
     *     <td>contextPath</td>
     *     <td>String</td>
     *     <td>应用上下文路径，如为空则为空字符串</td>
     *   </tr>
     * </table>
     *
     * <h3>参数获取优先级：</h3>
     * <ul>
     *   <li>端口：优先使用 server.port 配置，若为空则从事件对象中获取实际端口</li>
     *   <li>上下文路径：使用 server.servlet.context-path 配置，若为空则为空字符串</li>
     *   <li>主机：自动获取本机非回环 IP 地址</li>
     * </ul>
     *
     * @param event Web 服务器初始化完成事件对象，包含服务器相关信息
     * @return 包含协议、主机、端口、上下文路径等参数的 Map 集合
     */
    Map<String, Object> buildArgs(WebServerInitializedEvent event);

    /**
     * 按占位符模板格式化文档访问地址。
     *
     * <p>为什么：原实现依赖 Hutool {@code StrUtil.format(template, map, true)}，
     * 为保持 starter 零非 Spring 体系依赖，以 JDK 字符串替换实现同等语义 —— 干什么：
     * 将模板中 {@code {key}} 占位符替换为 parameters 中的值，值为 null 时替换为空字符串。</p>
     *
     * <p>示例：模板 {@code "{protocol}://{host}:{port}{contextPath}/doc.html"}，
     * parameters 含 protocol=http、host=192.168.1.100、port=8080、contextPath=空，
     * 结果为 {@code "http://192.168.1.100:8080/doc.html"}。</p>
     *
     * @param template    占位符模板，形如 {@code "{protocol}://{host}:{port}{contextPath}/doc.html"}
     * @param parameters 占位符取值 Map，键与模板中的 {@code {key}} 对应
     * @return 替换后的地址字符串
     */
    default String formatUrl(String template, Map<String, Object> parameters) {
        // 为什么：占位符数量有限且固定，逐键替换比正则解析更直观、无第三方依赖 —— 干什么：遍历参数键逐个替换模板占位符，null 值替换为空串
        String result = template;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object value = entry.getValue();
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, value == null ? "" : value.toString());
        }
        return result;
    }
}
