# Pet Mania API

Este repositório contém a correção e refatoração da **Pet Mania API**, um desafio prático focado em identificar e resolver problemas de comunicação, persistência e estruturação de rotas de uma aplicação Spring Boot integrada ao MySQL.

A aplicação serve como backend para gerir o registo, consulta e remoção de animais para adoção, comunicando tanto com uma interface web front-end como através de pedidos REST testados no Postman.

---

## Tecnologias Utilizadas

* **Java 25**
* **Spring Boot 4.1.2**
* **Spring Data JPA & Hibernate**
* **MySQL**
* **Swagger / OpenAPI (springdoc-openapi)** para documentação interativa
* **Postman** para testes dos endpoints[cite: 2]

---

## O que foi diagnosticado e corrigido?

Durante a análise do código inicial, foram identificados e corrigidos diversos problemas que impediam a aplicação de arrancar ou de processar pedidos corretamente:

### 1. Dependências e Versão no pom.xml
* **Versão incorreta do JDK:** O projeto apontava para a versão 21. Ajustámos para uma versão estável e compatível do Java 25.
* **Falta de starters essenciais:** O projeto declarava apenas o `spring-boot-starter` básico. Adicionamos o `spring-boot-starter-web` (necessário para subir o servidor Tomcat e processar pedidos HTTP) e o `spring-boot-starter-data-jpa` (para gerir entidades e ligação à base de dados).

### 2. Entidade Pet e Modelação na Base de Dados
* **Erro de conversão (int vs Integer):** O identificador da entidade estava definido como `int` primitivo com inicial maiúscula (`private int Id`). Como no registo o `id` não é enviado no payload JSON (é gerado pela base de dados), o conversor tentava injetar `null` num tipo primitivo, gerando um erro. Alterámos para o wrapper `Integer id`, adicionamos o construtor padrão sem argumentos exigido pelo Hibernate e criamos os getters e setters necessários.
* **Mapeamento de Enums e Check Constraints:** Os campos `sexo` e `porte` não tinham anotação explícita de enum. Por padrão, a JPA tenta gravar o valor ordinal numérico, o que violava as restrições (`CHECK constraints`) da tabela `tb_pet`. Adicionamos a anotação `@Enumerated(EnumType.STRING)` para garantir que valores textuais (como `MACHO`, `FEMEA`, `PEQUENO`, `MEDIO`, `GRANDE`) fossem gravados corretamente.

### 3. Camada de Repositório (PetRepository)
* No repositório existia a declaração inválida `public class PetRepository extends CrudRepository<Pet, int>` sem parâmetros. Removemos essa linha, aproveitando os métodos nativos fornecidos pelo `JpaRepository` (`findAll()`, `findById(ID id)` e `deleteById(ID id)`).

### 4. Camada de Controller (PetController)
* **Anotações e Rotas:** Existia um erro de sintaxe (`@RequestMaping`) e o caminho `/api/pets` estava declarado no `@RestController`, o que não configurava a rota base. Corrigimos para `@RequestMapping("/api/pets")`.
* **Inversão dos Métodos HTTP:** Os métodos estavam trocados — a listagem utilizava `@PostMapping` e a criação utilizava `@GetMapping`. Corrigimos para os padrões REST adequados:
  * `GET` para consulta de dados.
  * `POST` para registo de novos registos.
  * Implementação do endpoint de remoção `DELETE /{id}`.
  * Implementação da procura por identificador `GET /{id}`.
* **CORS:** Adicionámos a anotação `@CrossOrigin(origins = "*")` para permitir que a interface front-end comunique com a API sem bloqueios de política de mesma origem.

---

## Endpoints da API

O endereço base da aplicação é `http://localhost:8080/api/pets`.

| Método | Endpoint | Descrição | Status HTTP |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/pets` | Devolve todos os animais registados | `200 OK` |
| **GET** | `/api/pets/{id}` | Procura um animal específico pelo ID | `200 OK` |
| **POST** | `/api/pets` | Regista um novo animal | `201 Created` |
| **DELETE** | `/api/pets/{id}` | Remove um animal do sistema | `204 No Content` |

---

## Exemplo de Payload para Registo (POST)

Para testar o registo via Postman, configure o cabeçalho com `Content-Type: application/json` e utilize o seguinte corpo no pedido (sem o campo `id`):

```json
{
  "nome": "",
  "especie": "Cão",
  "raca": "Labrador",
  "idade": 3,
  "sexo": "Macho",
  "porte": "Medio",
  "personalidade": "Brincalhão e dócil",
  "foto": "[https://exemplo.com/fotos/rex.jpg](https://exemplo.com/fotos/rex.jpg)",
  "disponivel": true
}
