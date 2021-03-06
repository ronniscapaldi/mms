package org.openmbee.sdvc.core.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends SdvcException {

    public ForbiddenException(Object body) {
        super(HttpStatus.FORBIDDEN, body);
    }

}
