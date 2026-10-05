package br.edu.unirv.garagem.model;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Model da entidade Reserva: vincula um veículo a uma pessoa por um período.
 * <p>
 * Responsabilidade única (S do SOLID): guarda os dados e as validações simples
 * de cada campo. A regra de conflito entre reservas fica fora do Model
 * (IReservaRepository.existeConflito).
 */
public class Reserva {

    /** Gerado pelo repositório (maior Id + 1). */
    private int id;

    @NotNull(message = "Selecione um veículo")
    private Integer veiculoId;

    @NotNull(message = "Selecione uma pessoa")
    private Integer pessoaId;

    @NotNull(message = "Informe a data de início")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicio;

    @NotNull(message = "Informe a data de fim")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFim;

    public Reserva() {
    }

    public Reserva(int id, Integer veiculoId, Integer pessoaId, LocalDate dataInicio, LocalDate dataFim) {
        this.id = id;
        this.veiculoId = veiculoId;
        this.pessoaId = pessoaId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    /** Indica se a data informada está dentro do período da reserva (inclusive). */
    public boolean ocupaEm(LocalDate data) {
        return dataInicio != null && dataFim != null
                && !data.isBefore(dataInicio) && !data.isAfter(dataFim);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getVeiculoId() { return veiculoId; }
    public void setVeiculoId(Integer veiculoId) { this.veiculoId = veiculoId; }

    public Integer getPessoaId() { return pessoaId; }
    public void setPessoaId(Integer pessoaId) { this.pessoaId = pessoaId; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
}
