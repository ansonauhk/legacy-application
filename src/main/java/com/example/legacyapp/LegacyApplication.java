package com.example.legacyapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.TimeZone;

@SpringBootApplication
@EnableWebSecurity
public class LegacyApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(LegacyApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println("Application started with UTC timezone");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("Application shutting down");
    }

    @Bean
    public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
        return new WebSecurityConfigurerAdapter() {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http.csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/api/public/**").permitAll()
                    .antMatchers("/api/admin/**").authenticated()
                    .anyRequest().permitAll()
                    .and()
                    .httpBasic();
            }
        };
    }
}