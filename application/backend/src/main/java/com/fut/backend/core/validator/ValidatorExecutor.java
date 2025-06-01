package com.fut.backend.core.validator;

import com.fut.backend.domain.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ValidatorExecutor {
  private static final Logger logger = LoggerFactory.getLogger(ValidatorExecutor.class);
  private static final String VALIDATOR_PATH = "validator/validator_cli.jar";

  public List<String> validate(TestCase testCase) {
    File instanceFile = Paths.get(testCase.getInstancePath()).toFile();
    logger.info("Tentando acessar arquivo JSON: {}", instanceFile.getAbsolutePath());
    if (!instanceFile.exists() || !instanceFile.canRead()) {
      throw new IllegalArgumentException("Arquivo de instância inválido: " + testCase.getInstancePath());
    }

    File validatorFile = new File(VALIDATOR_PATH);
    if (!validatorFile.exists()) {
      throw new IllegalArgumentException("Validador FHIR não encontrado: " + VALIDATOR_PATH);
    }

    List<String> command = buildValidatorCommand(testCase);
    return executeValidatorCommand(command, testCase.getId());
  }

  private List<String> buildValidatorCommand(TestCase testCase) {
    List<String> command = new ArrayList<>();
    command.add("java");
    command.add("-jar");
    command.add(VALIDATOR_PATH);
    command.add("-version");
    command.add("4.0.1");
    command.add(testCase.getInstancePath());

    if (testCase.getContext().getProfiles() != null && !testCase.getContext().getProfiles().isEmpty()) {
      command.add("-profile");
      command.add(String.join(",", testCase.getContext().getProfiles()));
    }

    if (testCase.getContext().getIgs() != null && !testCase.getContext().getIgs().isEmpty()) {
      command.add("-ig");
      command.add(String.join(",", testCase.getContext().getIgs()));
    }

    return command;
  }

  private List<String> executeValidatorCommand(List<String> command, String testId) {
    List<String> output = new ArrayList<>();
    Process process = null;
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.redirectErrorStream(true);
      process = processBuilder.start();

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.add(line);
        }
      }

      if (!process.waitFor(30, TimeUnit.SECONDS)) {
        process.destroy();
        throw new IllegalStateException("Timeout na validação do teste: " + testId);
      }

      int exitCode = process.exitValue();
      if (exitCode != 0) {
        throw new IllegalStateException(
            "Validador retornou erro para teste " + testId + ": " + String.join("\n", output));
      }

    } catch (IOException | InterruptedException e) {
      throw new IllegalArgumentException("Erro ao executar validador para teste " + testId + ": " + e.getMessage(), e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return output;
  }
}