package com.petshop.api.dto.request;

import com.petshop.api.model.StatusAgendamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Dados para criar ou atualizar um agendamento")
public class AgendamentoRequest {

    @Schema(description = "Data e hora do agendamento (formato: YYYY-MM-DDTHH:mm:ss)",
            example = "2026-06-15T14:00:00")
    @NotNull(message = "A data e hora são obrigatórias")
    private LocalDateTime dataHora;

    @Schema(description = "ID do pet a ser atendido", example = "1")
    @NotNull(message = "O ID do pet é obrigatório")
    private Long petId;

    @Schema(description = "ID do serviço contratado", example = "2")
    @NotNull(message = "O ID do serviço é obrigatório")
    private Long servicoId;

    @Schema(description = "ID da unidade onde será realizado o atendimento", example = "1")
    @NotNull(message = "O ID da unidade é obrigatório")
    private Long unidadeId;

    @Schema(description = "Observações adicionais", example = "Pet tem alergia a shampoo de coco")
    private String observacoes;

    @Schema(description = "Status do agendamento", example = "PENDENTE",
            allowableValues = {"PENDENTE","CONFIRMADO","EM_ANDAMENTO","CONCLUIDO","CANCELADO"})
    private StatusAgendamento status;
}
