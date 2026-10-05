package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Pessoa;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Única classe do sistema que lê e grava o arquivo pessoas.json.
 * <p>
 * Responsabilidade única (S do SOLID): o Repository só lê e grava o JSON.
 * Nenhuma regra de negócio (validação, formatação, etc.) mora aqui — isso é
 * responsabilidade do Model e do Controller.
 */
@Repository
public class PessoaRepository implements IPessoaRepository {

    private static final String CAMINHO_ARQUIVO = "data/pessoas.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PessoaRepository() {
        garantirArquivoExiste();
    }

    /** Se o arquivo (ou a pasta data/) não existir, cria com uma lista vazia []. */
    private void garantirArquivoExiste() {
        try {
            File arquivo = new File(CAMINHO_ARQUIVO);
            File diretorio = arquivo.getParentFile();
            if (diretorio != null && !diretorio.exists()) {
                diretorio.mkdirs();
            }
            if (!arquivo.exists()) {
                Files.writeString(arquivo.toPath(), "[]");
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível preparar o arquivo " + CAMINHO_ARQUIVO, e);
        }
    }

    @Override
    public List<Pessoa> obterTodas() {
        try {
            File arquivo = new File(CAMINHO_ARQUIVO);
            if (!arquivo.exists() || arquivo.length() == 0) {
                return new ArrayList<>();
            }
            CollectionType tipoLista = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Pessoa.class);
            return objectMapper.readValue(arquivo, tipoLista);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler " + CAMINHO_ARQUIVO, e);
        }
    }

    private void salvarTodas(List<Pessoa> pessoas) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(CAMINHO_ARQUIVO), pessoas);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gravar " + CAMINHO_ARQUIVO, e);
        }
    }

    @Override
    public Optional<Pessoa> obterPorId(int id) {
        return obterTodas().stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    @Override
    public void adicionar(Pessoa pessoa) {
        List<Pessoa> pessoas = obterTodas();
        int novoId = pessoas.stream()
                .mapToInt(Pessoa::getId)
                .max()
                .orElse(0) + 1;
        pessoa.setId(novoId);
        pessoas.add(pessoa);
        salvarTodas(pessoas);
    }

    @Override
    public void atualizar(Pessoa pessoa) {
        List<Pessoa> pessoas = obterTodas();
        for (int i = 0; i < pessoas.size(); i++) {
            if (pessoas.get(i).getId() == pessoa.getId()) {
                pessoas.set(i, pessoa);
                break;
            }
        }
        salvarTodas(pessoas);
    }

    @Override
    public void remover(int id) {
        List<Pessoa> pessoas = obterTodas();
        pessoas.removeIf(p -> p.getId() == id);
        salvarTodas(pessoas);
    }
}
