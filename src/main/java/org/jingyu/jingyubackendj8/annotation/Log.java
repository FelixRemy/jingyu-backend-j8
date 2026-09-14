package org.jingyu.jingyubackendj8.annotation;

import java.lang.annotation.*;

/**
 * @author Colin
 * 操作日志注解
 * 打在Controller public方法上，实现无侵入采集接口日志
 */
@Target(ElementType.METHOD) // 作用在方法
@Retention(RetentionPolicy.RUNTIME) // 运行时生效，AOP可以读取
@Documented
public @interface Log {

    /**
     * 所属模块名称，例如：文章管理、用户管理
     */
    String module() default "";

    /**
     * 操作描述，例如：新增文章、修改密码
     */
    String desc() default "";

    /**
     * 是否记录请求入参
     */
    boolean recordParam() default true;

    /**
     * 是否记录接口返回结果
     */
    boolean recordResult() default true;
}
