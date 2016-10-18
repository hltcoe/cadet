package edu.jhu.hlt.cadet.feedback.store.sql;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResultItem;

@Entity
public class Feedback implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(updatable = false, nullable = false, length=128)
    private String commId;
    @Column(updatable = false, nullable = true, length=64)
    private String sentId;
    @Column(updatable = true, nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private SearchFeedback value;

    public Feedback() {}

    public Feedback(SearchResultItem result) {
        setCommId(result.getCommunicationId());
        if (result.isSetSentenceId()) {
            setSentId(result.getSentenceId().getUuidString());
        }
        setValue(SearchFeedback.NONE);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCommId() {
        return commId;
    }

    public void setCommId(String commId) {
        this.commId = commId;
    }

    public String getSentId() {
        return sentId;
    }

    public void setSentId(String sentId) {
        this.sentId = sentId;
    }

    public SearchFeedback getValue() {
        return value;
    }

    public void setValue(SearchFeedback value) {
        this.value = value;
    }
}
