
<template>
  <div class="creation-personnage">
    <h2>Création et Gestion des Personnages</h2>

      <div class="form-header">
        <button @click="goBack" class="back-btn">← Retour</button>
      </div>
    
    <!-- Formulaire de création -->
    <div class="form-container">
      <h3>{{ isEditing ? 'Modifier' : 'Créer' }} un personnage</h3>
      <form @submit.prevent="submitForm" class="personnage-form">
        <div class="form-group">
          <label for="nom">Nom du personnage *</label>
          <input
            type="text"
            id="nom"
            v-model="formData.nom"
            required
            placeholder="Entrez le nom du personnage"
          />
        </div>

        <div class="form-group">
          <label for="projet">Projet</label>
          <div class="combobox-container">
            <input
              type="text"
              id="projet"
              v-model="projetSearch"
              :disabled="!!formData.projetId"
              @focus="showProjetSuggestions = true"
              @blur="hideProjetSuggestions"
              @input="filterProjets"
              :placeholder="formData.projetId ? getProjetName(formData.projetId) : 'Rechercher un projet...'"
              required
              class="combobox-input"
            />
            <ul v-if="showProjetSuggestions && filteredProjets.length" class="suggestions-list">
              <li
                v-for="projet in filteredProjets"
                :key="projet.id"
                @mousedown="selectProjet(projet)"
                class="suggestion-item"
              >
                {{ projet.titre }}
              </li>
            </ul>
            <ul v-if="showProjetSuggestions && filteredProjets.length === 0" class="suggestions-list">
              <li class="suggestion-item no-results">Aucun projet trouvé</li>
            </ul>
          </div>
        </div>

        <div class="form-group">
          <label for="description">Description</label>
          <textarea
            id="description"
            v-model="formData.description"
            rows="4"
            placeholder="Décrivez le personnage..."
          ></textarea>
        </div>

        <div class="form-group">
          <label for="comedien">Comédien </label>
          <div class="combobox-container">
            <input
              type="text"
              id="comedien"
              v-model="comedienSearch"
              @focus="showComedienSuggestions = true"
              @blur="hideComedienSuggestions"
              @input="filterComediens"
              :placeholder="formData.comedienId ? getComedienName(formData.comedienId) : 'Rechercher un comédien (optionnel)...'"
              class="combobox-input"
            />
            <button
              type="button"
              v-if="formData.comedienId"
              @click="clearComedien"
              class="clear-button"
              title="Supprimer le comédien"
            >
              ×
            </button>
            <ul v-if="showComedienSuggestions && filteredComediens.length" class="suggestions-list">
              <li
                v-for="comedien in filteredComediens"
                :key="comedien.id"
                @mousedown="selectComedien(comedien)"
                class="suggestion-item"
              >
                {{ comedien.nom }} ({{ comedien.age }} ans)
              </li>
            </ul>
            <ul v-if="showComedienSuggestions && filteredComediens.length === 0" class="suggestions-list">
              <li class="suggestion-item no-results">Aucun comédien trouvé</li>
            </ul>
          </div>
        </div>

        <div class="form-actions">
          <button
            type="submit"
            :disabled="isSubmitting"
            class="btn-primary"
          >
            {{ isEditing ? 'Modifier' : 'Créer' }} le personnage
          </button>
          <button
            v-if="isEditing"
            type="button"
            @click="resetForm"
            class="btn-secondary"
          >
            Annuler
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CreationPersonnage',
  data() {
    return {
      formData: {
        nom: '',
        description: '',
        projetId: '',
        comedienId: null
      },
      projets: [],
      comediens: [],
      personnages: [],
      isEditing: false,
      editingId: null,
      isSubmitting: false,
      loading: false,
      // Nouvelles données pour les zones de liste modifiable
      projetSearch: '',
      comedienSearch: '',
      showProjetSuggestions: false,
      showComedienSuggestions: false,
      filteredProjets: [],
      filteredComediens: [],
      currentProjetId: null
    };
  },
  async mounted() {
    await this.loadProjets();
    await this.loadComediens();
    const projetId = this.$route.params.projetId || this.$route.params.id || this.$route.query.projetId;
    if (projetId) {
      this.currentProjetId = projetId;
      this.formData.projetId = projetId;
      this.fetchProjetDetails(projetId);  // Pré-remplit le nom
    } else {
      // Optionnel : Erreur si pas d'ID (mais garde la combobox active)
      console.warn('ID du projet non spécifié ; utilisation de la recherche manuelle.');
    }
    await this.loadPersonnages();
    // Initialiser les listes filtrées
    this.filteredProjets = [...this.projets];
    this.filteredComediens = [...this.comediens];
  },
  
  watch: {
    // Mettre à jour les listes filtrées quand les données changent
    projets: {
      handler(newVal) {
        this.filteredProjets = [...newVal];
      },
      deep: true
    },
    comediens: {
      handler(newVal) {
        this.filteredComediens = [...newVal];
      },
      deep: true
    }
  },
  methods: {
    async fetchProjetDetails(projetId) {
    try {
      const response = await axios.get(`/api/projets/${projetId}`);
      const projet = response.data;
      this.projetSearch = projet.titre;  // Pré-remplit l'input
    } catch (error) {
      console.error('Erreur lors du chargement du projet:', error);
      // Gère l'erreur, ex. this.errorMessage = 'Erreur de chargement du projet.';
    }
  },

  // Modifie selectProjet pour ne pas overrider si pré-rempli
  selectProjet(projet) {
    if (!this.formData.projetId) {  // Seulement si non pré-rempli
      this.formData.projetId = projet.id;
      this.projetSearch = projet.titre;
      this.showProjetSuggestions = false;
    }
  },
    async loadProjets() {
      try {
        const response = await axios.get('/api/projets');
        this.projets = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des projets:', error);
        alert('Erreur lors du chargement des projets');
      }
    },

    async loadComediens() {
      try {
        const response = await axios.get('/api/comediens');
        this.comediens = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des comédiens:', error);
        alert('Erreur lors du chargement des comédiens');
      }
    },

    async loadPersonnages() {
      this.loading = true;
      try {
        let url = '/api/personnages';
        if (this.currentProjetId) {
          url += `?projetId=${this.currentProjetId}`;
        }
        const response = await axios.get(url);
        this.personnages = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des personnages:', error);
        alert('Erreur lors du chargement des personnages');
      } finally {
        this.loading = false;
      }
    },

    async submitForm() {
      this.isSubmitting = true;
      try {
        if (this.isEditing) {
          await axios.put(`/api/personnages/${this.editingId}`, this.formData);
          alert('Personnage modifié avec succès');
        } else {
          await axios.post('/api/personnages', this.formData);
          alert('Personnage créé avec succès');
        }

        this.resetForm();
        await this.loadPersonnages();
      } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error);
        alert('Erreur lors de la sauvegarde: ' + (error.response?.data?.message || error.message));
      } finally {
        this.isSubmitting = false;
      }
    },

    editPersonnage(personnage) {
      this.formData = {
        nom: personnage.nom,
        description: personnage.description || '',
        projetId: personnage.projetId,
        comedienId: personnage.comedienId || null
      };
      this.isEditing = true;
      this.editingId = personnage.id;
      
      // Scroll to form
      document.querySelector('.form-container').scrollIntoView({ behavior: 'smooth' });
    },

    async deletePersonnage(id) {
      if (!confirm('Êtes-vous sûr de vouloir supprimer ce personnage ?')) {
        return;
      }

      try {
        await axios.delete(`/api/personnages/${id}`);
        alert('Personnage supprimé avec succès');
        await this.loadPersonnages();
      } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        alert('Erreur lors de la suppression');
      }
    },

    resetForm() {
      this.formData = {
        nom: '',
        description: '',
        projetId: this.currentProjetId || '',
        comedienId: null
      };
      this.projetSearch = this.currentProjetId ? this.getProjetName(this.currentProjetId) : '';
      this.comedienSearch = '';
      this.isEditing = false;
      this.editingId = null;
      this.filteredProjets = [...this.projets];
      this.filteredComediens = [...this.comediens];
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    goBack() {
      this.$router.go(-1);    
    },

    // Méthodes pour les zones de liste modifiable
    filterProjets() {
      const searchTerm = this.projetSearch.toLowerCase();
      if (!searchTerm) {
        this.filteredProjets = [...this.projets];
        return;
      }
      this.filteredProjets = this.projets.filter(projet => 
        projet.titre.toLowerCase().includes(searchTerm)
      );
    },

    filterComediens() {
      const searchTerm = this.comedienSearch.toLowerCase();
      if (!searchTerm) {
        this.filteredComediens = [...this.comediens];
        return;
      }
      this.filteredComediens = this.comediens.filter(comedien => 
        comedien.nom.toLowerCase().includes(searchTerm) || 
        comedien.age.toString().includes(searchTerm)
      );
    },

    selectComedien(comedien) {
      this.formData.comedienId = comedien.id;
      this.comedienSearch = `${comedien.nom} (${comedien.age} ans)`;
      this.showComedienSuggestions = false;
    },

    clearComedien() {
      this.formData.comedienId = null;
      this.comedienSearch = '';
      this.showComedienSuggestions = false;
    },

    hideProjetSuggestions() {
      // Petit délai pour permettre la sélection avant de cacher
      setTimeout(() => {
        this.showProjetSuggestions = false;
      }, 200);
    },

    hideComedienSuggestions() {
      // Petit délai pour permettre la sélection avant de cacher
      setTimeout(() => {
        this.showComedienSuggestions = false;
      }, 200);
    },

    getProjetName(id) {
      const projet = this.projets.find(p => p.id === id);
      return projet ? projet.titre : '';
    },

    getComedienName(id) {
      if (!id) return '';
      const comedien = this.comediens.find(c => c.id === id);
      return comedien ? `${comedien.nom} (${comedien.age} ans)` : '';
    }
  }
};
</script>

<style scoped>
.creation-personnage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  color: #2c3e50;
  margin-bottom: 30px;
  text-align: center;
}

.form-container {
  background: #f8f9fa;
  padding: 25px;
  border-radius: 10px;
  margin-bottom: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.personnage-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  position: relative;
}

.form-group:last-child {
  grid-column: 1 / -1;
}

label {
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
}

input, textarea {
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

input:focus, textarea:focus {
  outline: none;
  border-color: #3498db;
}

textarea {
  resize: vertical;
  min-height: 100px;
}

/* Styles pour les zones de liste modifiable */
.combobox-container {
  position: relative;
  width: 100%;
}

.combobox-input {
  width: 100%;
  box-sizing: border-box;
  padding-right: 30px; /* Espace pour le bouton si présent */
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

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}

.btn-primary {
  background: #3498db;
  color: white;
  padding: 12px 25px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s ease;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-primary:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
  padding: 12px 25px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s ease;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.personnages-list {
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.personnages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.personnage-card {
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  padding: 20px;
  background: white;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.personnage-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.personnage-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  border-bottom: 2px solid #f1f2f6;
  padding-bottom: 10px;
}

.personnage-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
}

.actions {
  display: flex;
  gap: 10px;
}

.btn-edit, .btn-delete {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 5px;
  border-radius: 4px;
  transition: background 0.2s ease;
}

.btn-edit:hover {
  background: #ffeaa7;
}

.btn-delete:hover {
  background: #ff7675;
}

.personnage-details p {
  margin: 8px 0;
  color: #555;
}

.date-info {
  font-size: 12px;
  color: #888;
  margin-top: 15px !important;
  font-style: italic;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #888;
  font-style: italic;
}

@media (max-width: 768px) {
  .personnage-form {
    grid-template-columns: 1fr;
  }
  
  .personnages-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn-primary, .btn-secondary {
    width: 100%;
  }
}

.clear-button {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: #ff6b6b;
  color: white;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-button:hover {
  background: #ee5253;
}
</style>
  