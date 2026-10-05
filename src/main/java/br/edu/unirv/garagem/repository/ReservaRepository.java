package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Reserva;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Única classe do sistema que lê e grava o arquivo reservas.json.
 * As datas são gravadas no formato ISO (ex.: "2026-10-10").
 */
@Repository
public class ReservaRepository implements IReservaRepository {

    private static final String CAMINHO_ARQUIVO = "data/reservas.json";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules() // registra o suporte a LocalDate (jsr310)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public ReservaRepository() {
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
    public List<Reserva> obterTodas() {
        try {
            File arquivo = new File(CAMINHO_ARQUIVO);
            if (!arquivo.exists() || arquivo.length() == 0) {
                return new ArrayList<>();
            }
            CollectionType tipoLista = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Reserva.class);
            return objectMapper.readValue(arquivo, tipoLista);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler " + CAMINHO_ARQUIVO, e);
        }
    }

    private void salvarTodas(List<Reserva> reservas) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(CAMINHO_ARQUIVO), reservas);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gravar " + CAMINHO_ARQUIVO, e);
        }
    }

    @Override
    public Optional<Reserva> obterPorId(int id) {
        return obterTodas().stream()
                .filter(r -> r.getId() == id)
                .findFirst();
    }

    @Override
    public void adicionar(Reserva reserva) {
        List<Reserva> reservas = obterTodas();
        int novoId = reservas.stream()
                .mapToInt(Reserva::getId)
                .max()
                .orElse(0) + 1;
        reserva.setId(novoId);
        reservas.add(reserva);
        salvarTodas(reservas);
    }

    @Override
    public void atualizar(Reserva reserva) {
        List<Reserva> reservas = obterTodas();
        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i).getId() == reserva.getId()) {
                reservas.set(i, reserva);
                break;
            }
        }
        salvarTodas(reservas);
    }

    @Override
    public void remover(int id) {
        List<Reserva> reservas = obterTodas();
        reservas.removeIf(r -> r.getId() == id);
        salvarTodas(reservas);
    }

    /**
     * Dois períodos do mesmo veículo entram em conflito quando:
     * novaInicio <= existenteFim E novaFim >= existenteInicio.
     */
    @Override
    public boolean existeConflito(int veiculoId, LocalDate inicio, LocalDate fim, int idIgnorado) {
        return obterTodas().stream()
                .filter(r -> r.getVeiculoId() != null && r.getVeiculoId() == veiculoId)
                .filter(r -> r.getId() != idIgnorado)
                .anyMatch(r -> !inicio.isAfter(r.getDataFim()) && !fim.isBefore(r.getDataInicio()));
    }
}
