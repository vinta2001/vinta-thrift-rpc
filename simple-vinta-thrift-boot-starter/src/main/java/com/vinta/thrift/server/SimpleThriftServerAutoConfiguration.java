package com.vinta.thrift.server;

import com.vinta.thrift.processor.VintaThriftPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleThriftServerAutoConfiguration {

    @Bean(name = "vintaThriftPostProcessor")
    public static VintaThriftPostProcessor vintaThriftPostProcessor() {
        return new VintaThriftPostProcessor();
    }
}
