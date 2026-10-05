# Garagem de Veículos — Entregas 1 e 2

**Grupo:** Eliaquim Anselmo Dias (1 integrante)

**Disciplina:** Estrutura de Dados / Aula 3 — MVC + padrão Repository + Injeção de Dependência

## 1. O que está pronto

| Entrega | Módulo | Status |
|---------|--------|--------|
| 1 | **Pessoas** — listar, cadastrar, editar, excluir (`data/pessoas.json`) | ✅ |
| 2 | **Veículos** — CRUD completo (`data/veiculos.json`) | ✅ |
| 2 | **Reservas** (página inicial) — listar, criar, editar, cancelar, status Disponível/Reservado hoje e bloqueio de conflito de período (`data/reservas.json`) | ✅ |

Todos os dados continuam salvos depois de fechar e abrir a aplicação.

## 2. Tecnologias e ferramentas de IA

- Java 17 · Spring Boot 3.2 (Spring Web + Thymeleaf + Bean Validation) · Maven
- Persistência em arquivos `.json` (Jackson `ObjectMapper`), sem banco de dados
- **IA utilizada:** Claude (Anthropic), para auxiliar na geração do código a partir das regras do professor. A arquitetura é de responsabilidade do integrante, que pode explicar qualquer parte do projeto (por exemplo, por que o Controller recebe uma interface pelo construtor).

## 3. Como executar

Pré-requisitos: JDK 17+ e Maven (ou o *Extension Pack for Java* do VS Code).

```bash
# a partir da pasta garagem-veiculos/
mvn spring-boot:run
```

Abra <http://localhost:8080> — a página inicial já é **Reservas**.

Para gerar o `.jar`:

```bash
mvn clean package
java -jar target/garagem-veiculos-0.0.1-SNAPSHOT.jar   # rode de dentro de garagem-veiculos/
```

> A pasta `data/` fica na raiz do projeto (fora do `.jar`); por isso o `.jar` deve ser executado a partir da pasta `garagem-veiculos/`. Se algum `.json` não existir, ele é criado com `[]`.

## 4. Estrutura do projeto

```
garagem-veiculos/
├── src/main/java/br/edu/unirv/garagem/
│   ├── GaragemApplication.java
│   ├── model/        Pessoa, Veiculo, Reserva (+ ReservaItem, VeiculoStatus: dados só para exibição)
│   ├── repository/   IPessoaRepository, PessoaRepository,
│   │                 IVeiculoRepository, VeiculoRepository,
│   │                 IReservaRepository, ReservaRepository
│   └── controller/   PessoaController, VeiculoController, ReservaController
├── src/main/resources/templates/
│   ├── pessoa/   index.html, PessoaForm.html, ConfirmarExclusao.html
│   ├── veiculo/  index.html, VeiculoForm.html, ConfirmarExclusao.html
│   └── reserva/  index.html, ReservaForm.html, ConfirmarExclusao.html
└── data/         pessoas.json, veiculos.json, reservas.json
```

## 5. Arquitetura (MVC + Repository + DI)

```
View  -->  Controller  -->  IRepository (interface)  <--  Repository  -->  arquivo .json
                ^                                              ^
                |_______ Container de DI do Spring ____________|
                  (@Repository + injeção pelo construtor)
```

O caminho dos dados é sempre **View → Controller → Interface → Repositório → JSON**. Os Controllers recebem **somente interfaces** pelo construtor; nenhum Controller lê/grava JSON, cria `new XRepository()` ou mantém lista `static`. Nenhuma View tem regra de negócio.

### Rotas

| Módulo | Operação | Método | Rota |
|--------|----------|--------|------|
| Pessoas | Listar / Cadastrar / Editar / Excluir | GET, GET+POST, GET+POST, GET+POST | `/pessoas`, `/pessoas/novo`, `/pessoas/{id}/editar`, `/pessoas/{id}/excluir` |
| Veículos | Listar / Cadastrar / Editar / Excluir | idem | `/veiculos`, `/veiculos/novo`, `/veiculos/{id}/editar`, `/veiculos/{id}/excluir` |
| Reservas | Listar (página inicial) | GET | `/` e `/reservas` |
| Reservas | Criar / Editar / Cancelar | GET+POST | `/reservas/novo`, `/reservas/{id}/editar`, `/reservas/{id}/excluir` |

## 6. Regra de negócio: um veículo, uma pessoa por período

Duas reservas do **mesmo veículo** entram em conflito quando:

```
novaInicio <= existenteFim  E  novaFim >= existenteInicio
```

Essa verificação está em `IReservaRepository.existeConflito(veiculoId, inicio, fim, idIgnorado)` — o Controller só chama e exibe a mensagem. Regras complementares tratadas no `ReservaController`:

- `DataFim` não pode ser anterior a `DataInicio`;
- pessoa e veículo precisam existir nos seus repositórios;
- ao editar, a reserva ignora a si mesma (`idIgnorado`);
- ao bloquear, a tela mostra uma mensagem de erro e a aplicação não quebra.

O status **Disponível / Reservado** é calculado a partir das reservas na data de hoje (`Reserva.ocupaEm(hoje)`).

## 7. SOLID aplicado

- **S — Responsabilidade Única:** o Model guarda dados e validações; o Repository só lê e grava JSON; o Controller só recebe a requisição, chama o repositório e escolhe a View.
- **O — Aberto/Fechado:** para trocar o JSON por banco de dados (EF Core/JPA), basta criar uma nova classe que implemente `IPessoaRepository`/`IVeiculoRepository`/`IReservaRepository`; Controllers e Views não mudam.
- **L — Substituição de Liskov:** qualquer implementação das interfaces (JSON, memória ou banco) funciona nos Controllers sem ajustes.
- **I — Segregação de Interfaces:** uma interface por entidade (`IPessoaRepository`, `IVeiculoRepository`, `IReservaRepository`), cada uma só com os métodos que usa.
- **D — Inversão de Dependência:** os Controllers dependem das **interfaces**, recebidas pelo construtor; quem entrega a implementação concreta é o container de DI do Spring.

## 8. Prints

Ficam na pasta `docs/`:

- Entrega 1: `docs/pessoas-listagem.png`, `docs/pessoas-formulario.png`
- Entrega 2: `docs/reservas-pagina-inicial.png`, `docs/reserva-conflito-bloqueada.png`
