package com.xiaoden.spring.context;

import com.xiaoden.spring.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dengfuhai
 * @description
 * @date 2019/10/11 0011
 */
public class FHDefaultListableBeanFactory extends FHAbstractApplicationContext {

    //spring中这个只是来保存配置信息
    protected Map<String, BeanDefinition> beanDefinitionMap=new ConcurrentHashMap<>();

    @Override
    protected void onRefresh(){

    };

    @Override
    protected void refreshBeanFactory() {

    }
}
