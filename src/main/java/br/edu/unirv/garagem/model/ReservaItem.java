package br.edu.unirv.garagem.model;

import java.time.LocalDate;

/**
 * Dados já "montados" para a listagem de reservas (veículo, pessoa e período).
 * Serve apenas para a View exibir; não é gravado em JSON.
 */
public record ReservaItem(int id, String placa, String modelo, String pessoaNome,
                          LocalDate dataInicio, LocalDate dataFim) {
}
