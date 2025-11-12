package io.beacon.notificationservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

  @Value("${beacon.firebase-config-path}")
  private String firebaseConfigPath;

  @Bean FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
    return FirebaseMessaging.getInstance(firebaseApp);
  }

  @Bean
  FirebaseApp firebaseApp(GoogleCredentials googleCredentials) {
    FirebaseOptions options = FirebaseOptions.builder().setCredentials(googleCredentials).build();
    return FirebaseApp.initializeApp(options);
  }

  @Bean
  GoogleCredentials googleCredentials() throws IOException {
    return GoogleCredentials.fromStream(new ClassPathResource(firebaseConfigPath).getInputStream()).toBuilder().build();
  }
}
