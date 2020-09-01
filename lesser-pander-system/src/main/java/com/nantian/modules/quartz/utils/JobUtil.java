package com.nantian.modules.quartz.utils;

import cn.hutool.core.util.StrUtil;
import com.nantian.utils.ApplicationContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * 异步执行定时任务
 *
 * @author shuyx
 */
@Component(value = "jobUtil")
public class JobUtil {

    /**
     * 异步执行任务
     *
     * @param beanName
     * @param methodName
     * @param params
     * @return
     * @throws Exception
     */
    @Async(value = "threadPoolTaskExecutor")
    public Future<Object> executeTask(String beanName, String methodName, String params) throws Exception {
        Object resultObject;
        Method method;
        Object bean = ApplicationContextHolder.getBean(beanName);
        if (StrUtil.isNotBlank(params)) {
            method = bean.getClass().getDeclaredMethod(methodName, String.class);
        } else {
            method = bean.getClass().getDeclaredMethod(methodName);
        }
        // 判断方法是否可执行
        ReflectionUtils.makeAccessible(method);
        if (StrUtil.isNotBlank(params)) {
            resultObject = method.invoke(bean, params);
        } else {
            resultObject = method.invoke(bean);
        }
        return new AsyncResult<>(resultObject);
    }
}
