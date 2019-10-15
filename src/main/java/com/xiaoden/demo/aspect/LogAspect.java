package com.xiaoden.demo.aspect;

/**
 * @author dengfuhai
 * @description
 */
public class LogAspect {
    //在调用一个方法之前，执行这个方法
    public void before(){
        //这个方法中的逻辑是我们自己写的，本来应该让用户自己写
        System.out.println("方法执行前调用");

    }
    //在调用一个方法之后，执行这个方法
    public void after(){
        System.out.println("方法执行后调用");

    }


}
