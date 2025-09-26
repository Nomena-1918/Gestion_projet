import { defineStore } from 'pinia'
import axios from 'axios'

export const useEcranTravailStore = defineStore('ecranTravail', {
  state: () => ({
    episodes: [],
    currentEpisode: null,
    sequences: [],
    currentSequence: null,
    currentEpisodeIndex: 0,
    currentPageIndex: 0,
    projetId: null,
    error: null,
    isLoading: false,
    lastViewedEpisodeId: null,
    lastViewedSequenceId: null,
  }),
  actions: {
    saveCurrentPosition() {
      this.lastViewedEpisodeId = this.currentEpisode ? this.currentEpisode.idEpisode : null;
      this.lastViewedSequenceId = this.currentSequence ? this.currentSequence.idSequence : null;
    },
    async restorePosition() {
      if (this.lastViewedEpisodeId) {
        await this.selectEpisodeById(this.lastViewedEpisodeId);
      }
      if (this.lastViewedSequenceId) {
        await this.selectSequenceById(this.lastViewedSequenceId);
      }
    },
    async fetchEpisodes(projetId) {
      this.isLoading = true
      try {
        this.projetId = projetId
        this.error = null
        console.log(`Fetching episodes for projetId: ${projetId}`)
        const response = await axios.get(`/api/ecran-travail/projets/${projetId}/episodes`)
        console.log('Response status:', response.status)
        console.log('Response data:', response.data)
        if (response.status === 204 || !response.data || response.data.length === 0) {
          this.error = 'Aucun épisode trouvé pour ce projet.'
          this.episodes = []
          this.currentEpisode = null
          this.sequences = []
          this.currentSequence = null
          this.currentEpisodeIndex = 0
          return
        }
        this.episodes = response.data
        this.episodes.sort((a, b) => a.ordre - b.ordre)

        if (this.episodes.length > 0) {
          this.currentEpisodeIndex = 0
          this.currentEpisode = this.episodes[0]
          await this.fetchSequences(this.episodes[0].idEpisode)
        }
      } catch (error) {
        console.error('Error details:', error)
        console.error('Response:', error.response)
        this.error = error.response?.status === 404
          ? `Projet non trouvé (ID: ${projetId}). Vérifiez que le projet existe dans la base de données.`
          : `Erreur lors du chargement des épisodes: ${error.message}`
        this.currentEpisode = null
        this.sequences = []
        this.currentSequence = null
        this.currentEpisodeIndex = 0
      } finally {
        this.isLoading = false
      }
    },
    async fetchSequences(episodeId, setToFirst = true) {
      this.isLoading = true
      try {
        this.error = null
        console.log(`Fetching sequences for episodeId: ${episodeId}`)
        const response = await axios.get(`/api/ecran-travail/episodes/${episodeId}/sequences`)
        if (response.status === 204 || !response.data || response.data.length === 0) {
          console.log('Aucune séquence trouvée pour cet épisode.')
          this.sequences = []
          this.currentSequence = null
          return
        }
        this.sequences = response.data
        this.sequences.sort((a, b) => a.ordre - b.ordre)
        if (this.sequences.length > 0 && setToFirst) {
          this.currentSequence = this.sequences[0]
          this.currentPageIndex = 0
          await this.fetchSequenceDetails(this.sequences[0].idSequence)
        } else if (!setToFirst) {
          // Mise à jour optionnelle de currentPageIndex si currentSequence est déjà défini
          if (this.currentSequence) {
            this.currentPageIndex = this.sequences.findIndex(s => s.idSequence === this.currentSequence.idSequence)
          }
        }
      } catch (error) {
        console.error('Erreur lors du chargement des séquences:', error.response?.data || error.message)
        this.sequences = []
        this.currentSequence = null
      } finally {
        this.isLoading = false
      }
    },
    async fetchSequenceDetails(sequenceId) {
      this.isLoading = true
      try {
        this.error = null
        console.log(`Fetching sequence details for sequenceId: ${sequenceId}`)
        const response = await axios.get(`/api/ecran-travail/sequences/${sequenceId}`)
        this.currentSequence = response.data
        console.log('Sequence details:', this.currentSequence)
        const episode = this.episodes.find(e => e.idEpisode === this.currentSequence.episodeId)
        if (episode) {
          this.currentEpisode = episode
          this.currentEpisodeIndex = this.episodes.findIndex(e => e.idEpisode === episode.idEpisode)
        }
      } catch (error) {
        console.error('Erreur lors du chargement des détails de la séquence:', error.response?.data || error.message)
        this.error = `Erreur lors du chargement des détails de la séquence: ${error.message}`
        this.currentSequence = null
      } finally {
        this.isLoading = false
      }
    },
    async selectEpisodeById(episodeId) {
      if (this.episodes.length === 0) {
        await this.fetchEpisodes(this.projetId)
      }
      const index = this.episodes.findIndex(e => e.idEpisode === parseInt(episodeId))
      if (index !== -1) {
        this.currentEpisodeIndex = index
        this.currentEpisode = this.episodes[index]
        this.sequences = []
        this.currentSequence = null
        await this.fetchSequences(this.currentEpisode.idEpisode)
      } else {
        this.error = 'Épisode non trouvé.'
      }
    },
    async selectSequenceById(sequenceId) {
      await this.fetchSequenceDetails(sequenceId)
      if (this.currentSequence) {
        await this.fetchSequences(this.currentSequence.episodeId, false)
        this.currentPageIndex = this.sequences.findIndex(s => s.idSequence === parseInt(sequenceId))
      } else {
        this.error = 'Séquence non trouvée.'
      }
    },
    async goToNextPage() {
      this.isLoading = true
      try {
        this.error = null
        
        if (this.currentSequence) {
          console.log(`Navigating to next sequence from: ${this.currentSequence.idSequence}`)
          const response = await axios.get(`/api/ecran-travail/sequences/${this.currentSequence.idSequence}/next`)
          if (response.status === 200) {
            this.currentSequence = response.data
            console.log('Détails de la séquence fetchés :', response.data)
            console.log('Scènes associées :', response.data.scenes)
            console.log('Dialogues de la première scène :', response.data.scenes?.[0]?.dialogues)
            console.log('Lieux de la première scène :', response.data.scenes?.[0]?.sceneLieus)
            this.currentPageIndex++
            console.log('Next sequence:', this.currentSequence)
            const episode = this.episodes.find(e => e.idEpisode === this.currentSequence.episodeId)
            if (episode) {
              this.currentEpisode = episode
              this.currentEpisodeIndex = this.episodes.findIndex(e => e.idEpisode === episode.idEpisode)
              if (this.currentSequence.episodeId !== this.sequences[0]?.episodeId) {
                await this.fetchSequences(this.currentSequence.episodeId, false)
              }
            }
            return
          } else if (response.status === 204) {
            console.log('No more sequences in episode, going to next episode')
          }
        }
        
        if (this.episodes.length > 0 && this.currentEpisodeIndex < this.episodes.length - 1) {
          this.currentEpisodeIndex++
          this.currentEpisode = this.episodes[this.currentEpisodeIndex]
          this.currentSequence = null
          this.currentPageIndex = 0
          this.sequences = []
          console.log(`Navigating to next episode: ${this.currentEpisode.titre}`)
          await this.fetchSequences(this.currentEpisode.idEpisode)
        } else {
          this.error = 'Fin du projet.'
        }
      } catch (error) {
        console.error('Erreur lors de la navigation suivante:', error.response?.data || error.message)
        this.error = error.response?.status === 204 ? 'Fin du projet.' : `Erreur lors de la navigation: ${error.message}`
      } finally {
        this.isLoading = false
      }
    },
    async goToPrevPage() {
      this.isLoading = true
      try {
        this.error = null
        
        // Try navigating to the previous sequence if available
        if (this.currentSequence) {
          console.log(`Navigating to previous sequence from: ${this.currentSequence.idSequence}`)
          const response = await axios.get(`/api/ecran-travail/sequences/${this.currentSequence.idSequence}/prev`)
          if (response.status === 200) {
            this.currentSequence = response.data
            this.currentPageIndex--
            console.log('Previous sequence:', this.currentSequence)
            const episode = this.episodes.find(e => e.idEpisode === this.currentSequence.episodeId)
            if (episode) {
              this.currentEpisode = episode
              this.currentEpisodeIndex = this.episodes.findIndex(e => e.idEpisode === episode.idEpisode)
              if (this.currentSequence.episodeId !== this.sequences[0]?.episodeId) {
                await this.fetchSequences(this.currentSequence.episodeId, false)
              }
            }
            return
          } else if (response.status === 204) {
            console.log('No previous sequences')
          }
        }
        
        // Navigate to the previous episode if available
        if (this.currentEpisodeIndex > 0) {
          console.log('Navigating to previous episode')
          this.currentEpisodeIndex--
          this.currentEpisode = this.episodes[this.currentEpisodeIndex]
          this.currentSequence = null
          this.currentPageIndex = 0
          this.sequences = []
          console.log(`Navigating to previous episode: ${this.currentEpisode.titre}`)
          await this.fetchSequences(this.currentEpisode.idEpisode)
          
          // If the previous episode has sequences, go to the last one
          if (this.sequences.length > 0) {
            this.currentSequence = this.sequences[this.sequences.length - 1]
            this.currentPageIndex = this.sequences.length - 1
            await this.fetchSequenceDetails(this.currentSequence.idSequence)
          }
          return
        }
        
        // If no episodes or at the first episode with no sequences, reset to initial state
        this.currentEpisode = null
        this.currentSequence = null
        this.currentEpisodeIndex = 0
        this.currentPageIndex = 0
        this.sequences = []
        this.error = 'Début du projet.'
      } catch (error) {
        console.error('Erreur lors de la navigation précédente:', error.response?.data || error.message)
        this.error = error.response?.status === 204 ? 'Début du projet.' : `Erreur lors de la navigation: ${error.message}`
      } finally {
        this.isLoading = false
      }
    },
  },
  getters: {
    hasNext: (state) => {
      if (state.episodes.length === 0) return false
      return state.currentEpisodeIndex < state.episodes.length - 1
    },
    hasPrev: (state) => {
      // Allow previous navigation if there are episodes or sequences, or if we can reset to initial state
      return state.currentEpisodeIndex > 0 || state.currentSequence || state.episodes.length === 0
    },
    getBackUrl: (state) => {
      let url = `/projet/${state.projetId}/ecran-travail`;
      if (state.lastViewedEpisodeId) {
        url += `?episodeId=${state.lastViewedEpisodeId}`;
        if (state.lastViewedSequenceId) {
          url += `&sequenceId=${state.lastViewedSequenceId}`;
        }
      } else if (state.currentEpisode) {
        url += `?episodeId=${state.currentEpisode.idEpisode}`;
        if (state.currentSequence) {
          url += `&sequenceId=${state.currentSequence.idSequence}`;
        }
      }
      return url;
    }
  },
  async fetchEpisodes(projetId) {
  // Validation robuste du projetId
  if (!projetId || projetId === 'null' || projetId === 'undefined') {
    console.error('projetId invalide:', projetId);
    this.error = 'ID du projet manquant ou invalide';
    this.isLoading = false;
    return;
  }
  
  this.isLoading = true;
  try {
    this.projetId = projetId;
    this.error = null;
    console.log(`Fetching episodes for projetId: ${projetId}`);
    
    // Tester les deux formats d'URL possibles
    let response;
    try {
      // Essayer d'abord sans le préfixe ecran-travail
      response = await axios.get(`/api/projets/${projetId}/episodes`);
    } catch (firstError) {
      console.log('Première tentative échouée, essai avec préfixe ecran-travail');
      // Essayer avec le préfixe ecran-travail
      response = await axios.get(`/api/ecran-travail/projets/${projetId}/episodes`);
    }
    
    console.log('Response status:', response.status);
    console.log('Response data:', response.data);
    
    if (response.status === 204 || !response.data || response.data.length === 0) {
      this.error = 'Aucun épisode trouvé pour ce projet.';
      this.episodes = [];
      this.currentEpisode = null;
      this.sequences = [];
      this.currentSequence = null;
      this.currentEpisodeIndex = 0;
      return;
    }
    
    this.episodes = response.data;
    this.episodes.sort((a, b) => a.ordre - b.ordre);

    if (this.episodes.length > 0) {
      this.currentEpisodeIndex = 0;
      this.currentEpisode = this.episodes[0];
      await this.fetchSequences(this.episodes[0].idEpisode);
    }
  } catch (error) {
    console.error('Error details:', error);
    console.error('Response:', error.response);
    
    this.error = `Erreur lors du chargement des épisodes: ${error.response?.data?.message || error.message}`;
    this.currentEpisode = null;
    this.sequences = [];
    this.currentSequence = null;
    this.currentEpisodeIndex = 0;
  } finally {
    this.isLoading = false;
  }
},
})


