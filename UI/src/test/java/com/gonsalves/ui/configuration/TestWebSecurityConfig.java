package com.gonsalves.ui.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
@TestConfiguration
@EnableWebSecurity
public class TestWebSecurityConfig {
    private static final String[] WHITE_LIST_URLS = {"/", "/home","/category", "/oauth2/authorization/auth0"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/images/**","/css/**","/js/**").permitAll();
        http.csrf().ignoringAntMatchers("/stripe/events", "api/v2/spans");
        return http
                .authorizeRequests()
                .mvcMatchers(WHITE_LIST_URLS).permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().defaultSuccessUrl("/")
                .and()
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .and()
                .build();

    }
}
