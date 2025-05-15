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
                .securityMatcher("/**") 
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/js/**", "/webjars/**", "/login",
                                "/images/**", "/fotos/**", "/resources/**").permitAll()
                        .requestMatchers("/ventas/**", "/ventas/listar*", "/ventas/listar/**",                                
                                "/proveedores/**", "/clientes/**", "/productos/**", "/dashboard",
                                 "/api/**", "/ingresoegreso", "/ingresoegreso/guardar", "/usuarios/listar")                        
                        .hasAnyRole("ADMIN", "VENTA")                        
                        .anyRequest().hasRole("ADMIN")
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
                )
                .sessionManagement(session -> session
                        .maximumSessions(3)  // Permite un máximo de 3 sesiones simultáneas por usuario
                        .maxSessionsPreventsLogin(false)  // No previene el inicio de sesión si el límite se alcanza
                        .expiredUrl("/login?expired=true")  // URL a la que se redirige cuando la sesión expira
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

