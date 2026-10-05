package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Veiculo;
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
 * Única classe do sistema que lê e grava o arquivo veiculos.json.
 * <p>
 * Responsabilidade única (S do SOLID): só lê e grava o JSON, sem regra de negócio.
 */
@Repository
public class VeiculoRepository implements IVeiculoRepository {

    private static final String CAMINHO_ARQUIVO = "data/veiculos.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public VeiculoRepository() {
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
    public List<Veiculo> obterTodos() {
        try {
            File arquivo = new File(CAMINHO_ARQUIVO);
            if (!arquivo.exists() || arquivo.length() == 0) {
                return new ArrayList<>();
            }
            CollectionType tipoLista = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Veiculo.class);
            return objectMapper.readValue(arquivo, tipoLista);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler " + CAMINHO_ARQUIVO, e);
        }
    }

    private void salvarTodos(List<Veiculo> veiculos) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(CAMINHO_ARQUIVO), veiculos);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gravar " + CAMINHO_ARQUIVO, e);
        }
    }

    @Override
    public Optional<Veiculo> obterPorId(int id) {
        return obterTodos().stream()
                .filter(v -> v.getId() == id)
                .findFirst();
    }

    @Override
    public void adicionar(Veiculo veiculo) {
        List<Veiculo> veiculos = obterTodos();
        int novoId = veiculos.stream()
                .mapToInt(Veiculo::getId)
                .max()
                .orElse(0) + 1;
        veiculo.setId(novoId);
        veiculos.add(veiculo);
        salvarTodos(veiculos);
    }

    @Override
    public void atualizar(Veiculo veiculo) {
        List<Veiculo> veiculos = obterTodos();
        for (int i = 0; i < veiculos.size(); i++) {
            if (veiculos.get(i).getId() == veiculo.getId()) {
                veiculos.set(i, veiculo);
                break;
            }
        }
        salvarTodos(veiculos);
    }

    @Override
    public void remover(int id) {
        List<Veiculo> veiculos = obterTodos();
        veiculos.removeIf(v -> v.getId() == id);
        salvarTodos(veiculos);
    }
}
