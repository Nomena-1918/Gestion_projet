package com.example.films.service;

import com.example.films.dto.LoginResponse;
import com.example.films.entity.Utilisateur;
import com.example.films.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;

@Service
public class AuthService {
    
    private final UtilisateurRepository utilisateurRepository;
    
    public AuthService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }
    
    public LoginResponse login(String email, String password) throws Exception {
        // Rechercher l'utilisateur par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Email ou mot de passe incorrect"));
        
        // Vérifier le mot de passe (comparaison simple pour l'exemple)
        if (!utilisateur.getMotDePasse().equals(password)) {
            throw new Exception("Email ou mot de passe incorrect");
        }
        
        // Générer un token simple (en production, utiliser JWT)
        String token = generateSimpleToken(utilisateur);
        
        return new LoginResponse(token, utilisateur);
    }
    
    private String generateSimpleToken(Utilisateur utilisateur) {
        // Token simple basé sur l'ID utilisateur et timestamp
        String tokenData = utilisateur.getId() + ":" + utilisateur.getEmail() + ":" + new Date().getTime();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }
} 