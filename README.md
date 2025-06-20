# FHIR Unit Test (FUT)

Repositório para o trabalho de **Construção de Software**, utilizando o padrão **FHIR** para validação automatizada de instâncias.

---

## 📄 Plano de Construção

Consulte o documento [`docs/Plano_De_Construcao.md`](docs/Plano_De_Construcao.md) para detalhes sobre requisitos, arquitetura e implementação.

---

## 🚀 Execução

O funcionamento segue os requisitos do plano. Os comandos devem ser executados na pasta principal do projeto (`fut`).  
Use endereçamento relativo ou absoluto para o arquivo JAR `application/backend/target/fut-0.0.1-SNAPSHOT.jar` se estiver em outro diretório.

---

## 🛠️ Primeira Execução

### 1. Baixe o código

- Faça o download do `.zip` do repositório e extraia.  
**ou**  
- Clone o repositório via:

```bash
git clone <URL_DO_REPOSITORIO>
```

### 2. Acesse o projeto via terminal

```bash
cd fut
```

### 3. Instale as dependências

#### Java 24
- Baixe e instale o [JDK 24](https://www.oracle.com/java/technologies/downloads/).  
- Configure o ambiente:

```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-24
set PATH=%JAVA_HOME%\bin;%PATH%
```

#### Maven 3.9.5
- Baixe o [Maven 3.9.5](https://maven.apache.org/download.cgi).  
- Configure o ambiente:

```cmd
set MAVEN_HOME=C:\apache-maven-3.9.5
set PATH=%MAVEN_HOME%\bin;%PATH%
```

#### Validator CLI
- Baixe o `validator_cli.jar` em [hl7.org/fhir/validator.html](https://hl7.org/fhir/validator.html)  
- Coloque o arquivo na pasta `validator/`

### 4. Compile o projeto

```bash
mvn clean package -f application/backend/pom.xml
```

---

## ▶️ Execução do Programa

### Com um arquivo de teste YAML:

```bash
java -jar application/backend/target/fut-0.0.1-SNAPSHOT.jar tests/patient-001.yml
```

### Para múltiplos testes com wildcard:

```bash
java -jar application/backend/target/fut-0.0.1-SNAPSHOT.jar tests/*.yml
```

### Para ajuda adicional:

```bash
java -jar application/backend/target/fut-0.0.1-SNAPSHOT.jar --help
```

---

## 📌 Importante

### Relatório de Testes

- O relatório JSON é gerado na pasta `reports/` com o nome `test-report.json`.

#### Exemplo:
```json
{
  "tests": [
    {
      "test_id": "Patient-001",
      "status": "success",
      "discrepancies": [],
      "execution_time_ms": 120
    }
  ],
  "total_tests": 1,
  "passed": 1,
  "failed": 0,
  "total_time_ms": 120,
  "timestamp": "2025-06-18T20:31:00Z"
}
```

### Logs
- Logs da execução ficam em `logs/fut.log`.

---

## 🧭 Estrutura do Projeto

```
/fut
├── application/
│   └── backend/
│       ├── src/
│       │   ├── main/java/com/fut/backend/
│       │   │   ├── cli/               # Executa via CLI
│       │   │   ├── core/              # Lógica principal
│       │   │   │   ├── reader/        # Leitura YAML
│       │   │   │   ├── validator/     # Executor FHIR
│       │   │   │   ├── comparator/    # Comparação de resultados
│       │   │   │   └── report/        # Geração de relatório
│       │   │   ├── domain/            # Modelos de dados
│       │   │   └── FutApplication.java
│       │   └── resources/
│       │       └── logback.xml
│       └── test/java/com/fut/backend/
│           ├── reader/
│           │   └── TestCaseReaderTest.java
│           └── validator/
│               └── ValidatorExecutorTest.java
│       └── pom.xml
├── validator/
│   └── validator_cli.jar
├── tests/
│   ├── patient-001.yml
│   └── instances/
│       └── patient-001.json
├── reports/
│   └── test-report.json
├── logs/
│   └── fut.log
├── docs/
│   ├── Plano_de_Construcao.md
│   └── diagrams/
└── README.md
```

