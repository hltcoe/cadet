/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.store;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.serialization.CompactCommunicationSerializer;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.util.ConcreteException;

/**
 * Assumes communications are stored as <comm id>.concrete
 * The concrete communications must be serialized with compact protocol.
 * Requires that the config parameter be set: files.data.dir
 * This overwrites the communication files so must have write access.
 */
public class FileStoreProvider implements StoreProvider {
    private static Logger logger = LoggerFactory.getLogger(FileStoreProvider.class);
    public static final String EXTENSION = "concrete";

    private String directory;
    private final CompactCommunicationSerializer serializer;

    public FileStoreProvider() {
        serializer = new CompactCommunicationSerializer();
    }

    @Override
    public void init(Config config) {
        if (!config.hasPath(CadetConfig.FILES_DATA_DIR)) {
            throw new RuntimeException("Directory is not set in config: " + CadetConfig.FILES_DATA_DIR);
        }
        directory = config.getString(CadetConfig.FILES_DATA_DIR);
        if (directory.charAt(directory.length() - 1) != File.separatorChar) {
            directory += File.separator;
        }

        File file = new File(directory);
        if (!file.exists()) {
            throw new RuntimeException("Directory " + directory + " does not exist");
        }
        if (!file.isDirectory()) {
            throw new RuntimeException(directory + " is not a directory");
        }
    }

    @Override
    public void close() {}

    @Override
    public void store(Communication comm) throws TException {
        logger.info("Storing Comm Id: " + comm.getId());

        String filename = directory + comm.getId() + "." + EXTENSION;
        if (!new File(filename).exists()) {
            // we're only suppose to be overwriting files so this shouldn't happen
            logger.warn(filename + " does not exist so won't save");
            return;
        }

        byte[] data = null;
        try {
            data = serializer.toBytes(comm);
        } catch (ConcreteException e) {
            logger.warn("Unable to serialize comm " + comm.getId(), e);
            return;
        }

        try(OutputStream os = Files.newOutputStream(Paths.get(filename), StandardOpenOption.TRUNCATE_EXISTING);
                        BufferedOutputStream bos = new BufferedOutputStream(os);) {
            bos.write(data);
        } catch (IOException e) {
            logger.warn("Failed to write " + comm.getId(), e);
            return;
        }
    }

    @Override
    public boolean alive() throws TException {
        return true;
    }

    @Override
    public ServiceInfo about() throws TException {
        return new ServiceInfo(this.getClass().getSimpleName(), "1.0.0");
    }

}
