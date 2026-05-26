package com.petshop.api.dto.response;

import com.petshop.api.model.StatusAgendamento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "Dados do agendamento retornados pela API")
public class AgendamentoResponse extends RepresentationModel<AgendamentoResponse> {

    @Schema(description = "ID do agendamento", example = "1")
    private Long id;

    @Schema(description = "Data e hora do agendamento", example = "2026-06-15T14:00:00")
    private LocalDateTime dataHora;

    @Schema(description = "Status atual", example = "PENDENTE")
    private StatusAgendamento status;

    @Schema(description = "Nome do pet", example = "Thor")
    private String nomePet;

    @Schema(description = "Nome do tutor", example = "Maria Oliveira")
    private String nomeCliente;

    @Schema(description = "Serviço contratado", example = "Banho e Tosa Completa")
    private String servico;

    @Schema(description = "Unidade de atendimento", example = "Unidade Centro")
    private String unidade;

    @Schema(description = "Observações", example = "Pet tem alergia a shampoo de coco")
    private String observacoes;
}
