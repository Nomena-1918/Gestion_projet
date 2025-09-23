<template>
  <div class="creation-lieu-container">

    <!-- Contenu principal -->
    <main class="main-content">
      <div class="page-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Gestion des Lieux</h2>
      </div>

      <!-- Formulaire de création -->
      <div class="creation-form">
        <h3>{{ isEditing ? 'Modifier' : 'Ajouter' }} un lieu</h3>
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label>Projet *</label>
            <div class="combobox-container">
             <input
                type="text"
                v-model="projetSearch"
                :disabled="!!formData.projetId"  
                @focus="showProjetSuggestions = !formData.projetId"  
                @blur="hideProjetSuggestions"
                @input="filterProjets"
                :placeholder="formData.projetId ? getProjetName(formData.projetId) : 'Rechercher un projet...'"
                required
                class="combobox-input"
              />
              <ul v-if="!formData.projetId && showProjetSuggestions && filteredProjets.length" class="suggestions-list">                <li
                  v-for="projet in filteredProjets"
                  :key="projet.id"
                  @mousedown="selectProjet(projet)"
                  class="suggestion-item"
                >
                  {{ projet.titre }} ({{ projet.genreNom }})
                </li>
              </ul>
              <ul v-if="showProjetSuggestions && filteredProjets.length === 0" class="suggestions-list">
                <li class="suggestion-item no-results">Aucun projet trouvé</li>
              </ul>
            </div>
            <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
          </div>
          
          <div class="form-group">
            <label>Nom du lieu *</label>
            <input v-model="formData.nomLieu" type="text" required placeholder="Ex: Appartement principal, Rue de Paris...">
          </div>
          
          <div class="form-group">
            <label>Type de lieu *</label>
            <select v-model="formData.typeLieu" required>
              <option value="">Sélectionnez un type</option>
              <option value="Intérieur">Intérieur</option>
              <option value="Extérieur">Extérieur</option>
              <option value="Intérieur/Extérieur">Intérieur/Extérieur</option>
              <option value="Studio">Studio</option>
              <option value="Naturel">Naturel</option>
              <option value="Urbain">Urbain</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>Adresse</label>
            <textarea v-model="formData.adresse" rows="3" placeholder="Adresse complète du lieu..."></textarea>
          </div>
          
          <button type="submit" class="submit-btn">{{ isEditing ? 'Modifier' : 'Ajouter' }} le lieu</button>
          <button v-if="isEditing" type="button" @click="resetForm" class="cancel-btn">Annuler</button>
        </form>
      </div>

      <!-- Liste des lieux -->
      <div class="lieux-list">
        <h3>Liste des lieux</h3>
        
        <div class="filters">
          <div class="filter-group">
            <input v-model="searchTerm" type="text" placeholder="Rechercher par nom..." />
          </div>
          
          <div class="filter-group">
            <select v-model="filterProjetId">
              <option value="">Tous les projets</option>
              <option v-for="projet in projets" :key="projet.id" :value="projet.id">
                {{ projet.titre }}
              </option>
            </select>
          </div>
          
          <div class="filter-group">
            <select v-model="filterTypeLieu">
              <option value="">Tous les types</option>
              <option value="Intérieur">Intérieur</option>
              <option value="Extérieur">Extérieur</option>
              <option value="Intérieur/Extérieur">Intérieur/Extérieur</option>
              <option value="Studio">Studio</option>
              <option value="Naturel">Naturel</option>
              <option value="Urbain">Urbain</option>
            </select>
          </div>
        </div>

        <div v-if="loading" class="loading">Chargement des lieux...</div>
        
        <div v-else-if="filteredLieux.length === 0" class="no-data">
          Aucun lieu trouvé.
        </div>
        
        <div v-else class="lieux-grid">
          <div v-for="lieu in filteredLieux" :key="lieu.id" class="lieu-card">
            <div class="lieu-header">
              <h4>{{ lieu.nomLieu }}</h4>
              <div class="lieu-actions">
                <span class="icon-edit" @click="editLieu(lieu)">✏️</span>
                <span class="icon-delete" @click="deleteLieu(lieu.id)">🗑️</span>
                <span 
                  class="icon-scenes" 
                  @click="viewLieuScenes(lieu)"
                  title="Voir les scènes utilisant ce lieu"
                >
                  🎬
                </span>
              </div>
            </div>
            
            <div class="lieu-info">
              <p><strong>Projet:</strong> {{ lieu.projetTitre }}</p>
              <p><strong>Type:</strong> {{ lieu.typeLieu }}</p>
              <p v-if="lieu.adresse"><strong>Adresse:</strong> {{ lieu.adresse }}</p>
              <p><strong>Créé le:</strong> {{ formatDate(lieu.creeLe) }}</p>
              <p><strong>Scènes associées:</strong> {{ lieu.sceneCount || 0 }}</p>
            </div>
            
            <!-- Liste des scènes utilisant ce lieu -->
            <div v-if="lieu.scenes && lieu.scenes.length" class="scenes-section">
              <h5>Scènes utilisant ce lieu:</h5>
              <div v-for="scene in lieu.scenes" :key="scene.id" class="scene-item">
                <span>{{ scene.titre }} ({{ scene.descriptionUtilisation }})</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Modale pour afficher les scènes d'un lieu -->
      <div v-if="showScenesModal" class="modal-overlay" @click="showScenesModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>Scènes utilisant "{{ selectedLieu.nomLieu }}"</h3>
            <button class="modal-close" @click="showScenesModal = false">×</button>
          </div>
          
          <div class="scenes-list">
            <div v-if="selectedLieuScenes.length === 0" class="no-scenes">
              Aucune scène n'utilise ce lieu.
            </div>
            
            <div v-else>
              <div v-for="scene in selectedLieuScenes" :key="scene.id" class="scene-detail">
                <h4>{{ scene.sceneTitre }}</h4>
                <p><strong>Séquence:</strong> {{ scene.sequenceTitre }}</p>
                <p><strong>Épisode:</strong> {{ scene.episodeTitre }}</p>
                <p><strong>Projet:</strong> {{ scene.projetTitre }}</p>
                <p><strong>Utilisation:</strong> {{ scene.descriptionUtilisation }}</p>
              </div>
            </div>
          </div>
          
          <div class="modal-actions">
            <button type="button" @click="showScenesModal = false" class="cancel-btn">Fermer</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

export default {
  name: 'CreationLieu',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user')) || null,
      showProfileMenu: false,
      formData: {
        projetId: '',
        nomLieu: '',
        typeLieu: '',
        adresse: ''
      },
      isEditing: false,
      editingId: null,
      projets: [],
      lieux: [],
      searchTerm: '',
      filterProjetId: '',
      filterTypeLieu: '',
      loading: true,
      showScenesModal: false,
      selectedLieu: {},
      selectedLieuScenes: [],
      // Nouvelles données pour la zone de liste modifiable
      projetSearch: '',
      showProjetSuggestions: false,
      filteredProjets: []
    };
  },
  computed: {
    userInitials() {
      if (!this.user?.nom) return '';
      const names = this.user.nom.split(' ');
      return names.map(n => n[0]).join('').toUpperCase();
    },
    filteredLieux() {
      return this.lieux.filter(lieu => {
        const matchesSearch = lieu.nomLieu.toLowerCase().includes(this.searchTerm.toLowerCase());
        const matchesProjet = !this.filterProjetId || lieu.projetId === parseInt(this.filterProjetId);
        const matchesType = !this.filterTypeLieu || lieu.typeLieu === this.filterTypeLieu;
        return matchesSearch && matchesProjet && matchesType;
      });
    }
  },
  async created() {
    axios.defaults.baseURL = API_BASE_URL;
    
    axios.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );
    
    await this.loadProjets();
    await this.loadLieux();
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  watch: {
    // Mettre à jour la liste filtrée quand les projets changent
    projets: {
      handler(newVal) {
        this.filteredProjets = [...newVal];
      },
      deep: true
    }
  },
 mounted() {
  const projetId = this.$route.params.projetId || this.$route.params.id || this.$route.query.projetId;
  
  if (projetId) {
    this.formData.projetId = projetId;
    this.fetchProjetDetails(projetId);
  } else {
    console.warn('ID du projet non spécifié ; utilisation de la recherche manuelle.');
    // Laisser l'utilisateur sélectionner le projet via la combobox
  }
  
  this.loadProjets();
  this.loadLieux();
},
  methods: {
    async fetchProjetDetails(projetId) {
    try {
      const response = await axios.get(`/api/projets/${projetId}`);
      const projet = response.data;
      this.projetSearch = `${projet.titre} (${projet.genreNom || 'Inconnu'})`;  // Pré-remplit l'input
      // Optionnel : Stocke le projet complet si besoin pour getProjetName
      this.selectedProjet = projet;  // Ajoute une data 'selectedProjet: null' si nécessaire
    } catch (error) {
      console.error('Erreur lors du chargement du projet:', error);
      // Gère l'erreur, ex. this.errorMessage = 'Erreur de chargement du projet.';
    }
  },

  // Modifie selectProjet pour ne pas overrider si pré-rempli
  selectProjet(projet) {
    if (!this.formData.projetId) {  // Seulement si non pré-rempli
      this.formData.projetId = projet.id;
      this.projetSearch = `${projet.titre} (${projet.genreNom})`;
      this.showProjetSuggestions = false;
    }
  },
    async loadProjets() {
      try {
        const response = await axios.get('/api/projets');
        this.projets = response.data;
        this.filteredProjets = [...this.projets];
      } catch (error) {
        console.error('Erreur lors du chargement des projets:', error);
        alert('Erreur lors du chargement des projets');
      }
    },
    async loadLieux() {
      this.loading = true;
      try {
        const response = await axios.get('/api/lieux');
        this.lieux = response.data;
        
        // Charger le nombre de scènes pour chaque lieu
        for (let lieu of this.lieux) {
          try {
            const scenesResponse = await axios.get(`/api/scene-lieux/lieux/${lieu.id}`);
            lieu.sceneCount = scenesResponse.data.length;
            
            // Charger les premières scènes pour l'affichage
            if (scenesResponse.data.length > 0) {
              lieu.scenes = scenesResponse.data.slice(0, 3); // Afficher seulement 3 scènes
            }
          } catch (error) {
            console.error('Erreur lors du chargement des scènes du lieu:', error);
            lieu.sceneCount = 0;
            lieu.scenes = [];
          }
        }
      } catch (error) {
        console.error('Erreur lors du chargement des lieux:', error);
        alert('Erreur lors du chargement des lieux: ' + (error.response?.data?.message || error.message));
      } finally {
        this.loading = false;
      }
    },
    async submitForm() {
      try {
        const payload = {
          ...this.formData,
          projetId: parseInt(this.formData.projetId)
        };
        
        if (this.isEditing) {
          await axios.put(`/api/lieux/${this.editingId}`, payload);
          alert('Lieu modifié avec succès!');
        } else {
          await axios.post('/api/lieux', payload);
          alert('Lieu créé avec succès!');
        }
        
        this.resetForm();
        await this.loadLieux();
      } catch (error) {
        console.error('Erreur lors de la sauvegarde du lieu:', error);
        alert('Erreur: ' + (error.response?.data?.message || error.message));
      }
    },
    editLieu(lieu) {
      this.formData = {
        projetId: lieu.projetId.toString(),
        nomLieu: lieu.nomLieu,
        typeLieu: lieu.typeLieu,
        adresse: lieu.adresse || ''
      };
      this.projetSearch = this.getProjetName(lieu.projetId);
      this.isEditing = true;
      this.editingId = lieu.id;
      
      document.querySelector('.creation-form').scrollIntoView({ behavior: 'smooth' });
    },
    async deleteLieu(lieuId) {
      if (!confirm('Êtes-vous sûr de vouloir supprimer ce lieu ?')) {
        return;
      }
      
      try {
        await axios.delete(`/api/lieux/${lieuId}`);
        await this.loadLieux();
        alert('Lieu supprimé avec succès!');
      } catch (error) {
        console.error('Erreur lors de la suppression du lieu:', error);
        alert('Erreur: ' + (error.response?.data?.message || error.message));
      }
    },
    resetForm() {
      this.formData = {
        projetId: '',
        nomLieu: '',
        typeLieu: '',
        adresse: ''
      };
      this.projetSearch = '';
      this.isEditing = false;
      this.editingId = null;
      this.filteredProjets = [...this.projets];
    },
    formatDate(dateString) {
      if (!dateString) return '';
      return new Date(dateString).toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    async viewLieuScenes(lieu) {
      this.selectedLieu = lieu;
      try {
        const response = await axios.get(`/api/scene-lieux/lieux/${lieu.id}`);
        this.selectedLieuScenes = response.data;
        this.showScenesModal = true;
      } catch (error) {
        console.error('Erreur lors du chargement des scènes du lieu:', error);
        alert('Erreur lors du chargement des scènes');
      }
    },
    goBack() {
      this.$router.go(-1);    
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
    async loadScenesForProject() {
      // Cette méthode peut être utilisée pour charger les scènes d'un projet spécifique
      // si nécessaire pour des fonctionnalités futures
    },
    
    // Méthodes pour la zone de liste modifiable
    filterProjets() {
      const searchTerm = this.projetSearch.toLowerCase();
      if (!searchTerm) {
        this.filteredProjets = [...this.projets];
        return;
      }
      this.filteredProjets = this.projets.filter(projet => 
        projet.titre.toLowerCase().includes(searchTerm) ||
        (projet.genreNom && projet.genreNom.toLowerCase().includes(searchTerm))
      );
    },

    selectProjet(projet) {
      this.formData.projetId = projet.id.toString();
      this.projetSearch = `${projet.titre} (${projet.genreNom})`;
      this.showProjetSuggestions = false;
    },

    hideProjetSuggestions() {
      // Petit délai pour permettre la sélection avant de cacher
      setTimeout(() => {
        this.showProjetSuggestions = false;
      }, 200);
    },

    getProjetName(id) {
      const projet = this.projets.find(p => p.id === parseInt(id));
      return projet ? `${projet.titre} (${projet.genreNom})` : '';
    }
  }
};
</script>

<style scoped>
.creation-lieu-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 1rem;
}

.profile-section {
  position: relative;
}

.profile-icon {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.profile-icon:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.profile-avatar {
  width: 32px;
  height: 32px;
  background-color: white;
  color: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 0.5rem;
}

.profile-name {
  margin-right: 0.5rem;
}

.profile-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 280px;
  z-index: 1000;
  margin-top: 0.5rem;
  overflow: hidden;
}

.profile-info {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.profile-avatar-large {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.2rem;
  margin-right: 1rem;
}

.profile-details h3 {
  margin: 0 0 0.25rem 0;
  color: #333;
}

.profile-details p {
  margin: 0.25rem 0;
  color: #666;
}

.role-badge {
  background-color: #e9ecef;
  color: #495057;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  display: inline-block;
}

.profile-actions {
  padding: 1rem 1.5rem;
}

.logout-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
}

.back-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 1rem;
}

.creation-form {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.creation-form h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.form-group {
  margin-bottom: 1rem;
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #555;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

/* Styles pour la zone de liste modifiable */
.combobox-container {
  position: relative;
  width: 100%;
}

.combobox-input {
  width: 100%;
  box-sizing: border-box;
}

.suggestions-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-top: none;
  border-radius: 0 0 6px 6px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
  margin: 0;
  padding: 0;
  list-style: none;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.suggestion-item {
  padding: 10px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.suggestion-item:hover {
  background-color: #f0f8ff;
}

.suggestion-item.no-results {
  color: #888;
  cursor: default;
}

.suggestion-item.no-results:hover {
  background-color: white;
}

.submit-btn,
.cancel-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  margin-right: 1rem;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.cancel-btn {
  background: #6c757d;
  color: white;
}

.lieux-list {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.lieux-list h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.filter-group {
  flex: 1;
  min-width: 200px;
}

.filter-group input,
.filter-group select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.loading,
.no-data {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-style: italic;
}

.lieux-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.lieu-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1.5rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.lieu-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.lieu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.lieu-header h4 {
  margin: 0;
  color: #333;
}

.lieu-actions span {
  margin-left: 0.75rem;
  cursor: pointer;
  font-size: 1.1rem;
}

.icon-edit {
  color: #f39c12;
}

.icon-delete {
  color: #dc3545;
}

.icon-scenes {
  color: #3498db;
}

.lieu-info p {
  margin: 0.5rem 0;
  color: #666;
}

.scenes-section {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
}

.scenes-section h5 {
  margin: 0 0 0.5rem 0;
  color: #555;
}

.scene-item {
  padding: 0.5rem;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
  padding-bottom: 1rem;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
}

.scenes-list {
  margin-bottom: 1.5rem;
}

.no-scenes {
  text-align: center;
  color: #666;
  font-style: italic;
  padding: 2rem;
}

.scene-detail {
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 1rem;
  margin-bottom: 1rem;
}

.scene-detail h4 {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.scene-detail p {
  margin: 0.25rem 0;
  color: #666;
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .profile-section {
    margin-top: 1rem;
  }
  
  .filters {
    flex-direction: column;
  }
  
  .lieux-grid {
    grid-template-columns: 1fr;
  }
  
  .modal-content {
    width: 95%;
    padding: 1rem;
  }
}
</style>