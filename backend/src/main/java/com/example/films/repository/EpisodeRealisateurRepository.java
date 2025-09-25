package com.example.films.repository;

import com.example.films.entity.EpisodeRealisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRealisateurRepository extends JpaRepository<EpisodeRealisateur, Long> {
    
    List<EpisodeRealisateur> findByEpisodeId(Long episodeId);
    
    Optional<EpisodeRealisateur> findByEpisodeIdAndRealisateurId(Long episodeId, Long realisateurId);
    
    @Query("SELECT er FROM EpisodeRealisateur er JOIN FETCH er.realisateur r JOIN FETCH r.utilisateur WHERE er.episode.id = :episodeId")
    List<EpisodeRealisateur> findByEpisodeIdWithRealisateur(@Param("episodeId") Long episodeId);
    List<EpisodeRealisateur> findByRealisateurId(Long realisateurId);

}