<template>
  <div class="scene-comedien-container">
    <!-- Contenu principal -->
    <main class="main-content">
      <div class="page-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Lier Comédien à une Scène</h2>
      </div>

      <!-- Formulaire de liaison -->
      <div class="liaison-form">
        <h3>Associer un comédien à une scène</h3>
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label>Comédien:</label>
            <!-- Champ comédien en lecture seule avec le nom pré-rempli -->
            <input 
              :value="selectedComedienName" 
              type="text" 
              disabled 
              class="comedien-disabled-input"
            />
            <input 
              v-model="newLiaison.idComedien" 
              type="hidden" 
            />
          </div>
          
          <div class="form-group">
            <label>Scène:</label>
            <!-- Zone de liste modifiable avec recherche -->
            <div class="combobox-container">
              <input
                v-model="sceneSearch"
                @input="filterScenes"
                @focus="showSceneDropdown = true"
                @blur="onSceneInputBlur"
                placeholder="Rechercher une scène..."
                class="combobox-input"
                required
              />
              <div v-if="showSceneDropdown" class="combobox-dropdown">
                <div
                  v-for="scene in filteredScenesList"
                  :key="scene.idScene"
                  @mousedown="selectScene(scene)"
                  class="combobox-option"
                  :class="{ 'selected': newLiaison.idScene === scene.idScene }"
                >
                  {{ scene.titre }} - {{ scene.sequenceTitre }} ({{ scene.statutNom }})
                </div>
                <div v-if="filteredScenesList.length === 0" class="combobox-no-results">
                  Aucune scène trouvée
                </div>
              </div>
            </div>
            <input 
              v-model="newLiaison.idScene" 
              type="hidden" 
            />
          </div>
          
          <button type="submit" class="submit-btn">Associer</button>
        </form>
      </div>

      <!-- Liste des associations -->
  <div class="liaisons-list">
        <h3>Associations Comédien-Scène</h3>
        
        <div class="filters">
          <div class="filter-group">
            <input v-model="searchTerm" type="text" placeholder="Rechercher par nom..." />
          </div>
        </div>

        <!-- Groupement par comédien -->
        <div class="comedien-group" v-for="comedien in groupedLiaisons" :key="comedien.id">
          <div class="comedien-header">
            <h4>{{ comedien.nom }}</h4>
            <span class="scene-count">{{ comedien.scenes.length }} scène(s)</span>
          </div>
          
          <div class="scenes-container">
            <div v-for="scene in comedien.scenes" :key="scene.id" class="scene-card">
              <div class="scene-info">
                <p><strong>Scène:</strong> {{ scene.sceneTitre }}</p>
                <p><strong>Séquence:</strong> {{ scene.sequenceTitre }}</p>
                <p><strong>Statut:</strong> {{ scene.sceneStatut }}</p>
                <p><strong>Créé le:</strong> {{ formatDate(scene.creeLe) }}</p>
              </div>
              <div class="scene-actions">
                <button @click="deleteLiaison(scene.id)" class="btn-delete-scene" title="Supprimer">
                  🗑️
                </button>
              </div>
            </div>  
            </div>
          </div>
            <div v-if="groupedLiaisons.length === 0" class="empty-state">
            Aucune association comédien-scène trouvée.
            </div>
        </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios';
import { API_BASE_URL } from '@/config/api';

export default {
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user')) || null,
      showProfileMenu: false,
      newLiaison: {
        idComedien: '',
        idScene: ''
      },
      selectedComedien: null,
      scenes: [],
      filteredScenesList: [],
      liaisons: [],
      searchTerm: '',
      sceneSearch: '',
      showSceneDropdown: false,
      selectedScene: null
    };
  },
  computed: {
    userInitials() {
      if (!this.user?.nom) return '';
      const names = this.user.nom.split(' ');
      return names.map(n => n[0]).join('').toUpperCase();
    },
    filteredLiaisons() {
      if (!this.searchTerm) return this.liaisons;
      return this.liaisons.filter(liaison =>
        liaison.comedienNom.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        liaison.sceneTitre.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    },
    selectedComedienName() {
      if (!this.selectedComedien) return 'Chargement...';
      return `${this.selectedComedien.nom} (${this.selectedComedien.email})`;
    },
    groupedLiaisons() {
    const grouped = {};
    
    this.filteredLiaisons.forEach(liaison => {
      if (!grouped[liaison.idComedien]) {
        grouped[liaison.idComedien] = {
          id: liaison.idComedien,
          nom: liaison.comedienNom,
          scenes: []
        };
      }
      grouped[liaison.idComedien].scenes.push(liaison);
    });
    
    return Object.values(grouped);
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
    
    // Récupérer l'ID du comédien depuis l'URL
    const comedienId = this.$route.query.comedienId;
    if (comedienId) {
      this.newLiaison.idComedien = comedienId;
      await this.loadSelectedComedien(comedienId);
    }
    
    await this.loadData();
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    async loadData() {
      try {
        await Promise.all([
          this.loadScenes(),
          this.loadLiaisons()
        ]);
        // Initialiser la liste filtrée avec toutes les scènes
        this.filteredScenesList = this.scenes;
      } catch (error) {
        console.error('Erreur lors du chargement des données:', error);
        alert('Erreur lors du chargement des données');
      }
    },
    
    async loadSelectedComedien(comedienId) {
      try {
        const response = await axios.get(`/api/comediens/${comedienId}`);
        this.selectedComedien = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement du comédien:', error);
        alert('Comédien non trouvé');
        this.goBack();
      }
    },
    
    async loadScenes() {
      try {
        const response = await axios.get('/api/scenes');
        this.scenes = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des scènes:', error);
      }
    },
    
    async loadLiaisons() {
      try {
        const response = await axios.get('/api/comedien-scene');
        this.liaisons = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des liaisons:', error);
      }
    },
    
    filterScenes() {
      if (!this.sceneSearch) {
        this.filteredScenesList = this.scenes;
        return;
      }
      
      const searchTerm = this.sceneSearch.toLowerCase();
      this.filteredScenesList = this.scenes.filter(scene =>
        scene.titre.toLowerCase().includes(searchTerm) ||
        scene.sequenceTitre.toLowerCase().includes(searchTerm) ||
        scene.statutNom.toLowerCase().includes(searchTerm)
      );
    },
    
    selectScene(scene) {
      this.newLiaison.idScene = scene.idScene;
      this.selectedScene = scene;
      this.sceneSearch = `${scene.titre} - ${scene.sequenceTitre} (${scene.statutNom})`;
      this.showSceneDropdown = false;
    },
    
    onSceneInputBlur() {
      // Fermer le dropdown après un court délai pour permettre la sélection
      setTimeout(() => {
        this.showSceneDropdown = false;
      }, 200);
    },
    
    async submitForm() {
      if (!this.newLiaison.idScene) {
        alert('Veuillez sélectionner une scène');
        return;
      }
      
      try {
        await axios.post('/api/comedien-scene', this.newLiaison);
        this.resetForm();
        await this.loadLiaisons();
        alert('Association créée avec succès!');
      } catch (error) {
        console.error('Erreur lors de la création de l\'association:', error);
        alert('Erreur: ' + (error.response?.data?.message || error.message));
      }
    },
    
    resetForm() {
      // Garder l'ID du comédien mais réinitialiser la scène
      this.newLiaison.idScene = '';
      this.sceneSearch = '';
      this.selectedScene = null;
      this.filteredScenesList = this.scenes;
    },
    
    async deleteLiaison(liaisonId) {
      if (confirm('Êtes-vous sûr de vouloir supprimer cette association ?')) {
        try {
          await axios.delete(`/api/comedien-scene/${liaisonId}`);
          await this.loadLiaisons();
          alert('Association supprimée avec succès!');
        } catch (error) {
          console.error('Erreur lors de la suppression:', error);
          alert('Erreur: ' + (error.response?.data?.message || error.message));
        }
      }
    },
    
    formatDate(date) {
      if (!date) return '';
      return new Date(date).toLocaleDateString('fr-FR');
    },
    
    goBack() {
      this.$router.push('/creation-comedien');
    },
    
    toggleProfileMenu() {
      this.showProfileMenu = !this.showProfileMenu;
    },
    
    handleClickOutside(event) {
      if (!event.target.closest('.profile-section')) {
        this.showProfileMenu = false;
      }
      if (!event.target.closest('.combobox-container')) {
        this.showSceneDropdown = false;
      }
    },
    
    seDeconnecter() {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      this.$router.push('/');
    }
  }
};
</script>


<style scoped>
.scene-comedien-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.comedien-disabled-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f8f9fa;
  color: #495057;
  cursor: not-allowed;
}

.combobox-container {
  position: relative;
  width: 100%;
}

.combobox-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.combobox-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.combobox-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  max-height: 200px;
  overflow-y: auto;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  margin-top: 2px;
}

.combobox-option {
  padding: 0.75rem;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.combobox-option:hover {
  background-color: #f8f9fa;
}

.combobox-option.selected {
  background-color: #e3f2fd;
  font-weight: 500;
}

.combobox-no-results {
  padding: 0.75rem;
  color: #6c757d;
  font-style: italic;
  text-align: center;
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

.liaison-form {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.liaison-form h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #555;
}

.form-group select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
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

.liaisons-list {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.liaisons-list h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.filters {
  margin-bottom: 1.5rem;
}

.filter-group input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 300px;
}

.liaisons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.liaison-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1.5rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.liaison-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.liaison-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.liaison-header h4 {
  margin: 0;
  color: #333;
}

.liaison-actions span {
  margin-left: 0.75rem;
  cursor: pointer;
  font-size: 1.1rem;
}

.icon-delete {
  color: #dc3545;
}

.liaison-info p {
  margin: 0.5rem 0;
  color: #666;
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

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .profile-section {
    margin-top: 1rem;
  }
  
  .liaisons-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-group input {
    width: 100%;
  }
}
.comedien-group {
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  overflow: hidden;
}

.comedien-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comedien-header h4 {
  margin: 0;
  font-size: 1.2rem;
}

.scene-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.9rem;
}

.scenes-container {
  padding: 1rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.scene-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: transform 0.2s, box-shadow 0.2s;
}

.scene-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.scene-info {
  flex: 1;
}

.scene-info p {
  margin: 0.25rem 0;
  font-size: 0.9rem;
  color: #555;
}

.scene-actions {
  margin-left: 1rem;
}

.btn-delete-scene {
  background: #ff6b6b;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0.5rem;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.2s;
}

.btn-delete-scene:hover {
  background: #ee5253;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
  font-style: italic;
}

/* Responsive */
@media (max-width: 768px) {
  .scenes-container {
    grid-template-columns: 1fr;
  }
  
  .comedien-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .scene-card {
    flex-direction: column;
  }
  
  .scene-actions {
    margin-left: 0;
    margin-top: 1rem;
    align-self: flex-end;
  }
}
</style>