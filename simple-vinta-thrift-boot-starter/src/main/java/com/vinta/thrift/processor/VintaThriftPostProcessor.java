package com.vinta.thrift.processor;

import com.vinta.thrift.anntation.VintaThriftServer;
import com.vinta.thrift.server.VintaThriftServerPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VintaThriftPostProcessor implements BeanFactoryPostProcessor, BeanClassLoaderAware, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(VintaThriftPostProcessor.class);

    private ClassLoader classLoader;
    private ConfigurableEnvironment environment;

    private Map<Integer, Set<String>> beanNameMaps = new HashMap<>();

    public VintaThriftPostProcessor() {
        logger.info("VintaThriftPostProcessor init");
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("VintaThriftPostProcessor postProcessBeanFactory");
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        filterThriftBeanName(registry);
        for (Map.Entry<Integer, Set<String>> entry : beanNameMaps.entrySet()) {
            BeanDefinitionBuilder server = buildThriftServerBeanDefinition(beanFactory, entry);
            String beanName = "multiServicePublisher" + entry.getKey();
            registry.registerBeanDefinition(beanName, server.getBeanDefinition());
        }
    }

    /**
     * 手动生成一个beanDefinition
     */
    private BeanDefinitionBuilder buildThriftServerBeanDefinition(ConfigurableListableBeanFactory beanFactory,
                                                                  Map.Entry<Integer, Set<String>> entry) {
        // 构建一个BeanDefinitionBuilder
        logger.info("VintaThriftPostProcessor buildThriftServerBeanDefinition");
        BeanDefinitionBuilder publisher = BeanDefinitionBuilder.genericBeanDefinition(VintaThriftServerPublisher.class);
        //  设置port属性
        publisher.addPropertyValue("port", entry.getKey());

        String appName = environment.getProperty("app.name");
        String hostname = environment.getProperty("app.hostname");


        Set<String> beanNames = entry.getValue();
        // 获取被@VintaThriftServer标注的thrift服务的bean名字
        String beanName = beanNames.toArray(new String[0])[0];
        logger.info("VintaThriftPostProcessor buildThriftServerBeanDefinition beanNames: {}", beanName);
        // 获取被@VintaThriftServer标注的thrift服务的bean的Class对象
        Class<?> clazz = getServiceImplementationClass(beanFactory, beanName);
        logger.info("VintaThriftPostProcessor buildThriftServerBeanDefinition clazz: {}", clazz);
        // 获取被@VintaThriftServer标注的thrift服务的bean的serviceInterface注解属性
        Class<?> serviceInterface = getServiceInterface(clazz);
        logger.info("VintaThriftPostProcessor buildThriftServerBeanDefinition serviceInterface: {}", serviceInterface);
        publisher.addPropertyValue("serviceInterface", getServiceInterfaceName(serviceInterface));
        publisher.addPropertyReference("serviceImpl", beanName);
        logger.info("VintaThriftPostProcessor buildThriftServerBeanDefinition serviceSimpleName: {}", appName);
        publisher.addPropertyValue("serviceSimpleName", appName);
        logger.info("VintaThriftPostProcessor buildThriftServerBeanDefinition hostname: {}", hostname);
        publisher.addPropertyValue("hostname", hostname);
        logger.info("VintaThriftPostProcessor buildThriftServerBeanDefinition publisher: {}", publisher);

        return publisher;
    }

    private String getServiceInterfaceName(Class<?> serviceInterface) {
        return serviceInterface.getName();
    }

    /**
     * 获取所有被@VintaThriftServer注解标注的thrift服务
     * @param registry
     */
    private void filterThriftBeanName(BeanDefinitionRegistry registry) {
        logger.info("VintaThriftPostProcessor filterThriftBeanName");
        // 获取所有beanDefinitionNames
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            //  获取指定beanDefinitionName的beanDefinition
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            //  获取beanDefinition的beanClassName
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                // 解析beanClassName
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, classLoader);
                //  获取beanDefinitionName对应的beanDefinition上的VintaThriftServer注解
                VintaThriftServer vintaThriftserver = AnnotationUtils.getAnnotation(clazz, VintaThriftServer.class);
                // 如果被注解修饰，则保存beanDefinitionName
                if (vintaThriftserver != null) {
                    int port = getThriftPort(vintaThriftserver);
                    if (!beanNameMaps.containsKey(port)) {
                        beanNameMaps.put(port, new HashSet<>());
                    }
                    beanNameMaps.get(port).add(beanDefinitionName);
                }
            }
        }
    }

    private Class<?> getServiceImplementationClass(ConfigurableListableBeanFactory beanFactory, String beanName) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        String beanClassName = beanDefinition.getBeanClassName();
        assert beanClassName != null;
        return ClassUtils.resolveClassName(beanClassName, classLoader);
    }

    private Class<?> getServiceInterface(Class<?> clazz) {
        VintaThriftServer vintaThriftserver = AnnotationUtils.getAnnotation(clazz, VintaThriftServer.class);
        assert vintaThriftserver != null;
        if (vintaThriftserver.serviceInterface().equals(Object.class)) {
            Class<?>[] allInterfaces = ClassUtils.getAllInterfacesForClass(clazz);
            return allInterfaces[0];
        } else {
            return vintaThriftserver.serviceInterface();
        }
    }

    private int getThriftPort(VintaThriftServer vintaThriftserver) {
        return vintaThriftserver.port();
    }
}








