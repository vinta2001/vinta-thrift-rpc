package com.vinta.some.thrift.test.server.service;

import com.vinta.some.thrift.client.api.VintaThriftTestService;
import com.vinta.thrift.anntation.VintaThriftServer;
import org.apache.thrift.TException;

@VintaThriftServer(port = 8081)
public class VintaThriftTestServiceImpl implements VintaThriftTestService.Iface{
    @Override
    public String helloString(String para) throws TException {
        return "hello, "+ para;
    }
}
