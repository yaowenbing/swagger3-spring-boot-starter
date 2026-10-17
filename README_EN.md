# swagger3-spring-boot-starter

A Spring Boot 2 API documentation starter built on springdoc-openapi + Knife4j: one dependency for OpenAPI 3 docs, endpoint grouping, Long precision handling, and startup address printing.

中文 | [English](README_EN.md)

## Introduction

Integrating springdoc by hand means writing OpenAPI configuration, wiring Knife4j enhancements, fixing frontend Long precision loss, and remembering doc URLs — repeated in every project. This starter converges all of it:

1. **Zero-config docs** — add the dependency and get OpenAPI 3 docs (title/version/description/contact/license auto-assembled) plus the Knife4j enhanced UI;
2. **Endpoint grouping** — the `@Swagger3Group` annotation groups endpoints by module (all/common/basic/business);
3. **Long precision handling** — a Jackson module is registered automatically so Long serializes as String, eliminating the frontend `9007199254740991` precision loss;
4. **Startup address printing** — Swagger UI and Knife4j UI URLs are printed automatically after the web container starts (including the machine's non-loopback IP).

## Features

- **Zero-config setup** — docs work out of the box; no `@Bean OpenAPI` needed
- **Configuration-driven doc metadata** — app name, description, version, contact, protocols, all via `worm.swagger3.*`
- **Global Authorization security scheme** — APIKEY (HEADER) scheme auto-assembled; the Knife4j page can send the Token directly
- **Endpoint grouping annotation** — `@Swagger3Group` supports class/method levels, method overrides class
- **Long → String serialization** — enabled by default, disable with `worm.swagger3.long-to-string=false`
- **Startup address printing** — auto-detects protocol (443 → https), port (`server.port` first), context path, and local IP
- **Overridable** — `Swagger3Manager` / `OpenAPI` are `@ConditionalOnMissingBean`; register a same-type bean to override
- **Master switch** — `worm.swagger3.enabled=false` disables docs entirely

## Requirements

| Dependency | Version |
| --- | --- |
| JDK | 8+ |
| Maven | 3.6+ |
| Spring Boot | 2.7.x |
| Knife4j | 4.4.0 (transitively via the starter) |

This repository targets the JDK 8 / Spring Boot 2.7 baseline; build output is Java 8 bytecode, ready to use in Spring Boot 2.x projects.

## Quick Start

### 1. Add the dependency

```xml
<dependency>
    <groupId>com.worm</groupId>
    <artifactId>swagger3-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Start the app and the console prints the doc addresses automatically:

```text
[swagger3-spring-boot-starter] Swagger UI 访问地址: http://192.168.1.100:8080/swagger-ui/index.html
[knife4j-spring-boot-starter] Knife4j UI 访问地址: http://192.168.1.100:8080/doc.html
```

Visit `http://localhost:8080/doc.html` for the Knife4j enhanced documentation page.

### 2. Configure doc metadata

```yaml
worm:
  swagger3:
    enabled: true                 # master switch (default true)
    application-name: Order Service   # app name in the doc title
    description: Order domain API docs
    version: V 1.0.0
    contact-name: Zhang San
    contact-url: https://example.com
    contact-mail: zhangsan@example.com
    long-to-string: true          # serialize Long as String (default true)
```

### 3. Group endpoints

```java
@RestController
@Swagger3Group(Swagger3Group.GroupType.BUSINESS)   // class level: the whole controller joins the business group
public class OrderController {

    @GetMapping("/order/{id}")
    public Order getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @Swagger3Group(Swagger3Group.GroupType.COMMON)  // method level overrides class level
    @GetMapping("/order/health")
    public String health() {
        return "ok";
    }
}
```

Group types (`Swagger3Group.GroupType`):

| Group | Identifier | Use case |
| --- | --- | --- |
| `DEFAULT` | default | All endpoints (default when unannotated) |
| `COMMON` | common | Health checks, system config, dictionaries, uploads/downloads |
| `BASIC` | basic | Users, roles, permissions, departments |
| `BUSINESS` | business | Orders, payments, inventory, reports |

### 4. Disable docs

```yaml
worm:
  swagger3:
    enabled: false   # recommended for production to avoid exposing API info
```

### 5. Custom override

Register a same-type bean to override the default assembly:

```java
@Bean
@ConditionalOnMissingBean
public OpenAPI customOpenAPI() {
    return new OpenAPI().info(new Info().title("My Docs"));
}
```

## Full Configuration List

Prefix `worm.swagger3`:

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | Master switch; no doc beans are registered when off |
| `application-name` | String | `项目` | App name in the doc title |
| `description` | String | `这是当前项目的接口API在线文档地址！！！（生产环境不能打开）` | Doc description |
| `version` | String | `V 1.0.0` | App version (concatenated into Info.version) |
| `contact-name` | String | `姚文兵` | Contact name |
| `contact-url` | String | `https://www.xxx.com` | Contact URL |
| `contact-mail` | String | `379746421@qq.com` | Contact email |
| `protocols` | List\<String\> | `[https, http]` | Supported protocol list |
| `long-to-string` | boolean | `true` | Whether Long serializes as String (web environment only) |

## Architecture and Package Structure

```text
com.worm
└── swagger3
    ├── annotation
    │   └── Swagger3Group              // Endpoint grouping annotation (class/method levels)
    ├── config
    │   ├── Swagger3ConfigAutoConfiguration   // The sole doc auto-configuration (registered via AutoConfiguration.imports)
    │   └── LongPrecisionAutoConfiguration    // Long → String Jackson module (web environment)
    ├── knife4j
    │   └── Knife4jApplicationRunner   // Knife4j UI address printing listener
    ├── manager
    │   └── Swagger3Manager            // OpenAPI doc info & security scheme assembly
    ├── properties
    │   └── Swagger3Properties         // Configuration properties (prefix worm.swagger3)
    ├── runner
    │   └── SwaggerApplicationRunner   // Swagger UI address printing listener
    └── service
        ├── IDocService                // Doc address parameter building + URL template formatting
        └── impl.DocServiceImpl        // Protocol/port/host/context-path resolution
```

Auto-configured bean list (`Swagger3ConfigAutoConfiguration`, active when `worm.swagger3.enabled=true`):

| Bean | Registration condition | Description |
| --- | --- | --- |
| `Swagger3Manager` | When missing | OpenAPI doc assembler |
| `OpenAPI` | When missing | Doc metadata + Authorization security scheme |
| `SwaggerApplicationRunner` | Unconditional | Swagger UI address startup printing |
| `Knife4jApplicationRunner` | Unconditional | Knife4j UI address startup printing |
| `IDocService` | Unconditional | Doc address parameter building service |

## FAQ

### 1. Why isn't `long-to-string` in `Swagger3Properties`?

The switch is only read by `LongPrecisionAutoConfiguration`'s `@ConditionalOnProperty` (a condition property) and does not need to bind to a properties class; putting it in `Swagger3Properties` would create a "second source of truth". The configuration style is identical to the other properties (`worm.swagger3.long-to-string`).

### 2. How is the host IP for address printing determined?

`DocServiceImpl` iterates the machine's network interfaces and picks the first **non-loopback, non-virtual, up** site-local IPv4 address; on failure it falls back to `127.0.0.1`. In cloud/multi-NIC environments, access the container's internal IP or configure a domain directly.

### 3. No doc address printed?

Address printing relies on `WebServerInitializedEvent`, which fires only after a Servlet web application starts; the master switch `worm.swagger3.enabled` must also be `true` (default).

### 4. How do I use this with Spring Boot 3 / the jakarta namespace?

This repository is the open-source JDK 8 / Spring Boot 2.7 (javax) baseline, using `knife4j-openapi3-spring-boot-starter`. For the jakarta baseline (Spring Boot 3+), use the `knife4j-openapi3-jakarta-spring-boot-starter` variant and change `javax.*` references to `jakarta.*`.

## License

This project is licensed under the [Apache License 2.0](LICENSE). See the [LICENSE](LICENSE) file for details.
