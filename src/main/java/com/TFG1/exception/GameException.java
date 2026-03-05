package com.TFG1.exception;

import com.TFG1.service.I18nService;

public class GameException extends RuntimeException {

    private final String errorKey;

    public GameException(String errorKey) {
        super(I18nService.get("ES", errorKey));
        this.errorKey = errorKey;
    }

    public GameException(String errorKey, Throwable cause) {
        super(I18nService.get("ES", errorKey), cause);
        this.errorKey = errorKey;
    }

    public String getErrorKey() {
        return errorKey;
    }
}
