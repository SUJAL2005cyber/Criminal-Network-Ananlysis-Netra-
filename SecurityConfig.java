package com.sih.demo.config;

import com.sih.demo.security.CustomUserDetailsService;
import com.sih.demo.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // This project uses normal HttpSession authentication for JSP pages.
            // The REST API remains JWT-based.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .csrf(csrf -> csrf.disable())

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .authorizeHttpRequests(auth -> auth

                // Public browser routes
                .requestMatchers(new AntPathRequestMatcher("/"))
                    .permitAll()
                .requestMatchers(new AntPathRequestMatcher("/admin/login"))
                    .permitAll()
                .requestMatchers(new AntPathRequestMatcher("/admin/register"))
                    .permitAll()
                .requestMatchers(new AntPathRequestMatcher("/admin/logout"))
                    .permitAll()

                // JSP pages are protected by AdminViewController's HttpSession check.
                // Spring Security must not block them before the controller runs.
                .requestMatchers(new AntPathRequestMatcher("/admin/**"))
                    .permitAll()

                // JSP/CSS/static resources
                .requestMatchers(new AntPathRequestMatcher("/resources/**"))
                    .permitAll()
                .requestMatchers(new AntPathRequestMatcher("/css/**"))
                    .permitAll()
                .requestMatchers(new AntPathRequestMatcher("/js/**"))
                    .permitAll()
                .requestMatchers(new AntPathRequestMatcher("/images/**"))
                    .permitAll()
                .requestMatchers(new AntPathRequestMatcher("/favicon.ico"))
                    .permitAll()
                .requestMatchers(new AntPathRequestMatcher("/error"))
                    .permitAll()

                // Authentication API
                .requestMatchers(new AntPathRequestMatcher("/api/auth/**"))
                    .permitAll()

                // Admin REST API
                .requestMatchers(new AntPathRequestMatcher("/api/admin/**"))
                    .hasRole("ADMIN")

                // Everything else under /api requires JWT authentication.
                .requestMatchers(new AntPathRequestMatcher("/api/**"))
                    .authenticated()

                .anyRequest()
                    .permitAll()
            )

            .authenticationProvider(authenticationProvider())

            .addFilterBefore(
                    jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:4200",
                        "http://localhost:8081"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
