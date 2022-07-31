package ru.georgii.fonarserver.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.georgii.fonarserver.server.FonarConfiguration;

import javax.servlet.Filter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomTokenAuthenticationProvider customTokenAuthenticationProvider;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .antMatcher(FonarConfiguration.API_VERSION_PREFIX_ANT).addFilterBefore(getFilter(), AnonymousAuthenticationFilter.class);

    }

    @Override
    public void configure(WebSecurity web) throws Exception {

        web.ignoring().antMatchers("/v3/api-docs/**",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui/**",
                "/webjars/**",
                "/docs");

        web.ignoring().antMatchers("/socket.io/*", "/version", "/console.html", "/register");
    }

    private RequestMatcher getRequestMatchers() {
        return new OrRequestMatcher(new AntPathRequestMatcher("/**"));
    }

    private Filter getFilter() throws Exception {
        return new CustomAuthenticationProcessingFilter(getRequestMatchers(), authenticationManager());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customTokenAuthenticationProvider);
    }

}