import { defineStore } from 'pinia';
import axios from 'axios';

export const useEcranTravailStore = defineStore('ecranTravail', {
  state: () => ({
    episodes: [],
    currentEpisode: null,
    currentSequence: null,
    isLoading: false,
    error: null,
    currentPage: 0,
    hasNext: false,
    hasPrev: false
  }),
  actions: {
    async fetchEpisodes(projetId) {
      this.isLoading = true;
      try {
        const response = await axios.get(`/api/episodes/projet/${projetId}`);
        this.episodes = response.data;
        if (this.episodes.length > 0) {
          this.currentEpisode = this.episodes[0]; // Sélectionner le premier épisode par défaut
          this.currentSequence = this.currentEpisode.sequences?.[0] || null;
        }
        this.hasNext = this.episodes.length > 1;
        this.hasPrev = false;
      } catch (error) {
        this.error = 'Erreur lors du chargement des épisodes';
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },
    async selectEpisodeById(episodeId) {
      this.isLoading = true;
      try {
        const episode = this.episodes.find(e => e.idEpisode === episodeId);
        if (episode) {
          this.currentEpisode = episode;
          this.currentSequence = episode.sequences?.[0] || null;
          this.currentPage = this.episodes.findIndex(e => e.idEpisode === episodeId);
          this.hasNext = this.currentPage < this.episodes.length - 1;
          this.hasPrev = this.currentPage > 0;
        } else {
          this.error = 'Épisode non trouvé';
        }
      } catch (error) {
        this.error = 'Erreur lors de la sélection de l\'épisode';
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },
    goToNextPage() {
      if (this.hasNext) {
        this.currentPage++;
        this.currentEpisode = this.episodes[this.currentPage];
        this.currentSequence = this.currentEpisode.sequences?.[0] || null;
        this.hasNext = this.currentPage < this.episodes.length - 1;
        this.hasPrev = this.currentPage > 0;
      }
    },
    goToPrevPage() {
      if (this.hasPrev) {
        this.currentPage--;
        this.currentEpisode = this.episodes[this.currentPage];
        this.currentSequence = this.currentEpisode.sequences?.[0] || null;
        this.hasNext = this.currentPage < this.episodes.length - 1;
        this.hasPrev = this.currentPage > 0;
      }
    }
  }
});