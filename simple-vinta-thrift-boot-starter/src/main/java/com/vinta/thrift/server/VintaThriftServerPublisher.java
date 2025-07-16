package com.vinta.thrift.server;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VintaThriftServerPublisher implements InitializingBean, DisposableBean {

    public static final Logger logger = LoggerFactory.getLogger(VintaThriftServerPublisher.class);

    private Integer port;

    private String hostname;

    private String serviceSimpleName;
    private Object serviceImpl;
    private Class<?> serviceInterface;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TServerSocket serverTransport;
    private TServer server;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getServiceSimpleName() {
        return serviceSimpleName;
    }

    public void setServiceSimpleName(String serviceSimpleName) {
        this.serviceSimpleName = serviceSimpleName;
    }

    public Object getServiceImpl() {
        return serviceImpl;
    }

    public void setServiceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void destroy() {
        logger.info("[关闭RPC服务]:serviceSimpleName: {}, hostname: {}, port: {},销毁资源并关闭服务",
                serviceSimpleName, hostname, port);
        if (server != null && server.isServing()) {
            try {
                server.stop();
                serverTransport.close();
                executor.shutdown();

            } catch (Exception e) {
                logger.error("关闭 Thrift 服务失败", e);
            } finally {
                server = null;
                serverTransport = null;
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("[开启RPC服务]:serviceSimpleName: {}, hostname:{}, port: {}，获取资源并开启服务",
                serviceSimpleName, hostname, port);
        try {
            Class<?> iface = serviceImpl.getClass().getInterfaces()[0];
            if (!iface.getName().endsWith("$Iface")) {
                throw new IllegalArgumentException("ServiceImpl 必须实现 Thrift 生成的 Iface 接口");
            }
            Class<?> outerClass = iface.getEnclosingClass();
            if (outerClass == null) {
                throw new IllegalStateException("无法获取外部类，请检查 Iface 是否为内部接口");
            }
            serverTransport = new TServerSocket(getPort());

            TProcessor processor = getProcessor(outerClass, iface, serviceImpl);
            TProtocolFactory protocolFactory = getProtocolFactory();
            TThreadPoolServer.Args tArgs = new TThreadPoolServer.Args(serverTransport)
                    .processor(processor)
                    .protocolFactory(protocolFactory)
                    .minWorkerThreads(5)
                    .maxWorkerThreads(20);
            server = new TSimpleServer(tArgs);
            executor.execute(() -> server.serve());
        } catch (Exception e) {
            logger.error("启动 Thrift 服务失败", e);
            throw e;
        }
    }

    private Class<?> getProcessorClass(Class<?> clazz) throws Exception {
        return Arrays.stream(clazz.getDeclaredClasses())
                .filter(c -> c.getName().endsWith("Processor"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到 Processor 类"));
    }

    private TProcessor getProcessor(Class<?> clazz, Class<?> iface, Object serviceImpl) throws Exception {
        Class<?> processorClass = getProcessorClass(clazz);

        Constructor<?> constructor = processorClass.getConstructor(iface);
        return (TProcessor) constructor.newInstance(serviceImpl);
    }


    private TProcessor getProcessor(Class<?> clazz) throws Exception {
        return null;
    }

    private TProtocolFactory getProtocolFactory() {
        return new TBinaryProtocol.Factory();
    }

    private TProtocol getProtocol() {
        return null;
    }
}
