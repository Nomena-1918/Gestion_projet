package com.example.films.repository;

import com.example.films.entity.DisponibiliteComedien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DisponibiliteComedienRepository extends JpaRepository<DisponibiliteComedien, Long> {
    
    @Query("SELECT d FROM DisponibiliteComedien d WHERE d.comedien.id = :comedienId AND d.date = :date")
    Optional<DisponibiliteComedien> findByComedienIdAndDate(@Param("comedienId") Long comedienId, 
                                                          @Param("date") LocalDate date);
}