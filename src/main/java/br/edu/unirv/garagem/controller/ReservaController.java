package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.model.*;
import br.edu.unirv.garagem.repository.IPessoaRepository;
import br.edu.unirv.garagem.repository.IReservaRepository;
import br.edu.unirv.garagem.repository.IVeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller de Reserva (página inicial do sistema).
 * <p>
 * Recebe as três interfaces de repositório pelo construtor (Injeção de
 * Dependência). Nunca lê nem grava JSON: só chama os repositórios, aplica as
 * mensagens de erro e escolhe a View. A verificação de conflito de períodos
 * está em IReservaRepository.existeConflito.
 */
@Controller
public class ReservaController {

    private final IReservaRepository reservaRepository;
    private final IVeiculoRepository veiculoRepository;
    private final IPessoaRepository pessoaRepository;

    public ReservaController(IReservaRepository reservaRepository,
                             IVeiculoRepository veiculoRepository,
                             IPessoaRepository pessoaRepository) {
        this.reservaRepository = reservaRepository;
        this.veiculoRepository = veiculoRepository;
        this.pessoaRepository = pessoaRepository;
    }

    /** GET / (página inicial) e GET /reservas -> reservas + status dos veículos. */
    @GetMapping({"/", "/reservas"})
    public String listar(Model model) {
        List<Veiculo> veiculos = veiculoRepository.obterTodos();
        List<Pessoa> pessoas = pessoaRepository.obterTodas();
        List<Reserva> reservas = reservaRepository.obterTodas();
        LocalDate hoje = LocalDate.now();

        List<ReservaItem> itens = reservas.stream()
                .map(r -> new ReservaItem(
                        r.getId(),
                        veiculos.stream().filter(v -> v.getId() == r.getVeiculoId())
                                .map(Veiculo::getPlaca).findFirst().orElse("(veículo removido)"),
                        veiculos.stream().filter(v -> v.getId() == r.getVeiculoId())
                                .map(Veiculo::getModelo).findFirst().orElse("-"),
                        pessoas.stream().filter(p -> p.getId() == r.getPessoaId())
                                .map(Pessoa::getNome).findFirst().orElse("(pessoa removida)"),
                        r.getDataInicio(),
                        r.getDataFim()))
                .toList();

        List<VeiculoStatus> status = veiculos.stream()
                .map(v -> {
                    Optional<Reserva> ativa = reservas.stream()
                            .filter(r -> r.getVeiculoId() == v.getId() && r.ocupaEm(hoje))
                            .findFirst();
                    String quem = ativa.flatMap(r -> pessoas.stream()
                                    .filter(p -> p.getId() == r.getPessoaId())
                                    .map(Pessoa::getNome).findFirst())
                            .orElse(null);
                    return new VeiculoStatus(v, ativa.isPresent(), quem);
                })
                .toList();

        model.addAttribute("reservas", itens);
        model.addAttribute("statusVeiculos", status);
        model.addAttribute("hoje", hoje);
        return "reserva/index";
    }

    /** GET /reservas/novo -> formulário vazio. */
    @GetMapping("/reservas/novo")
    public String novoFormulario(Model model) {
        model.addAttribute("reserva", new Reserva());
        carregarListas(model);
        return "reserva/ReservaForm";
    }

    /** POST /reservas/novo -> valida, verifica conflito e cadastra. */
    @PostMapping("/reservas/novo")
    public String cadastrar(@Valid @ModelAttribute("reserva") Reserva reserva,
                            BindingResult result, Model model) {
        validarRegrasDeNegocio(reserva, 0, result);
        if (result.hasErrors()) {
            carregarListas(model);
            return "reserva/ReservaForm";
        }
        reservaRepository.adicionar(reserva);
        return "redirect:/";
    }

    /** GET /reservas/{id}/editar -> formulário preenchido. */
    @GetMapping("/reservas/{id}/editar")
    public String editarFormulario(@PathVariable int id, Model model) {
        Optional<Reserva> reserva = reservaRepository.obterPorId(id);
        if (reserva.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("reserva", reserva.get());
        carregarListas(model);
        return "reserva/ReservaForm";
    }

    /** POST /reservas/{id}/editar -> valida (ignorando a própria reserva) e atualiza. */
    @PostMapping("/reservas/{id}/editar")
    public String atualizar(@PathVariable int id,
                            @Valid @ModelAttribute("reserva") Reserva reserva,
                            BindingResult result, Model model) {
        reserva.setId(id);
        validarRegrasDeNegocio(reserva, id, result);
        if (result.hasErrors()) {
            carregarListas(model);
            return "reserva/ReservaForm";
        }
        reservaRepository.atualizar(reserva);
        return "redirect:/";
    }

    /** GET /reservas/{id}/excluir -> tela de confirmação do cancelamento. */
    @GetMapping("/reservas/{id}/excluir")
    public String confirmarExclusao(@PathVariable int id, Model model) {
        Optional<Reserva> reserva = reservaRepository.obterPorId(id);
        if (reserva.isEmpty()) {
            return "redirect:/";
        }
        Reserva r = reserva.get();
        model.addAttribute("reserva", r);
        model.addAttribute("placa", veiculoRepository.obterPorId(r.getVeiculoId())
                .map(Veiculo::getPlaca).orElse("(veículo removido)"));
        model.addAttribute("pessoaNome", pessoaRepository.obterPorId(r.getPessoaId())
                .map(Pessoa::getNome).orElse("(pessoa removida)"));
        return "reserva/ConfirmarExclusao";
    }

    /** POST /reservas/{id}/excluir -> cancela a reserva. */
    @PostMapping("/reservas/{id}/excluir")
    public String excluir(@PathVariable int id) {
        reservaRepository.remover(id);
        return "redirect:/";
    }

    /** Preenche os selects do formulário com dados vindos dos repositórios. */
    private void carregarListas(Model model) {
        model.addAttribute("veiculos", veiculoRepository.obterTodos());
        model.addAttribute("pessoas", pessoaRepository.obterTodas());
    }

    /**
     * Regras complementares: DataFim >= DataInicio, pessoa e veículo existentes
     * e ausência de conflito de período. Só adiciona mensagens de erro ao
     * BindingResult; a aplicação nunca quebra.
     */
    private void validarRegrasDeNegocio(Reserva reserva, int idIgnorado, BindingResult result) {
        if (reserva.getVeiculoId() != null && veiculoRepository.obterPorId(reserva.getVeiculoId()).isEmpty()) {
            result.rejectValue("veiculoId", "inexistente", "O veículo selecionado não existe");
        }
        if (reserva.getPessoaId() != null && pessoaRepository.obterPorId(reserva.getPessoaId()).isEmpty()) {
            result.rejectValue("pessoaId", "inexistente", "A pessoa selecionada não existe");
        }
        if (reserva.getDataInicio() == null || reserva.getDataFim() == null) {
            return;
        }
        if (reserva.getDataFim().isBefore(reserva.getDataInicio())) {
            result.rejectValue("dataFim", "periodoInvalido",
                    "A data de fim não pode ser anterior à data de início");
            return;
        }
        if (reserva.getVeiculoId() != null
                && reservaRepository.existeConflito(reserva.getVeiculoId(), reserva.getDataInicio(),
                reserva.getDataFim(), idIgnorado)) {
            result.reject("conflito",
                    "Reserva bloqueada: este veículo já está reservado em parte (ou todo) do período informado.");
        }
    }
}
