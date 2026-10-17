package com.worm.swagger3.service.impl;

import com.worm.StubObjectProvider;
import com.worm.swagger3.service.IDocService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DocServiceImpl 测试：文档地址参数构建与 URL 格式化。
 *
 * 为什么：启动打印的文档地址是消费方访问文档的唯一入口，协议判断、端口优先级、
 * 上下文路径与占位符替换任一出错都会给出错误地址 —— 干什么：逐分支钉死参数与 URL 结果
 */
class DocServiceImplTest {

    /**
     * 构造带指定端点的 Servlet Web 服务器事件。
     */
    private ServletWebServerInitializedEvent eventWithPort(int port) {
        WebServer webServer = mock(WebServer.class);
        when(webServer.getPort()).thenReturn(port);
        // 为什么：Boot 2.7 的 ServletWebServerInitializedEvent 构造器参数顺序为 (WebServer, context) —— 干什么：按 (webServer, context) 顺序构造
        return new ServletWebServerInitializedEvent(webServer, new AnnotationConfigServletWebServerApplicationContext());
    }

    @Test
    @DisplayName("buildArgs：配置端口优先，普通端口判定为 http")
    void buildArgsUsesConfiguredPort() {
        ServerProperties serverProperties = new ServerProperties();
        serverProperties.setPort(9090);
        IDocService docService = new DocServiceImpl(new StubObjectProvider<>(serverProperties));

        Map<String, Object> args = docService.buildArgs(eventWithPort(8080));

        assertThat(args.get("port")).isEqualTo(9090);
        assertThat(args.get("protocol")).isEqualTo("http");
        assertThat((String) args.get("host")).isNotNull().isNotEmpty();
        assertThat(args.get("contextPath")).isEqualTo("");
    }

    @Test
    @DisplayName("buildArgs：443 端口判定为 https")
    void buildArgsDetectsHttpsOnSslPort() {
        ServerProperties serverProperties = new ServerProperties();
        serverProperties.setPort(443);
        IDocService docService = new DocServiceImpl(new StubObjectProvider<>(serverProperties));

        Map<String, Object> args = docService.buildArgs(eventWithPort(443));

        assertThat(args.get("protocol")).isEqualTo("https");
    }

    @Test
    @DisplayName("buildArgs：ServerProperties 缺失时回退事件实际端口")
    void buildArgsFallsBackToEventPort() {
        IDocService docService = new DocServiceImpl(new StubObjectProvider<>(null));

        Map<String, Object> args = docService.buildArgs(eventWithPort(8080));

        assertThat(args.get("port")).isEqualTo(8080);
        assertThat(args.get("protocol")).isEqualTo("http");
        assertThat(args.get("contextPath")).isEqualTo("");
    }

    @Test
    @DisplayName("buildArgs：配置了上下文路径时透传")
    void buildArgsPassesContextPath() {
        ServerProperties serverProperties = new ServerProperties();
        serverProperties.getServlet().setContextPath("/api");
        IDocService docService = new DocServiceImpl(new StubObjectProvider<>(serverProperties));

        Map<String, Object> args = docService.buildArgs(eventWithPort(8080));

        assertThat(args.get("contextPath")).isEqualTo("/api");
    }

    @Test
    @DisplayName("formatUrl：占位符全部替换")
    void formatUrlReplacesAllPlaceholders() {
        IDocService docService = new DocServiceImpl(new StubObjectProvider<>(null));
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("protocol", "http");
        parameters.put("host", "192.168.1.100");
        parameters.put("port", 8080);
        parameters.put("contextPath", "/api");

        String url = docService.formatUrl("{protocol}://{host}:{port}{contextPath}/doc.html", parameters);

        assertThat(url).isEqualTo("http://192.168.1.100:8080/api/doc.html");
    }

    @Test
    @DisplayName("formatUrl：null 值替换为空字符串")
    void formatUrlReplacesNullWithEmpty() {
        IDocService docService = new DocServiceImpl(new StubObjectProvider<>(null));
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("protocol", "http");
        parameters.put("host", "127.0.0.1");
        parameters.put("port", 8080);
        parameters.put("contextPath", null);

        String url = docService.formatUrl("{protocol}://{host}:{port}{contextPath}/doc.html", parameters);

        assertThat(url).isEqualTo("http://127.0.0.1:8080/doc.html");
    }
}
