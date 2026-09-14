package org.jingyu.jingyubackendj8.aop;


import org.jingyu.jingyubackendj8.annotation.Log;
import org.jingyu.jingyubackendj8.util.JsonUtil;
import org.jingyu.jingyubackendj8.util.LogTimeHolder;
import org.jingyu.jingyubackendj8.util.SensitiveDesensitizeUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author Colin
 * AOP操作日志切面：只打印日志到logback文件，不存入数据库
 * 拦截 @Log 注解的Controller方法
 */
@Aspect
@Component
@Slf4j
public class OperationLogFileAspect {

    /**
     * 切点：拦截所有加了@Log注解的方法
     */
    @Pointcut("@annotation(org.jingyu.jingyubackendj8.annotation.Log)")
    public void pointCut() {
    }

    /**
     * 环绕通知：可以在方法前后执行逻辑，捕获异常，计算耗时
     */
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        // 记录接口开始时间存入ThreadLocal
        LogTimeHolder.setStartTime(System.currentTimeMillis());
        // 获取目标方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnno = method.getAnnotation(Log.class);
        if (logAnno == null) {
            // 没有注解直接执行业务，不打印操作日志
            return joinPoint.proceed();
        }
        String module = logAnno.module();
        String desc = logAnno.desc();



        // 获取http请求对象
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = null;
        HttpServletRequest request = null;
        if (requestAttributes instanceof ServletRequestAttributes) {
            servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            request = servletRequestAttributes.getRequest();
        }
        String url = "-";
        String httpMethod = "-";
        String ip = "-";
        if(request != null){
            url = request.getRequestURI();
            httpMethod = request.getMethod();
            ip = getIp(request);
        }

        String reqParam = null;
        String respResult = null;
        boolean success = true;
        String errorMsg = null;

        try {
            if (logAnno.recordParam()) {
                try {
                    String rawJson = JsonUtil.toJson(joinPoint.getArgs());
                    reqParam = SensitiveDesensitizeUtil.desensitize(rawJson);
                } catch (Exception ex) {
                    log.warn("[操作日志]入参序列化失败", ex);
                    reqParam = "[参数序列化失败]";
                }
            }
            // 执行真正的Controller业务方法
            Object result = joinPoint.proceed();

            // 返回结果同理
            if (logAnno.recordResult()) {
                try {
                    String rawResp = JsonUtil.toJson(result);
                    respResult = SensitiveDesensitizeUtil.desensitize(rawResp);
                } catch (Exception ex) {
                    log.warn("[操作日志]返回结果序列化失败", ex);
                    respResult = "[返回值序列化失败]";
                }
            }

            return result;

        } catch (Throwable e) {
            // 业务抛出异常，标记失败，记录异常信息
            success = false;
            errorMsg = e.getMessage();
            log.error("[操作日志]接口发生异常", e);
            throw e; // 异常继续向外抛出，交给全局异常处理器处理，不能吞掉异常
        } finally {
            // 计算接口耗时
            long startTime = LogTimeHolder.getStartTime();
            long costMs = System.currentTimeMillis() - startTime;

            // ！！必须清理ThreadLocal，防止内存泄漏
            LogTimeHolder.clear();

            // ======================核心：打印日志到文件，不操作数据库======================
            log.info("[操作日志] module={},desc={},url={},method={},ip={},success={},costMs={},reqParam={},respResult={},errorMsg={}",
                    module,
                    desc,
                    url,
                    httpMethod,
                    ip,
                    success,
                    costMs,
                    reqParam,
                    respResult,
                    errorMsg
            );
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多个代理取第一个
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("Proxy‑Client‑IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}

