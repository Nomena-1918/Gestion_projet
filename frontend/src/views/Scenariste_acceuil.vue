<template>
  <div class="app-wrapper">
    <div class="accueil-container">
      <!-- Contenu principal -->
      <main class="main-content-scenariste">
        <div class="welcome-section">
          <h2>Bienvenue, {{ user?.nom }} !</h2>
          <p>Vous êtes connecté en tant que {{ user?.role }}</p>
        </div><br>

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
      editError: ''
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
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside);
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
    }
  }
};
</script>

<style scoped>
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