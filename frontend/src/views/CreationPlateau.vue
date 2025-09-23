<template>
  <div class="creation-plateau-container">
    <!-- Contenu principal -->
    <main class="main-content">
      <div class="page-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Gestion des Plateaux</h2>
      </div>

      <!-- Formulaire de création -->
      <div class="creation-form">
        <h3>{{ isEditing ? 'Modifier' : 'Ajouter' }} un plateau</h3>
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label>Lieu *</label>
            <div class="combobox-container">
              <input
                type="text"
                v-model="lieuSearch"
                @focus="showLieuSuggestions = true"
                @blur="hideLieuSuggestions"
                @input="filterLieux"
                :placeholder="formData.lieuId ? getLieuName(formData.lieuId) : 'Rechercher un lieu...'"
                required
                class="combobox-input"
              />
              <ul v-if="showLieuSuggestions && filteredLieux.length" class="suggestions-list">
                <li
                  v-for="lieu in filteredLieux"
                  :key="lieu.id"
                  @mousedown="selectLieu(lieu)"
                  class="suggestion-item"
                >
                  {{ lieu.nomLieu }} - {{ lieu.projetTitre }}
                </li>
              </ul>
              <ul v-if="showLieuSuggestions && filteredLieux.length === 0" class="suggestions-list">
                <li class="suggestion-item no-results">Aucun lieu trouvé</li>
              </ul>
            </div>
          </div>
         
          
         <!-- <div class="form-group">
            <label>Scène (optionnel)</label>
            <select v-model="formData.sceneId">
              <option value="">Sélectionnez une scène</option>
              <option v-for="scene in scenesFiltrees" :key="scene.idScene" :value="scene.idScene">
                {{ scene.titre }} - {{ scene.sequenceTitre }}
              </option>
            </select>
          </div>  -->
          
          <div class="form-group">
            <label>Nom du plateau *</label>
            <input v-model="formData.nom" type="text" required placeholder="Ex: Plateau principal, Plateau extérieur...">
          </div>
          
          <div class="form-group">
            <label>Type de plateau *</label>
            <select v-model="formData.typePlateau" required>
              <option value="">Sélectionnez un type</option>
              <option value="Intérieur">Intérieur</option>
              <option value="Extérieur">Extérieur</option>
              <option value="Studio">Studio</option>
              <option value="Naturel">Naturel</option>
              <option value="Décor">Décor</option>
              <option value="Virtuel">Virtuel</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>Description</label>
            <textarea v-model="formData.description" rows="3" placeholder="Description du plateau..."></textarea>
          </div>
          
          <button type="submit" class="submit-btn">{{ isEditing ? 'Modifier' : 'Ajouter' }} le plateau</button>
          <button v-if="isEditing" type="button" @click="resetForm" class="cancel-btn">Annuler</button>
        </form>
      </div>

      <!-- Liste des plateaux -->
      <div class="plateaux-list">
        <h3>Liste des plateaux</h3>
        
        <div class="filters">
          <div class="filter-group">
            <input v-model="searchTerm" type="text" placeholder="Rechercher par nom..." />
          </div>
          
          <div class="filter-group">
            <select v-model="filterLieuId">
              <option value="">Tous les lieux</option>
              <option v-for="lieu in lieux" :key="lieu.id" :value="lieu.id">
                {{ lieu.nomLieu }}
              </option>
            </select>
          </div>
          
          <div class="filter-group">
            <select v-model="filterTypePlateau">
              <option value="">Tous les types</option>
              <option value="Intérieur">Intérieur</option>
              <option value="Extérieur">Extérieur</option>
              <option value="Studio">Studio</option>
              <option value="Naturel">Naturel</option>
              <option value="Décor">Décor</option>
              <option value="Virtuel">Virtuel</option>
            </select>
          </div>
        </div>

        <div v-if="loading" class="loading">Chargement des plateaux...</div>
        
        <div v-else-if="filteredPlateaux.length === 0" class="no-data">
          Aucun plateau trouvé.
        </div>
        
        <div v-else class="plateaux-grid">
          <div v-for="plateau in filteredPlateaux" :key="plateau.id" class="plateau-card">
            <div class="plateau-header">
              <h4>{{ plateau.nom }}</h4>
              <div class="plateau-actions">
                <span class="icon-edit" @click="editPlateau(plateau)">✏️</span>
                <span class="icon-delete" @click="deletePlateau(plateau.id)">🗑️</span>
              </div>
            </div>
            
            <div class="plateau-info">
              <p><strong>Type:</strong> {{ plateau.typePlateau }}</p>
              <p><strong>Lieu:</strong> {{ plateau.lieuNom }}</p>
              <p v-if="plateau.sceneTitre"><strong>Scène:</strong> {{ plateau.sceneTitre }}</p>
              <p v-if="plateau.sequenceTitre"><strong>Séquence:</strong> {{ plateau.sequenceTitre }}</p>
              <p v-if="plateau.episodeTitre"><strong>Épisode:</strong> {{ plateau.episodeTitre }}</p>
              <p v-if="plateau.projetTitre"><strong>Projet:</strong> {{ plateau.projetTitre }}</p>
              <p v-if="plateau.description"><strong>Description:</strong> {{ plateau.description }}</p>
              <p><strong>Créé le:</strong> {{ formatDate(plateau.creeLe) }}</p>
            </div>
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
  name: 'CreationPlateau',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user')) || null,
      showProfileMenu: false,
      formData: {
        lieuId: '',
        sceneId: '',
        nom: '',
        typePlateau: '',
        description: ''
      },
      isEditing: false,
      editingId: null,
      lieux: [],
      scenes: [],
      plateaux: [],
      searchTerm: '',
      filterLieuId: '',
      filterTypePlateau: '',
      loading: true,
      // Nouvelles données pour la zone de liste modifiable
      lieuSearch: '',
      showLieuSuggestions: false,
      filteredLieux: []
    };
  },
  computed: {
    userInitials() {
      if (!this.user?.nom) return '';
      const names = this.user.nom.split(' ');
      return names.map(n => n[0]).join('').toUpperCase();
    },
    filteredPlateaux() {
      return this.plateaux.filter(plateau => {
        const matchesSearch = plateau.nom.toLowerCase().includes(this.searchTerm.toLowerCase());
        const matchesLieu = !this.filterLieuId || plateau.lieuId === parseInt(this.filterLieuId);
        const matchesType = !this.filterTypePlateau || plateau.typePlateau === this.filterTypePlateau;
        return matchesSearch && matchesLieu && matchesType;
      });
    },
    scenesFiltrees() {
      if (!this.formData.lieuId) return [];
      
      const lieu = this.lieux.find(l => l.id === parseInt(this.formData.lieuId));
      if (!lieu) return [];
      
      return this.scenes.filter(scene => scene.projetId === lieu.projetId);
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
    
    await this.loadLieux();
    await this.loadScenes();
    await this.loadPlateaux();
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  watch: {
    // Mettre à jour la liste filtrée quand les lieux changent
    lieux: {
      handler(newVal) {
        this.filteredLieux = [...newVal];
      },
      deep: true
    }
  },
  methods: {
    async loadLieux() {
      try {
        const response = await axios.get('/api/lieux');
        this.lieux = response.data;
        this.filteredLieux = [...this.lieux];
      } catch (error) {
        console.error('Erreur lors du chargement des lieux:', error);
        alert('Erreur lors du chargement des lieux');
      }
    },
    async loadScenes() {
      try {
        const response = await axios.get('/api/scenes');
        this.scenes = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des scènes:', error);
        alert('Erreur lors du chargement des scènes');
      }
    },
    async loadPlateaux() {
      this.loading = true;
      try {
        const response = await axios.get('/api/plateaux');
        this.plateaux = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des plateaux:', error);
        alert('Erreur lors du chargement des plateaux: ' + (error.response?.data?.message || error.message));
      } finally {
        this.loading = false;
      }
    },
    async submitForm() {
      try {
        const payload = {
          ...this.formData,
          lieuId: parseInt(this.formData.lieuId),
          sceneId: this.formData.sceneId ? parseInt(this.formData.sceneId) : null
        };
        
        if (this.isEditing) {
          await axios.put(`/api/plateaux/${this.editingId}`, payload);
          alert('Plateau modifié avec succès!');
        } else {
          await axios.post('/api/plateaux', payload);
          alert('Plateau créé avec succès!');
        }
        
        this.resetForm();
        await this.loadPlateaux();
      } catch (error) {
        console.error('Erreur lors de la sauvegarde du plateau:', error);
        alert('Erreur: ' + (error.response?.data?.message || error.message));
      }
    },
    editPlateau(plateau) {
      this.formData = {
        lieuId: plateau.lieuId.toString(),
        sceneId: plateau.sceneId ? plateau.sceneId.toString() : '',
        nom: plateau.nom,
        typePlateau: plateau.typePlateau,
        description: plateau.description || ''
      };
      this.lieuSearch = this.getLieuName(plateau.lieuId);
      this.isEditing = true;
      this.editingId = plateau.id;
      
      document.querySelector('.creation-form').scrollIntoView({ behavior: 'smooth' });
    },
    async deletePlateau(plateauId) {
      if (!confirm('Êtes-vous sûr de vouloir supprimer ce plateau ?')) {
        return;
      }
      
      try {
        await axios.delete(`/api/plateaux/${plateauId}`);
        await this.loadPlateaux();
        alert('Plateau supprimé avec succès!');
      } catch (error) {
        console.error('Erreur lors de la suppression du plateau:', error);
        alert('Erreur: ' + (error.response?.data?.message || error.message));
      }
    },
    resetForm() {
      this.formData = {
        lieuId: '',
        sceneId: '',
        nom: '',
        typePlateau: '',
        description: ''
      };
      this.lieuSearch = '';
      this.isEditing = false;
      this.editingId = null;
      this.filteredLieux = [...this.lieux];
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
    onLieuChange() {
      // Réinitialiser la sélection de scène quand le lieu change
      this.formData.sceneId = '';
    },
    goBack() {
      this.$router.push('/scenariste');
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
    
    // Méthodes pour la zone de liste modifiable
    filterLieux() {
      const searchTerm = this.lieuSearch.toLowerCase();
      if (!searchTerm) {
        this.filteredLieux = [...this.lieux];
        return;
      }
      this.filteredLieux = this.lieux.filter(lieu => 
        lieu.nomLieu.toLowerCase().includes(searchTerm) ||
        lieu.projetTitre.toLowerCase().includes(searchTerm)
      );
    },

    selectLieu(lieu) {
      this.formData.lieuId = lieu.id.toString();
      this.lieuSearch = `${lieu.nomLieu} - ${lieu.projetTitre}`;
      this.showLieuSuggestions = false;
      this.onLieuChange(); // Appeler pour réinitialiser la scène si nécessaire
    },

    hideLieuSuggestions() {
      // Petit délai pour permettre la sélection avant de cacher
      setTimeout(() => {
        this.showLieuSuggestions = false;
      }, 200);
    },

    getLieuName(id) {
      const lieu = this.lieux.find(l => l.id === parseInt(id));
      return lieu ? `${lieu.nomLieu} - ${lieu.projetTitre}` : '';
    }
  }
};
</script>

<style scoped>
.creation-plateau-container {
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

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.cancel-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  margin-left: 0.5rem;
}

.plateaux-list {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.plateaux-list h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.filters {
  margin-bottom: 1.5rem;
  display: flex;
  gap: 1rem;
}

.filter-group input,
.filter-group select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 300px;
}

.plateaux-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.plateau-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1.5rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.plateau-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.plateau-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.plateau-header h4 {
  margin: 0;
  color: #333;
}

.plateau-actions span {
  margin-left: 0.75rem;
  cursor: pointer;
  font-size: 1.1rem;
}

.icon-delete {
  color: #dc3545;
}

.plateau-info p {
  margin: 0.5rem 0;
  color: #666;
}

.loading {
  text-align: center;
  color: #666;
  padding: 2rem;
}

.no-data {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 2rem;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .profile-section {
    margin-top: 1rem;
  }
  
  .plateaux-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-group input,
  .filter-group select {
    width: 100%;
  }
  
  .filters {
    flex-direction: column;
  }
}

</style>

