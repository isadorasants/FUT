package com.fut.backend.domain;

import lombok.Data;

import java.util.List;

@Data
public class ValidationContext {
  private List<String> igs;
  private List<String> profiles;
  private List<String> resources;
}