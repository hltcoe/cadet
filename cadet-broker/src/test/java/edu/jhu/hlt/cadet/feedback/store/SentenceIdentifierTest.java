/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.feedback.store;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.jhu.hlt.concrete.UUID;

public class SentenceIdentifierTest {

    @Test
    public void testToString() {
        List<String[]> testCases = new ArrayList<String[]>();
        testCases.add(new String[]{"[nytimes_49]:[123e4567-e89b]", "nytimes_49", "123e4567-e89b"});
        testCases.add(new String[]{"[nytimes\\:78]:[123e4567-e89b]", "nytimes:78", "123e4567-e89b"});
        testCases.add(new String[]{"[nytimes_66]\\:]:[123e4567-e89b]", "nytimes_66]:", "123e4567-e89b"});

        for (String[] testCase : testCases) {
            SentenceIdentifier si = new SentenceIdentifier(testCase[1], new UUID(testCase[2]));
            assertEquals(testCase[0], si.toString());
        }
    }

    @Test
    public void testValueOf() {
        List<String[]> testCases = new ArrayList<String[]>();
        testCases.add(new String[]{"[nytimes_49]:[123e4567-e89b]", "nytimes_49", "123e4567-e89b"});
        testCases.add(new String[]{"[nytimes\\:78]:[123e4567-e89b]", "nytimes:78", "123e4567-e89b"});
        testCases.add(new String[]{"[nytimes_66]\\:]:[123e4567-e89b]", "nytimes_66]:", "123e4567-e89b"});

        for (String[] testCase : testCases) {
            SentenceIdentifier si = SentenceIdentifier.valueOf(testCase[0]);
            assertEquals(testCase[1], si.getCommunicationId());
            assertEquals(testCase[2], si.getSentenceId().getUuidString());
        }
    }

    @Test
    public void testEquals() {
        SentenceIdentifier si1 = new SentenceIdentifier("twitter1234", new UUID("123-45-67890"));
        SentenceIdentifier si2 = new SentenceIdentifier("twitter1234", new UUID("123-45-67890"));
        SentenceIdentifier si3 = new SentenceIdentifier("twitter12345", new UUID("123-45-67890"));

        assertTrue(si1.equals(si2));
        assertFalse(si1.equals(si3));
    }

    @Test
    public void testHashCode() {
        SentenceIdentifier si1 = new SentenceIdentifier("twitter1234", new UUID("123-45-67890"));
        SentenceIdentifier si2 = new SentenceIdentifier("twitter1234", new UUID("123-45-67890"));
        SentenceIdentifier si3 = new SentenceIdentifier("twitter12345", new UUID("123-45-67890"));

        assertEquals(si1.hashCode(), si2.hashCode());
        assertNotEquals(si1.hashCode(), si3.hashCode());
    }
}
