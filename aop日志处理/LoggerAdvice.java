package com.brandslink.cloud.warehouse.advice;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * 全局日志处理
 *
 * @Author: zhangjinhua
 * @Date: 2019/6/21 16:49
 */
@Aspect
@Component
@Slf4j
public class LoggerAdvice {

    private static final String ID = "loggerAdvice";

    /**
     * Controller层切入点定义
     */
    @Pointcut("execution(* com.brandslink.cloud.warehouse.controller..*.*(..))")
    public void controller() {
    }

    /**
     * 环绕通知日志
     */
    @Around("controller()")
    public Object controllerBefore(ProceedingJoinPoint pjp) {
        //增加uuid，方便问题定位
        MDC.put(ID, UUID.randomUUID().toString().replace("-", ""));
        log.debug("uuid开始为：{}", MDC.get(ID));

        long beginTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n==========================================").append("\r\n");
        sb.append("Request Start : " + new Date()).append("\r\n");
        sb.append("Request url : " + request.getRequestURL().toString()).append("\r\n");
        sb.append("Http Method : " + request.getMethod()).append("\r\n");
        sb.append("CLASS_METHOD : " + pjp.getTarget().getClass().getSimpleName() + "."
                + pjp.getSignature().getName()).append("\r\n");
        sb.append("Params : " + Arrays.toString(pjp.getArgs())).append("\r\n");

        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            sb.append("Request error : " + e.getMessage()).append("\r\n");
            //清除uuid
            log.debug("uuid结束为：{}", MDC.get(ID));
            MDC.clear();
        } finally {
            long costMs = System.currentTimeMillis() - beginTime;
            sb.append("Request Result : " + result).append("\r\n");
            sb.append("Request End : " + new Date() + ",used : " + costMs + "ms").append("\r\n");
            sb.append("==========================================").append("\r\n");
            log.info(sb.toString());
            //清除uuid
            log.debug("uuid结束为：{}", MDC.get(ID));
            MDC.clear();
        }
        return result;
    }
}
