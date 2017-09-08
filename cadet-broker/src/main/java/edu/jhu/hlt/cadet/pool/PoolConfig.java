package edu.jhu.hlt.cadet.pool;

public class PoolConfig {
    private final int size;
    private final long timeout; // in milliseconds

    public PoolConfig(int size, long timeout) {
        this.size = size;
        this.timeout = timeout;
    }

    public int getSize() {
        return size;
    }

    public long getTimeout() {
        return timeout;
    }
}
