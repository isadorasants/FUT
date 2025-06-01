package com.fut.backend.core;

import com.fut.backend.core.comparator.ResultComparator;
import com.fut.backend.core.reader.TestCaseReader;
import com.fut.backend.core.report.ReportGenerator;
import com.fut.backend.core.validator.ValidatorExecutor;
import com.fut.backend.domain.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class TestManager {

  private static final Logger logger = LoggerFactory.getLogger(TestManager.class);

  private final TestCaseReader testCaseReader;
  private final ValidatorExecutor validatorExecutor;
  private final ResultComparator resultComparator;
  private final ReportGenerator reportGenerator;

  public TestManager(TestCaseReader testCaseReader,
      ValidatorExecutor validatorExecutor,
      ResultComparator resultComparator,
      ReportGenerator reportGenerator) {
    this.testCaseReader = testCaseReader;
    this.validatorExecutor = validatorExecutor;
    this.resultComparator = resultComparator;
    this.reportGenerator = reportGenerator;
  }

  public void executeTests(List<String> testFiles) {
    ExecutorService executor = Executors.newFixedThreadPool(4);
    List<TestCase> allTestCases = new ArrayList<>();
    Map<String, List<String>> discrepanciesByTestId = new HashMap<>();

    for (String testFile : testFiles) {
      try {
        List<TestCase> testCases = testCaseReader.loadTestCases(testFile);
        allTestCases.addAll(testCases);

        for (TestCase testCase : testCases) {
          executor.submit(() -> {
            try {
              List<String> validatorOutput = validatorExecutor.validate(testCase);
              List<String> discrepancies = resultComparator.compare(validatorOutput, testCase.getExpectedResult());
              synchronized (discrepanciesByTestId) {
                discrepanciesByTestId.put(testCase.getId(), discrepancies);
              }
            } catch (Exception e) {
              logger.error("Erro ao executar teste {}: {}", testCase.getId(), e.getMessage());
              synchronized (discrepanciesByTestId) {
                discrepanciesByTestId.put(testCase.getId(), List.of("Erro: " + e.getMessage()));
              }
            }
          });
        }
      } catch (Exception e) {
        logger.error("Erro ao carregar arquivo {}: {}", testFile, e.getMessage());
      }
    }

    executor.shutdown();
    try {
      if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow();
        logger.warn("Timeout na execução dos testes");
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      logger.error("Interrupção na execução dos testes: {}", e.getMessage());
    }

    reportGenerator.generateReport(allTestCases, discrepanciesByTestId);
  }
}