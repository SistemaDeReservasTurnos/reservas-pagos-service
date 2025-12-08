package com.servicio.reservas.pago.infraestructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(this.jwtAuthenticationConverter())
                        )
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        Converter<Jwt, Collection<GrantedAuthority>> combinedAuthoritiesConverter = getJwtCollectionConverter(defaultAuthoritiesConverter);

        converter.setJwtGrantedAuthoritiesConverter(combinedAuthoritiesConverter);

        return converter;
    }

    private static Converter<Jwt, Collection<GrantedAuthority>> getJwtCollectionConverter(JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter) {
        JwtGrantedAuthoritiesConverter rolesAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        rolesAuthoritiesConverter.setAuthoritiesClaimName("roles");
        rolesAuthoritiesConverter.setAuthorityPrefix("");

        return jwt -> {
            Collection<GrantedAuthority> defaults = defaultAuthoritiesConverter.convert(jwt);
            Collection<GrantedAuthority> roles = rolesAuthoritiesConverter.convert(jwt);

            return Stream.concat(defaults.stream(), roles.stream())
                    .collect(Collectors.toList());
        };
    }
}
