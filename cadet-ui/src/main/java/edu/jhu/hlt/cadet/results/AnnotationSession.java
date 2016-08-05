package edu.jhu.hlt.cadet.results;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.Annotation;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;
import edu.jhu.hlt.concrete.uuid.UUIDFactory;

/**
 * Thread safe annotation session.
 *
 * Stores the list of items to be annotated and new annotations.
 */
public class AnnotationSession {
    private final UUID id;
    private final SearchResults searchResults;
    private List<AnnotationUnitIdentifier> orderedResults;
    private Set<AnnotationUnitIdentifier> processedResults;
    private Set<Annotation> annotations;
    private Object resultsLock = new Object();
    private Object annotationLock = new Object();

    public AnnotationSession(SearchResults results) {
        id = UUIDFactory.newUUID();
        searchResults = results;
        processedResults = new HashSet<AnnotationUnitIdentifier>();
        annotations = new HashSet<Annotation>();

        if (searchResults.getSearchResultsIterator().next().isSetSentenceId()) {
            orderedResults = searchResults.getSearchResults().stream()
                                .map(entry -> createAUI(entry.getCommunicationId(), entry.getSentenceId()))
                                .collect(Collectors.toList());
        } else {
            orderedResults = searchResults.getSearchResults().stream()
                                .map(entry -> new AnnotationUnitIdentifier(entry.getCommunicationId()))
                                .collect(Collectors.toList());
        }
    }

    /**
     * Get the ID of this annotation session
     * @return
     */
    public UUID getId() {
        return id;
    }

    /**
     * Update the order of the data to be provided by getNext()
     *
     * @param list  a sorted list of annotation unit identifiers
     * @return true if the update succeeded
     */
    public boolean updateSort(List<AnnotationUnitIdentifier> list) {
        if (list.size() == orderedResults.size()) {
            synchronized(resultsLock) {
                orderedResults = list;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the next chunk of annotation units
     *
     * @param chunkSize  the maximum number of items to return
     * @return list of annotation unit identifiers
     */
    public List<AnnotationUnitIdentifier> getNext(int chunkSize) {
        List<AnnotationUnitIdentifier> chunk = null;
        synchronized(resultsLock) {
            chunk =  orderedResults.stream()
                        .filter(entry -> !processedResults.contains(entry))
                        .limit(chunkSize)
                        .collect(Collectors.toList());
        }
        chunk.stream().forEach(entry -> processedResults.add(entry));
        return chunk;
    }

    /**
     * Add an annotation that has been completed
     *
     * @param unitId  annotation unit identifier
     * @param communication  communication object that holds the annotation
     */
    public void addAnnotation(AnnotationUnitIdentifier unitId, Communication communication) {
        synchronized(annotationLock) {
            annotations.add(new Annotation(unitId, communication));
        }
    }

    /**
     * Get the annotations that have been completed
     *
     * @param clear  optionally clear the annotations after returning
     * @return a set of annotations
     */
    public Set<Annotation> getAnnotations(boolean clear) {
        Set<Annotation> data = null;
        synchronized(annotationLock) {
            data = new HashSet<>(annotations);
            if (clear) {
                annotations.clear();
            }
        }
        return data;
    }

    protected static AnnotationUnitIdentifier createAUI(String commId, UUID sentId) {
        AnnotationUnitIdentifier aui = new AnnotationUnitIdentifier(commId);
        aui.setSentenceId(sentId);
        return aui;
    }
}
