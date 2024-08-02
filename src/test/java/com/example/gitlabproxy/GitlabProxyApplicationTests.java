package com.example.gitlabproxy;

import javax.cache.spi.CachingProvider;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;

@SpringBootTest
class GitlabProxyApplicationTests extends AbstractTest {

    @Autowired
    private CacheManager cacheManager;
    
    @Test
    void contextLoads() {
    }

    @Test
    public void testEhcacheIsUsedAsCacheManager() {
        softly.assertThat(cacheManager).isInstanceOf(JCacheCacheManager.class);

        JCacheCacheManager jCacheCacheManager = (JCacheCacheManager) cacheManager;
        javax.cache.CacheManager jCacheManager = jCacheCacheManager.getCacheManager();

        @SuppressWarnings("null")
        CachingProvider cachingProvider = jCacheManager.getCachingProvider();
        softly.assertThat(cachingProvider).isInstanceOf(EhcacheCachingProvider.class);
    }

    @Test
    public void testGroupsCacheExists() {
        softly.assertThat(cacheManager.getCache("groupsCache")).isNotNull();
    }
}
