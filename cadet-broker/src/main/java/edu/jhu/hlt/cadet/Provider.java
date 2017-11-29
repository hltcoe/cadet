/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet;

import com.typesafe.config.Config;

/**
 * Any dependencies that ConfigManager builds must implement this interface
 */
public interface Provider extends AutoCloseable {
    /**
     * Must be called before use
     *
     * @param config  Config object for this provider
     */
    void init(Config config);

    /**
     * Must be called after use to give the provider the chance to shutdown
     */
    @Override
    void close();
}
