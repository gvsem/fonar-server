package ru.georgii.fonarserver.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import ru.georgii.fonarserver.server.FonarConfiguration;

@Configuration
public class InterceptorsConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    FonarConfiguration fonar;

    @Bean
    AuthInterceptor getAuthenticationInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getAuthenticationInterceptor())
                .excludePathPatterns("/socket.io/*", "/version", "/console.html", "/register", "/*/user/photo").pathMatcher(new AntPathMatcher());
    }

}