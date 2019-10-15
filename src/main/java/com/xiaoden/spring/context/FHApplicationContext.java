package com.xiaoden.spring.context;

import com.xiaoden.demo.mvc.action.DemoAction;
import com.xiaoden.demo.mvc.action.MyAction;
import com.xiaoden.spring.annotation.Autowried;
import com.xiaoden.spring.annotation.Controller;
import com.xiaoden.spring.annotation.Service;
import com.xiaoden.spring.aop.FHAopConfig;
import com.xiaoden.spring.beans.BeanDefinition;
import com.xiaoden.spring.beans.BeanPostProcessor;
import com.xiaoden.spring.beans.BeanWrapper;
import com.xiaoden.spring.context.support.BeanDefinitionReader;
import com.xiaoden.spring.core.BeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dengfuhai
 * @description
 * @date 2019/9/26 0026
 */
public class FHApplicationContext extends FHDefaultListableBeanFactory implements BeanFactory {

    private String[] configLocation;

    private BeanDefinitionReader reader;


    //这个才是用来保证注册单例式的容器,里面存放根据BeanDefinition配置new的实例对象
    //也就是被wrapper包装之前的原型实例对象
    private Map<String,Object> beanCacheMap=new HashMap<>();

    //这个是存放包装原型bean之后的对象
    private Map<String,BeanWrapper> beanWrapperMap=new ConcurrentHashMap<>();


    //构造方法读取配置文件...这种是不确定个数，原理和(String[] names)相同
    public FHApplicationContext(String ... locations) {
        this.configLocation=locations;
        onRefresh();
    }

    @Override
    public void onRefresh(){
            System.out.println("开始了");
        //定位
         reader = new BeanDefinitionReader(configLocation);

        //加载
        //这个其实获得的是bean的className,需要通过className获得beanDefinition
        List<String> beanClassNames=reader.loadBeanDefinitions();

        //注册
        doRegisty(beanClassNames);

        //依赖注入（lazy-init=false）,自动调用getBean方法
        doAutorited();

//        DemoAction myAction=(DemoAction) this.getBean("demoAction");
//        myAction.query(null,null,"xiaodeng");

    }



    //将BeanDefinition注册到beanDefinitionMap中
    private void doRegisty(List<String> beanDefinitions) {
        try{
        for (String className:beanDefinitions) {//看是className吧
            //beanName有三种情况
            //1.默认是类名的首字母小写
            //2.自定义名字,这里没有考虑。
            //3、接口注入
            Class<?> clazz = Class.forName(className);
            //如果是接口，不能实例化，暂时不做处理
            if(clazz.isInterface()){
                continue;
            }
            //从BeanDefinitionRead中获得BeanDefinition
            BeanDefinition beanDefinition = reader.registerBean(className);
            if(beanDefinition!=null){
                this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            }
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> i:interfaces) {
        //如果是多个实现类，只能覆盖之前的接口对应的BeanDefinition，spring中就是，
        //spring如果真发生了，会报错，可以使用自定义的名字
                this.beanDefinitionMap.put(i.getName(),beanDefinition);
            }

        }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    //通过读取BeanDefinittion中的信息，然后通过反射获取到一个实例并返回
    //spring的做法是，不会吧最原始的对象放出去，会用BeanWrapper来进行一次封装
    //装饰器模式
    //1.保留原来的oop关系2.在原来的基础上拓展，增强（为aop打基础）
    public Object getBean(String beanName) {
        //beanName是ioc容器存放BeanDefinition的keyname，默认类名首字母小写
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        //全包名
        String beanClassName = beanDefinition.getBeanClassName();

        try {
            //生成通知事件
            BeanPostProcessor processor = new BeanPostProcessor();


            Object bean = instantionBean(beanDefinition);//实例化对象
            if(bean==null){
                return null;
            }
            //实例化初始化之前调用一次（也就是实例对象生成代理的包装对象之前）
            processor.postProcessBeforeInitialization(bean,beanName);

            BeanWrapper beanWrapper = new BeanWrapper(bean);
            //设置该对象的aop配置
            beanWrapper.setAopConfig(instantionAopConfig(beanDefinition));

            this.beanWrapperMap.put(beanName,beanWrapper);

            //实例化初始化之后调用一次
            processor.postProcessAfterInitialization(bean,beanName);


            //已经变成返回代理对象了
            return this.beanWrapperMap.get(beanName).getWrappedInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //传入BeanDefinition，获得一个实例Bean
    public Object instantionBean(BeanDefinition beanDefinition){
        Object instance=null;
        String beanClassName = beanDefinition.getBeanClassName();
        try {
            //如果beanCacheMap中已经有该Bean对象，直接获取该对象，保持注册式单例
            if(this.beanCacheMap.containsKey(beanClassName)){
                instance=this.beanCacheMap.get(beanClassName);
            }else{
                Class<?> clazz = Class.forName(beanClassName);
                instance = clazz.newInstance();
                this.beanCacheMap.put(beanClassName,instance);

            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //开始执行自动化的依赖注入
    private void doAutorited() {
        //获得所有的代理对象，即把所有的包装对象BeanWrapper放到Spring容器中
        for (Map.Entry<String,BeanDefinition> entry:
                this.beanDefinitionMap.entrySet()) {
            if(!entry.getValue().isLazyInit()){
                Object bean = getBean(entry.getKey());//得到的是代理对象
                System.out.println(bean.getClass());
            }

        }
        //注入依赖，就是绑定对象和对象之间的关系
        for (Map.Entry<String,BeanWrapper> entry:
                this.beanWrapperMap.entrySet()) {
            //如果想要实现aop就需要注入代理对象
            //如果不需要实现aop
            populateBean(entry.getKey(),entry.getValue().getOriginalInstance());//给原始对象进行依赖注入

        }


    }
    //依赖注入
    //这里只有注入代理对象才能实现aop增强，但考虑到spring容器中beanWrapperMap
    //存放的最好是最原始的对象的依赖关系
    //这里我试了试，真实对象依赖的是Service代理对象
    public void populateBean(String beanName,Object instance){
        Class<?> clazz = instance.getClass();
        //如果不是Controller或者Service的类，就没必要依赖注入了，普通类是不能写autowired的
        if(!(clazz.isAnnotationPresent(Controller.class)||clazz.isAnnotationPresent(Service.class))){
            return ;
        }

        //获得所有的属性对象
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field:declaredFields) {
            //如果没有属性没有Autowired就不用依赖注入 了
            if(!field.isAnnotationPresent(Autowried.class)){
                continue;
            }
            //然后获得该属性对象的名字
            //这是获得该属性对象标记的Autowried注解对象
            Autowried autowried = field.getAnnotation(Autowried.class);
            //获得该注解对象的名字
            String autowiredBeanName = autowried.value().trim();
            if("".equals(autowiredBeanName)){
                //如果注解名字为空就是没有自定义名字
                //默认该属性对象的类型首字母小写
                 autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);
            try {
               field.set(instance,this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }
    /*
     * 功能描述:得到BeanDefinition在ioc容器中的名字
     */
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);

    }
    public Properties getConfig(){
        return this.reader.getConfig();
    }


    @Override
    protected void refreshBeanFactory() {

    }

    private FHAopConfig instantionAopConfig(BeanDefinition beanDefinition)throws Exception{
        FHAopConfig aopConfig = new FHAopConfig();
        String expression= reader.getConfig().getProperty("pointCut");
        String[] aspectBefores = reader.getConfig().getProperty("aspectBefore").split("\\s");

        String[] aspectAfters = reader.getConfig().getProperty("aspectAfter").split("\\s");
        String beanClassName = beanDefinition.getBeanClassName();
        //获得bean对象的
        Class<?> clazz = Class.forName(beanClassName);
        Pattern compile = Pattern.compile(expression);
        //得到加强代码的类
        Class<?> aspectclazz = Class.forName(aspectBefores[0]);
        //遍历getBean()中的所有方法，看是否需要aop增强方法
        for (Method method:clazz.getMethods()) {
            Matcher matcher = compile.matcher(method.toString());
            if(matcher.matches()){
                Class<?>[] interfaces = clazz.getInterfaces();
                String name= method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                //这是获得接口的该方法，因为之后方法invoke后，比较的是interface.method
                Method method1 = interfaces[0].getMethod(name,parameterTypes);
                //把匹配切面规则的方法，添加到aop配置中
                aopConfig.put(method1,aspectclazz.newInstance(),new Method[]{aspectclazz.getMethod(aspectBefores[1]),aspectclazz.getMethod(aspectAfters[1])});

            }
        }
        return aopConfig;
    }
}
