package com.example.URLShortenerService.controller;

import com.example.URLShortenerService.dto.ShortenUrlRequest;
import com.example.URLShortenerService.dto.ShortenUrlResponse;
import com.example.URLShortenerService.entity.ShortURL;
import com.example.URLShortenerService.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Controller
public class HomeController {
    
    @Autowired
    private UrlShortenerService urlShortenerService;
    
    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("shortenUrlRequest", new ShortenUrlRequest());
        return "index";
    }
    
    @PostMapping("/shorten")
    public String processForm(@Valid @ModelAttribute ShortenUrlRequest request, 
                            BindingResult bindingResult, 
                            Model model, 
                            HttpServletRequest httpRequest) {
        
        if (bindingResult.hasErrors()) {
            return "index";
        }
        
        try {
            ShortURL shortUrl = urlShortenerService.createShortUrl(request.getUrl());
            
            // Build full short URL
            String baseUrl = getBaseUrl(httpRequest);
            String fullShortUrl = baseUrl + "/" + shortUrl.getShortCode();
            
            ShortenUrlResponse response = new ShortenUrlResponse(
                shortUrl.getShortCode(),
                fullShortUrl,
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt(),
                shortUrl.getExpireAt()
            );
            
            model.addAttribute("result", response);
            model.addAttribute("shortenUrlRequest", new ShortenUrlRequest());
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to shorten URL: " + e.getMessage());
        }
        
        return "index";
    }
    
    @GetMapping("/{shortCode}")
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode) {
        Optional<String> originalUrl = urlShortenerService.getOriginalUrl(shortCode);
        
        if (originalUrl.isPresent()) {
            return new RedirectView(originalUrl.get());
        } else {
            return new RedirectView("/?error=Short URL not found");
        }
    }
    
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }
        
        return baseUrl.toString();
    }
}