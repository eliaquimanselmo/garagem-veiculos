package br.edu.unirv.garagem.model;

/**
 * Situação de um veículo na data de hoje (Disponível ou Reservado).
 * Calculada a partir das reservas; não é gravada em JSON.
 */
public record VeiculoStatus(Veiculo veiculo, boolean reservado, String reservadoPara) {
}
