package com.shopnest.config;

import com.shopnest.security.CustomUserDetailsService;
import com.shopnest.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    // ── Password encoder ──────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── Authentication provider ───────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ── Authentication manager ────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Security filter chain ─────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Public ────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/search").permitAll()

                        // ── Product management ────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/products/**")
                        .hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.PUT,    "/api/products/**")
                        .hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.PATCH,  "/api/products/**")
                        .hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                        .hasRole("ADMIN")

                        // ── Order management ──────────────────
                        .requestMatchers(HttpMethod.GET, "/api/orders")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/user/**")
                        .hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/orders")
                        .hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/**")
                        .hasAnyRole("ADMIN", "CUSTOMER")

                        // ── Cart ──────────────────────────────
                        .requestMatchers("/api/cart/**")
                        .hasAnyRole("ADMIN", "CUSTOMER")

                        // ── All other requests require auth ───
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}