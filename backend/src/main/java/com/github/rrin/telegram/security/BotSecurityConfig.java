package com.github.rrin.telegram.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotSecurityConfig {

    /** Only the bot endpoints are guarded; the rest of the API has no authentication yet. */
    @Bean
    public FilterRegistrationBean<BotTokenFilter> botTokenFilter(@Value("${coperti.bot.token:}") String token) {
        FilterRegistrationBean<BotTokenFilter> registration = new FilterRegistrationBean<>(new BotTokenFilter(token));
        registration.addUrlPatterns("/api/bot/*");
        return registration;
    }
}
