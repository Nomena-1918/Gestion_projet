package com.example.films.controller;

import com.example.films.dto.ComedienDTO;
import com.example.films.dto.CreateComedienDTO;
import com.example.films.service.ComedienService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/comediens")
public class ComedienController {
    private final ComedienService comedienService;

    public ComedienController(ComedienService comedienService) {
        this.comedienService = comedienService;
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<ComedienDTO>> getComediensByProjet(@PathVariable Long projetId) {
        try {
            List<ComedienDTO> comediens = comedienService.getComediensByProjet(projetId);
            return ResponseEntity.ok(comediens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ComedienDTO>> getAllComediens() {
        try {
            List<ComedienDTO> comediens = comedienService.getAllComediens();
            return ResponseEntity.ok(comediens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComedienDTO> getComedienById(@PathVariable Long id) {
        try {
            ComedienDTO comedien = comedienService.getComedienById(id);
            return ResponseEntity.ok(comedien);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ComedienDTO> createComedien(
    @RequestParam("projetId") Long projetId, 
    @RequestParam("nom") String nom,
    @RequestParam("age") Integer age,
    @RequestParam("email") String email,
    @RequestParam(value = "photo", required = false) MultipartFile photo,
    @RequestParam(value = "dateDisponibilite", required = false) List<LocalDate> datesDisponibilite,
    @RequestParam(value = "statutDisponibilite", required = false) List<String> statutsDisponibilite) {
    
    try {
        CreateComedienDTO createComedienDTO = new CreateComedienDTO();
        createComedienDTO.setProjetId(projetId); 
        createComedienDTO.setNom(nom);
        createComedienDTO.setAge(age);
        createComedienDTO.setEmail(email);

        if (photo != null && !photo.isEmpty()) {
            String photoPath = comedienService.savePhoto(photo);
            createComedienDTO.setPhotoPath(photoPath);
        }

        ComedienDTO createdComedien = comedienService.createComedien(createComedienDTO);
        
        // Gérer les disponibilités multiples
        if (datesDisponibilite != null && statutsDisponibilite != null) {
            for (int i = 0; i < datesDisponibilite.size(); i++) {
                if (i < statutsDisponibilite.size()) {
                    comedienService.addDisponibilite(
                        createdComedien.getId(), 
                        datesDisponibilite.get(i), 
                        statutsDisponibilite.get(i)
                    );
                }
            }
        }

        return new ResponseEntity<>(createdComedien, HttpStatus.CREATED);
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(null);
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComedienDTO> updateComedien(
            @PathVariable Long id,
            @RequestParam(value = "nom", required = false) String nom,
            @RequestParam(value = "age", required = false) Integer age,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "projetId", required = false) Long projetId,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "dateDisponibilite", required = false) LocalDate dateDisponibilite,
            @RequestParam(value = "statutDisponibilite", required = false) String statutDisponibilite) {
        
        try {
            CreateComedienDTO updateComedienDTO = new CreateComedienDTO();
            
            // Seulement inclure les champs qui sont fournis
            if (nom != null) updateComedienDTO.setNom(nom);
            if (age != null) updateComedienDTO.setAge(age);
            if (email != null) updateComedienDTO.setEmail(email);
            if (projetId != null) updateComedienDTO.setProjetId(projetId);
            if (dateDisponibilite != null) updateComedienDTO.setDateDisponibilite(dateDisponibilite);
            if (statutDisponibilite != null) updateComedienDTO.setStatutDisponibilite(statutDisponibilite);

            if (photo != null && !photo.isEmpty()) {
                String photoPath = comedienService.savePhoto(photo);
                updateComedienDTO.setPhotoPath(photoPath);
            }

            ComedienDTO updatedComedien = comedienService.updateComedien(id, updateComedienDTO);
            return ResponseEntity.ok(updatedComedien);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

     @GetMapping("/photo/{filename}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String filename) {
        try {
            byte[] photo = comedienService.getPhoto(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(photo);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

     @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComedien(@PathVariable Long id) {
        try {
            comedienService.deleteComedien(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    @PostMapping("/{id}/disponibilites")
    public ResponseEntity<Void> addDisponibilite(@PathVariable Long id,
                                                @RequestParam LocalDate date,
                                                @RequestParam String statut) {
        try {
            comedienService.addDisponibilite(id, date, statut);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/disponibilites/{disponibiliteId}")
    public ResponseEntity<Void> updateDisponibilite(@PathVariable Long id,
                                                @PathVariable Long disponibiliteId,
                                                @RequestBody DisponibiliteUpdateRequest request) {
        try {
            comedienService.updateDisponibilite(id, disponibiliteId, request.getDate(), request.getStatut());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/disponibilites/{disponibiliteId}")
    public ResponseEntity<Void> deleteDisponibilite(@PathVariable Long id,
                                                @PathVariable Long disponibiliteId) {
        try {
            comedienService.deleteDisponibilite(id, disponibiliteId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Classe pour la requête de mise à jour
    public static class DisponibiliteUpdateRequest {
        private LocalDate date;
        private String statut;
        
        // Getters et setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }
    }
}