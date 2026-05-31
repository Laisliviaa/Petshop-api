package com.example.petshopapi.dto.response;

import com.example.petshopapi.model.StatusAgendamento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Schema(description = "Dados de resposta de um agendamento")
public class AgendamentoResponse extends RepresentationModel<AgendamentoResponse> {
    @Schema(example = "1")              private Long              id;
    @Schema(example = "1")              private Long              petId;
    @Schema(example = "Rex")            private String            nomePet;
    @Schema(example = "1")              private Long              servicoId;
    @Schema(example = "Banho e Tosa")   private String            nomeServico;
    @Schema(example = "80.00")          private Double            precoServico;
    @Schema(example = "1")              private Long              unidadeId;
    @Schema(example = "Unidade Centro") private String            nomeUnidade;
    @Schema(example = "2026-06-10T09:00:00") private LocalDateTime dataHora;
    @Schema(example = "PENDENTE")       private StatusAgendamento status;
    @Schema(example = "Pet agitado")    private String            observacoes;
}
