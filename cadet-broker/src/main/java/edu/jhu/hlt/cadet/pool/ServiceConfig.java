package edu.jhu.hlt.cadet.pool;

public class ServiceConfig {
    private final String host;
    private final int port;

    public ServiceConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
