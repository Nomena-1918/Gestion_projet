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
    
    List<Comedien> findByNomContainingIgnoreCase(String nom);
    
    @Query("SELECT c FROM Comedien c LEFT JOIN FETCH c.disponibilites WHERE c.id = :id")
    Optional<Comedien> findByIdWithDisponibilites(@Param("id") Long id);
    
    @Query("SELECT c FROM Comedien c LEFT JOIN FETCH c.disponibilites")
    List<Comedien> findAllWithDisponibilites();
}