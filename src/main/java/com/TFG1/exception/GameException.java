package com.TFG1.exception;

public class GameException extends RuntimeException {

    private final String errorKey; // Aqui se guarda todo lo que este en lang de errores (mas o menos)xd

    public GameException(String errorKey) {
        super(errorKey);
        this.errorKey = errorKey;
    }

    public String getErrorKey() {
        return errorKey;
    }

    // Esta clase se tiene que referir a la que este en javalin para manejar las
    // excepciones
}
