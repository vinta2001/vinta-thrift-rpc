package com.vinta.thrift.anntation;


import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface VintaThriftServer {

    String value() default "";

    Class<?> serviceInterface() default Object.class;
    int port() default -1;
}
