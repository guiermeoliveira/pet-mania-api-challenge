# Pet Mania API

Este repositório contém a correção e refatoração da **Pet Mania API**, um desafio prático focado em identificar e resolver problemas de comunicação, persistência e estruturação de rotas de uma aplicação Spring Boot integrada ao MySQL[cite: 1, 2].

A aplicação serve como backend para gerir o registo, consulta e remoção de animais para adoção, comunicando tanto com uma interface web front-end como através de pedidos REST testados no Postman[cite: 1, 2].

---

## Tecnologias Utilizadas

* **Java 21**
* **Spring Boot 3.3.4**
* **Spring Data JPA & Hibernate**
* **MySQL**
* **Swagger / OpenAPI (springdoc-openapi)** para documentação interativa
* **Postman** para testes dos endpoints[cite: 2]

---

## O que foi diagnosticado e corrigido?

Durante a análise do código inicial, foram identificados e corrigidos diversos problemas que impediam a aplicação de arrancar ou de processar pedidos corretamente:

### 1. Dependências e Versão no pom.xml
* **Versão incorreta do Spring Boot:** O projeto apontava para a versão 4.1.2 do starter parent, que ainda não existia. Ajustámos para uma versão estável e compatível do Spring Boot 3 (3.3.4).
* **Falta de starters essenciais:** O projeto declarava apenas o `spring-boot-starter` básico. Adicionámos o `spring-boot-starter-web` (necessário para subir o servidor Tomcat e processar pedidos HTTP) e o `spring-boot-starter-data-jpa` (para gerir entidades e ligação à base de dados).

### 2. Entidade Pet e Modelação na Base de Dados
* **Erro de conversão (int vs Integer):** O identificador da entidade estava definido como `int` primitivo com inicial maiúscula (`private int Id`). Como no registo o `id` não é enviado no payload JSON (é gerado pela base de dados), o conversor tentava injetar `null` num tipo primitivo, gerando um erro de desserialização no Jackson[cite: 6]. Alterámos para o wrapper `Integer id`, adicionámos o construtor padrão sem argumentos exigido pelo Hibernate e criámos os getters e setters necessários.
* **Mapeamento de Enums e Check Constraints:** Os campos `sexo` e `porte` não tinham anotação explícita de enum. Por padrão, a JPA tenta gravar o valor ordinal numérico, o que violava as restrições (`CHECK constraints`) da tabela `tb_pet` no MySQL[cite: 7]. Adicionámos a anotação `@Enumerated(EnumType.STRING)` para garantir que valores textuais (como `MACHO`, `FEMEA`, `PEQUENO`, `MEDIO`, `GRANDE`) fossem gravados corretamente.

### 3. Camada de Repositório (PetRepository)
* No repositório existia a declaração inválida `List<Pet> findById();` sem parâmetros. Removemos essa linha, aproveitando os métodos nativos fornecidos pelo `JpaRepository` (`findAll()`, `findById(ID id)` e `deleteById(ID id)`).

### 4. Camada de Controller (PetController)
* **Anotações e Rotas:** Existia um erro de sintaxe (`@RequestMaping`) e o caminho `/api/pets` estava declarado no `@RestController`, o que não configurava a rota base. Corrigimos para `@RequestMapping("/api/pets")`.
* **Inversão dos Métodos HTTP:** Os métodos estavam trocados — a listagem utilizava `@PostMapping` e a criação utilizava `@GetMapping`. Corrigimos para os padrões REST adequados:
  * `GET` para consulta de dados.
  * `POST` para registo