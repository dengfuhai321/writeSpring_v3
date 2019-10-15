package com.xiaoden.spring.webmvc.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @description：Spring5对WebFlux的支持
 * Servlet2.x: 单实例多线程的阻塞式io(BIO)
 * Servlet3.x: 单实例多线程的异步非阻塞式io(AIO异步非阻塞，NIO同步非阻塞)
 * WebFlux 是在Servlet3.x的基础上应运而生
 *  特点：1.基于Servlet3.x是实现异步非阻塞的Http响应
 *       2.api完美支持Rest风格
 *       3.支持函数式编程
 *       4.Mono返回单个对象，Flux返回集合
 *       如果要使用这个，最好在springboot中使用
 */
public class AsyncServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //解决中文乱码
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=utf-8");

        //实现两个线程，异步通知另外一个线程
        PrintWriter out = resp.getWriter();
        //这是一个线程
        out.print(Thread.currentThread().getName()+"接到请求");
        //异步的context
        //这是另外一个线程的异步容器
        AsyncContext asyncContext = req.startAsync();

        asyncContext.addListener(new AsyncListener() {
            //完成是执行
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                out.println(Thread.currentThread().getName()+"完成请求");
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
                out.println("请求超时");
            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {
                out.println("请求错误");
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
                out.println("请求开始");
            }
        });
        //另外一个线程，这个线程是异步的，上面的线程通知这个线程，然后异步实现这个线程代码
        asyncContext.start(new Runnable() {
            @Override
            public void run() {
                out.println(Thread.currentThread().getName()+"真正的业务逻辑，正在执行");
                asyncContext.complete();//结束该容器
            }
        });
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
}
