package com.vinta.thrift;

import com.vinta.thrift.server.VintaThriftServerPublisher;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Method;
import java.util.Arrays;


public class VintaThriftMethodInterceptor implements MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(VintaThriftServerPublisher.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result;
        logger.info("VintaThriftMethodInterceptor invoke before");
        try {
            Object[] args = invocation.getArguments();
            Method method = invocation.getMethod();
            System.out.println(method.getDeclaringClass()==Object.class);
//            result = method.invoke(this,args);
            result = invocation.proceed();
            logger.info("VintaThriftMethodInterceptor invoke after");
            return result;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static void main(String[] args) {
//        ProxyFactory proxyFactory = new ProxyFactory(Service.class, new VintaThriftMethodInterceptor());
        VintaThriftMethodInterceptor interceptor = new VintaThriftMethodInterceptor();
        ServiceImpl service = new ServiceImpl();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(service);
        proxyFactory.addAdvice(interceptor);
        Service proxy = (Service) proxyFactory.getProxy();
        proxy.hello("vinta");
    }
}

interface Service {
    void hello(String msg);
}

class ServiceImpl implements Service {
    @Override
    public void hello(String msg) {
        System.out.println(msg + ", hello");
    }
}