package edu.jhu.hlt.cadet.feedback.store;

import java.util.regex.Pattern;

import edu.jhu.hlt.concrete.UUID;

/**
 * Sentences are identified by communication ID and sentence ID
 */
public class SentenceIdentifier {
    private final String communicationId;
    private final UUID sentenceId;
    private volatile int hashCode;
    
    public SentenceIdentifier(String commId, UUID sentId) {
        if (commId == null || commId.isEmpty()) {
            throw new IllegalArgumentException("commId is not a valid string");
        }
        if (sentId == null || sentId.getUuidString().isEmpty()) {
            throw new IllegalArgumentException("sentId is not a valid UUID");
        }
        communicationId = commId;
        sentenceId = sentId;
    }

    public String getCommunicationId() {
        return communicationId;
    }

    public UUID getSentenceId() {
        return sentenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SentenceIdentifier)) {
            return false;
        }
        SentenceIdentifier si = (SentenceIdentifier) o;
        return si.getCommunicationId().equals(communicationId) &&
                        si.getSentenceId().equals(sentenceId);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = 17;
            result = 31 * result + communicationId.hashCode();
            result = 31 * result + sentenceId.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        // returns [commmId]:[sentId] and escapes ":" in case "]:[" is in an ID
        return "[" + escape(communicationId)  + "]:[" + escape(sentenceId.getUuidString()) + "]";
    }

    public static SentenceIdentifier valueOf(String s) {
        String[] parts = s.split(Pattern.quote("]:["));
        if (parts.length != 2) {
            throw new IllegalArgumentException("String must have be of format [id]:[id] but is " + s);
        }
        parts[0] = parts[0].substring(1);
        parts[1] = parts[1].substring(0, parts[1].length() - 1);

        return new SentenceIdentifier(unescape(parts[0]), 
                        new UUID(unescape(parts[1])));
    }

    private static String escape(String s) {
        return s.replaceAll(":", "\\\\:");
    }

    private static String unescape(String s) {
        return s.replaceAll("\\\\:", ":");
    }
}
