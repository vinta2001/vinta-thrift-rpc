package com.vinta.some.thrift.test.server.controller;

import com.vinta.some.thrift.client.api.VintaThriftTestService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VintaThriftTestController {

    @Resource
    private VintaThriftTestService.Iface vintaThriftTestService;
    @GetMapping("/hello")
    public String test() {
        return "hello world";
    }
}
