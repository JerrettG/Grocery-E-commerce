package com.gonsalves.ui.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Autowired
    private LogoutHandler logoutHandler;
    private static final String[] WHITE_LIST_URLS = {"/", "/home","/category", "/oauth2/authorization/auth0", "/stripe/events"};
    private static final String[] CSRF_WHITE_LIST_URLS = {"/stripe/events", "/api/v2/spans",  "/services/**","/payment/create-payment-intent","/api/**/cartService/**",
    "/api/**/customerProfileService/**", "/api/**/orderService/**", "/api/v1/productService/product/all"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeRequests().antMatchers("/images/**","/css/**","/js/**").permitAll();
        http.csrf().ignoringAntMatchers(CSRF_WHITE_LIST_URLS);
        return http
                .authorizeRequests()
                .mvcMatchers(WHITE_LIST_URLS).permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().defaultSuccessUrl("/")
                .and()
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .addLogoutHandler(logoutHandler)
                .and()
                .build();

    }
}