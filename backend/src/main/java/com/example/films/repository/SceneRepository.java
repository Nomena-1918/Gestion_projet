package com.example.films.repository;

import com.example.films.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    
    @Query("SELECT s FROM Scene s LEFT JOIN FETCH s.sequence WHERE s.sequence.id = :sequenceId")
    List<Scene> findBySequenceId(@Param("sequenceId") Long sequenceId);
    
    @Query("SELECT s FROM Scene s LEFT JOIN FETCH s.sequence WHERE s.id = :id")
    Optional<Scene> findByIdWithSequence(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM Scene s WHERE s.sequence.id = :sequenceId")
    Integer countBySequenceId(@Param("sequenceId") Long sequenceId);
   
    @Query("SELECT s FROM Scene s LEFT JOIN FETCH s.sequence seq ORDER BY s.titre, seq.titre")
    List<Scene> findAllOrdered();
    
    @Query("SELECT s FROM Scene s LEFT JOIN FETCH s.sequence seq LEFT JOIN FETCH seq.episode e LEFT JOIN FETCH e.projet WHERE s.id = :id")
    Optional<Scene> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM Scene s LEFT JOIN FETCH s.sequence seq LEFT JOIN FETCH seq.episode e LEFT JOIN FETCH e.projet WHERE s.id = :id")
    Optional<Scene> findByIdWithFullDetails(@Param("id") Long id);

}