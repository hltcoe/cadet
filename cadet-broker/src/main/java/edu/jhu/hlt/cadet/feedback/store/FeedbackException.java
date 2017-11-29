/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.feedback.store;

public class FeedbackException extends Exception {
    private static final long serialVersionUID = 2170893254453411181L;

    public FeedbackException(String message) {
        super(message);
    }

    public FeedbackException(Throwable cause) {
        super(cause);
    }

    public FeedbackException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeedbackException(String message, Throwable cause, boolean enableSuppression,
                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
