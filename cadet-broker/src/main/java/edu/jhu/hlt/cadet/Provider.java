package edu.jhu.hlt.cadet;

import com.typesafe.config.Config;

/**
 * Any dependencies that ConfigManager builds must implement this interface
 */
public interface Provider {
    /**
     * Must be called before use
     * 
     * @param config  Config object for this provider
     */
    void init(Config config);
}
