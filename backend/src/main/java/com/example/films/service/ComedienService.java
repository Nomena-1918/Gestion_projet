package com.example.films.service;

import com.example.films.dto.ComedienDTO;
import com.example.films.dto.CreateComedienDTO;
import com.example.films.dto.DisponibiliteDTO;
import com.example.films.entity.Comedien;
import com.example.films.entity.DisponibiliteComedien;
import com.example.films.repository.ComedienRepository;
import com.example.films.repository.DisponibiliteComedienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComedienService {
    private final ComedienRepository comedienRepository;
    private final DisponibiliteComedienRepository disponibiliteRepository;
     private final String uploadDir = "assets/photos/";

     public ComedienService(ComedienRepository comedienRepository, 
                         DisponibiliteComedienRepository disponibiliteRepository) {
        this.comedienRepository = comedienRepository;
        this.disponibiliteRepository = disponibiliteRepository;
        
        // Créer le répertoire s'il n'existe pas
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage", e);
        }
    }

    @Transactional
    public ComedienDTO createComedien(CreateComedienDTO createComedienDTO) {
        // Vérifier si l'email existe déjà
        if (comedienRepository.findByEmail(createComedienDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Un comédien avec cet email existe déjà");
        }

        // Créer le comédien
        Comedien comedien = new Comedien();
        comedien.setNom(createComedienDTO.getNom());
        comedien.setAge(createComedienDTO.getAge());
        comedien.setEmail(createComedienDTO.getEmail());
        comedien.setPhotoPath(createComedienDTO.getPhotoPath());

        Comedien savedComedien = comedienRepository.save(comedien);

        // Créer la disponibilité si fournie
        if (createComedienDTO.getDateDisponibilite() != null && 
            createComedienDTO.getStatutDisponibilite() != null) {
            DisponibiliteComedien disponibilite = new DisponibiliteComedien();
            disponibilite.setComedien(savedComedien);
            disponibilite.setDate(createComedienDTO.getDateDisponibilite());
            disponibilite.setStatut(createComedienDTO.getStatutDisponibilite());
            disponibiliteRepository.save(disponibilite);
        }

        return convertToDTO(savedComedien);
    }

        public String savePhoto(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Chemin complet du fichier
        Path filePath = Paths.get(uploadDir + uniqueFilename);
        
        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath);

        return uniqueFilename;
    }

    public byte[] getPhoto(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir + filename);
        return Files.readAllBytes(filePath);
    }

    public void deletePhoto(String filename) throws IOException {
        if (filename != null) {
            Path filePath = Paths.get(uploadDir + filename);
            Files.deleteIfExists(filePath);
        }
    }

    public List<ComedienDTO> getAllComediens() {
        List<Comedien> comediens = comedienRepository.findAllWithDisponibilites();
        return comediens.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ComedienDTO getComedienById(Long id) {
        Comedien comedien = comedienRepository.findByIdWithDisponibilites(id)
                .orElseThrow(() -> new RuntimeException("Comédien non trouvé"));
        return convertToDTO(comedien);
    }

    @Transactional
    public ComedienDTO updateComedien(Long id, CreateComedienDTO updateComedienDTO) {
        Comedien comedien = comedienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comédien non trouvé"));

        // Vérifier si l'email est déjà utilisé par un autre comédien
        if (!comedien.getEmail().equals(updateComedienDTO.getEmail()) &&
            comedienRepository.findByEmail(updateComedienDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Un autre comédien utilise déjà cet email");
        }

        comedien.setNom(updateComedienDTO.getNom());
        comedien.setAge(updateComedienDTO.getAge());
        comedien.setEmail(updateComedienDTO.getEmail());
        comedien.setPhotoPath(updateComedienDTO.getPhotoPath());

        Comedien updatedComedien = comedienRepository.save(comedien);
        return convertToDTO(updatedComedien);
    }


    @Transactional
   public void deleteComedien(Long id) {
        Comedien comedien = comedienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comédien non trouvé"));
        
        // Supprimer la photo associée
        if (comedien.getPhotoPath() != null) {
            try {
                deletePhoto(comedien.getPhotoPath());
            } catch (IOException e) {
                // Log l'erreur mais continue la suppression
                System.err.println("Erreur lors de la suppression de la photo: " + e.getMessage());
            }
        }
        
        comedienRepository.delete(comedien);
    }

    @Transactional
    public void addDisponibilite(Long comedienId, LocalDate date, String statut) {
        Comedien comedien = comedienRepository.findById(comedienId)
                .orElseThrow(() -> new RuntimeException("Comédien non trouvé"));

        // Vérifier si une disponibilité existe déjà pour cette date
        Optional<DisponibiliteComedien> existing = disponibiliteRepository
                .findByComedienIdAndDate(comedienId, date);

        if (existing.isPresent()) {
            throw new RuntimeException("Une disponibilité existe déjà pour cette date");
        }

        DisponibiliteComedien disponibilite = new DisponibiliteComedien(comedien, date, statut);
        disponibiliteRepository.save(disponibilite);
    }

    @Transactional
    public void updateDisponibilite(Long comedienId, Long disponibiliteId, LocalDate date, String statut) {
        Comedien comedien = comedienRepository.findById(comedienId)
            .orElseThrow(() -> new RuntimeException("Comédien non trouvé"));
        
        DisponibiliteComedien disponibilite = disponibiliteRepository.findById(disponibiliteId)
            .orElseThrow(() -> new RuntimeException("Disponibilité non trouvée"));
        
        // Vérifier que la disponibilité appartient bien au comédien
        if (!disponibilite.getComedien().getId().equals(comedienId)) {
            throw new RuntimeException("Cette disponibilité n'appartient pas à ce comédien");
        }
        
        disponibilite.setDate(date);
        disponibilite.setStatut(statut);
        
        disponibiliteRepository.save(disponibilite);
    }

    @Transactional
    public void deleteDisponibilite(Long comedienId, Long disponibiliteId) {
        Comedien comedien = comedienRepository.findById(comedienId)
            .orElseThrow(() -> new RuntimeException("Comédien non trouvé"));
        
        DisponibiliteComedien disponibilite = disponibiliteRepository.findById(disponibiliteId)
            .orElseThrow(() -> new RuntimeException("Disponibilité non trouvée"));
        
        // Vérifier que la disponibilité appartient bien au comédien
        if (!disponibilite.getComedien().getId().equals(comedienId)) {
            throw new RuntimeException("Cette disponibilité n'appartient pas à ce comédien");
        }
        
        disponibiliteRepository.delete(disponibilite);
    }

       private ComedienDTO convertToDTO(Comedien comedien) {
        ComedienDTO dto = new ComedienDTO();
        dto.setId(comedien.getId());
        dto.setNom(comedien.getNom());
        dto.setAge(comedien.getAge());
        dto.setEmail(comedien.getEmail());
        dto.setPhotoPath(comedien.getPhotoPath());
        dto.setCreeLe(comedien.getCreeLe());
        dto.setModifieLe(comedien.getModifieLe());

        // Convertir les disponibilités
        if (comedien.getDisponibilites() != null) {
            List<DisponibiliteDTO> disponibilitesDTO = comedien.getDisponibilites().stream()
                    .map(this::convertDisponibiliteToDTO)
                    .collect(Collectors.toList());
            dto.setDisponibilites(disponibilitesDTO);
        }

        return dto;
    }


private DisponibiliteDTO convertDisponibiliteToDTO(DisponibiliteComedien disponibilite) {
        DisponibiliteDTO dto = new DisponibiliteDTO();
        dto.setId(disponibilite.getId());
        dto.setDate(disponibilite.getDate());
        dto.setStatut(disponibilite.getStatut());
        return dto;
    }
}

