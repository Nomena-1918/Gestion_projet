<template>
  <div class="creation-dialogue-container">

    <!-- Contenu principal -->
    <main class="main-content">
      <div class="page-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Gestion des Dialogues</h2>
      </div>

      <!-- Formulaire de création -->
      <div class="creation-form">
        <h3>{{ isEditing ? 'Modifier' : 'Ajouter' }} un dialogue</h3>
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label>Scène</label>
            <input 
              :value="sceneTitle" 
              type="text" 
              disabled 
              placeholder="Nom de la scène" 
            />
            <input type="hidden" v-model="formData.sceneId" />
          </div>
          
          <div class="form-group">
            <label>Personnage</label>
            <select v-model="formData.personnageId">
              <option :value="null">Narration (sans personnage)</option>
              <option v-for="personnage in personnages" :key="personnage.id" :value="personnage.id">
                {{ personnage.nom }} ({{ personnage.projetTitre }})
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label>Texte *</label>
            <textarea v-model="formData.texte" required rows="4" placeholder="Entrez le texte du dialogue..."></textarea>
          </div>
          
          

          <div class="form-group">
            <label>Ordre *</label>
            <input 
              v-model="formData.ordre" 
              type="number" 
              min="1" 
              required
              @blur="validateOrder"
              :class="{ 'error-input': orderError }"
            >
            <div v-if="suggestedOrder" class="suggestion-text">
              Suggestion: Le prochain ordre disponible est {{ suggestedOrder }}
              <button type="button" @click="useSuggestedOrder" class="suggestion-btn">
                Utiliser cette valeur
              </button>
            </div>
            <div v-if="orderError" class="error-text">
              {{ orderError }}
            </div>
          </div>
          
          <div class="form-group">
            <label>Observation</label>
            <textarea v-model="formData.observation" rows="3" placeholder="Notes ou observations..."></textarea>
          </div>

                 
          <button type="submit" class="submit-btn">{{ isEditing ? 'Modifier' : 'Ajouter' }} le dialogue</button>
          <button v-if="isEditing" type="button" @click="resetForm" class="cancel-btn">Annuler</button>
        </form>
      </div>
      
    </main>
  </div>
</template>

<script>
import axios from 'axios';
import '../assets/css/dialogue.css';

export default {
  name: 'AddDialogueScene',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user')) || null,
      showProfileMenu: false,
      formData: {
        sceneId: '',
        personnageId: null,
        texte: '',
        ordre: 1,
        observation: '',
      },
      sceneTitle: '',
      scene: {}, // Stocker les détails complets de la scène (incluant sequence et episode)
      isEditing: false,
      editingId: null,
      scenes: [],
      personnages: [],
      searchTerm: '',
      filterSceneId: '',
      filterPersonnageId: '',
      loading: true,
      dialogues: [],
      newDialogueId: null,
      existingOrders: [], 
      suggestedOrder: null,
      orderError: '',
      originalOrder: null,
      projetId: null
    };
  },
  computed: {
    userInitials() {
      if (!this.user?.nom) return '';
      const names = this.user.nom.split(' ');
      return names.map(n => n[0]).join('').toUpperCase();
    },
    filteredDialogues() {
      return this.dialogues.filter(dialogue => {
        const matchesSearch = dialogue.texte.toLowerCase().includes(this.searchTerm.toLowerCase());
        const matchesScene = !this.filterSceneId || dialogue.sceneId === parseInt(this.filterSceneId);
        const matchesPersonnage = !this.filterPersonnageId || 
          (this.filterPersonnageId === 'null' && !dialogue.personnageId) || 
          dialogue.personnageId === parseInt(this.filterPersonnageId);
        return matchesSearch && matchesScene && matchesPersonnage;
      });
    },
    sceneId() {
      return this.$route.params.sceneId;
    }
  },
  watch: {
    // Charger les ordres existants quand la scène change
    'formData.sceneId': function(newSceneId) {
      if (newSceneId) {
        this.loadExistingOrders();
      } else {
        this.existingOrders = [];
        this.suggestedOrder = null;
        this.orderError = '';
      }
    }
  },
  async created() {
    // Configuration des intercepteurs axios
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

    await Promise.all([
      this.loadPersonnages(),
      this.loadDialogues()
    ]);
    this.loading = false;
    document.addEventListener('click', this.handleClickOutside);

    if (this.sceneId) {
      await this.loadSceneDetails();
      this.formData.sceneId = this.sceneId;
      this.loadExistingOrders();
    }
  },

  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
   async loadSceneDetails() {
      try {
        const response = await axios.get(`/api/scenes/${this.sceneId}`);
        this.scene = response.data;
        this.sceneTitle = this.scene.titre;

        // Récupérer l'ID du projet correctement
        if (this.scene.projetId) {
          this.projetId = this.scene.projetId;
        } else if (this.scene.sequenceId) {
          // Si projetId n'est pas directement dans la scène, chercher via la séquence
          try {
            const sequenceResponse = await axios.get(`/api/sequences/${this.scene.sequenceId}`);
            if (sequenceResponse.data.episodeId) {
              const episodeResponse = await axios.get(`/api/episodes/${sequenceResponse.data.episodeId}`);
              this.projetId = episodeResponse.data.projetId;
            }
          } catch (error) {
            console.error('Erreur lors de la récupération du projet:', error);
          }
        }

      } catch (error) {
        console.error('Erreur lors du chargement des détails de la scène:', error);
        this.sceneTitle = 'Scène inconnue';
      }
    },
    async loadPersonnages() {
      try {
        const response = await axios.get('/api/personnages');
        this.personnages = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des personnages:', error);
      }
    },
    async loadDialogues() {
      try {
        const response = await axios.get('/api/dialogues');
        this.dialogues = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des dialogues:', error);
      }
    },
    async loadExistingOrders() {
      if (!this.formData.sceneId) return;
      
      try {
        // Récupérer tous les dialogues de cette scène pour vérifier les ordres existants
        const response = await axios.get(`/api/dialogues/scene/${this.formData.sceneId}`);
        this.existingOrders = response.data.map(dialogue => dialogue.ordre);
        
        // Calculer le prochain ordre disponible
        this.calculateSuggestedOrder();
      } catch (error) {
        console.error('Erreur lors du chargement des ordres existants:', error);
      }
    },
    calculateSuggestedOrder() {
      if (this.existingOrders.length === 0) {
        this.suggestedOrder = 1;
      } else {
        // Trouver le plus grand ordre existant et ajouter 1
        const maxOrder = Math.max(...this.existingOrders);
        this.suggestedOrder = maxOrder + 1;
      }
      
      // Pré-remplir le champ avec la suggestion si on n'est pas en mode édition
      if (!this.isEditing) {
        this.formData.ordre = this.suggestedOrder;
      }
    },
    validateOrder() {
      if (!this.formData.ordre) {
        this.orderError = 'L\'ordre est requis';
        return;
      }
      
      if (this.formData.ordre < 1) {
        this.orderError = 'L\'ordre doit être un nombre positif';
        return;
      }
      
      // Vérifier si l'ordre existe déjà (sauf pour l'édition du dialogue courant)
      if (this.existingOrders.includes(this.formData.ordre) && 
          (!this.isEditing || this.formData.ordre !== this.originalOrder)) {
        this.orderError = `L'ordre ${this.formData.ordre} existe déjà dans cette scène`;
        return;
      }
      
      // Si tout est valide, effacer l'erreur
      this.orderError = '';
    },
    useSuggestedOrder() {
      this.formData.ordre = this.suggestedOrder;
      this.validateOrder();
    },
    
   async submitForm() {
      this.validateOrder();
      
      if (this.orderError) {
        alert('Veuillez corriger les erreurs avant de soumettre');
        return;
      }
      
      try {
        // Valider que sceneId est présent
        if (!this.formData.sceneId) {
          alert('Erreur: ID de scène manquant');
          return;
        }

        const payload = {
          sceneId: parseInt(this.formData.sceneId),
          personnageId: this.formData.personnageId ? parseInt(this.formData.personnageId) : null,
          texte: this.formData.texte,
          ordre: parseInt(this.formData.ordre),
          observation: this.formData.observation || '',
          
        };

        console.log('Payload envoyé:', payload); // Debug
        
        let response;
        if (this.isEditing) {
          response = await axios.put(`/api/dialogues/${this.editingId}`, payload);
          alert('Dialogue modifié avec succès!');
        } else {
          response = await axios.post('/api/dialogues', payload);
          this.newDialogueId = response.data.id;
          alert('Dialogue ajouté avec succès!');
          
          // Redirection après création - Vérifier que projetId est disponible
          if (this.projetId) {
            this.$router.push({
              path: `/projet/${this.projetId}/ecran-travail`,
              query: { 
                episodeId: this.scene.episode?.idEpisode || (this.scene.sequence ? this.scene.sequence.episodeId : null),
                sequenceId: this.scene.sequence?.idSequence || this.scene.sequenceId 
              }
            });
          } else {
            // Fallback si projetId n'est pas disponible
            this.$router.push('/scenariste');
          }
        }
        this.resetForm();
      } catch (error) {
        console.error('Erreur lors de la soumission du dialogue:', error);
        console.error('Détails de l\'erreur:', error.response?.data);
        alert('Erreur: ' + (error.response?.data?.message || error.message || 'Erreur inconnue'));
      }
    },

    resetForm() {
      this.formData = {
        sceneId: this.sceneId || '',
        personnageId: null,
        texte: '',
        ordre: 1,
        observation: ''
      };
      this.isEditing = false;
      this.editingId = null;
      this.originalOrder = null;
      this.existingOrders = [];
      this.suggestedOrder = null;
      this.orderError = '';
      
      // Recalculer l'ordre suggéré après réinitialisation
      if (this.sceneId) {
        this.loadExistingOrders();
      }
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
   goBack() {
      if (this.newDialogueId && !this.isEditing && this.projetId) {
        this.$router.push({
          path: `/projet/${this.projetId}/ecran-travail`,
          query: { 
            sequenceId: this.scene.sequence?.idSequence || this.scene.sequenceId 
          }
        });
      } else {
        this.$router.go(-1);
      }
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
  }
};
</script>

<style scoped>
.creation-dialogue-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Arial', sans-serif;
}

.main-content {
  padding: 20px 0;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.back-btn {
  background-color: #95a5a6;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background-color: #7f8c8d;
}

.page-header h2 {
  color: #2c3e50;
  font-size: 22px;
  margin: 0;
}

.creation-form {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.creation-form h3 {
  color: #2c3e50;
  font-size: 20px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-weight: 500;
  font-size: 14px;
}

.form-group select,
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background-color: white;
  box-sizing: border-box;
}

.form-group select:focus,
.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.submit-btn,
.cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-right: 10px;
}

.submit-btn {
  background-color: #3498db;
  color: white;
}

.submit-btn:hover {
  background-color: #2980b9;
}

.cancel-btn {
  background-color: #95a5a6;
  color: white;
}

.cancel-btn:hover {
  background-color: #7f8c8d;
}

.error-input {
  border-color: #e74c3c !important;
  box-shadow: 0 0 0 2px rgba(231, 76, 60, 0.2) !important;
}

.suggestion-text {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #3498db;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.suggestion-btn {
  background: transparent;
  color: #3498db;
  border: 1px solid #3498db;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s ease;
}

.suggestion-btn:hover {
  background: rgba(52, 152, 219, 0.1);
}

.error-text {
  margin-top: 0.5rem;
  color: #e74c3c;
  font-size: 0.9rem;
}

.color-selection {
  display: flex;
  align-items: center;
  gap: 10px;
}

.color-preview {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.color-input {
  width: 50px;
  height: 30px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.color-selection select {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

option {
  padding: 5px;
  font-weight: bold;
}

/* Responsive pour les sélecteurs de couleur */
@media (max-width: 768px) {
  .color-selection {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .color-preview, .color-input {
    align-self: flex-start;
  }
}

@media (max-width: 768px) {
  .creation-dialogue-container {
    padding: 15px;
  }

  .form-group select,
  .form-group input,
  .form-group textarea {
    font-size: 13px;
  }

  .submit-btn,
  .cancel-btn {
    width: 100%;
    margin-bottom: 10px;
  }
}

@media (max-width: 480px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .back-btn {
    width: 100%;
  }
}
</style>