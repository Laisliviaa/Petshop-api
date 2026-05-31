package com.example.petshopapi.controller;

import com.example.petshopapi.apikey.*;
import com.example.petshopapi.exception.ApiErrorResponse;
import com.example.petshopapi.exception.ConflictException;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/apikeys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = """
        Geração e gerenciamento das chaves de acesso à API.

        | Role | Permissões |
        |---|---|
        | `VISITANTE` | Apenas GET |
        | `FUNCIONARIO` | GET + POST + PUT |
        | `ADMIN` | Tudo + DELETE |

        **Chaves pré-carregadas no DataInitializer:**

        | Chave | Role |
        |---|---|
        | `petshop-admin-key-2026` | ADMIN |
        | `petshop-funcionario-key-2026` | FUNCIONARIO |
        | `petshop-visitante-key-2026` | VISITANTE |

        **Endpoint de geração (`POST /api/v1/apikeys`) é público** — não exige X-API-Key.

        **Regras para exclusão (`DELETE`):**
        - Não é possível excluir a própria chave em uso
        - Não é possível excluir a última chave ADMIN ativa
        """)
public class ApiKeyController {

    private final ApiKeyRepository apiKeyRepository;

    @Operation(summary = "Gera nova API Key",
               description = "**Público — não exige X-API-Key.** "
                           + "Após gerar, copie o campo `apiKey` e use-o no header `X-API-Key`.")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "API Key gerada com sucesso",
            headers = @Header(name = "Location", description = "URI da chave criada")),
        @ApiResponse(responseCode = "400",
            description = "Dados inválidos (clientName em branco, etc.)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiKeyResponse> gerar(@Valid @RequestBody ApiKeyRequest req) {
        ApiKey entity = new ApiKey();
        entity.setClientName(req.getClientName());
        if (req.getRole() != null) entity.setRole(req.getRole());
        ApiKey saved = apiKeyRepository.save(entity);
        return ResponseEntity.created(URI.create("/api/v1/apikeys/" + saved.getId()))
                .body(toResponse(saved));
    }

    @Operation(summary = "Lista todas as API Keys", description = "Público.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada"),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listar() {
        return ResponseEntity.ok(apiKeyRepository.findAll().stream().map(this::toResponse).toList());
    }

    @Operation(summary = "Busca API Key por ID", description = "Público.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chave encontrada"),
        @ApiResponse(responseCode = "404",
            description = "Chave não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiKeyResponse> buscar(
            @Parameter(description = "ID da API Key", example = "1", required = true)
            @PathVariable Long id) {
        return apiKeyRepository.findById(id)
                .map(k -> ResponseEntity.ok(toResponse(k)))
                .orElseThrow(() -> new RecursoNaoEncontradoException("ApiKey", id));
    }

    @Operation(summary = "Exclui API Key",
               description = """
                       Requer role **ADMIN**. Remove a chave permanentemente do banco.

                       **Regras de proteção:**
                       - Não é possível excluir a própria chave que está sendo usada na requisição
                       - Não é possível excluir a última chave ADMIN ativa (evita lock-out)
                       """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Chave excluída com sucesso"),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Apenas ADMIN pode excluir chaves",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Chave não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409",
            description = "Tentativa de excluir a própria chave ativa ou a última chave ADMIN",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "ID da API Key a excluir", example = "2", required = true)
            @PathVariable Long id) {

        ApiKey key = apiKeyRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("ApiKey", id));

        // Impede excluir a própria chave que está sendo usada na requisição
        if (key.getKeyValue().equals(apiKey)) {
            throw new ConflictException(
                    "Não é possível excluir a própria chave em uso. "
                    + "Autentique-se com outra chave ADMIN para excluir esta.");
        }

        // Impede excluir a última chave ADMIN ativa
        if (key.getRole() == ApiKey.Role.ADMIN) {
            long adminsAtivos = apiKeyRepository.findAll().stream()
                    .filter(k -> k.getRole() == ApiKey.Role.ADMIN && k.isActive())
                    .count();
            if (adminsAtivos <= 1) {
                throw new ConflictException(
                        "Não é possível excluir a última chave ADMIN ativa. "
                        + "Crie outra chave ADMIN antes de excluir esta.");
            }
        }

        apiKeyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ApiKeyResponse toResponse(ApiKey k) {
        return new ApiKeyResponse(k.getId(), k.getKeyValue(), k.getClientName(),
                k.getRole(), k.isActive(), k.getCreatedAt());
    }
}
