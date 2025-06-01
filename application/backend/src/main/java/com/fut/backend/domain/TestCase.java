package com.fut.backend.domain;

import lombok.Data;

@Data
public class TestCase {
  private String id;
  private String description;
  private String instancePath;
  private ValidationContext context;
  private ExpectedResult expectedResult;
}