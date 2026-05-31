package com.example.petshopapi.dto.request;

import com.example.petshopapi.model.StatusAgendamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
@Schema(description = "Dados para criar ou atualizar um agendamento")
public class AgendamentoRequest {
    @Schema(example = "1") @NotNull(message = "O ID do pet é obrigatório") private Long petId;
    @Schema(example = "1") @NotNull(message = "O ID do serviço é obrigatório") private Long servicoId;
    @Schema(example = "1") @NotNull(message = "O ID da unidade é obrigatório") private Long unidadeId;
    @Schema(example = "2026-06-10T09:00:00") @NotNull(message = "A data e hora são obrigatórias") private LocalDateTime dataHora;
    @Schema(example = "PENDENTE") private StatusAgendamento status;
    @Schema(example = "Pet agitado, cuidado ao tosiar") private String observacoes;
}
