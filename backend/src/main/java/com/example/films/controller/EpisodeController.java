package com.example.films.controller;

import com.example.films.dto.CreateEpisodeDTO;
import com.example.films.dto.EpisodeDTO;
import com.example.films.service.EpisodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/episodes") 
public class EpisodeController {
    private final EpisodeService episodeService;

    public EpisodeController(EpisodeService episodeService) {
        this.episodeService = episodeService;
    }

    // Endpoint pour récupérer les épisodes d'un projet
    @GetMapping("/projet/{projetId}")
    public List<EpisodeDTO> getEpisodesByProjetId(@PathVariable Long projetId) {
        return episodeService.getEpisodesByProjetId(projetId);
    }

    // Endpoint simple pour récupérer un épisode par son ID
    @GetMapping("/{id}")
    public ResponseEntity<EpisodeDTO> getEpisodeById(@PathVariable Long id) {
        try {
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
    public ResponseEntity<EpisodeDTO> updateEpisode(@PathVariable Long id, @RequestBody CreateEpisodeDTO updateEpisodeDTO) {
        try {
            EpisodeDTO updatedEpisode = episodeService.updateEpisode(id, updateEpisodeDTO);
            return ResponseEntity.ok(updatedEpisode);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEpisode(@PathVariable Long id) {
        try {
            episodeService.deleteEpisode(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
