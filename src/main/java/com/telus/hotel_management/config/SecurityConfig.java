package com.telus.hotel_management.config;

import com.telus.hotel_management.security.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);


    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter, PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        log.info("SecurityConfig initialized with custom UserDetailsService and JwtAuthenticationFilter.");
    }

    //    @Bean
//    public PasswordEncoder passwordEncoder() {
//        log.info("Creating PasswordEncoder bean (BCrypt).");
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        log.info("Creating AuthenticationManager bean.");
//        return authConfig.getAuthenticationManager();
//    }

    //
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring HttpSecurity filter chain.");
        http.csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/signin", "/api/signup").permitAll()
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers("/api/rooms/all").authenticated()
                .requestMatchers("/api/rooms/**").hasAuthority("ADMIN")
                .requestMatchers("/api/reservation/all").hasAnyAuthority("ADMIN", "HOTEL")
                .requestMatchers("/api/reservation/my").authenticated()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("HttpSecurity filter chain configured successfully.");
        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.info("Creating DaoAuthenticationProvider with custom UserDetailsService and PasswordEncoder.");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}
