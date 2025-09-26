<template>
  <div class="app-wrapper">
    <div class="accueil-container">
      <!-- Contenu principal -->
      <main class="main-content-scenariste">
        <div class="welcome-section">
          <h2>Bienvenue, {{ user?.nom }} !</h2>
          <p>Vous êtes connecté en tant que {{ user?.role }}</p>
        </div><br>

        <!-- Barre de recherche globale -->
        <div class="global-search-section">
          <div class="search-container">
            <i class="fas fa-search search-icon"></i>
            <input 
              type="text" 
              v-model="searchQuery" 
              @input="performSearch" 
              placeholder="Rechercher des épisodes, scènes, séquences..." 
              class="search-input"
            />
            <button v-if="searchQuery" @click="clearSearch" class="clear-search-btn">
              <i class="fas fa-times"></i>
            </button>
          </div>

          <!-- Résultats de recherche -->
          <div v-if="showSearchResults" class="search-results">
            <div class="search-results-header">
              <h3>Résultats de recherche ({{ searchResults.length }})</h3>
              <button @click="clearSearch" class="close-results-btn">
                <i class="fas fa-times"></i>
              </button>
            </div>
            <div class="results-list">
              <div 
                v-for="result in searchResults" 
                :key="result.id + result.type" 
                class="search-result-item"
                @click="navigateToResult(result)"
              >
                <span class="result-type-badge" :class="result.type">
                  {{ getTypeLabel(result.type) }}
                </span>
                <div class="result-content">
                  <h4>{{ result.titre }}</h4>
                  <div class="result-details">
                    <span>Projet: {{ result.projetTitre }}</span>
                    <span v-if="result.episodeTitre">Épisode: {{ result.episodeTitre }}</span>
                    <span v-if="result.sequenceTitre">Séquence: {{ result.sequenceTitre }}</span>
                    <span>Ordre: {{ result.ordre }}</span>
                  </div>
                  <p class="result-synopsis" v-if="result.synopsis">
                    {{ truncateText(result.synopsis, 100) }}
                  </p>
                </div>
                <i class="fas fa-arrow-right result-arrow"></i>
              </div>
              <div v-if="searchResults.length === 0" class="no-results">
                Aucun résultat trouvé pour "{{ searchQuery }}"
              </div>
            </div>
          </div>
        </div>

        <div class="title-filtre">
          <center>
            <h2>Les projets existants</h2>
          </center>
        </div><br>
        
        <div class="filters-scenariste">
          <div class="filter-group-scenariste">
            <label>Période de mise à jour:</label>
            <select v-model="filterTimePeriod">
              <option value="all">Toutes les périodes</option>
              <option value="today">Aujourd'hui</option>
              <option value="this_week">Cette semaine</option>
              <option value="this_month">Ce mois-ci</option>
              <option value="this_year">Cette année</option>
              <option value="recent">Récent (7 derniers jours)</option>
            </select>
          </div>
          <div class="filter-group-scenariste">
            <label>Genre:</label>
            <select v-model="filterGenre">
              <option value="">Tous</option>
              <option v-for="genre in genres" :key="genre.idGenre" :value="genre.nomGenre">{{ genre.nomGenre }}</option>
            </select>
          </div>
          <div class="filter-group-scenariste">
            <label>Statut:</label>
            <select v-model="filterStatut">
              <option value="">Tous</option>
              <option v-for="statut in statuts" :key="statut.idStatutProjet" :value="statut.nomStatutsProjet">{{ statut.nomStatutsProjet }}</option>
            </select>
          </div>

          <div class="add-project-btn-container">
            <button class="add-project-btn" @click="goToAddProject">
              <i class="fas fa-plus-circle icon"></i> Projet
            </button>
          </div>
        </div> <br> <br>
        
        <!-- Formulaire de modification de projet -->
        <div v-if="editingProject" class="edit-project-modal">
          <div class="modal-content">
            <div class="modal-header">
              <h3><i class="fas fa-edit icon"></i> Modifier le projet</h3>
              <button @click="cancelEdit" class="close-btn"><i class="fas fa-times icon"></i></button>
            </div>
            <form @submit.prevent="submitEdit" class="edit-form">
              <div class="form-group">
                <label for="edit-titre">Titre du projet *</label>
                <input 
                  type="text" 
                  id="edit-titre"
                  v-model="editForm.titre" 
                  required 
                  class="form-input"
                />
              </div>

              <div class="form-group">
                <label for="edit-synopsis">Synopsis</label>
                <textarea 
                  id="edit-synopsis"
                  v-model="editForm.synopsis" 
                  rows="4"
                  class="form-textarea"
                ></textarea>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label for="edit-genre">Genre *</label>
                  <select 
                    id="edit-genre"
                    v-model="editForm.genreId" 
                    required
                    class="form-select"
                  >
                    <option value="">Sélectionnez un genre</option>
                    <option v-for="genre in genres" :key="genre.idGenre" :value="genre.idGenre">
                      {{ genre.nomGenre }}
                    </option>
                  </select>
                </div>

                <div class="form-group">
                  <label for="edit-statut">Statut *</label>
                  <select 
                    id="edit-statut"
                    v-model="editForm.statutId" 
                    required
                    class="form-select"
                  >
                    <option value="">Sélectionnez un statut</option>
                    <option v-for="statut in statuts" :key="statut.idStatutProjet" :value="statut.idStatutProjet">
                      {{ statut.nomStatutsProjet }}
                    </option>
                  </select>
                </div>
              </div>

              <div class="form-group">
                <label for="edit-dateFin">Date de fin *</label>
                <input 
                  type="date" 
                  id="edit-dateFin"
                  v-model="editForm.dateFin" 
                  required
                  class="form-input"
                />
              </div>

              <div v-if="editError" class="error-message">
                {{ editError }}
              </div>

              <div class="form-actions">
                <button type="button" @click="cancelEdit" class="cancel-btn"><i class="fas fa-times icon"></i> Annuler</button>
                <button type="submit" class="submit-btn" :disabled="editLoading">
                  <i class="fas fa-save icon"></i> {{ editLoading ? 'Modification en cours...' : 'Modifier le projet' }}
                </button>
              </div>
            </form>
          </div>
        </div>

        <!-- liste des projets -->
        <div class="project-list">
          <div v-for="(project, index) in filteredProjects" :key="project.id" class="project-card-scen" :style="{'--index': index + 1}">
            <div class="episode-header">
                <div class="episode-statut-scene">
                  <p>{{ project.statutNom }}</p>
                <div class="episode-actions">
                  <span class="icon-edit" @click="startEdit(project)"><i class="fas fa-edit"></i></span>
                  <span class="icon-delete" @click="deleteProject(project.id)"><i class="fas fa-trash-alt"></i></span>
              </div>
              </div>
            </div>

            <h3><i class="fas fa-film icon"></i> Titre: {{ project.titre }}</h3>
            <p>Genre: {{ project.genreNom }}</p>
            <p>Créé le: {{ formatDate(project.creeLe) }}</p>
            <p>Modifié le: {{ formatDate(project.modifieLe) }}</p>
            
            <div class="episode-footer">
              <div class="button-group">
                <button class="details-btn" @click="$router.push(`/projet/${project.id}`)">
                  <i class="fas fa-info-circle icon"></i> Détails
                </button>
                <button class="add-episode-btn-scen" @click="goToAddEpisode(project.id)">
                  <i class="fas fa-plus-circle icon"></i> Épisode
                </button>
              </div>
              <button class="work-screen-btn" @click="$router.push(`/projet/${project.id}/ecran-travail`)">
                <i class="fas fa-desktop icon" style="color: #21294F;"></i> Écran de Travail
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import '../assets/css/scenariste_acceuil.css';

export default {
  name: 'ScenaristeAccueilView',
  data() {
    return {
      showProfileMenu: false,
      user: null,
      projects: [],
      genres: [],
      statuts: [],
      filterTimePeriod: 'all',
      filterGenre: '',
      filterStatut: '',
      editingProject: null,
      editForm: {
        titre: '',
        synopsis: '',
        genreId: '',
        statutId: '',
        dateFin: ''
      },
      editLoading: false,
      editError: '',
      
      // Nouvelles données pour la recherche
      searchQuery: '',
      searchResults: [],
      showSearchResults: false,
      searchTimeout: null
    };
  },
  computed: {
    userInitials() {
      if (!this.user?.nom) return '?';
      return this.user.nom
        .split(' ')
        .map(name => name.charAt(0))
        .join('')
        .toUpperCase()
        .substring(0, 2);
    },
    filteredProjects() {
      let list = this.projects;
      
      // Filtre par genre
      if (this.filterGenre) {
        list = list.filter(p => p.genreNom === this.filterGenre);
      }
      
      // Filtre par statut
      if (this.filterStatut) {
        list = list.filter(p => p.statutNom === this.filterStatut);
      }
      
      // Filtre par période de mise à jour
      if (this.filterTimePeriod !== 'all') {
        const now = new Date();
        let startDate;
        
        switch (this.filterTimePeriod) {
          case 'today':
            startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
            break;
          case 'this_week':
            const dayOfWeek = now.getDay();
            const diffToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
            startDate = new Date(now);
            startDate.setDate(now.getDate() - diffToMonday);
            startDate.setHours(0, 0, 0, 0);
            break;
          case 'this_month':
            startDate = new Date(now.getFullYear(), now.getMonth(), 1);
            break;
          case 'this_year':
            startDate = new Date(now.getFullYear(), 0, 1);
            break;
          case 'recent':
            startDate = new Date(now);
            startDate.setDate(now.getDate() - 7);
            break;
          default:
            startDate = null;
        }
        
        if (startDate) {
          list = list.filter(p => {
            const modifieLe = new Date(p.modifieLe);
            return modifieLe >= startDate;
          });
        }
      }
      
      return list;
    }
  },
  mounted() {
    this.loadUser();
    this.fetchGenres();
    this.fetchStatuts();
    this.fetchProjects();
    document.addEventListener('click', this.handleClickOutside);
    
    // Fermer les résultats de recherche en cliquant à l'extérieur
    document.addEventListener('click', this.handleClickOutsideSearch);
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside);
    document.removeEventListener('click', this.handleClickOutsideSearch);
  },
  methods: {
    loadUser() {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        this.user = JSON.parse(userStr);
        
        if (this.user.role !== 'SCENARISTE' && this.user.role !== 'REALISATEUR') {
          this.$router.push('/accueil');
        }
      } else {
        this.$router.push('/');
      }
    },
    
    // Méthodes de recherche
    performSearch() {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout);
      }
      
      if (this.searchQuery.trim().length < 2) {
        this.showSearchResults = false;
        this.searchResults = [];
        return;
      }
      
      this.searchTimeout = setTimeout(() => {
        this.executeSearch();
      }, 300);
    },

async executeSearch() {
  try {
    const query = this.searchQuery.trim();
    if (query.length < 2) return;
    
    console.log('Lancement de la recherche pour:', query);
    
    // Recherche dans les épisodes, scènes et séquences
    const [episodesResponse, scenesResponse, sequencesResponse] = await Promise.all([
      axios.get(`/api/recherche/episodes?q=${encodeURIComponent(query)}`).catch(error => {
        console.error('Erreur recherche épisodes:', error);
        return { data: [] };
      }),
      axios.get(`/api/recherche/scenes?q=${encodeURIComponent(query)}`).catch(error => {
        console.error('Erreur recherche scènes:', error);
        return { data: [] };
      }),
      axios.get(`/api/recherche/sequences?q=${encodeURIComponent(query)}`).catch(error => {
        console.error('Erreur recherche séquences:', error);
        return { data: [] };
      })
    ]);
    
    console.log('Réponses reçues:', {
      episodes: episodesResponse.data,
      scenes: scenesResponse.data,
      sequences: sequencesResponse.data
    });
    
    const episodes = (episodesResponse.data || []).map(episode => ({
      ...episode,
      type: 'episode',
      projetTitre: episode.projetTitre || 'Projet inconnu',
      projetId: episode.projetId || this.getProjetIdFromEpisode(episode)
    }));
    
    const scenes = (scenesResponse.data || []).map(scene => ({
      ...scene,
      type: 'scene',
      projetTitre: scene.projetTitre || 'Projet inconnu',
      episodeTitre: scene.episodeTitre || 'Épisode inconnu',
      projetId: scene.projetId || this.getProjetIdFromScene(scene)
    }));
    
    const sequences = (sequencesResponse.data || []).map(sequence => ({
      ...sequence,
      type: 'sequence',
      projetTitre: sequence.projetTitre || 'Projet inconnu',
      episodeTitre: sequence.episodeTitre || 'Épisode inconnu',
      projetId: sequence.projetId || this.getProjetIdFromSequence(sequence)
    }));
    
    this.searchResults = [...episodes, ...scenes, ...sequences];
    this.showSearchResults = true;
    
    console.log('Résultats combinés:', this.searchResults);
    
  } catch (error) {
    console.error('Erreur globale lors de la recherche:', error);
    this.searchResults = [];
    this.showSearchResults = true;
  }
},
    
    clearSearch() {
      this.searchQuery = '';
      this.searchResults = [];
      this.showSearchResults = false;
    },
    
    getTypeLabel(type) {
      const labels = {
        'episode': 'Épisode',
        'scene': 'Scène',
        'sequence': 'Séquence'
      };
      return labels[type] || type;
    },
    
    truncateText(text, maxLength) {
      if (text.length <= maxLength) return text;
      return text.substring(0, maxLength) + '...';
    },
    
    // navigateToResult(result) {
    //   // Navigation vers l'écran de travail avec les paramètres appropriés
    //   const queryParams = {};
      
    //   if (result.type === 'episode') {
    //     queryParams.episodeId = result.id;
    //   } else if (result.type === 'sequence') {
    //     queryParams.episodeId = result.episodeId;
    //     queryParams.sequenceId = result.id;
    //   } else if (result.type === 'scene') {
    //     queryParams.episodeId = result.episodeId;
    //     queryParams.sequenceId = result.sequenceId;
    //   }
      
    //   this.$router.push({
    //     path: `/projet/${result.projetId}/ecran-travail`,
    //     query: queryParams
    //   });
      
    //   this.clearSearch();
    // },
    
    navigateToResult(result) {
  // Vérifier que result.projetId est bien défini
  if (!result.projetId) {
    console.error('ID du projet manquant dans le résultat de recherche');
    return;
  }
  
  const queryParams = {};
  
  if (result.type === 'episode') {
    queryParams.episodeId = result.id;
  } else if (result.type === 'sequence') {
    queryParams.episodeId = result.episodeId;
    queryParams.sequenceId = result.id;
  } else if (result.type === 'scene') {
    queryParams.episodeId = result.episodeId;
    queryParams.sequenceId = result.sequenceId;
  }
  
  this.$router.push({
    path: `/projet/${result.projetId}/ecran-travail`,
    query: queryParams
  });
  
  this.clearSearch();
},
    handleClickOutsideSearch(event) {
      const searchContainer = event.target.closest('.global-search-section');
      if (!searchContainer) {
        this.showSearchResults = false;
      }
    },
    async fetchGenres() {
      try {
        const response = await axios.get('/api/genres');
        this.genres = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des genres:', error);
      }
    },
    async fetchStatuts() {
      try {
        const response = await axios.get('/api/statuts-projet');
        this.statuts = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des statuts:', error);
      }
    },
    async fetchProjects() {
      try {
        const response = await axios.get('/api/projets');
        this.projects = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des projets:', error);
      }
    },
    startEdit(project) {
      this.editingProject = project;
      this.editForm = {
        titre: project.titre,
        synopsis: project.synopsis || '',
        genreId: project.genreId,
        statutId: this.getCurrentStatutId(project.statutNom),
        dateFin: project.dateFin
      };
    },
    getCurrentStatutId(statutNom) {
      const statut = this.statuts.find(s => s.nomStatutsProjet === statutNom);
      return statut ? statut.idStatutProjet : '';
    },
    async submitEdit() {
      this.editLoading = true;
      this.editError = '';

      try {
        const response = await axios.put(`/api/projets/${this.editingProject.id}`, this.editForm);
        
        if (response.status === 200) {
          // Mettre à jour la liste des projets
          await this.fetchProjects();
          this.cancelEdit();
        }
      } catch (error) {
        console.error('Erreur lors de la modification du projet:', error);
        this.editError = error.response?.data?.message || 'Erreur lors de la modification du projet';
      } finally {
        this.editLoading = false;
      }
    },
    cancelEdit() {
      this.editingProject = null;
      this.editForm = {
        titre: '',
        synopsis: '',
        genreId: '',
        statutId: '',
        dateFin: ''
      };
      this.editError = '';
    },
    async deleteProject(projectId) {
      if (confirm('Êtes-vous sûr de vouloir supprimer ce projet ?')) {
        try {
          await axios.delete(`/api/projets/${projectId}`);
          await this.fetchProjects(); // Rafraîchir la liste
        } catch (error) {
          console.error('Erreur lors de la suppression du projet:', error);
          alert('Erreur lors de la suppression du projet');
        }
      }
    },
    formatDate(date) {
      return new Date(date).toLocaleString();
    },
    toggleProfileMenu() {
      this.showProfileMenu = !this.showProfileMenu;
    },
    handleClickOutside(event) {
      if (!event.target.closest('.profile-section')) {
        this.showProfileMenu = false;
      }
    },
    seDeconnecter() {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      this.$router.push('/');
    },
    goToAddEpisode(projectId) {
      this.$router.push(`/projet/${projectId}/add-episode`);
    },
    goToAddProject() {
      this.$router.push('/add-project');
    },
    // Méthode de secours pour la recherche
    async executeSearch() {
  try {
    const query = this.searchQuery.trim();
    if (query.length < 2) return;
    
    console.log('Lancement de la recherche pour:', query);
    
    // D'abord essayer la recherche normale
    try {
      const [episodesResponse, scenesResponse, sequencesResponse] = await Promise.all([
        axios.get(`/api/recherche/episodes?q=${encodeURIComponent(query)}`),
        axios.get(`/api/recherche/scenes?q=${encodeURIComponent(query)}`),
        axios.get(`/api/recherche/sequences?q=${encodeURIComponent(query)}`)
      ]);
      
      console.log('Réponses reçues:', {
        episodes: episodesResponse.data,
        scenes: scenesResponse.data,
        sequences: sequencesResponse.data
      });
      
      this.processSearchResults(episodesResponse.data, scenesResponse.data, sequencesResponse.data);
      
    } catch (apiError) {
      console.warn('Recherche API échouée, utilisation de la recherche de secours:', apiError);
      await this.executeSearchFallback(query);
    }
    
  } catch (error) {
    console.error('Erreur globale lors de la recherche:', error);
    this.searchResults = [];
    this.showSearchResults = true;
  }
},

processSearchResults(episodesData, scenesData, sequencesData) {
  const episodes = (episodesData || []).map(episode => ({
    ...episode,
    type: 'episode',
    projetTitre: episode.projetTitre || 'Projet inconnu',
    projetId: episode.projetId || this.extractProjetId(episode)
  }));
  
  const scenes = (scenesData || []).map(scene => ({
    ...scene,
    type: 'scene',
    projetTitre: scene.projetTitre || 'Projet inconnu',
    episodeTitre: scene.episodeTitre || 'Épisode inconnu',
    projetId: scene.projetId || this.extractProjetId(scene)
  }));
  
  const sequences = (sequencesData || []).map(sequence => ({
    ...sequence,
    type: 'sequence',
    projetTitre: sequence.projetTitre || 'Projet inconnu',
    episodeTitre: sequence.episodeTitre || 'Épisode inconnu',
    projetId: sequence.projetId || this.extractProjetId(sequence)
  }));
  
  this.searchResults = [...episodes, ...scenes, ...sequences];
  this.showSearchResults = true;
  
  console.log('Résultats combinés:', this.searchResults);
},

async executeSearchFallback(query) {
  try {
    // Recherche simplifiée - récupérer tous les projets et filtrer côté client
    const projetsResponse = await axios.get('/api/projets');
    const allProjects = projetsResponse.data || [];
    
    // Filtrer les projets dont le titre contient la requête
    const filteredProjects = allProjects.filter(project => 
      project.titre && project.titre.toLowerCase().includes(query.toLowerCase())
    );
    
    // Transformer en format de résultat de recherche
    this.searchResults = filteredProjects.map(project => ({
      id: project.id,
      titre: project.titre,
      type: 'projet',
      projetTitre: project.titre,
      synopsis: project.synopsis || '',
      ordre: 0,
      projetId: project.id
    }));
    
    this.showSearchResults = true;
    console.log('Résultats de secours:', this.searchResults);
    
  } catch (fallbackError) {
    console.error('Recherche de secours échouée:', fallbackError);
    this.searchResults = [];
    this.showSearchResults = true;
  }
},

extractProjetId(item) {
  // Logique pour extraire l'ID du projet si disponible
  return item.projetId || null;
},

  }
};
</script>

<style scoped>

/* Styles pour les résultats de recherche */
.search-results {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 800px;
  max-height: 400px;
  overflow-y: auto;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  margin-top: 5px;
}

.search-results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f9fa;
}

.search-results-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.close-results-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 5px;
}

.results-list {
  padding: 10px 0;
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.search-result-item:hover {
  background-color: #f8f9fa;
}

.search-result-item:last-child {
  border-bottom: none;
}

.result-type-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  margin-right: 12px;
  min-width: 70px;
  text-align: center;
}

.result-type-badge.episode {
  background-color: #e3f2fd;
  color: #1976d2;
}

.result-type-badge.scene {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.result-type-badge.sequence {
  background-color: #e8f5e8;
  color: #388e3c;
}

.result-content {
  flex: 1;
}

.result-content h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #333;
}

.result-details {
  margin: 0 0 5px 0;
  font-size: 12px;
  color: #666;
}

.result-details span {
  margin-right: 10px;
}

.result-synopsis {
  margin: 0;
  font-size: 12px;
  color: #888;
  font-style: italic;
}

.result-arrow {
  color: #666;
  margin-left: 10px;
}

.no-results {
  text-align: center;
  padding: 20px;
  color: #666;
  font-style: italic;
}

/* Styles existants pour les boutons... */
.episode-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.episode-statut-scene {
  background: #F8FFA1;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #21294F;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.episode-actions {
  display: flex;
  gap: 0.5rem;
  margin-left: auto;
}

.episode-footer {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid #E8D9E9;
}

.button-group {
  display: flex;
  gap: 0.5rem;
  justify-content: space-between;
}

.details-btn, .add-episode-btn-scen {
  flex: 1;
  padding: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s, transform 0.2s, box-shadow 0.2s;
  text-align: center;
  font-size: 0.9rem;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.details-btn {
  background: #243168;
  color: white;
}

.details-btn:hover {
  background: #446BB2;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.add-episode-btn-scen {
  background: #A8A3F6;
  color: #21294F;
}

.add-episode-btn-scen:hover {
  background: #D1B2D5;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.work-screen-btn {
  width: 100%;
  padding: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s, transform 0.2s, box-shadow 0.2s;
  text-align: center;
  font-size: 0.9rem;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFDD00;
  color: #21294F;
}

.work-screen-btn:hover {
  background: #E0F79C;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.icon-edit, .icon-delete {
  cursor: pointer;
  transition: transform 0.2s, color 0.2s;
  font-size: 1.2rem;
  margin-left: 0.5rem;
}

.icon-edit:hover {
  transform: scale(1.2);
  color: #446BB2;
}

.icon-delete:hover {
  transform: scale(1.2);
  color: #dc3545;
}

/* Styles supplémentaires pour l'organisation des boutons */
.episode-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.episode-statut-scene {
  background: #F8FFA1;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #21294F;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.episode-actions {
  display: flex;
  gap: 0.5rem;
  margin-left: auto;
}

.episode-footer {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid #E8D9E9;
}

.button-group {
  display: flex;
  gap: 0.5rem;
  justify-content: space-between;
}

.details-btn, .add-episode-btn-scen {
  flex: 1;
  padding: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s, transform 0.2s, box-shadow 0.2s;
  text-align: center;
  font-size: 0.9rem;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.details-btn {
  background: #243168;
  color: white;
}

.details-btn:hover {
  background: #446BB2;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.add-episode-btn-scen {
  background: #A8A3F6;
  color: #21294F;
}

.add-episode-btn-scen:hover {
  background: #D1B2D5;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.work-screen-btn {
  width: 100%;
  padding: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s, transform 0.2s, box-shadow 0.2s;
  text-align: center;
  font-size: 0.9rem;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFDD00;
  color: #21294F;
}

.work-screen-btn:hover {
  background: #E0F79C;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.icon-edit, .icon-delete {
  cursor: pointer;
  transition: transform 0.2s, color 0.2s;
  font-size: 1.2rem;
  margin-left: 0.5rem;
}

.icon-edit:hover {
  transform: scale(1.2);
  color: #446BB2;
}

.icon-delete:hover {
  transform: scale(1.2);
  color: #dc3545;
}
</style>