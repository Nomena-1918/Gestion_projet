package com.example.films.repository;

import com.example.films.entity.Personnage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonnageRepository extends JpaRepository<Personnage, Long> {
    
    List<Personnage> findByProjetId(Long projetId);
    
    List<Personnage> findByComedienId(Long comedienId);
    
    List<Personnage> findByComedienIsNull();
    
    @Query("SELECT p FROM Personnage p LEFT JOIN FETCH p.projet LEFT JOIN FETCH p.comedien WHERE p.id = :id")
    Optional<Personnage> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT p FROM Personnage p LEFT JOIN FETCH p.projet LEFT JOIN FETCH p.comedien")
    List<Personnage> findAllWithDetails();
    
    boolean existsByNomAndProjetId(String nom, Long projetId);
}

