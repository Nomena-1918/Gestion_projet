package com.example.films.repository;

import com.example.films.entity.DisponibiliteComedien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibiliteComedienRepository extends JpaRepository<DisponibiliteComedien, Long> {
    
    @Query("SELECT d FROM DisponibiliteComedien d WHERE d.comedien.id = :comedienId AND d.date = :date")
    Optional<DisponibiliteComedien> findByComedienIdAndDate(@Param("comedienId") Long comedienId, 
                                                          @Param("date") LocalDate date);
    
    @Query("SELECT d FROM DisponibiliteComedien d WHERE d.comedien.id = :comedienId")
    List<DisponibiliteComedien> findDisponibilitesByComedien(@Param("comedienId") Long comedienId);
    
    // Ajoutez ces deux méthodes qui étaient dans ComedienRepository par erreur
    List<DisponibiliteComedien> findByComedienId(Long comedienId);
    
}