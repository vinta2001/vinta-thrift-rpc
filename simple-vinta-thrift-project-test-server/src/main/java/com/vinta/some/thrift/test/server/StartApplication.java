package com.vinta.some.thrift.test.server;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.vinta")
public class StartApplication {
    public static void main(String[] args) {



        SpringApplication.run(StartApplication.class, args);
    }
}