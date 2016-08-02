package edu.jhu.hlt.cadet.learn;

import java.util.List;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;

public interface SortReceiverCallback {
    public void addSort(UUID sessionId, List<AnnotationUnitIdentifier> unitIds);
}
