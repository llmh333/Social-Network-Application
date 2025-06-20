package com.example.projectbase;

import com.example.projectbase.config.properties.AdminInfoProperties;
import com.example.projectbase.constant.GenderConstant;
import com.example.projectbase.constant.PermissionRole;
import com.example.projectbase.constant.RoleConstant;
import com.example.projectbase.domain.entity.Role;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties({AdminInfoProperties.class})
@SpringBootApplication
public class ProjectBaseApplication {

  private final UserRepository userRepository;

  private final RoleRepository roleRepository;

  private final PasswordEncoder passwordEncoder;



  public static void main(String[] args) {
    Environment env = SpringApplication.run(ProjectBaseApplication.class, args).getEnvironment();
    String appName = env.getProperty("spring.application.name");
    if (appName != null) {
      appName = appName.toUpperCase();
    }
    String port = env.getProperty("server.port");
    log.info("-------------------------START " + appName
        + " Application------------------------------");
    log.info("   Application         : " + appName);
    log.info("   Url swagger-ui      : http://localhost:" + port + "/swagger-ui.html");
    log.info("-------------------------START SUCCESS " + appName
        + " Application------------------------------");
  }
}
