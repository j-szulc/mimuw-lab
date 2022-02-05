package com.rtbhouse;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class TestProxyBackend {
    public static void main(String[] args) throws Exception {
        TestProxyBackend backend = new TestProxyBackend();
        backend.run(Integer.parseInt(args[0]));
    }

    public void run(int port) throws Exception {
        QueuedThreadPool queuedThreadPool = new QueuedThreadPool(1000, 10,
                new BlockingArrayQueue<>(1000));
        queuedThreadPool.setName("http-threads-pool");

        Server server = new Server(queuedThreadPool);

        HttpConfiguration config = new HttpConfiguration();
        config.setSendServerVersion(false);
        config.setRequestHeaderSize(32 * 1024);

        ConnectionFactory connectionFactory = new HttpConnectionFactory(config);

        ServerConnector connector = new ServerConnector(server, null, null, null, -1, -1, connectionFactory);
        connector.setPort(port);
        connector.setAcceptQueueSize(1024);

        server.setConnectors(new Connector[] { connector });

        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.addServlet(AsyncServlet.class, "/");
        servletHandler.addServlet(HealthServlet.class, "/health");

        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setInflateBufferSize(1024);
        gzipHandler.setHandler(servletHandler);

        server.setHandler(gzipHandler);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (Exception e) {
                System.out.println(e);
            }
        }));
    }
}
