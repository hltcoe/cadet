package edu.jhu.hlt.cadet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.jhu.hlt.cadet.learn.SortReceiverServer;

/**
 * Performs an initialization required by servlets or servers
 *
 */
public class BootLoader implements ServletContextListener {
    private static Logger logger = LoggerFactory.getLogger(BootLoader.class);
    private static final String CONFIG_NAME = "cadet.config";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        initConfigManager(event.getServletContext());
        if (ConfigManager.getInstance().isLearningOn()) {
            launchSortServer();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (ConfigManager.getInstance().isLearningOn()) {
            shutdownSortServer();
        }
        ConfigManager.getInstance().close();
    }

    private void initConfigManager(ServletContext context) {
        String configFile = context.getInitParameter(CONFIG_NAME);
        ConfigManager.getInstance().init(configFile);
        logger.info("Finished processing configuration");
    }

    private SortReceiverServer server;
    private Thread serverThread;

    private void launchSortServer() {
        server = ConfigManager.getInstance().getSortReceiverServer();
        serverThread = new Thread(server);
        serverThread.start();
        logger.info("Started the sort server on port " + server.getPort());
    }

    private void shutdownSortServer() {
        if (serverThread != null) {
            server.close();
            try {
                serverThread.join();
                logger.info("Sort server shutting down");
            } catch (InterruptedException e) {
                logger.warn("Something funky happened shuting down the sort server");
            }
        }
    }
}
