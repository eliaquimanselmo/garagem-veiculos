package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato do repositório de Reserva (D - Inversão de Dependência).
 * Além dos cinco métodos obrigatórios, expõe existeConflito, para que a
 * regra de sobreposição de períodos fique fora do Controller.
 */
public interface IReservaRepository {

    List<Reserva> obterTodas();

    Optional<Reserva> obterPorId(int id);

    void adicionar(Reserva reserva);

    void atualizar(Reserva reserva);

    void remover(int id);

    /**
     * Verifica se já existe reserva do mesmo veículo cujo período se sobrepõe
     * ao período informado. Ignora a reserva com id igual a idIgnorado
     * (use 0 ao criar uma reserva nova).
     */
    boolean existeConflito(int veiculoId, LocalDate inicio, LocalDate fim, int idIgnorado);
}
