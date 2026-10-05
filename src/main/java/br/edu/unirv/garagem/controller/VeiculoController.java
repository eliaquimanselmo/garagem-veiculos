package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.model.Veiculo;
import br.edu.unirv.garagem.repository.IVeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller de Veiculo.
 * <p>
 * Recebe a interface IVeiculoRepository pelo construtor (Injeção de
 * Dependência) e nunca acessa o JSON diretamente.
 */
@Controller
@RequestMapping("/veiculos")
public class VeiculoController {

    private final IVeiculoRepository veiculoRepository;

    public VeiculoController(IVeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    /** GET /veiculos -> lista todos os veículos. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("veiculos", veiculoRepository.obterTodos());
        return "veiculo/index";
    }

    /** GET /veiculos/novo -> formulário vazio. */
    @GetMapping("/novo")
    public String novoFormulario(Model model) {
        model.addAttribute("veiculo", new Veiculo());
        return "veiculo/VeiculoForm";
    }

    /** POST /veiculos/novo -> valida e cadastra. */
    @PostMapping("/novo")
    public String cadastrar(@Valid @ModelAttribute("veiculo") Veiculo veiculo, BindingResult result) {
        if (placaJaCadastrada(veiculo.getPlaca(), veiculo.getId())) {
            result.rejectValue("placa", "duplicada", "Esta placa já está cadastrada");
        }
        if (result.hasErrors()) {
            return "veiculo/VeiculoForm";
        }
        veiculoRepository.adicionar(veiculo);
        return "redirect:/veiculos";
    }

    /** GET /veiculos/{id}/editar -> formulário preenchido. */
    @GetMapping("/{id}/editar")
    public String editarFormulario(@PathVariable int id, Model model) {
        Optional<Veiculo> veiculo = veiculoRepository.obterPorId(id);
        if (veiculo.isEmpty()) {
            return "redirect:/veiculos";
        }
        model.addAttribute("veiculo", veiculo.get());
        return "veiculo/VeiculoForm";
    }

    /** POST /veiculos/{id}/editar -> valida e atualiza. */
    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable int id,
                            @Valid @ModelAttribute("veiculo") Veiculo veiculo,
                            BindingResult result) {
        veiculo.setId(id);
        if (placaJaCadastrada(veiculo.getPlaca(), id)) {
            result.rejectValue("placa", "duplicada", "Esta placa já está cadastrada para outro veículo");
        }
        if (result.hasErrors()) {
            return "veiculo/VeiculoForm";
        }
        veiculoRepository.atualizar(veiculo);
        return "redirect:/veiculos";
    }

    /** GET /veiculos/{id}/excluir -> tela de confirmação. */
    @GetMapping("/{id}/excluir")
    public String confirmarExclusao(@PathVariable int id, Model model) {
        Optional<Veiculo> veiculo = veiculoRepository.obterPorId(id);
        if (veiculo.isEmpty()) {
            return "redirect:/veiculos";
        }
        model.addAttribute("veiculo", veiculo.get());
        return "veiculo/ConfirmarExclusao";
    }

    /** POST /veiculos/{id}/excluir -> remove o veículo. */
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable int id) {
        veiculoRepository.remover(id);
        return "redirect:/veiculos";
    }

    /** Regra de negócio (placa única, sem diferenciar maiúsculas e hífen). */
    private boolean placaJaCadastrada(String placa, int idAtual) {
        if (placa == null) {
            return false;
        }
        String normalizada = normalizar(placa);
        return veiculoRepository.obterTodos().stream()
                .anyMatch(v -> v.getPlaca() != null
                        && normalizar(v.getPlaca()).equals(normalizada)
                        && v.getId() != idAtual);
    }

    private String normalizar(String placa) {
        return placa.replace("-", "").replace(" ", "").toUpperCase();
    }
}
