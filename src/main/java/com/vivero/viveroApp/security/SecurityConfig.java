package com.vivero.viveroApp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class SecurityConfig {

    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/css/**", "/js/**", "/webjars/**", "/login",
                        "/images/**", "/fotos/**", "/resources/**", "/plantasvivero/**").permitAll()
                .requestMatchers("/ventas/**",
                        "/plantas/listar", "/tierra/listar", "/grow/listar",
                        "/decoracion/listar", "/maceta/listar", "/fertilizante/listar", "/herramienta/listar",
                        "/insecticida/listar", "/semilla/listar",
                        "/proveedores/**", "/clientes/**", "/dashboard",
                        "/plantas/pdf", "/maceta/pdf", "/tierra/pdf", "/grow/pdf", "/decoracion/pdf")
                .hasAnyRole("ADMIN", "VENTA")
                .anyRequest().hasRole("ADMIN") // El resto solo para admin
                )
                .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Obtener variables del entorno
        String adminUsername = dotenv.get("ADMIN_USER");
        String adminPassword = dotenv.get("ADMIN_PASSWORD");
        String ventaUsername = dotenv.get("VENTA_USER");
        String ventaPassword = dotenv.get("VENTA_PASSWORD");

        // Verificar que las variables del entorno existen
        if (adminUsername == null || adminPassword == null || ventaUsername == null || ventaPassword == null) {
            throw new IllegalStateException("Las variables de entorno necesarias no están configuradas correctamente.");
        }

        // Crear usuarios en memoria
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN")
                .build());
        manager.createUser(User.withUsername(ventaUsername)
                .password(passwordEncoder().encode(ventaPassword))
                .roles("VENTA")
                .build());
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
