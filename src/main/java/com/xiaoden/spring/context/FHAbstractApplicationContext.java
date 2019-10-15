package com.xiaoden.spring.context;

/**
 * @author dengfuhai
 * @description
 * @date 2019/10/11 0011
 */
public abstract class FHAbstractApplicationContext {
    /*
     * 功能描述:提供给子类重写
     */
    protected void onRefresh() {
    }

    protected abstract void refreshBeanFactory();


}
