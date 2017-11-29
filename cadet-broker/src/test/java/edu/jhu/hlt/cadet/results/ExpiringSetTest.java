/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.results;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;

public class ExpiringSetTest {

    @Test
    public void test_expire() throws InterruptedException {
        AnnotationUnitIdentifier item1 = new AnnotationUnitIdentifier("test1");
        AnnotationUnitIdentifier item2 = new AnnotationUnitIdentifier("test2");
        ExpiringSet es = new ExpiringSet(30);
        es.add(item1);
        Thread.sleep(20);
        es.add(item2);
        Thread.sleep(20);

        Set<AnnotationUnitIdentifier> expiredItems = es.expire();

        assertEquals(1, expiredItems.size());
        assertEquals(item1, expiredItems.iterator().next());
        assertEquals(1, es.size());
    }

    @Test
    public void test_remove() throws InterruptedException {
        AnnotationUnitIdentifier item1 = new AnnotationUnitIdentifier("test1");
        AnnotationUnitIdentifier item2 = new AnnotationUnitIdentifier("test2");
        ExpiringSet es = new ExpiringSet(30);
        es.add(item1);
        es.add(item2);

        es.remove(item1);

        assertEquals(1, es.size());
        assertEquals(item2, es.iterator().next());
    }

}
