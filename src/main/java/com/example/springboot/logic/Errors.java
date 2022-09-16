package com.example.springboot.logic;

public enum Errors {
    E01("invalid move"),
    E02("missing piece"),
    E03("destination of piece already has one"),
    E04("wrong colour");

    private final String message;

    Errors(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
