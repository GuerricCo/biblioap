package com.bibli.config;

import java.time.Duration;
import org.ehcache.config.builders.*;
import org.ehcache.jsr107.Eh107Configuration;
import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        var ehcache = jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Object.class,
                Object.class,
                ResourcePoolsBuilder.heap(ehcache.getMaxEntries())
            )
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                .build()
        );
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, com.bibli.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, com.bibli.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, com.bibli.domain.User.class.getName());
            createCache(cm, com.bibli.domain.Authority.class.getName());
            createCache(cm, com.bibli.domain.User.class.getName() + ".authorities");
            createCache(cm, com.bibli.domain.Library.class.getName());
            createCache(cm, com.bibli.domain.Book.class.getName());
            createCache(cm, com.bibli.domain.Book.class.getName() + ".authorses");
            createCache(cm, com.bibli.domain.Author.class.getName());
            createCache(cm, com.bibli.domain.Author.class.getName() + ".bookses");
            createCache(cm, com.bibli.domain.Category.class.getName());
            createCache(cm, com.bibli.domain.Member.class.getName());
            createCache(cm, com.bibli.domain.Loan.class.getName());
            createCache(cm, com.bibli.domain.Review.class.getName());
            createCache(cm, com.bibli.domain.Reservation.class.getName());
            // jhipster-needle-ehcache-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}
