package com.cdeer.apns.http2.core.model;

import java.io.InputStream;

/**
 * Apns配置
 */
public class ApnsConfig {

    private String name;// 配置名称

    private InputStream keyStore;// 证书

    private String password;// 密码

    private boolean isDevEnv = false;// 是否是开发环境

    private int poolSize = 3;// 线程池大小

    private int cacheLength = 100;// 缓存长度

    private int retries = 1;// 重试次数

    private int intervalTime = 1800000;// 间隔时间

    private int timeout = 10000;// 超时时间

    private String topic;// 标题,即证书的bundleID

    public ApnsConfig() {
    }

    public InputStream getKeyStore() {
        return this.keyStore;
    }

    public void setKeyStore(InputStream keyStore) {
        this.keyStore = keyStore;
    }

    public void setKeyStore(String keystore) {
        InputStream is = ApnsConfig.class.getClassLoader().getResourceAsStream(keystore);
        if (is == null) {
            throw new IllegalArgumentException("Keystore file not found. " + keystore);
        } else {
            this.setKeyStore(is);
        }
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDevEnv() {
        return this.isDevEnv;
    }

    public void setDevEnv(boolean isDevEnv) {
        this.isDevEnv = isDevEnv;
    }

    public int getPoolSize() {
        return this.poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getCacheLength() {
        return this.cacheLength;
    }

    public void setCacheLength(int cacheLength) {
        this.cacheLength = cacheLength;
    }

    public int getRetries() {
        return this.retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getName() {
//        return this.name != null && !"".equals(this.name.trim()) ? this.name : (this.isDevEnv() ? "dev-env" : "product-env");
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIntervalTime() {
        return this.intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
