package com.br.personniMoveis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Todas requisições para API são autorizadas e validadas no filtro.
     * 
     * @param http
     * @return
     * @throws Exception 
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(
                        // Qualquer requisição para as seguintes URIs não precisa de autenticação.
                        "/", 
                        "/home", 
                        "/users/**", 
                        "/products"
                )
                .permitAll()
                // Requisições para URIs diferentes das especificadas exigem 
                // autenticação e/ou autorização de um role específico 
                // falta implementar...
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .permitAll()
            )
            .rememberMe(Customizer.withDefaults());
        return http.build();
    }
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = PasswordEncoderFactories.createDelegatingPasswordEncoder().
//                            .username("user")
//                            .password("password")
//                            .roles("USER")
//                            .build();
//        
//        return new InMemoryUserDetailsManager(user);
//    }
}
