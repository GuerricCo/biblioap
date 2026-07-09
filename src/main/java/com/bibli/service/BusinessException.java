package com.bibli.service;

import java.io.Serial;

/**
 * A business-rule violation raised from the service layer (e.g. no copies available, an action is
 * no longer possible given an entity's current state). Kept free of any web-layer dependency so it
 * doesn't violate the technical architecture layering; {@link com.bibli.web.rest.errors.ExceptionTranslator}
 * translates it into the same 400 response shape as {@link com.bibli.web.rest.errors.BadRequestAlertException}.
 */
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String entityName;

    private final String errorKey;

    public BusinessException(String defaultMessage, String entityName, String errorKey) {
        super(defaultMessage);
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getErrorKey() {
        return errorKey;
    }
}
