# swagger3-spring-boot-starter

Spring Boot 2 接口文档 Starter：基于 springdoc-openapi + Knife4j，一行依赖接入 OpenAPI 3 文档、接口分组、Long 精度处理与文档地址启动打印。

[English](README_EN.md) | 中文

## 简介

手工集成 springdoc 要写 OpenAPI 配置、配 Knife4j 增强、处理前端 Long 精度丢失、记住文档地址——每个项目重复一遍。本 starter 把这些收敛为：

1. **零配置文档** — 引入即生成 OpenAPI 3 文档（标题/版本/描述/联系人/许可证自动装配）+ Knife4j UI 增强；
2. **接口分组** — `@Swagger3Group` 注解按模块给接口分组（全部/公共/基础/业务）；
3. **Long 精度处理** — 自动注册 Jackson 模块，Long 序列化为字符串，杜绝前端 `9007199254740991` 精度丢失；
4. **启动打印地址** — Web 容器启动完成后自动打印 Swagger UI 与 Knife4j UI 访问地址（含本机非回环 IP）。

## 特性

- **零配置接入** — 引入依赖即有文档，无需写 `@Bean OpenAPI`
- **配置驱动文档元数据** — 应用名、描述、版本、联系人、协议全部经 `worm.swagger3.*` 配置
- **Authorization 全局安全方案** — 自动装配 APIKEY（HEADER）安全方案，Knife4j 页面可直接填写 Token 调试
- **接口分组注解** — `@Swagger3Group` 支持类/方法两级标注，方法级覆盖类级
- **Long → String 序列化** — 默认启用，`worm.swagger3.long-to-string=false` 一键关闭
- **启动地址打印** — 自动识别协议（443 → https）、端口（`server.port` 优先）、上下文路径与本机 IP
- **可覆盖** — `Swagger3Manager` / `OpenAPI` 均 `@ConditionalOnMissingBean`，注册同名 Bean 即覆盖
- **总开关** — `worm.swagger3.enabled=false` 整体关闭文档功能

## 环境要求

| 依赖 | 版本 |
| --- | --- |
| JDK | 8+ |
| Maven | 3.6+ |
| Spring Boot | 2.7.x |
| Knife4j | 4.4.0（随 starter 传递引入） |

本仓库为 JDK 8 / Spring Boot 2.7 基线，编译产物为 Java 8 字节码，可直接用于 Spring Boot 2.x 项目。

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.worm</groupId>
    <artifactId>swagger3-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

启动应用，控制台自动打印文档地址：

```text
[swagger3-spring-boot-starter] Swagger UI 访问地址: http://192.168.1.100:8080/swagger-ui/index.html
[knife4j-spring-boot-starter] Knife4j UI 访问地址: http://192.168.1.100:8080/doc.html
```

访问 `http://localhost:8080/doc.html` 即可看到 Knife4j 增强文档页。

### 2. 配置文档元数据

```yaml
worm:
  swagger3:
    enabled: true                 # 总开关（默认 true）
    application-name: 订单服务      # 文档标题中的应用名
    description: 订单域接口文档
    version: V 1.0.0
    contact-name: 张三
    contact-url: https://example.com
    contact-mail: zhangsan@example.com
    long-to-string: true          # Long 序列化为 String（默认 true）
```

### 3. 接口分组

```java
@RestController
@Swagger3Group(Swagger3Group.GroupType.BUSINESS)   // 类级：整个控制器归入业务接口
public class OrderController {

    @GetMapping("/order/{id}")
    public Order getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @Swagger3Group(Swagger3Group.GroupType.COMMON)  // 方法级覆盖类级
    @GetMapping("/order/health")
    public String health() {
        return "ok";
    }
}
```

分组类型（`Swagger3Group.GroupType`）：

| 分组 | 标识 | 适用场景 |
| --- | --- | --- |
| `DEFAULT` | default | 全部接口（默认，未标注时的归属） |
| `COMMON` | common | 健康检查、系统配置、字典、上传下载 |
| `BASIC` | basic | 用户、角色、权限、部门等基础功能 |
| `BUSINESS` | business | 订单、支付、库存、报表等核心业务 |

### 4. 关闭文档

```yaml
worm:
  swagger3:
    enabled: false   # 生产环境建议关闭，避免接口信息暴露
```

### 5. 自定义覆盖

注册同名 Bean 即可覆盖默认装配：

```java
@Bean
@ConditionalOnMissingBean
public OpenAPI customOpenAPI() {
    return new OpenAPI().info(new Info().title("我的文档"));
}
```

## 配置项完整列表

前缀 `worm.swagger3`：

| 配置项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | 文档总开关，关闭后不注册任何文档 Bean |
| `application-name` | String | `项目` | 文档标题中的应用名 |
| `description` | String | `这是当前项目的接口API在线文档地址！！！（生产环境不能打开）` | 文档描述 |
| `version` | String | `V 1.0.0` | 应用版本（拼接进 Info.version） |
| `contact-name` | String | `姚文兵` | 联系人姓名 |
| `contact-url` | String | `https://www.xxx.com` | 联系人网址 |
| `contact-mail` | String | `379746421@qq.com` | 联系人邮箱 |
| `protocols` | List\<String\> | `[https, http]` | 文档支持的协议列表 |
| `long-to-string` | boolean | `true` | Long 是否序列化为 String（仅 Web 环境生效） |

## 架构与包结构

```text
com.worm
└── swagger3
    ├── annotation
    │   └── Swagger3Group              // 接口分组注解（类/方法两级）
    ├── config
    │   ├── Swagger3ConfigAutoConfiguration   // 唯一文档自动配置（经 AutoConfiguration.imports 注册）
    │   └── LongPrecisionAutoConfiguration    // Long → String Jackson 模块（Web 环境）
    ├── knife4j
    │   └── Knife4jApplicationRunner   // Knife4j UI 地址打印监听器
    ├── manager
    │   └── Swagger3Manager            // OpenAPI 文档信息与安全方案装配
    ├── properties
    │   └── Swagger3Properties         // 配置属性（前缀 worm.swagger3）
    ├── runner
    │   └── SwaggerApplicationRunner   // Swagger UI 地址打印监听器
    └── service
        ├── IDocService                // 文档地址参数构建 + URL 模板格式化
        └── impl.DocServiceImpl        // 协议/端口/主机/上下文路径解析
```

自动装配的 Bean 清单（`Swagger3ConfigAutoConfiguration`，`worm.swagger3.enabled=true` 时生效）：

| Bean | 注册条件 | 说明 |
| --- | --- | --- |
| `Swagger3Manager` | 缺失时 | OpenAPI 文档装配器 |
| `OpenAPI` | 缺失时 | 文档元数据 + Authorization 安全方案 |
| `SwaggerApplicationRunner` | 无条件 | Swagger UI 地址启动打印 |
| `Knife4jApplicationRunner` | 无条件 | Knife4j UI 地址启动打印 |
| `IDocService` | 无条件 | 文档地址参数构建服务 |

## FAQ

### 1. 为什么错误也返回 HTTP 200？

与文档功能无关的通用约定，详见配套的 `result-spring-boot-starter`。本 starter 只负责文档生成，不干预响应格式。

### 2. `long-to-string` 为什么不在 `Swagger3Properties` 里？

该开关只被 `LongPrecisionAutoConfiguration` 的 `@ConditionalOnProperty` 读取（条件属性），不需要绑定到属性类；放在 `Swagger3Properties` 中反而会造成「两处真相」。配置方式与其它项完全一致（`worm.swagger3.long-to-string`）。

### 3. 地址打印的主机 IP 是怎么取的？

`DocServiceImpl` 遍历本机网卡，取首个**非回环、非虚拟、已启用**的站点本地 IPv4 地址；获取失败时回退 `127.0.0.1`。云环境/多网卡场景如需指定地址，可直接访问容器内网 IP 或配置域名。

### 4. 没有打印文档地址？

地址打印依赖 `WebServerInitializedEvent`，仅在 Web 应用（Servlet）启动完成后触发；同时总开关 `worm.swagger3.enabled` 必须为 `true`（默认）。

### 5. 如何接入 Spring Boot 3 / jakarta 命名空间版本？

本仓库为 JDK 8 / Spring Boot 2.7（javax）基线的开源版本，使用 `knife4j-openapi3-spring-boot-starter`。jakarta 基线（Spring Boot 3+）请使用 `knife4j-openapi3-jakarta-spring-boot-starter` 对应变体，并将 `javax.*` 引用改为 `jakarta.*`。

## 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源，详见 [LICENSE](LICENSE) 文件。
