package com.example.films.service;

import com.example.films.repository.EpisodeRealisateurRepository;
import com.example.films.repository.EpisodeScenaristeRepository;
import com.example.films.repository.RealisateurRepository;
import com.example.films.repository.ScenaristeRepository;
import com.example.films.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
    
    private final UtilisateurRepository utilisateurRepository;
    private final RealisateurRepository realisateurRepository;
    private final ScenaristeRepository scenaristeRepository;
    private final EpisodeRealisateurRepository episodeRealisateurRepository;
    private final EpisodeScenaristeRepository episodeScenaristeRepository;
    
    public AuthorizationService(UtilisateurRepository utilisateurRepository,
                               RealisateurRepository realisateurRepository,
                               ScenaristeRepository scenaristeRepository,
                               EpisodeRealisateurRepository episodeRealisateurRepository,
                               EpisodeScenaristeRepository episodeScenaristeRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.realisateurRepository = realisateurRepository;
        this.scenaristeRepository = scenaristeRepository;
        this.episodeRealisateurRepository = episodeRealisateurRepository;
        this.episodeScenaristeRepository = episodeScenaristeRepository;
    }
    
    public boolean hasAccessToEpisode(Long utilisateurId, Long episodeId) {
        var utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        if ("ADMIN".equals(utilisateur.getRole())) {
            return true;
        }
        
        if ("REALISATEUR".equals(utilisateur.getRole())) {
            var realisateur = realisateurRepository.findByUtilisateurId(utilisateurId)
                    .orElseThrow(() -> new RuntimeException("Réalisateur non trouvé"));
            
            return episodeRealisateurRepository.findByEpisodeIdAndRealisateurId(episodeId, realisateur.getId())
                    .isPresent();
        }
        
        if ("SCENARISTE".equals(utilisateur.getRole())) {
            var scenariste = scenaristeRepository.findByUtilisateurId(utilisateurId)
                    .orElseThrow(() -> new RuntimeException("Scénariste non trouvé"));
            
            return episodeScenaristeRepository.findByEpisodeIdAndScenaristeId(episodeId, scenariste.getId())
                    .isPresent();
        }
        
        return false;
    }
}