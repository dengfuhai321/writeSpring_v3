package com.xiaoden.spring.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @description：对application.xml文件中<aop></aop>中expression的封装
 * 配置文件的目的：告诉spring，哪些类的那些方法需要增强，增强的内容是啥
 *  这里是对配置文件所体现的内容进行封装
 */
public class FHAopConfig {
    //记录每个方法和该执行前后的操作对象
    //以目标对象需要增强的method作为key，需要增强的代码内容作为value
    private Map<Method,FHAspect> points=new HashMap<>();
    /*
     * 功能描述:添加需要面向aop的方法
     * @param target需要加强的方法对象
     * @param aspect要加强的代码方法的类
     * @param points切点前后执行的方法数组
     */
    public void put(Method target,Object aspect,Method[] points){
        this.points.put(target,new FHAspect(aspect,points));
    }
    //获得aop中该方法的操作
    public FHAspect get(Method method){
        return this.points.get(method);
    }
    //查看aop中是否有该方法
    public boolean contain(Method method){
        return this.points.containsKey(method);
    }
    /*
     * 功能描述:对增强的代码封装
     */
    public class FHAspect{
        private Object aspect;//目标方法属于的类
        private Method[] points;//方法执行前后增强的代码

        public FHAspect(Object aspect, Method[] points) {
            this.aspect = aspect;
            this.points = points;
        }

        public Object getAspect() {
            return aspect;
        }

        public void setAspect(Object aspect) {
            this.aspect = aspect;
        }

        public Method[] getPoints() {
            return points;
        }

        public void setPoints(Method[] points) {
            this.points = points;
        }
    }

}
