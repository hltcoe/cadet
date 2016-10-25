package edu.jhu.hlt.cadet;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import edu.jhu.hlt.cadet.ConfigManager;

public class ConfigManagerTest {

    private String getFilePath(String filename) {
        ClassLoader classLoader = ConfigManagerTest.class.getClassLoader();
        java.net.URL url = classLoader.getResource(filename);
        try {
            Path path = Paths.get(url.toURI());
            return path.toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test(expected = RuntimeException.class)
    public void testBadConfigParameter() {
        String filename = getFilePath("ConfigManager/bad-provider-name.conf");
        ConfigManager cm = ConfigManager.getInstance();
        cm.init(filename);
        fail("Did not throw exception");
    }

    @Test(expected = RuntimeException.class)
    public void testDeprecatedServiceNames() {
        String filename = getFilePath("ConfigManager/deprecated-service-names.conf");
        ConfigManager cm = ConfigManager.getInstance();
        cm.init(filename);
        fail("Did not throw exception");
    }


    @Ignore
    public void testMissingConfigParameter() {
        String filename = getFilePath("ConfigManager/missing-provider-name.conf");
        ConfigManager cm = ConfigManager.getInstance();
        cm.init(filename);
        // don't have access to provider
        //assertTrue(cm.getRetrieverHandler() instanceof MockRetrieverProvider);
    }

    @Test(expected = RuntimeException.class)
    public void testUseBeforeInit() {
        ConfigManager cm = ConfigManager.getInstance();
        cm.getResultsHandler();
        fail("Did not throw exception");
    }

}
