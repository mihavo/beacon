package io.beacon.historyservice.exceptions;

import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {
  @Override public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
    Map<String, Object> attributes = super.getErrorAttributes(request, ErrorAttributeOptions.defaults());

    Object statusObj = attributes.get("status");
    if (statusObj instanceof Integer status && status >= 500) {
      attributes.remove("message");
    }
    return attributes;
  }
}

