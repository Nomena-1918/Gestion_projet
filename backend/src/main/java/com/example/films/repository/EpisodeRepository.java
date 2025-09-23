
package com.example.films.repository;

import com.example.films.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    
    @Query("SELECT e FROM Episode e LEFT JOIN FETCH e.projet WHERE e.projet.id = :projetId")
    List<Episode> findByProjetId(@Param("projetId") Long projetId);
    
    @Query("SELECT e FROM Episode e LEFT JOIN FETCH e.projet WHERE e.id = :id")
    Optional<Episode> findByIdWithProjet(@Param("id") Long id);
    
    @Query("SELECT COUNT(e) FROM Episode e WHERE e.projet.id = :projetId")
    Integer countByProjetId(@Param("projetId") Long projetId);
    List<Episode> findByProjetIdOrderByOrdreAsc(Long projetId);

}