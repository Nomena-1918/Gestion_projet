package com.example.films.repository;

import com.example.films.entity.Comedien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComedienRepository extends JpaRepository<Comedien, Long> {
    Optional<Comedien> findByEmail(String email);

    Optional<Comedien> findByProjetIdAndEmail(Long projetId, String email);
     
    List<Comedien> findByProjetId(Long projetId);
    
    List<Comedien> findByNomContainingIgnoreCaseAndProjetId(String nom, Long projetId);
    
    @Query("SELECT c FROM Comedien c LEFT JOIN FETCH c.disponibilites WHERE c.projet.id = :projetId")
    List<Comedien> findByProjetIdWithDisponibilites(@Param("projetId") Long projetId);
    
    @Query("SELECT c FROM Comedien c LEFT JOIN FETCH c.disponibilites WHERE c.id = :id AND c.projet.id = :projetId")
    Optional<Comedien> findByIdAndProjetIdWithDisponibilites(@Param("id") Long id, @Param("projetId") Long projetId);
    
    List<Comedien> findByNomContainingIgnoreCase(String nom);
    
    @Query("SELECT c FROM Comedien c LEFT JOIN FETCH c.disponibilites WHERE c.id = :id")
    Optional<Comedien> findByIdWithDisponibilites(@Param("id") Long id);
    
    @Query("SELECT DISTINCT c FROM Comedien c LEFT JOIN FETCH c.disponibilites")
    List<Comedien> findAllWithDisponibilites();
}