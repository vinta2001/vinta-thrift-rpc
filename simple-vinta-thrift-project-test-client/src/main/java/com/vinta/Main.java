package com.vinta;

import com.vinta.some.thrift.client.api.VintaThriftTestService;
import com.vinta.thrift.client.VintaThriftClientProxy;
import org.apache.thrift.TException;



public class Main {
    public static void main( String[] args ) {
        System.out.println("客户端启动....");
        try {
            VintaThriftClientProxy clientProxy = new VintaThriftClientProxy();
            clientProxy.setRemoteAppKey("com.vinta.some.thrift.test.server");
            clientProxy.setRemoteHost("localhost");
            clientProxy.setRetries(3);
            clientProxy.setTimeout(100);
            clientProxy.setServiceInterface(VintaThriftTestService.Client.class);
            VintaThriftTestService.Client client = (VintaThriftTestService.Client) clientProxy.getClient();
            System.out.println(client.helloString("vinta"));
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }
}