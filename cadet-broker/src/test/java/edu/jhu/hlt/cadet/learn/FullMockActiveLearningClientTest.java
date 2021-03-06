/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.learn;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.AnnotationTask;
import edu.jhu.hlt.concrete.services.AnnotationTaskType;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;
import edu.jhu.hlt.concrete.services.AnnotationUnitType;

public class FullMockActiveLearningClientTest {

    /**
     * Integration test that requires a server running to accept the sorts
     */
    @Ignore
    @Test
    public void test() {
        Config config = ConfigFactory.empty();
        config = config.withValue(CadetConfig.SORT_HOST, ConfigValueFactory.fromAnyRef("localhost"));
        config = config.withValue(CadetConfig.SORT_PORT, ConfigValueFactory.fromAnyRef(9898));
        config = config.withValue(CadetConfig.SORT_PERIOD, ConfigValueFactory.fromAnyRef(5));
        ActiveLearningClient client = new FullMockActiveLearningClient();
        client.init(config);

        List<AnnotationUnitIdentifier> list = new ArrayList<AnnotationUnitIdentifier>();
        for (int i=1; i<=10; i++) {
            list.add(new AnnotationUnitIdentifier(String.valueOf(i)));
        }
        AnnotationTask task = new AnnotationTask(AnnotationTaskType.NER, AnnotationUnitType.COMMUNICATION, list);
        task.setLanguage("eng");
        client.start(new UUID("test"), task);

        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        client.stop(new UUID("test"));
        client.close();

        try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
