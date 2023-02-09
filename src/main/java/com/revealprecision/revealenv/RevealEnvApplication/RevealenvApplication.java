package com.revealprecision.revealenv.RevealEnvApplication;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Configuration
@RequiredArgsConstructor
public class RevealenvApplication {

  private final ConfigurationClient configurationClient;
  private final ObjectMapper objectMapper;

  public static void main(String[] args) {
    SpringApplication.run(RevealenvApplication.class, args);
  }

  @GetMapping(value = "/envs", produces = "application/json")
  public ResponseEntity<List<Env>> get() {
    PagedIterable<ConfigurationSetting> configurationSettings = configurationClient.listConfigurationSettings(
        null);

    List<Env> collect = configurationSettings.stream().map(configurationSetting -> {
      try {
        Map<?, ?> data = objectMapper.readValue(configurationSetting.getValue(), Map.class);
        return new Env(configurationSetting.getKey(), data);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
      return null;
    }).filter(Objects::nonNull).collect(Collectors.toList());
    return ResponseEntity.ok(collect);
  }

}

@Configuration
class Config {

  @Value("${azure-endpoint}")
  String endpoint;

  @Value("${azure-id}")
  String id;

  @Value("${azure-secret}")
  String secret;


  @Bean
  ConfigurationClient getConfigClient() {
    String connectionString = "Endpoint=" + endpoint + ";" +
        "Id=" + id +";"+
        "Secret=" + secret;
    return new ConfigurationClientBuilder()
        .connectionString(
            connectionString)
        .buildClient();
  }

  @Bean
  ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }

}

@Setter
@Getter
@AllArgsConstructor
class Env {

  private String key;
  private Map<?, ?> data;
}


@Setter
@Getter
class EnvData {

  private String revealServerUrl;
  private String authUrl;
}

