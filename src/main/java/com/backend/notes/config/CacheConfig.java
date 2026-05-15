package com.backend.notes.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .recordStats()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10000));
        
        // Configure individual caches
        cacheManager.registerCustomCache("notes", Caffeine.newBuilder()
            .recordStats()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build());
        
        cacheManager.registerCustomCache("userNotesPage", Caffeine.newBuilder()
            .recordStats()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build());
        
        cacheManager.registerCustomCache("userByEmail", Caffeine.newBuilder()
            .recordStats()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build());
        
        return cacheManager;
    }
}
