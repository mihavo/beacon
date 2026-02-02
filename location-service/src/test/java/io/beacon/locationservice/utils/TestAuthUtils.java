package io.beacon.locationservice.utils;

public class TestAuthUtils {

  public static final String AUTH_HEADER = "Authorization";

  public static String createMockAuthHeader() {
    return "Bearer mock-jwt-token";
  }
}
