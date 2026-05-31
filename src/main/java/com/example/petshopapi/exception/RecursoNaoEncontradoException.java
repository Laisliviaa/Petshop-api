package com.example.petshopapi.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(Long id) { super("Recurso com ID " + id + " não encontrado."); }
    public RecursoNaoEncontradoException(String recurso, Long id) { super(recurso + " com ID " + id + " não encontrado."); }
    public RecursoNaoEncontradoException(String mensagem) { super(mensagem); }
}
