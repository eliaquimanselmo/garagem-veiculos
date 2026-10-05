package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Pessoa;

import java.util.List;
import java.util.Optional;

/**
 * Contrato do repositório de Pessoa.
 * <p>
 * O Controller conversa SOMENTE com esta interface (D - Inversão de
 * Dependência). Ele nunca sabe como (nem onde) os dados são salvos — hoje é
 * um arquivo JSON, amanhã pode virar um banco de dados, bastando trocar a
 * implementação (O - Aberto/Fechado).
 */
public interface IPessoaRepository {

    List<Pessoa> obterTodas();

    Optional<Pessoa> obterPorId(int id);

    void adicionar(Pessoa pessoa);

    void atualizar(Pessoa pessoa);

    void remover(int id);
}
