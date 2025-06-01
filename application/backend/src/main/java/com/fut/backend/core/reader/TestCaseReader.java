package com.fut.backend.core.reader;

import com.fut.backend.domain.ExpectedResult;
import com.fut.backend.domain.Invariant;
import com.fut.backend.domain.TestCase;
import com.fut.backend.domain.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TestCaseReader {

  private static final Logger logger = LoggerFactory.getLogger(TestCaseReader.class);
  private final Yaml yaml = new Yaml();

  public List<TestCase> loadTestCases(String yamlPath) {
    try {
      Path path = Paths.get(yamlPath).toAbsolutePath().normalize();
      logger.info("Carregando arquivo de teste: {}", path);

      if (!Files.exists(path)) {
        throw new IllegalArgumentException("Arquivo não encontrado: " + path);
      }
      if (!Files.isReadable(path)) {
        throw new IllegalArgumentException("Sem permissão para ler o arquivo: " + path);
      }
      if (Files.size(path) == 0) {
        throw new IllegalArgumentException("Arquivo YAML vazio: " + path);
      }

      try (FileInputStream input = new FileInputStream(path.toFile())) {
        Object yamlData = yaml.load(input);

        if (yamlData == null) {
          throw new YAMLException("Estrutura YAML inválida: documento vazio");
        }

        List<Map<String, Object>> testCases = yamlData instanceof List ? (List<Map<String, Object>>) yamlData
            : Collections.singletonList((Map<String, Object>) yamlData);

        return testCases.stream()
            .map(data -> parseTestCase(data, path.getParent().toFile()))
            .collect(Collectors.toList());
      }
    } catch (IOException e) {
      throw new RuntimeException("Falha ao ler arquivo YAML: " + yamlPath, e);
    } catch (YAMLException e) {
      throw new RuntimeException("Erro de sintaxe no YAML: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new RuntimeException("Erro inesperado ao processar YAML: " + e.getMessage(), e);
    }
  }

  private TestCase parseTestCase(Map<String, Object> data, File baseDir) {
    if (data == null) {
      throw new IllegalArgumentException("Dados do teste não podem ser nulos");
    }

    if (!data.containsKey("test_id")) {
      throw new IllegalArgumentException("Campo obrigatório 'test_id' não encontrado");
    }

    TestCase testCase = new TestCase();
    testCase.setId(data.get("test_id").toString());
    testCase.setDescription(data.getOrDefault("description", "").toString());

    String instancePath = data.containsKey("instance_path") ? data.get("instance_path").toString()
        : "instances/" + testCase.getId() + ".json";

    String resolvedPath = resolveInstancePath(baseDir, instancePath);
    logger.debug("Caminho resolvido para instance_path: {}", resolvedPath);
    testCase.setInstancePath(resolvedPath);

    testCase.setContext(parseValidationContext(
        (Map<String, Object>) data.getOrDefault("context", Collections.emptyMap())));

    testCase.setExpectedResult(parseExpectedResult(
        (Map<String, Object>) data.getOrDefault("expected_results", Collections.emptyMap())));

    return testCase;
  }

  private String resolveInstancePath(File baseDir, String instancePath) {
    Path resolvedPath;
    if (instancePath.startsWith("/") || instancePath.matches("^[A-Za-z]:.*")) {

      resolvedPath = Paths.get(instancePath).normalize();
    } else {
      resolvedPath = Paths.get(baseDir.getAbsolutePath(), instancePath).normalize();
    }

    if (!Files.exists(resolvedPath)) {
      logger.warn("Arquivo de instância FHIR não encontrado: {}", resolvedPath);
    }

    return resolvedPath.toString();
  }

  private ValidationContext parseValidationContext(Map<String, Object> contextData) {
    ValidationContext context = new ValidationContext();

    if (contextData != null) {
      context.setIgs(parseStringList(contextData.get("igs")));
      context.setProfiles(parseStringList(contextData.get("profiles")));
      context.setResources(parseStringList(contextData.get("resources")));
    }

    return context;
  }

  private ExpectedResult parseExpectedResult(Map<String, Object> resultData) {
    ExpectedResult result = new ExpectedResult();

    if (resultData != null) {
      result.setStatus(resultData.getOrDefault("status", "").toString());
      result.setErrors(parseStringList(resultData.get("errors")));
      result.setWarnings(parseStringList(resultData.get("warnings")));
      result.setInformations(parseStringList(resultData.get("informations")));
      result.setInvariants(parseInvariantList(resultData.get("invariants")));
    }

    return result;
  }

  private List<String> parseStringList(Object input) {
    if (input instanceof List) {
      return ((List<?>) input).stream()
          .filter(obj -> obj != null)
          .map(Object::toString)
          .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  private List<Invariant> parseInvariantList(Object input) {
    if (input instanceof List) {
      return ((List<?>) input).stream()
          .filter(Map.class::isInstance)
          .map(Map.class::cast)
          .map(this::parseInvariant)
          .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  private Invariant parseInvariant(Map<String, Object> data) {
    Invariant invariant = new Invariant();
    invariant.setExpression(data.getOrDefault("expression", "").toString());

    Object expected = data.get("expected");
    if (expected instanceof Boolean) {
      invariant.setExpected((Boolean) expected);
    } else if (expected != null) {
      invariant.setExpected(Boolean.parseBoolean(expected.toString()));
    }

    return invariant;
  }
}