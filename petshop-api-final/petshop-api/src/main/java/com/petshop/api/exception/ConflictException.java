package com.petshop.api.exception;

/** HTTP 409 — conflito de unicidade (ex: CPF já cadastrado). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
