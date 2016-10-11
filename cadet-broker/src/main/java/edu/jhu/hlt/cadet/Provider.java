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
    void close() throws Exception;
}
