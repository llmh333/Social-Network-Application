package com.example.projectbase.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import com.google.firebase.auth.FirebaseAuth;

@Configuration
public class FirebaseConfig {

   @Bean
   public FirebaseApp firebaseApp() throws IOException {
      ClassPathResource serviceAccount = new ClassPathResource("firebase.json");

      if (!serviceAccount.exists()) {
         throw new IllegalStateException("firebase.json not found in resources");
      }

      FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
            .build();

      if (FirebaseApp.getApps().isEmpty()) {
         return FirebaseApp.initializeApp(options);
      } else {
         return FirebaseApp.getInstance();
      }
   }

   @Bean
   public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
      return FirebaseAuth.getInstance(firebaseApp);
   }
}
