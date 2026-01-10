package com.example.URLShortenerService.repository;

import com.example.URLShortenerService.entity.ShortURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortURL, Long> {
    
    Optional<ShortURL> findByShortCode(String shortCode);
    
    Optional<ShortURL> findByOriginalUrl(String originalUrl);
}