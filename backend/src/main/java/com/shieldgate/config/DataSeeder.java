package com.shieldgate.config;

import com.shieldgate.domain.AdminUser;
import com.shieldgate.domain.enums.ClientTier;
import com.shieldgate.repository.AdminUserRepository;
import com.shieldgate.service.ApiClientService;
import com.shieldgate.service.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RuleService ruleService;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApiClientService apiClientService;

    @Value("${app.demo.admin.username:admin}")
    private String demoAdminUsername;

    @Value("${app.demo.admin.password:admin12345}")
    private String demoAdminPassword;

    @Value("${app.demo.client.principal-id:demo-client}")
    private String demoPrincipalId;

    @Value("${app.demo.client.api-key:demo-free-key}")
    private String demoApiKey;

    public DataSeeder(
        RuleService ruleService,
        AdminUserRepository adminUserRepository,
        PasswordEncoder passwordEncoder,
        ApiClientService apiClientService
    ) {
        this.ruleService = ruleService;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.apiClientService = apiClientService;
    }

    @Override
    public void run(String... args) {
        ruleService.getCurrentRule();

        if (adminUserRepository.findByUsername(demoAdminUsername).isEmpty()) {
            AdminUser user = new AdminUser();
            user.setUsername(demoAdminUsername);
            user.setPasswordHash(passwordEncoder.encode(demoAdminPassword));
            user.setRole("ADMIN");
            adminUserRepository.save(user);
        }

        apiClientService.createIfAbsent(demoPrincipalId, demoApiKey, ClientTier.FREE);

        log.info("ShieldGate demo admin username: {}", demoAdminUsername);
        log.info("ShieldGate demo admin password: {}", demoAdminPassword);
        log.info("ShieldGate demo API key: {}", demoApiKey);
    }
}
