package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.model.Pessoa;
import br.edu.unirv.garagem.repository.IPessoaRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller de Pessoa.
 * <p>
 * Recebe a interface IPessoaRepository pelo construtor (D - Inversão de
 * Dependência / Injeção de Dependência). O Controller só recebe a
 * requisição, chama o repositório através da interface e escolhe a View —
 * ele NUNCA lê ou grava o arquivo JSON diretamente.
 */
@Controller
@RequestMapping("/pessoas")
public class PessoaController {

    private final IPessoaRepository pessoaRepository;

    public PessoaController(IPessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    /** GET /pessoas -> lista todas as pessoas cadastradas. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pessoas", pessoaRepository.obterTodas());
        return "pessoa/index";
    }

    /** GET /pessoas/novo -> exibe o formulário vazio. */
    @GetMapping("/novo")
    public String novoFormulario(Model model) {
        model.addAttribute("pessoa", new Pessoa());
        return "pessoa/PessoaForm";
    }

    /** POST /pessoas/novo -> valida e cadastra uma nova pessoa. */
    @PostMapping("/novo")
    public String cadastrar(@Valid @ModelAttribute("pessoa") Pessoa pessoa, BindingResult result) {
        if (cpfJaCadastrado(pessoa.getCpf(), pessoa.getId())) {
            result.rejectValue("cpf", "duplicado", "Este CPF já está cadastrado");
        }
        if (result.hasErrors()) {
            return "pessoa/PessoaForm";
        }
        pessoaRepository.adicionar(pessoa);
        return "redirect:/pessoas";
    }

    /** GET /pessoas/{id}/editar -> exibe o formulário preenchido. */
    @GetMapping("/{id}/editar")
    public String editarFormulario(@PathVariable int id, Model model) {
        Optional<Pessoa> pessoa = pessoaRepository.obterPorId(id);
        if (pessoa.isEmpty()) {
            return "redirect:/pessoas";
        }
        model.addAttribute("pessoa", pessoa.get());
        return "pessoa/PessoaForm";
    }

    /** POST /pessoas/{id}/editar -> valida e atualiza os dados da pessoa. */
    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable int id,
                             @Valid @ModelAttribute("pessoa") Pessoa pessoa,
                             BindingResult result) {
        pessoa.setId(id);
        if (cpfJaCadastrado(pessoa.getCpf(), id)) {
            result.rejectValue("cpf", "duplicado", "Este CPF já está cadastrado para outra pessoa");
        }
        if (result.hasErrors()) {
            return "pessoa/PessoaForm";
        }
        pessoaRepository.atualizar(pessoa);
        return "redirect:/pessoas";
    }

    /** GET /pessoas/{id}/excluir -> tela de confirmação antes de excluir. */
    @GetMapping("/{id}/excluir")
    public String confirmarExclusao(@PathVariable int id, Model model) {
        Optional<Pessoa> pessoa = pessoaRepository.obterPorId(id);
        if (pessoa.isEmpty()) {
            return "redirect:/pessoas";
        }
        model.addAttribute("pessoa", pessoa.get());
        return "pessoa/ConfirmarExclusao";
    }

    /** POST /pessoas/{id}/excluir -> efetivamente remove a pessoa. */
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable int id) {
        pessoaRepository.remover(id);
        return "redirect:/pessoas";
    }

    /**
     * Regra de negócio (CPF único). Fica no Controller e não no Repository,
     * pois é regra de aplicação, não responsabilidade de persistência.
     */
    private boolean cpfJaCadastrado(String cpf, int idAtual) {
        return pessoaRepository.obterTodas().stream()
                .anyMatch(p -> p.getCpf() != null
                        && p.getCpf().equalsIgnoreCase(cpf)
                        && p.getId() != idAtual);
    }
}
