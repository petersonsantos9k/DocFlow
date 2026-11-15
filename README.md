# DocFlow

Plataforma para análise automatizada de repositórios Java e geração de documentação de APIs com foco em projetos Spring Boot.

## Visão geral

O DocFlow coleta um repositório de código-fonte, identifica controladores e endpoints REST, analisa estruturas Java e extrai metadados relevantes para documentação técnica.

A solução é voltada para reduzir trabalho manual de mapeamento de APIs e fornecer uma base estruturada para documentação mais consistente.

## Stack

- Java 25
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Maven
- JGit
- JavaParser
- H2 Database

## Requisitos

- JDK 25+
- Maven 3.9+
- Git
- acesso à internet para consultar repositórios públicos do GitHub

## Desenvolvimento

```bash
cd docflow-api
./mvnw spring-boot:run
```

A aplicação fica disponível em [http://localhost:8080](http://localhost:8080).

## Scripts

- `./mvnw spring-boot:run` — inicia a aplicação localmente
- `./mvnw test` — executa os testes automatizados
- `./mvnw verify` — compila e valida o projeto

## Objetivos de qualidade

O projeto adota princípios simples para manter código legível e fácil de evoluir:

- funções e classes com responsabilidade clara;
- módulos com escopo bem definido;
- baixa complexidade condicional e de navegação;
- uso de nomes expressivos e parâmetros limitados;
- revisão contínua da clareza e manutenção do código.

Esses critérios ajudam a reduzir o contexto necessário para leitura, alteração e extensão do sistema.
