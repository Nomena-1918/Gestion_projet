package com.example.films.controller;

import com.example.films.dto.CreateSceneDTO;
import com.example.films.dto.SceneDTO;
import com.example.films.service.AuthorizationService;
import com.example.films.service.SceneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scenes")
public class SceneController {
    private final SceneService sceneService;
     private final AuthorizationService authorizationService;

    public SceneController(SceneService sceneService,
                           AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;  
        this.sceneService = sceneService;
    }

    // Endpoint pour récupérer les scènes d'une séquence
    @GetMapping("/sequences/{sequenceId}")
    public List<SceneDTO> getScenesBySequenceId(@PathVariable Long sequenceId) {
        return sceneService.getScenesBySequenceId(sequenceId);
    }

    // Endpoint pour récupérer toutes les scènes
    @GetMapping
    public ResponseEntity<List<SceneDTO>> getAllScenes() {
        try {
            List<SceneDTO> scenes = sceneService.getAllScenes();
            return ResponseEntity.ok(scenes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint pour récupérer une scène par son ID
    @GetMapping("/{id}")
    public ResponseEntity<SceneDTO> getSceneById(@PathVariable Long id) {
        try {
            SceneDTO scene = sceneService.getSceneById(id);
            return ResponseEntity.ok(scene);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint pour créer une scène dans une séquence
    @PostMapping("/sequences/{sequenceId}")
    public ResponseEntity<SceneDTO> createScene(@PathVariable Long sequenceId, 
                                            @RequestBody CreateSceneDTO createSceneDTO,
                                            @RequestHeader("X-User-Id") Long userId) {
        try {
            // Vérifier l'accès via la séquence et l'épisode parent
            if (!authorizationService.hasAccessToSequence(userId, sequenceId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            SceneDTO createdScene = sceneService.createScene(sequenceId, createSceneDTO);
            return new ResponseEntity<>(createdScene, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint pour mettre à jour une scène
   @PutMapping("/{id}")
    public ResponseEntity<SceneDTO> updateScene(@PathVariable Long id, 
                                            @RequestBody CreateSceneDTO updateSceneDTO,
                                            @RequestHeader("X-User-Id") Long userId) {
        try {
            if (!authorizationService.hasAccessToScene(userId, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            SceneDTO updatedScene = sceneService.updateScene(id, updateSceneDTO);
            return ResponseEntity.ok(updatedScene);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // Endpoint pour supprimer une scène
   @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long id,
                                        @RequestHeader("X-User-Id") Long userId) {
        try {
            if (!authorizationService.hasAccessToScene(userId, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            sceneService.deleteScene(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}