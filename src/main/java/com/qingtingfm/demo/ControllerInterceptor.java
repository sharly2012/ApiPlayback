package com.qingtingfm.demo;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Objects;
import java.util.UUID;

@Aspect
@Component
public class ControllerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ControllerInterceptor.class);
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private static final ThreadLocal<String> key = new ThreadLocal<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义拦截规则：拦截com.**.**.controller..)包下面的所有类中，有@RequestMapping注解的方法
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void controllerMethodPointcut() {
    }

    /**
     * 请求方法前打印内容
     *
     * @param joinPoint JoinPoint
     */
    @Before("controllerMethodPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        // 请求开始时间
        startTime.set(System.currentTimeMillis());

        // 上下文的Request容器
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = Objects.requireNonNull(sra).getRequest();
        // 获取请求头
        Enumeration<String> enumeration = request.getHeaderNames();
        StringBuilder headers = new StringBuilder();
        JSONObject header = new JSONObject();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            headers.append(name).append(":").append(value).append(",");
            header.put(name, value);
        }

        // uri
        String uri = UUID.randomUUID() + "_" + request.getRequestURI();

        // 获取param
        String method = request.getMethod();
        StringBuilder params = new StringBuilder();
        if (HttpMethod.GET.toString().equals(method)) {// get请求
            String queryString = request.getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                //params.append(URLEncodedUtils.encode(queryString, "UTF-8"));
                params.append(queryString);

            }
        } else {//其他请求
            Object[] paramsArray = joinPoint.getArgs();
            if (paramsArray != null && paramsArray.length > 0) {
                for (Object o : paramsArray) {
                    if (o instanceof Serializable) {
                        params.append(o.toString()).append(",");
                    } else {
                        //使用json序列化 反射等等方法 反序列化会影响请求性能建议重写tostring方法实现系列化接口
                        try {
                            String param = objectMapper.writeValueAsString(o);
                            if (param != null && !param.isEmpty())
                                params.append(param).append(",");
                        } catch (JsonProcessingException e) {
                            log.error("doBefore obj to json exception obj={},msg={}", o, e);
                        }
                    }
                }
            }
        }
        key.set(uri);
        System.out.println("********** 开始请求拦截 **********");
        System.out.println("请求拦截 uri：" + uri + " method：" + method + " params：" + params + " headers：" + headers);
    }

    /**
     * 在方法执行后打印返回内容
     *
     * @param obj object
     */
    @AfterReturning(returning = "obj", pointcut = "controllerMethodPointcut()")
    public void doAfterReturning(Object obj) {
        long costTime = System.currentTimeMillis() - startTime.get();
        String uri = key.get();
        startTime.remove();
        key.remove();
        String result = null;
        if (obj instanceof Serializable) {
            result = obj.toString();
        } else {
            if (obj != null) {
                try {
                    result = objectMapper.writeValueAsString(obj);
                } catch (JsonProcessingException e) {
                    log.error("doAfterReturning obj to json exception obj={},msg={}", obj, e);
                }
            }
        }
        System.out.println("结果拦截 uri：" + uri + " result：" + result + " costTime：" + costTime);
        System.out.println("********** 拦截结束 **********");
    }
}
