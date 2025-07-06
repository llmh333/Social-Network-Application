package com.example.projectbase;

import com.example.projectbase.config.properties.AdminInfoProperties;
import com.example.projectbase.constant.GenderConstant;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties({AdminInfoProperties.class})
@SpringBootApplication
@EnableWebSecurity
@EnableScheduling
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

  @Bean
  CommandLineRunner init(AdminInfoProperties userInfo) {
    return args -> {
      //init role
      Optional<Role> role = roleRepository.findByRoleName(RoleConstant.ADMIN);
      if (role.isEmpty()) {
        List<String> permissions = new ArrayList<>();
        permissions.add("CREATE");
        permissions.add("READ");
        permissions.add("UPDATE");
        permissions.add("DELETE");
        roleRepository.save(Role.builder().name(RoleConstant.ADMIN).permissions(permissions).build());
        roleRepository.save(Role.builder().name(RoleConstant.USER).permissions(permissions).build());
      }
      role = roleRepository.findByRoleName(RoleConstant.ADMIN);
      //init admin
      Optional<User> user = userRepository.findByUsername("admin");
      if (user.isEmpty()) {
        User admin = User.builder()
                .username(userInfo.getUsername())
                .password(passwordEncoder.encode(userInfo.getPassword()))
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .role(role.get())
                .gender(GenderConstant.FEMALE)
                .email(userInfo.getEmail())
                .dob(LocalDate.now())
                .email("admin@example.com")
                .build();
        userRepository.save(admin);
      }
    };
  }
}
