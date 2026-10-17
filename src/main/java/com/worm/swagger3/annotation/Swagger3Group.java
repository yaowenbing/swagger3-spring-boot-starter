package com.worm.swagger3.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.annotation.*;

/**
 * Swagger3 接口分组注解
 * <p>
 * 该注解用于对 Swagger API 文档中的接口进行分类显示。
 * 通过在 Controller 类或方法上标注此注解，可以将接口归类到不同的分组中，
 * 便于在 Swagger UI 中按模块查看和管理接口。
 * </p>
 *
 * <h3>使用说明：</h3>
 * <ul>
 *   <li>放在 Controller 类上：该类下所有接口都会归入指定分组</li>
 *   <li>放在方法上：当前方法的分组优先级高于类上的分组</li>
 *   <li>如果不标注此注解，接口默认归入 "全部接口" 分组</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 示例1：在类上标注，类下所有接口归入业务接口分组
 * {@code @RestController}
 * {@code @Swagger3Group(Swagger3Group.GroupType.BUSINESS)}
 * public class UserController {
 *     // 所有方法都会归入业务接口分组
 * }
 *
 * // 示例2：在方法上标注，覆盖类上的分组
 * {@code @RestController}
 * {@code @Swagger3Group(Swagger3Group.GroupType.BUSINESS)}
 * public class UserController {
 *     {@code @Swagger3Group(Swagger3Group.GroupType.COMMON)}
 *     public Result&lt;Void&gt; health() {
 *         // 此方法归入公共接口分组
 *     }
 * }
 * </pre>
 *
 * @author 姚文兵
 * @version 1.0.0
 * @since 2023-05-14
 * @see GroupType
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Swagger3Group {

    /**
     * 接口的分类类型
     * <p>
     * 指定当前接口所属的分组类型，默认为 {@link GroupType#DEFAULT}（全部接口）。
     * </p>
     *
     * @return 接口的分类类型，默认为 DEFAULT
     */
    GroupType value() default GroupType.DEFAULT;

    /**
     * 接口分组类型枚举
     * <p>
     * 定义 Swagger 文档中接口的分组类型，用于对接口进行分类展示。
     * </p>
     *
     * <h3>分组说明：</h3>
     * <ul>
     *   <li>{@link #DEFAULT} - 全部接口：包含所有标注了 @RestController 或 @Controller 的接口</li>
     *   <li>{@link #COMMON} - 公共接口：通用的公共基础接口，如健康检查、系统配置等</li>
     *   <li>{@link #BASIC} - 基础接口：业务基础接口，如用户管理、权限管理等基础功能</li>
     *   <li>{@link #BUSINESS} - 业务接口：核心业务接口，如订单管理、支付处理等</li>
     * </ul>
     *
     * @author 姚文兵
     * @version 1.0.0
     * @since 2023-05-14
     */
    @Getter
    @AllArgsConstructor
    enum GroupType {

        /**
         * 全部接口分组
         * <p>
         * 默认分组，包含所有标注了 @RestController 或 @Controller 注解的接口。
         * 不需要显式标注 @Swagger3Group 注解即可被扫描到。
         * </p>
         */
        DEFAULT("default", "全部接口"),

        /**
         * 公共接口分组
         * <p>
         * 用于标注通用的公共基础接口，如：
         * <ul>
         *   <li>健康检查接口</li>
         *   <li>系统配置接口</li>
         *   <li>公共字典接口</li>
         *   <li>文件上传下载接口</li>
         * </ul>
         * </p>
         */
        COMMON("common", "公共接口"),

        /**
         * 基础接口分组
         * <p>
         * 用于标注业务基础接口，如：
         * <ul>
         *   <li>用户管理接口</li>
         *   <li>角色管理接口</li>
         *   <li>权限管理接口</li>
         *   <li>部门管理接口</li>
         * </ul>
         * </p>
         */
        BASIC("basic", "基础接口"),

        /**
         * 业务接口分组
         * <p>
         * 用于标注核心业务接口，如：
         * <ul>
         *   <li>订单管理接口</li>
         *   <li>支付处理接口</li>
         *   <li>库存管理接口</li>
         *   <li>报表统计接口</li>
         * </ul>
         * </p>
         */
        BUSINESS("business", "业务接口");

        /**
         * 分组类型标识
         * <p>
         * 用于 Swagger 内部识别分组的唯一标识，
         * 在 Swagger UI 的 URL 中会使用此标识。
         * </p>
         */
        private final String type;

        /**
         * 分组显示名称
         * <p>
         * 在 Swagger UI 页面中显示的分组名称，
         * 用于用户友好的分组展示。
         * </p>
         */
        private final String display;
    }
}
