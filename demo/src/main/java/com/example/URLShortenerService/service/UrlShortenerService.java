package com.example.URLShortenerService.service;

import com.example.URLShortenerService.entity.ShortURL;
import com.example.URLShortenerService.repository.ShortUrlRepository;
import com.example.URLShortenerService.util.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UrlShortenerService {
    
    private static final int MAX_COLLISION_ATTEMPTS = 10;
    
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    
    @Autowired
    private ShortCodeGenerator shortCodeGenerator;
    
    @Transactional
    public ShortURL createShortUrl(String originalUrl) {
        // Check if URL already exists
        Optional<ShortURL> existingUrl = shortUrlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }
        
        // Generate unique short code with collision handling
        String shortCode = generateUniqueShortCode();
        
        // Create and save new ShortURL
        ShortURL shortUrl = new ShortURL();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setShortCode(shortCode);
        
        return shortUrlRepository.save(shortUrl);
    }
    
    @Transactional(readOnly = true)
    public Optional<String> getOriginalUrl(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .map(ShortURL::getOriginalUrl);
    }
    
    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;
        
        do {
            shortCode = shortCodeGenerator.generateShortCode();
            attempts++;
            
            if (attempts > MAX_COLLISION_ATTEMPTS) {
                throw new RuntimeException("Unable to generate unique short code after " + MAX_COLLISION_ATTEMPTS + " attempts");
            }
        } while (shortUrlRepository.findByShortCode(shortCode).isPresent());
        
        return shortCode;
    }
}