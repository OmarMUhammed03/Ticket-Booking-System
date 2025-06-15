package org.example.authservice.config;

import org.example.commonlibrary.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.authservice.repository.AuthUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService(final AuthUserRepository repository) {
        return new org.springframework.security.core.userdetails.UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String email) {
                return repository.findByEmail(email)
                        .orElseThrow(() -> new NotFoundException("User not found"));
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((auth) ->
                        auth
                                .requestMatchers(HttpMethod.POST,
                                        "/auth/login")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST,
                                        "/auth/register")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST,
                                        "/auth/refresh-token")
                                .permitAll()
                                .requestMatchers("/admin/**")
                                .hasAuthority("ADMIN")
                                .requestMatchers("/swagger-ui/**",
                                        "/v3/api-docs/**")
                                .permitAll()
                                .requestMatchers("/actuator/**")
                                .permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(s -> s
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
            final PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
