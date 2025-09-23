package com.example.films.controller;

import com.example.films.dto.CreateSequenceDTO;
import com.example.films.dto.SequenceDTO;
import com.example.films.service.SequenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sequences")
public class SequenceController {
    private final SequenceService sequenceService;

    public SequenceController(SequenceService sequenceService) {
        this.sequenceService = sequenceService;
    }

    @GetMapping("/episodes/{episodeId}")
    public List<SequenceDTO> getSequencesByEpisodeId(@PathVariable Long episodeId) {
        return sequenceService.getSequencesByEpisodeId(episodeId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SequenceDTO> getSequenceById(@PathVariable Long id) {
        try {
            SequenceDTO sequence = sequenceService.getSequenceById(id);
            return ResponseEntity.ok(sequence);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/episodes/{episodeId}")
    public ResponseEntity<SequenceDTO> createSequence(@PathVariable Long episodeId, @RequestBody CreateSequenceDTO createSequenceDTO) {
        try {
            SequenceDTO createdSequence = sequenceService.createSequence(episodeId, createSequenceDTO);
            return new ResponseEntity<>(createdSequence, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SequenceDTO> updateSequence(@PathVariable Long id, @RequestBody CreateSequenceDTO updateSequenceDTO) {
        try {
            SequenceDTO updatedSequence = sequenceService.updateSequence(id, updateSequenceDTO);
            return ResponseEntity.ok(updatedSequence);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSequence(@PathVariable Long id) {
        try {
            sequenceService.deleteSequence(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}