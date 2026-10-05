package br.edu.unirv.garagem.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Model da entidade Veiculo.
 * <p>
 * Responsabilidade única (S do SOLID): guarda os dados do veículo e as regras
 * de validação de cada campo. Não sabe ler/gravar arquivo nem conhece a View.
 */
public class Veiculo {

    /** Gerado pelo repositório (maior Id + 1). */
    private int id;

    @NotBlank(message = "A placa é obrigatória")
    private String placa;

    @NotBlank(message = "A marca é obrigatória")
    private String marca;

    @NotBlank(message = "O modelo é obrigatório")
    private String modelo;

    @NotNull(message = "O ano é obrigatório")
    @Min(value = 1900, message = "Informe um ano a partir de 1900")
    @Max(value = 2100, message = "Informe um ano até 2100")
    private Integer ano;

    /** Cor é opcional, conforme especificação. */
    private String cor;

    public Veiculo() {
    }

    public Veiculo(int id, String placa, String marca, String modelo, Integer ano, String cor) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
}
