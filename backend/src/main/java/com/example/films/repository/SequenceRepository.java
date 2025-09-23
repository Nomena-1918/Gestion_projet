package com.example.films.repository;

import com.example.films.entity.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SequenceRepository extends JpaRepository<Sequence, Long> {
    
    @Query("SELECT s FROM Sequence s LEFT JOIN FETCH s.episode WHERE s.episode.id = :episodeId")
    List<Sequence> findByEpisodeId(@Param("episodeId") Long episodeId);
    
    @Query("SELECT s FROM Sequence s LEFT JOIN FETCH s.episode WHERE s.id = :id")
    Optional<Sequence> findByIdWithEpisode(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM Sequence s WHERE s.episode.id = :episodeId")
    Integer countByEpisodeId(@Param("episodeId") Long episodeId);

}