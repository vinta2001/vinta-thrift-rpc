package com.vinta.thrift.client;

import com.vinta.thrift.VintaThriftMethodInterceptor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class VintaThriftClientProxy<T> {

    private Class<T> serviceInterface;
    private String remoteAppKey;
    private Integer remotePort;
    private Integer timeout;
    private Integer retries;
    private String remoteHost;

    private TTransport transport;

    public Object getClient() {
        try {
            transport = new TSocket(remoteHost, getRemotePort(), 30000);
            // 2.创建TProtocol  协议要和服务端一致
            TProtocol protocol = new TBinaryProtocol(transport);
            Constructor<?> constructor = serviceInterface.getConstructor(TProtocol.class);
            transport.open();
//            VintaThriftMethodInterceptor vintaThriftMethodInterceptor = new VintaThriftMethodInterceptor();
//            return new ProxyFactory(serviceInterface,vintaThriftMethodInterceptor).getProxy();
            return constructor.newInstance(protocol);
        } catch (
                TTransportException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public Integer getRemotePort() {
        return remotePort != null ? remotePort : Integer.valueOf(8081);
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
}
