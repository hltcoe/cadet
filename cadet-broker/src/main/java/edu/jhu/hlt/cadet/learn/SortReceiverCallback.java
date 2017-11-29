/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.learn;

import java.util.List;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;

public interface SortReceiverCallback {
    public void addSort(UUID sessionId, List<AnnotationUnitIdentifier> unitIds);
}
