<template>
  <div class="add-sequence-container">

    <!-- Contenu principal -->
    <main class="main-content">
      <div class="sequence-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Ajouter une nouvelle séquence</h2>
      </div>

      <!-- Formulaire d'ajout de séquence -->
      <div class="add-sequence-form">
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label for="titre">Titre de la séquence *</label>
            <input
              id="titre"
              v-model="formData.titre"
              type="text"
              required
              placeholder="Entrez le titre de la séquence"
            />
          </div>

          <div class="form-group">
            <label for="ordre">Ordre dans l'épisode *</label>
            <input
              id="ordre"
              v-model="formData.ordre"
              type="number"
              min="1"
              required
              placeholder="Numéro d'ordre"
              :class="{ 'error-input': ordreError }"
              @blur="validateOrdre"
            />
            <span v-if="ordreError" class="error-text">{{ ordreError }}</span>
            <span v-if="suggestedOrdre" class="suggestion-text">
              Suggestion: Le prochain ordre disponible est {{ suggestedOrdre }}
               <button type="button" @click="useSuggestedOrder" class="suggestion-btn">
                Utiliser cette valeur
            </button>
            </span>
          </div>

          <div class="form-group">
            <label for="synopsis">Synopsis *</label>
            <textarea
              id="synopsis"
              v-model="formData.synopsis"
              required
              rows="5"
              placeholder="Décrivez le contenu de cette séquence"
            ></textarea>
          </div>

          <div class="form-group">
            <label for="episode">Épisode lié</label>
            <input
              id="episode"
              :value="episode.titre"
              type="text"
              disabled
            />
          </div>

          <div class="form-group">
            <label for="statut">Statut *</label>
            <select
              id="statut"
              v-model="formData.statutId"
              required
            >
              <option value="" disabled>Sélectionnez un statut</option>
              <option 
                v-for="statut in statutsSequence" 
                :key="statut.id" 
                :value="statut.id"
              >
                {{ statut.nomStatutsSequence }}
              </option>
            </select>
          </div>

          <div class="form-actions">
            <button 
              type="submit" 
              class="submit-btn"
              :disabled="loading || ordreError !== ''"
            >
              {{ loading ? 'Création en cours...' : 'Créer la séquence' }}
            </button>
            <button 
              type="button" 
              @click="goBack" 
              class="cancel-btn"
            >
              Annuler
            </button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user')) || null,
      showProfileMenu: false,
      episode: {},
      statutsSequence: [],
      existingOrders: [], // Liste des ordres existants
      formData: {
        titre: '',
        ordre: null,
        synopsis: '',
        statutId: null
      },
      loading: false,
      ordreError: '', // Erreur spécifique à l'ordre
      suggestedOrdre: null // Ordre suggéré
    };
  },
  computed: {
    userInitials() {
      if (!this.user?.nom) return '';
      const names = this.user.nom.split(' ');
      return names.map(n => n[0]).join('').toUpperCase();
    },
    episodeId() {
      return this.$route.params.episodeId || this.$route.params.id;
    }
  },
  async created() {
    await this.loadEpisode();
    await this.loadStatutsSequence();
    await this.fetchExistingSequences(); // Charger les séquences existantes
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    async loadEpisode() {
      try {
        const response = await axios.get(`/api/episodes/${this.episodeId}`);
        this.episode = response.data;

      } catch (error) {
        console.error('Erreur lors du chargement de l\'épisode:', error);
        alert('Impossible de charger les détails de l\'épisode');
      }
    },
    async loadStatutsSequence() {
      try {
        const response = await axios.get('/api/statuts-sequence');
        this.statutsSequence = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des statuts:', error);
        alert('Impossible de charger les statuts disponibles');
      }
    },
    async fetchExistingSequences() {
      try {
        const response = await axios.get(`/api/sequences/episodes/${this.episodeId}`);
        
        // Récupérer tous les ordres existants
        this.existingOrders = response.data.map(sequence => sequence.ordre);
        
        // Calculer le prochain ordre disponible
        this.calculateSuggestedOrdre();
        
      } catch (error) {
        console.error('Erreur lors du chargement des séquences existantes:', error);
      }
    },
    calculateSuggestedOrdre() {
      if (this.existingOrders.length === 0) {
        this.suggestedOrdre = 1;
        this.formData.ordre = 1;
        return;
      }
      
      // Trouver le plus grand ordre existant
      const maxOrder = Math.max(...this.existingOrders);
      this.suggestedOrdre = maxOrder + 1;
      
      // Pré-remplir avec l'ordre suggéré
      this.formData.ordre = this.suggestedOrdre;
    },
    validateOrdre() {
      if (!this.formData.ordre) {
        this.ordreError = 'L\'ordre est requis';
        return;
      }
      
      const orderNum = parseInt(this.formData.ordre);
      
      if (orderNum < 1) {
        this.ordreError = 'L\'ordre doit être au moins 1';
        return;
      }
      
      // Vérifier si l'ordre existe déjà
      if (this.existingOrders.includes(orderNum)) {
        this.ordreError = `L'ordre ${orderNum} existe déjà pour cet épisode. Veuillez choisir un autre numéro.`;
        return;
      }
      
      this.ordreError = '';
    },
    async submitForm() {
      // Valider l'ordre avant soumission
      this.validateOrdre();
      if (this.ordreError) {
        return;
      }
      
      this.loading = true;
      try {
        await axios.post(`/api/sequences/episodes/${this.episodeId}`, this.formData);
        
        alert('Séquence créée avec succès!');
        this.goBackToEpisodeDetails();
      } catch (error) {
        console.error('Erreur lors de la création de la séquence:', error);
        
        // Gestion spécifique des erreurs de duplication d'ordre
        if (error.response?.status === 400 && error.response?.data?.message?.includes('ordre')) {
          this.ordreError = 'Cet ordre existe déjà pour cet épisode. Veuillez choisir un autre numéro.';
        } else {
          alert('Erreur lors de la création de la séquence: ' + 
            (error.response?.data?.message || 'Veuillez réessayer'));
        }
      } finally {
        this.loading = false;
      }
    },
    goBack() {
      this.$router.go(-1);
    },
    goBackToEpisodeDetails() {
      this.$router.push(`/episode/${this.episodeId}/detail-episode`);
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
    }
  }
};
</script>

<style scoped>
/* Import Font Awesome pour les icônes */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');
/* Conteneur principal */
.add-sequence-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a2639 0%, #0f172a 100%); /* Bleu nuit */
  padding: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: fadeIn 1s ease-in-out;
}

/* Contenu principal */
.main-content {
  max-width: 800px;
  width: 100%;
  background: rgba(30, 41, 59, 0.3); /* Bleu nuit semi-transparent */
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  margin-top: 2rem;
  animation: slideIn 0.7s ease-out;
}

/* En-tête du formulaire */
.sequence-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.sequence-header h2 {
  color: #e2e8f0; /* Gris clair */
  margin: 0;
  font-size: 2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.back-btn {
  background: linear-gradient(135deg, #4b5563 0%, #374151 100%); /* Gris bleu */
  color: #ffffff;
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.back-btn:hover {
  transform: scale(1.05);
  background: linear-gradient(135deg, #374151 0%, #4b5563 100%);
}

/* Formulaire */
.add-sequence-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.add-sequence-form form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 500;
  margin-bottom: 0.5rem;
  color: #e2e8f0; /* Gris clair */
  font-size: 1.1rem;
}

.form-group input,
.form-group textarea,
.form-group select {
  padding: 0.8rem;
  border: none;
  border-radius: 10px;
  background: rgba(30, 41, 59, 0.5); /* Bleu nuit sombre */
  color: #e2e8f0;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: #94a3b8; /* Gris bleu pâle */
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  background: rgba(30, 41, 59, 0.7);
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.5); /* Halo bleu */
}

.form-group textarea {
  min-height: 120px;
  resize: vertical;
}

.form-group input[disabled] {
  background: rgba(30, 41, 59, 0.7);
  color: #94a3b8;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.submit-btn {
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%); /* Bleu nuit */
  color: #ffffff;
  padding: 0.8rem 1.8rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: scale(1.05);
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-btn {
  background: linear-gradient(135deg, #4b5563 0%, #374151 100%); /* Gris bleu */
  color: #ffffff;
  padding: 0.8rem 1.8rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  transform: scale(1.05);
  background: linear-gradient(135deg, #374151 0%, #4b5563 100%);
}

/* Styles pour les messages d'erreur et suggestions */
.error-input {
  border: 2px solid #ef4444 !important;
}

.error-text {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
}

.suggestion-text {
  color:  #93c5fd; 
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
  font-style: italic;
}

.suggestion-btn {
  background: transparent;
  color: #93c5fd;
  border: 1px solid #93c5fd;
  padding: 0.2rem 0.5rem;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s ease;
}

.suggestion-btn:hover {
  background: rgba(147, 197, 253, 0.1);
}

/* Animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from { transform: translateY(-30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

/* Responsive */
@media (max-width: 768px) {
  .add-sequence-container {
    padding: 1rem;
  }

  .main-content {
    padding: 1.2rem;
  }

  .form-actions {
    flex-direction: column;
  }

  .submit-btn,
  .cancel-btn {
    width: 100%;
  }
}
</style>