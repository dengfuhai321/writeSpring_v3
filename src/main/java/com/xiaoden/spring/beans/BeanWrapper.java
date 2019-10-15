package com.xiaoden.spring.beans;

import com.xiaoden.spring.aop.FHAopConfig;
import com.xiaoden.spring.aop.FHAopProxy;
import com.xiaoden.spring.core.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * @author dengfuhai
 * @description
 * @date 2019/9/26 0026
 */
public class BeanWrapper extends FactoryBean {

    //依赖一个产生代理对象的工具类
    private FHAopProxy aopProxy=new FHAopProxy();
    //还会用到观察者模式
    //1.支持事件响应,会有一个监听
    private BeanPostProcessor postProcessor;

    public BeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(BeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    //原始的通过反射new出来，然后包装保存
    private Object originalinstance;//原始对象

    private Object wrapperinstance;//包装过的对象
    //从这里开始我们要把动态的代码添加进来了
    public BeanWrapper(Object instance){
        //这里先把不改变，弄成一样的
        this.originalinstance=instance;
        //把返回的对象变成代理的了
        this.wrapperinstance= aopProxy.getProxy(instance);

    }

    //返回代理对象
    public Object getWrappedInstance(){
        return this.wrapperinstance;
    }
    //返回原始对象
    public Object getOriginalInstance(){
        return this.originalinstance;
    }
    //返回代理以后的class
    public Class<?> getWrapperClass(){
        return this.wrapperinstance.getClass();
    }

    public void setAopConfig(FHAopConfig aopConfig){
        aopProxy.setConfig(aopConfig);
    }



}
