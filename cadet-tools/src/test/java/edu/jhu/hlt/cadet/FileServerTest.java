/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Test;

import edu.jhu.hlt.concrete.services.NotImplementedException;

public class FileServerTest {

    @Test
    public void test() throws NotImplementedException, TException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/123.concrete").getFile());
        FileServer.Handler handler = new FileServer(0, "").new Handler(file.getParent());

        assertEquals(3, handler.getCommunicationCount());

        List<String> ids = handler.getCommunicationIDs(0, 10);
        assertEquals(3, ids.size());
        Collections.sort(ids);
        assertEquals("123", ids.get(0));
    }

}
