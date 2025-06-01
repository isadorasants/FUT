package com.fut.backend.cli;

import com.fut.backend.core.TestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CliRunner implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(CliRunner.class);
  private final TestManager testManager;

  public CliRunner(TestManager testManager) {
    this.testManager = testManager;
  }

  @Override
  public void run(String... args) {
    try {
      if (args.length == 0) {
        showUsage();
        return;
      }

      List<String> testFiles = resolveTestFiles(args);
      if (testFiles.isEmpty()) {
        logger.error("Nenhum arquivo de teste válido encontrado");
        showUsage();
        return;
      }

      logger.info("Iniciando execução de {} teste(s)", testFiles.size());
      testManager.executeTests(testFiles);
      logger.info("Execução concluída com sucesso");

    } catch (Exception e) {
      logger.error("Falha crítica na execução: {}", e.getMessage(), e);
      System.exit(1);
    }
  }

  private List<String> resolveTestFiles(String[] args) {
    File projectRoot = new File(System.getProperty("user.dir")).getParentFile().getParentFile();
    File testDir = new File(projectRoot, "tests");

    return Arrays.stream(args)
        .flatMap(arg -> {
          String normalizedArg = arg.replace("\\", "/");
          File file = new File(arg);

          if (file.isAbsolute() && file.exists()) {
            return Arrays.stream(new String[] { file.getAbsolutePath() });
          }

          if (arg.contains("*")) {
            String pattern = arg.replace("*", ".*");
            File dir = testDir.exists() ? testDir : new File(".");
            return Arrays.stream(dir.listFiles((d, name) -> name.matches(pattern)))
                .map(File::getAbsolutePath);
          }

          File testFile = new File(testDir, arg);
          return Arrays.stream(new String[] { testFile.getAbsolutePath() });
        })
        .map(File::new)
        .filter(file -> {
          if (!file.exists()) {
            logger.warn("Arquivo não encontrado: {}", file.getAbsolutePath());
            return false;
          }
          if (!file.getName().toLowerCase().endsWith(".yml") &&
              !file.getName().toLowerCase().endsWith(".yaml")) {
            logger.warn("Arquivo não é YAML: {}", file.getAbsolutePath());
            return false;
          }
          return true;
        })
        .map(File::getAbsolutePath)
        .collect(Collectors.toList());
  }

  private void showUsage() {
    logger.info("""
        Uso:
          java -jar fut.jar <arquivo-testes.yml>  # Executa um arquivo específico
          java -jar fut.jar test-*.yml            # Executa múltiplos testes
          java -jar fut.jar                       # Mostra esta ajuda

        Exemplos:
          java -jar fut.jar tests/patient-001.yml
          java -jar fut.jar tests/*.yml
        """);
  }
}
