package com.xiaoden.spring.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * @description:返回代理对象的原始对象
 */
public class FHAopProxyUtis {
    /*
     * 功能描述:返回代理对象的原始对象
     */
    public static Object getTargetObject(Object target)throws Exception{
        //先判断一下，这个传进来的对象是不是一个代理过的对象
        boolean aopProxy = isAopProxy(target);
        if(!aopProxy){
        return target;//如果不是代理对象直接返回
        }

        return getProxyTargetObject(target);

    }
    private static  boolean isAopProxy(Object object){
        return Proxy.isProxyClass(object.getClass());
    }
    private static  Object getProxyTargetObject(Object proxy)throws Exception{
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        FHAopProxy aopProxy=(FHAopProxy)h.get(proxy);
        Field target = aopProxy.getClass().getDeclaredField("target");
        target.setAccessible(true);

        return target.get(aopProxy);
    }


}
