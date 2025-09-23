<template>
  <div class="add-episode-container">
    <!-- Contenu principal -->
    <main class="main-content">
      <div class="form-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Créer votre épisode</h2>
      </div>

      <form @submit.prevent="submitForm" class="episode-form">
        <div class="form-group">
          <label for="titre">Titre de l'épisode *</label>
          <input 
            type="text" 
            id="titre"
            v-model="form.titre" 
            required 
            placeholder="Entrez le titre"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="ordre">Ordre dans le projet *</label>
          <input 
            type="number" 
            id="ordre"
            v-model="form.ordre" 
            required 
            placeholder="Entrez le nombre"
            min="1"
            class="form-input"
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
          <label for="projet">Titre du projet *</label>
          <input 
            id="projet"
            :value="projetTitre" 
            type="text"
            disabled
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="statut">Statut *</label>
          <select 
            id="statut"
            v-model="form.statutId" 
            required
            class="form-select"
          >
            <option value="">Sélectionner le statut</option>
            <option v-for="statut in statutsEpisode" :key="statut.idStatutEpisode" :value="statut.idStatutEpisode">
              {{ statut.nomStatutsEpisode }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="synopsis">Synopsis</label>
          <textarea 
            id="synopsis"
            v-model="form.synopsis" 
            rows="6"
            placeholder="Entrez le synopsis"
            class="form-textarea"
          ></textarea>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

       <div class="form-actions">
          <button type="button" @click="goBack" class="cancel-btn">Annuler</button>
          <button type="submit" class="submit-btn" :disabled="loading || ordreError !== ''">
            {{ loading ? 'Création en cours...' : 'Créer' }}
          </button>
        </div>
      </form>
    </main>
  </div>
</template>

<script>
import axios from 'axios';
import '../assets/css/acceuil.css';

export default {
  name: 'AddEpisodeView',
  data() {
    return {
      showProfileMenu: false,
      user: null,
      projetTitre: '',
      statutsEpisode: [],
      existingOrders: [], // Liste des ordres existants
      form: {
        titre: '',
        ordre: '',
        projetId: this.$route.params.id || this.$route.params.projetId || '',
        statutId: '',
        synopsis: ''
      },
      loading: false,
      errorMessage: '',
      ordreError: '', // Erreur spécifique à l'ordre
      suggestedOrdre: null // Ordre suggéré
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
    }
  },
  mounted() {
    this.loadUser();
    this.fetchProjetDetails();
    this.fetchStatutsEpisode();
    this.fetchExistingEpisodes();
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
      } else {
        this.$router.push('/');
      }
    },
    async fetchProjetDetails() {
      try {
        const projetId = this.$route.params.id || this.$route.params.projetId || this.$route.query.projetId;
        if (!projetId) {
          this.errorMessage = 'ID du projet non spécifié';
          return;
        }
        
        const response = await axios.get(`/api/projets/${projetId}`);
        this.projetTitre = response.data.titre;
        this.form.projetId = projetId;
      } catch (error) {
        console.error('Erreur lors du chargement du projet:', error);
        this.errorMessage = 'Erreur lors du chargement des détails du projet. Veuillez réessayer.';
      }
    },
    async fetchStatutsEpisode() {
      try {
        const response = await axios.get('/api/statuts-episode');
        this.statutsEpisode = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des statuts d\'épisode:', error);
        this.errorMessage = 'Erreur lors du chargement des statuts';
      }
    },
    async fetchExistingEpisodes() {
      try {
        const projetId = this.$route.params.id || this.$route.params.projetId || this.$route.query.projetId;
        if (!projetId) return;
        
        const response = await axios.get(`/api/episodes/projet/${projetId}`);
        
        // Récupérer tous les ordres existants
        this.existingOrders = response.data.map(episode => episode.ordre);
        
        // Calculer le prochain ordre disponible
        this.calculateSuggestedOrdre();
        
      } catch (error) {
        console.error('Erreur lors du chargement des épisodes existants:', error);
      }
    },
    calculateSuggestedOrdre() {
      if (this.existingOrders.length === 0) {
        this.suggestedOrdre = 1;
        this.form.ordre = 1;
        return;
      }
      
      // Trouver le plus grand ordre existant
      const maxOrder = Math.max(...this.existingOrders);
      this.suggestedOrdre = maxOrder + 1;
      
      // Pré-remplir avec l'ordre suggéré
      this.form.ordre = this.suggestedOrdre;
    },
    validateOrdre() {
      if (!this.form.ordre) {
        this.ordreError = 'L\'ordre est requis';
        return;
      }
      
      const orderNum = parseInt(this.form.ordre);
      
      if (orderNum < 1) {
        this.ordreError = 'L\'ordre doit être au moins 1';
        return;
      }
      
      // Vérifier si l'ordre existe déjà
      if (this.existingOrders.includes(orderNum)) {
        this.ordreError = `L'ordre ${orderNum} existe déjà pour ce projet. Veuillez choisir un autre numéro.`;
        return;
      }
      
      this.ordreError = '';
    },
    useSuggestedOrder() {
      this.scene.ordre = this.suggestedOrder;
      this.validateOrder();
    },
    async submitForm() {
      // Valider l'ordre avant soumission
      this.validateOrdre();
      if (this.ordreError) {
        return;
      }
      
      this.loading = true;
      this.errorMessage = '';

      try {
        const response = await axios.post(`/api/episodes/projet/${this.form.projetId}`, this.form);
        
        if (response.status === 201) {
          this.$router.push(`/projet/${this.form.projetId}`);
        }
      } catch (error) {
        console.error('Erreur lors de la création de l\'épisode:', error);
        
        // Gestion spécifique des erreurs de duplication d'ordre
        if (error.response?.status === 400 && error.response?.data?.message?.includes('ordre')) {
          this.ordreError = 'Cet ordre existe déjà pour ce projet. Veuillez choisir un autre numéro.';
        } else {
          this.errorMessage = error.response?.data?.message || 'Erreur lors de la création de l\'épisode';
        }
      } finally {
        this.loading = false;
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
    }
  }
};
</script>

<style scoped>
/* Import Font Awesome pour les icônes */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

/* Conteneur principal */
.add-episode-container {
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
  max-width: 800px; /* Aligné avec AddSequence.vue */
  width: 100%;
  background: rgba(30, 41, 59, 0.3); /* Bleu nuit semi-transparent */
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  margin-top: 2rem;
  animation: slideIn 0.7s ease-out;
}

/* En-tête du formulaire */
.form-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.form-header h2 {
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
.episode-form {
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

.form-input,
.form-select,
.form-textarea {
  padding: 0.8rem;
  border: none;
  border-radius: 10px;
  background: rgba(30, 41, 59, 0.5); /* Bleu nuit sombre */
  color: #e2e8f0;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: #94a3b8; /* Gris bleu pâle */
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  background: rgba(30, 41, 59, 0.7);
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.5); /* Halo bleu */
}

.form-textarea {
  min-height: 120px;
  resize: vertical;
}

.form-input[disabled] {
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

/* Message d'erreur */
.error-message {
  background: rgba(239, 68, 68, 0.2); /* Rouge semi-transparent */
  color: #ef4444;
  padding: 1rem;
  border-radius: 10px;
  border: 1px solid rgba(239, 68, 68, 0.4);
  margin-top: 1rem;
  animation: shake 0.3s ease-in-out;
}

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

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  50% { transform: translateX(5px); }
  75% { transform: translateX(-5px); }
}

/* Responsive */
@media (max-width: 768px) {
  .add-episode-container {
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
