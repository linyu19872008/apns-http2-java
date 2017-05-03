package com.cdeer.apns.http2.core.netty;


import com.cdeer.apns.http2.core.model.ApnsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 连接池
 */
public class NettyApnsConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(NettyApnsConnectionPool.class);

    private static final String HOST_DEVELOPMENT = "api.development.push.apple.com";

    private static final String HOST_PRODUCTION = "api.push.apple.com";

    private static final String ALGORITHM = "sunx509";

    private static final String KEY_STORE_TYPE = "PKCS12";

    private static final int PORT = 2197;

    private volatile boolean isShutdown;

    public BlockingQueue<NettyApnsConnection> connectionQueue;

    public NettyApnsConnectionPool(ApnsConfig config) {
        int poolSize = config.getPoolSize();
        connectionQueue = new LinkedBlockingQueue<>(poolSize);

        String host = config.isDevEnv() ? HOST_DEVELOPMENT : HOST_PRODUCTION;
        KeyManagerFactory keyManagerFactory = createKeyManagerFactory(config);
        for (int i = 0; i < poolSize; i++) {
//            NettyApnsConnection connection = new NettyApnsConnection("conn-" + i, host, PORT,
//                    config.getRetries(), config.getTimeout(), config.getTopic(), keyManagerFactory);
            NettyApnsConnection connection = new NettyApnsConnection(config.getName(), host, PORT,
                    config.getRetries(), config.getTimeout(), config.getTopic(), keyManagerFactory);
            connection.setConnectionPool(this);
            connectionQueue.add(connection);
        }
    }

    private KeyManagerFactory createKeyManagerFactory(ApnsConfig config) {
        try {
            char[] password = config.getPassword().toCharArray();
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            keyStore.load(config.getKeyStore(), password);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(ALGORITHM);
            keyManagerFactory.init(keyStore, password);
            return keyManagerFactory;
        } catch (Exception e) {
            log.error("createKeyManagerFactory", e);
            throw new IllegalStateException("create key manager factory failed");
        }
    }

    public NettyApnsConnection acquire() {
        try {
            return connectionQueue.take();
        } catch (InterruptedException e) {
            log.error("acquire", e);
        }

        return null;
    }

    public void release(NettyApnsConnection connection) {
        if (connection != null) {
            connectionQueue.add(connection);
        }
    }

    public void shutdown() {
        isShutdown = true;
    }

    public boolean isShutdown() {
        return isShutdown;
    }
}
