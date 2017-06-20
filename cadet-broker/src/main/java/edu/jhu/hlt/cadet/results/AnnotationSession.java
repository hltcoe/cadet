package edu.jhu.hlt.cadet.results;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.Annotation;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;
import edu.jhu.hlt.concrete.uuid.UUIDFactory;

/**
 * Thread safe annotation session.
 *
 * Stores the list of items to be annotated and new annotations.
 */
public class AnnotationSession {
    private final UUID id;
    private final SearchResult searchResults;
    private List<AnnotationUnitIdentifier> orderedItems;
    private Set<AnnotationUnitIdentifier> availableItems;
    private Set<AnnotationUnitIdentifier> outForAnnotationItems;
    private Set<AnnotationUnitIdentifier> completedItems;
    private Set<Annotation> annotations;
    private Object resultsLock = new Object();
    private Object annotationLock = new Object();
    private long timeToLive;

    /**
     * Create an annotations session
     *
     * @param results  a search result with documents/sentences to be annotated
     * @param timeToLive  how long before incomplete items should be returned to the queue
     */
    public AnnotationSession(SearchResult results, long timeToLive) {
        id = UUIDFactory.newUUID();
        searchResults = results;
        this.timeToLive = timeToLive;

        availableItems = new HashSet<AnnotationUnitIdentifier>();
        outForAnnotationItems = new HashSet<AnnotationUnitIdentifier>();
        completedItems = new HashSet<AnnotationUnitIdentifier>();
        annotations = new HashSet<Annotation>();

        if (searchResults.getSearchResultItemsIterator().next().isSetSentenceId()) {
            orderedItems = searchResults.getSearchResultItems().stream()
                                .map(entry -> createAUI(entry.getCommunicationId(), entry.getSentenceId()))
                                .collect(Collectors.toList());
        } else {
            orderedItems = searchResults.getSearchResultItems().stream()
                                .map(entry -> new AnnotationUnitIdentifier(entry.getCommunicationId()))
                                .collect(Collectors.toList());
        }
        orderedItems.stream().forEach(item -> availableItems.add(item));
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
        if (list.size() == orderedItems.size()) {
            synchronized(resultsLock) {
                orderedItems = list;
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
            chunk =  orderedItems.stream()
                        .filter(entry -> availableItems.contains(entry))
                        .limit(chunkSize)
                        .collect(Collectors.toList());
        }

        chunk.stream().forEach(entry -> {
            outForAnnotationItems.add(entry);
            availableItems.remove(entry);
        });
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
            completedItems.add(unitId);
            outForAnnotationItems.remove(unitId);
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
