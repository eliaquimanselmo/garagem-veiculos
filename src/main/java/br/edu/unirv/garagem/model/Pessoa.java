package br.edu.unirv.garagem.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Model da entidade Pessoa.
 * <p>
 * Responsabilidade única (S do SOLID): esta classe guarda os dados da pessoa
 * e as regras de validação de cada campo (via Bean Validation). Ela NÃO sabe
 * ler nem gravar arquivo, e NÃO conhece o Controller nem a View.
 */
public class Pessoa {

    /** Gerado pelo repositório (maior Id + 1). Não é validado pelo usuário. */
    private int id;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail em um formato válido")
    private String email;

    /** Telefone é opcional, conforme especificação. */
    private String telefone;

    public Pessoa() {
    }

    public Pessoa(int id, String nome, String cpf, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
