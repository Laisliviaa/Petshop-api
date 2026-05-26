package com.petshop.api.apikey;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints públicos para geração e gerenciamento de chaves de API.
 * <p>
 * Não requer autenticação (excluído do {@link ApiKeyFilter}).
 */
@Tag(name = "Autenticação",
     description = "Geração e gerenciamento de chaves de API (X-API-Key). "
             + "Estes endpoints são públicos — não exigem autenticação.")
@RestController
@RequestMapping("/api/v1/auth/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyRepository repository;

    @Operation(summary = "Gera uma nova chave de API",
               description = "Cria e retorna uma chave de API ativa para uso no cabeçalho X-API-Key. "
                       + "Guarde a chave com segurança — ela não pode ser recuperada depois.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chave gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ApiKeyResponse> gerar(@Valid @RequestBody ApiKeyRequest request) {
        ApiKey entity = new ApiKey();
        entity.setNomeCliente(request.getNomeCliente());
        entity.setChave("pk_" + UUID.randomUUID().toString().replace("-", ""));
        ApiKey salva = repository.save(entity);
        return ResponseEntity.created(URI.create("/api/v1/auth/keys/" + salva.getId()))
                .body(toResponse(salva));
    }

    @Operation(summary = "Lista todas as chaves de API ativas")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listar() {
        List<ApiKeyResponse> list = repository.findAll().stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Revoga (desativa) uma chave de API por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chave revogada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revogar(@PathVariable Long id) {
        return repository.findById(id).map(key -> {
            key.setAtiva(false);
            repository.save(key);
            return ResponseEntity.<Void>noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private ApiKeyResponse toResponse(ApiKey k) {
        return new ApiKeyResponse(k.getId(), k.getChave(), k.getNomeCliente(), k.getCriadaEm(), k.isAtiva());
    }
}
