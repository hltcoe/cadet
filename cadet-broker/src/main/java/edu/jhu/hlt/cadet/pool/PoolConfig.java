/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
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
