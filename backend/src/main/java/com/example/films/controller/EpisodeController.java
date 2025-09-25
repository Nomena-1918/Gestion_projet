package com.example.films.controller;

import com.example.films.dto.CreateEpisodeDTO;
import com.example.films.dto.EpisodeDTO;
import com.example.films.service.AuthorizationService;
import com.example.films.service.EpisodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/episodes") 
public class EpisodeController {
    private final EpisodeService episodeService;
    private final AuthorizationService authorizationService;

    public EpisodeController(EpisodeService episodeService, 
                            AuthorizationService authorizationService) {
        this.episodeService = episodeService;
          this.authorizationService = authorizationService; 
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<EpisodeDTO>> getEpisodesByProjetId(@PathVariable Long projetId, 
                                                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            // Vérifier l'accès au projet si nécessaire (seulement si userId est fourni)
            if (userId != null) {
                // Logique de vérification d'accès optionnelle
            }
            
            List<EpisodeDTO> episodes = episodeService.getEpisodesByProjetId(projetId);
            return ResponseEntity.ok(episodes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    
        @GetMapping("/utilisateur/{utilisateurId}")
        public ResponseEntity<List<EpisodeDTO>> getEpisodesByUtilisateurId(@PathVariable Long utilisateurId) {
            try {
                List<EpisodeDTO> episodes = episodeService.getEpisodesByUtilisateurId(utilisateurId);
                return ResponseEntity.ok(episodes);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }

    // Endpoint simple pour récupérer un épisode par son ID
    @GetMapping("/{id}")
    public ResponseEntity<EpisodeDTO> getEpisodeById(@PathVariable Long id, 
                                               @RequestHeader("X-User-Id") Long userId) {
        try {
            // Vérifier l'accès
            if (!authorizationService.hasAccessToEpisode(userId, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            EpisodeDTO episode = episodeService.getEpisodeById(id);
            return ResponseEntity.ok(episode);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/projet/{projetId}")
    public ResponseEntity<EpisodeDTO> createEpisode(@PathVariable Long projetId, @RequestBody CreateEpisodeDTO createEpisodeDTO) {
        try {
            EpisodeDTO createdEpisode = episodeService.createEpisode(projetId, createEpisodeDTO);
            return new ResponseEntity<>(createdEpisode, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/projet/{projetId}/check-order/{order}")
    public ResponseEntity<Boolean> checkOrderAvailability(
        @PathVariable Long projetId, 
        @PathVariable Integer order) {
        boolean exists = episodeService.orderExists(projetId, order);
        return ResponseEntity.ok(!exists); // true si disponible
    }

    @PutMapping("/{id}")
    public ResponseEntity<EpisodeDTO> updateEpisode(@PathVariable Long id, 
                                                @RequestBody CreateEpisodeDTO updateEpisodeDTO,
                                                @RequestHeader("X-User-Id") Long userId) {
        try {
            // Vérifier l'accès avant de permettre la modification
            if (!authorizationService.hasAccessToEpisode(userId, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            EpisodeDTO updatedEpisode = episodeService.updateEpisode(id, updateEpisodeDTO);
            return ResponseEntity.ok(updatedEpisode);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

   @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEpisode(@PathVariable Long id,
                                            @RequestHeader("X-User-Id") Long userId) {
        try {
            
            if (!authorizationService.hasAccessToEpisode(userId, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            episodeService.deleteEpisode(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
