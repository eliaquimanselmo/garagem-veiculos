package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Veiculo;

import java.util.List;
import java.util.Optional;

/**
 * Contrato do repositório de Veiculo.
 * <p>
 * O Controller conversa SOMENTE com esta interface (D - Inversão de
 * Dependência). Interface exclusiva da entidade (I - Segregação de Interfaces).
 */
public interface IVeiculoRepository {

    List<Veiculo> obterTodos();

    Optional<Veiculo> obterPorId(int id);

    void adicionar(Veiculo veiculo);

    void atualizar(Veiculo veiculo);

    void remover(int id);
}
