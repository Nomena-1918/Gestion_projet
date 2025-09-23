package com.example.films.service;

import com.example.films.dto.CreateEpisodeDTO;
import com.example.films.dto.EpisodeDTO;
import com.example.films.entity.Episode;
import com.example.films.entity.EpisodeStatut;
import com.example.films.entity.Projet;
import com.example.films.entity.StatutEpisode;
import com.example.films.repository.EpisodeRepository;
import com.example.films.repository.EpisodeStatutRepository;
import com.example.films.repository.ProjetRepository;
import com.example.films.repository.SequenceRepository;
import com.example.films.repository.StatutEpisodeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    private final EpisodeStatutRepository episodeStatutRepository;
    private final ProjetRepository projetRepository;
    private final StatutEpisodeRepository statutEpisodeRepository;
    private final SequenceRepository sequenceRepository;

    public EpisodeService(EpisodeRepository episodeRepository,
                         EpisodeStatutRepository episodeStatutRepository,
                         ProjetRepository projetRepository,
                         StatutEpisodeRepository statutEpisodeRepository,
                         SequenceRepository sequenceRepository) {
        this.episodeRepository = episodeRepository;
        this.episodeStatutRepository = episodeStatutRepository;
        this.projetRepository = projetRepository;
        this.statutEpisodeRepository = statutEpisodeRepository;
         this.sequenceRepository = sequenceRepository;
    }

   public List<EpisodeDTO> getEpisodesByProjetId(Long projetId) {
        List<Episode> episodes = episodeRepository.findByProjetId(projetId);
        // Pas de throw si vide ; on retourne simplement la liste triée (potentiellement vide)
        List<Episode> sortedEpisodes = episodes.stream()
                .sorted(Comparator.comparingInt(Episode::getOrdre))
                .collect(Collectors.toList());
        return sortedEpisodes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Nouvelle méthode pour vérifier si un ordre existe déjà
    public boolean orderExists(Long projetId, Integer order) {
        List<Episode> existingEpisodes = episodeRepository.findByProjetId(projetId);
        return existingEpisodes.stream()
                .anyMatch(ep -> ep.getOrdre().equals(order));
    }
    
    @Transactional
    public EpisodeDTO createEpisode(Long projetId, CreateEpisodeDTO createEpisodeDTO) {
        // Vérifier si l'ordre existe déjà pour ce projet
        List<Episode> existingEpisodes = episodeRepository.findByProjetId(projetId);
        boolean orderExists = existingEpisodes.stream()
                .anyMatch(ep -> ep.getOrdre().equals(createEpisodeDTO.getOrdre()));
        
        if (orderExists) {
            throw new RuntimeException("L'ordre " + createEpisodeDTO.getOrdre() + " existe déjà pour ce projet");
        }
        
        // Créer l'épisode
        Episode episode = new Episode();
        episode.setTitre(createEpisodeDTO.getTitre());
        episode.setOrdre(createEpisodeDTO.getOrdre());
        episode.setSynopsis(createEpisodeDTO.getSynopsis());
        
        // Associer le projet
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        episode.setProjet(projet);
        
        // Sauvegarder l'épisode
        Episode savedEpisode = episodeRepository.save(episode);
        
        // Créer le statut de l'épisode
        EpisodeStatut episodeStatut = new EpisodeStatut();
        episodeStatut.setEpisode(savedEpisode);
        
        StatutEpisode statut = statutEpisodeRepository.findById(createEpisodeDTO.getStatutId())
                .orElseThrow(() -> new RuntimeException("Statut non trouvé"));
        episodeStatut.setStatut(statut);
        episodeStatut.setDateDebut(LocalDateTime.now());
        
        episodeStatutRepository.save(episodeStatut);
        
        // Retourner le DTO
        return convertToDTO(savedEpisode);
    }

    private EpisodeDTO convertToDTO(Episode episode) {
        EpisodeDTO dto = new EpisodeDTO();
        dto.setIdEpisode(episode.getId());
        dto.setProjetId(episode.getProjet().getId());
        dto.setTitre(episode.getTitre());
        dto.setOrdre(episode.getOrdre());
        dto.setSynopsis(episode.getSynopsis());
        dto.setCreeLe(episode.getCreeLe());
        dto.setModifieLe(episode.getModifieLe());
        
        
        // Récupérer le statut le plus récent
        Optional<EpisodeStatut> statutOpt = episodeStatutRepository.findLatestStatutByEpisodeId(episode.getId());
            if (statutOpt.isPresent()) {
                EpisodeStatut statut = statutOpt.get();
                dto.setStatutNom(statut.getStatut().getNomStatutsEpisode());
                
                // Si le statut est "Tourné" (ou autre statut terminal), utiliser date_fin
                if (statut.getDateFin() != null && 
                    ("tourne".equals(statut.getStatut().getCode()) || 
                    "valide".equals(statut.getStatut().getCode()))) {
                    dto.setDateFin(statut.getDateFin());
                }
            } else {
                dto.setStatutNom("Non défini");
            }
            
            dto.setNombreSequences(sequenceRepository.countByEpisodeId(episode.getId()));
            
            return dto;
        }

    @Transactional
    public EpisodeDTO updateEpisode(Long id, CreateEpisodeDTO updateEpisodeDTO) {
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Épisode non trouvé"));
        
        episode.setTitre(updateEpisodeDTO.getTitre());
        episode.setOrdre(updateEpisodeDTO.getOrdre());
        episode.setSynopsis(updateEpisodeDTO.getSynopsis());
        
        Episode updatedEpisode = episodeRepository.save(episode);
        
        // Mettre à jour le statut si nécessaire
        if (updateEpisodeDTO.getStatutId() != null) {
            Optional<EpisodeStatut> currentStatutOpt = episodeStatutRepository.findLatestStatutByEpisodeId(id);
            
            if (currentStatutOpt.isPresent()) {
                EpisodeStatut currentStatut = currentStatutOpt.get();
                // Si le statut a changé, créer une nouvelle entrée
                if (!currentStatut.getStatut().getIdStatutEpisode().equals(updateEpisodeDTO.getStatutId())) {
                    currentStatut.setDateFin(LocalDateTime.now());
                    episodeStatutRepository.save(currentStatut);
                    
                    EpisodeStatut newStatut = new EpisodeStatut();
                    newStatut.setEpisode(episode);
                    StatutEpisode statut = statutEpisodeRepository.findById(updateEpisodeDTO.getStatutId())
                            .orElseThrow(() -> new RuntimeException("Statut non trouvé"));
                    newStatut.setStatut(statut);
                    newStatut.setDateDebut(LocalDateTime.now());
                    episodeStatutRepository.save(newStatut);
                }
            }
        }
        
        return convertToDTO(updatedEpisode);
    }

    @Transactional
    public void deleteEpisode(Long id) {
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Épisode non trouvé"));
        episodeRepository.delete(episode);
    }

    public EpisodeDTO getEpisodeById(Long id) {
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Épisode non trouvé"));
        
        EpisodeDTO dto = convertToDTO(episode);
        
        // Ajoutez le comptage des séquences si nécessaire
        Integer sequenceCount = sequenceRepository.countByEpisodeId(id);
        dto.setNombreSequences(sequenceCount);
        
        return dto;
    }
        
}

