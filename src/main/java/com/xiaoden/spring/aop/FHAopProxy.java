package com.xiaoden.spring.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author dengfuhai
 * @description：这是代理对象，（这里默认使用jdk动态代理）
 */
public class FHAopProxy implements InvocationHandler {
    //aop的配置文件
    private FHAopConfig config;

    //需要代理的目标对象
    private Object target;

    public void setConfig(FHAopConfig config){
        this.config=config;
    }


    public Object getProxy(Object instance){
        this.target=instance;
        Class<?> clazz = instance.getClass();
        //返回代理对象参数，目标对象的类加载，目标对象的接口，this
        return Proxy.newProxyInstance(clazz.getClassLoader(),
                clazz.getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //在原始方法执行前要执行的增强的方法
        if(this.config.contain(method)){
            FHAopConfig.FHAspect fhAspect = this.config.get(method);
            fhAspect.getPoints()[0].invoke(fhAspect.getAspect());
        }
        //反射调用原始的方法
        Object invoke = method.invoke(this.target, args);
        //在原始方法执行后要执行的增强的方法
        if(this.config.contain(method)){
            FHAopConfig.FHAspect fhAspect = this.config.get(method);
            fhAspect.getPoints()[1].invoke(fhAspect.getAspect());
        }
        //返回原始方法返回值
        return invoke;
    }
}
