/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.feedback.store.sql;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;

import edu.jhu.hlt.cadet.feedback.store.FeedbackException;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;
import edu.jhu.hlt.concrete.search.SearchType;

@Entity
public class FeedbackRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(updatable = false, nullable = true, length=128)
    private String uuid;
    @Column(updatable = false, nullable = true, length=32)
    private String userId;
    @Column(updatable = false, nullable = true, length=128)
    private String queryName;
    @Column(updatable = false, nullable = false)
    private Date timestamp;
    @Lob
    @Column(updatable = false, nullable = false)
    private byte[] searchResultsBlob;
    @ElementCollection
    private Set<String> labels;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Feedback> feedback;
    @Enumerated(EnumType.ORDINAL)
    @Column(updatable = false, nullable = false)
    private SearchType searchType;

    public static FeedbackRecord create(SearchResult results) {
        SearchQuery query = results.getSearchQuery();
        FeedbackRecord record = new FeedbackRecord();
        record.setUuid(results.getUuid().getUuidString());
        record.setTimestamp(new Date());
        record.setFeedback(results.getSearchResultItems());
        record.setSearchType(query.getType());
        if (query.isSetUserId()) {
            record.setUserId(query.getUserId());
        }
        if (query.isSetName()) {
            record.setQueryName(query.getName());
        }
        if (query.isSetLabels()) {
            record.setLabels(new HashSet<String>(query.getLabels()));
        }

        // TSerializer is not thread safe so construct each time
        TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());
        try {
            record.setSearchResultsBlob(serializer.serialize(results));
        } catch (TException e) {
            // can't imagine this happening so rethrow as unchecked
            throw new RuntimeException("Failed to serialize search results", e);
        }

        return record;
    }

    public FeedbackRecord() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getSearchResultsBlob() {
        return searchResultsBlob;
    }

    public void setSearchResultsBlob(byte[] blob) {
        searchResultsBlob = blob;
    }

    public SearchResult getSearchResults() throws FeedbackException {
        // TDeserializer is not thread safe so construct each time
        TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
        SearchResult results = new SearchResult();
        try {
            deserializer.deserialize(results, searchResultsBlob);
        } catch (TException e) {
            // this likely means an old feedback object is being deserialized
            throw new FeedbackException("Unable to deserialize search results", e);
        }
        return results;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public Set<Feedback> getFeedback() {
        return feedback;
    }

    public void setFeedback(Set<Feedback> feedback) {
        this.feedback = feedback;
    }

    public void setFeedback(List<SearchResultItem> results) {
        feedback = new HashSet<Feedback>();
        for (SearchResultItem result : results) {
            feedback.add(new Feedback(result));
        }
    }

    public Feedback getFeedbackItem(String commId, String sentId) {
        for (Feedback item : feedback) {
            if (item.getCommId().equals(commId)) {
                if (sentId == null) {
                    return item;
                } else {
                    if (item.getSentId().equals(sentId)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }
}
