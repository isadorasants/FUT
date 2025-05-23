# 📦 FHIR Unit Test (FUT) – Plano de Construção

## 1. Introdução

### 1.1 Visão Geral do Projeto

O **FHIR Unit Test (FUT)** é uma aplicação Java baseada em **Spring Boot**, voltada para automatizar a execução de testes de conformidade em instâncias FHIR (em formato JSON), com base em especificações definidas em arquivos **YAML**.  

A aplicação oferece uma interface via **linha de comando (CLI)** que coordena:
- Leitura dos casos de teste,
- Validação via `validator_cli.jar` (ferramenta oficial do HL7),
- Geração de relatórios JSON detalhados (e futuramente HTML).

### 1.2 Escopo do Projeto

O sistema é responsável por:

- 📥 Ler arquivos YAML contendo: ID do teste, descrição, caminho da instância FHIR e resultado esperado.  
- ✅ Validar instâncias JSON com `validator_cli.jar`.  
- 🔍 Comparar resultados obtidos com os esperados.  
- 🧾 Gerar relatórios em JSON com diagnósticos e discrepâncias.  
- ❗ Tratar erros como arquivos malformados, instâncias ausentes ou falhas na execução do validador.  
- 🧩 Possuir estrutura modular e com suporte inicial à execução concorrente.

## 2. Requisitos

### 2.1 Requisitos Funcionais

- ✅ **Execução via CLI**: Exemplo `java -jar fut.jar testeA.yml`
- 🗂️ **Suporte a arquivos `.yaml`** com estrutura definida.
- 🧪 **Validação FHIR** com `validator_cli.jar`.
- 📊 **Comparação de resultados**: status, erros, avisos.
- 📄 **Geração de relatórios em JSON**.
- ✳️ **Suporte a wildcards** para testes em lote: `test-*.yml`
- 🧱 **Tratamento de erros robusto** com mensagens claras.

### 2.2 Requisitos Não Funcionais

- 🧬 Desenvolvido em **Java 24** com **Spring Boot 3.2**.
- 📦 Bibliotecas: `Jackson`, `SnakeYAML`, `SLF4J/Logback`.
- 🧠 Arquitetura modular e escalável.
- ⏱️ Suporte a **execução concorrente** com timeout por teste.
- 📘 Logging detalhado para depuração.
- 🔌 Extensível para múltiplos validadores FHIR no futuro.

## 3. Casos de Uso

### 3.1 Execução via CLI

```bash
java -jar fut.jar                # Executa todos os .yml do diretório atual
java -jar fut.jar testeA.yml    # Executa um teste específico
java -jar fut.jar test-*.yml    # Executa testes que seguem padrão de nome
```

#### Exemplo com Wildcards

```bash
java -jar fut.jar patient-*.yml
```

**Saída esperada** (resumo do relatório JSON):

```json
{
  "tests": [
    {
      "test_id": "Patient-001",
      "status": "success",
      "discrepancies": [],
      "execution_time_ms": 150
    },
    {
      "test_id": "Patient-002",
      "status": "failed",
      "discrepancies": [
        "Unexpected error: Patient.gender is 'male' but expected 'female'"
      ],
      "execution_time_ms": 180
    }
  ],
  "total_tests": 2,
  "passed": 1,
  "failed": 1,
  "total_time_ms": 330
}
```

## 4. Arquitetura e Design

### 4.1 Componentes Principais

| Componente         | Responsabilidade |
|--------------------|------------------|
| **CliRunner**       | Inicia a aplicação via CLI |
| **TestManager**     | Orquestra o fluxo completo (leitura, validação, comparação, relatório) |
| **TestCaseReader**  | Lê e valida os arquivos YAML |
| **ValidatorExecutor** | Executa `validator_cli.jar` |
| **ResultComparator** | Compara saída com os resultados esperados |
| **ReportGenerator**  | Gera relatórios JSON (HTML no futuro) |

## 5. Tecnologias Utilizadas

| Item                  | Ferramenta/Tecnologia |
|------------------------|------------------------|
| Linguagem              | Java 24                |
| Framework              | Spring Boot 3.2        |
| CLI                    | CommandLineRunner      |
| Validador FHIR         | `validator_cli.jar` (HL7) |
| Parser YAML            | SnakeYAML 2.2          |
| Manipulação JSON       | Jackson 2.15           |
| Logging                | SLF4J + Logback        |
| Build Tool             | Maven 3.9              |
| Testes                 | JUnit 5, Mockito       |
| Diagramas              | PlantUML               |

> 🔽 O arquivo `validator_cli.jar` deve ser baixado manualmente do site oficial HL7: [https://hl7.org/fhir](https://hl7.org/fhir)