<template>
  <div class="add-scene-container">

    <main class="main-content">
      <div class="scene-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Ajouter une Scène</h2>
      </div>

      <div class="scene-form">
        <form @submit.prevent="submitScene">
          <div class="form-group">
            <label>Titre de la scène *</label>
            <input v-model="scene.titre" type="text" required />
          </div>
          
          <div class="form-group">
            <label>Ordre dans la séquence *</label>
            <input 
              v-model="scene.ordre" 
              type="number" 
              min="1" 
              required 
              @blur="validateOrder"
              :class="{ 'error-input': orderError }"
            />
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
            <label>Synopsis *</label>
            <textarea v-model="scene.synopsis" required></textarea>
          </div>
          
          <div class="form-group">
            <label>Séquence liée</label>
            <input :value="sequenceTitre" type="text" disabled />
          </div>
          
          <div class="form-group">
            <label>Statut *</label>
            <select v-model="scene.statutId" required>
              <option value="">Sélectionnez un statut</option>
              <option v-for="statut in statutsScene" :key="statut.id" :value="statut.id">
                {{ statut.nomStatutsScene }}
              </option>
            </select>
          </div>
          
          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>
          
          <div class="form-actions">
            <button type="submit" class="submit-btn" :disabled="loading || orderError !== ''">
              {{ loading ? 'Création en cours...' : 'Créer la Scène' }}
            </button>
            <button type="button" @click="goBack" class="cancel-btn">
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
      scene: {
        titre: '',
        ordre: null,
        synopsis: '',
        statutId: null
      },
      statutsScene: [],
      sequenceTitre: '',
      loading: false,
      errorMessage: '',
      existingOrders: [], // Liste des ordres existants
      suggestedOrder: null, // Ordre suggéré
      orderError: '' // Erreur spécifique à l'ordre
    };
  },
  computed: {
    userInitials() {
      if (!this.user?.nom) return '';
      const names = this.user.nom.split(' ');
      return names.map(n => n[0]).join('').toUpperCase();
    },
    sequenceId() {
      return this.$route.params.sequenceId;
    }
  },
  async created() {
    await this.loadStatutsScene();
    await this.loadSequenceDetails();
    await this.loadExistingOrders(); // Charger les ordres existants
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    async loadStatutsScene() {
      try {
        const response = await axios.get('/api/statuts-scene');
        this.statutsScene = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des statuts:', error);
        this.errorMessage = 'Erreur lors du chargement des statuts';
      }
    },
    async loadSequenceDetails() {
      try {
        // Récupérer les détails de la séquence
        const sequenceResponse = await axios.get(`/api/sequences/${this.sequenceId}`);
        const sequence = sequenceResponse.data;
        this.sequenceTitre = sequence.titre;
      } catch (error) {
        console.error('Erreur lors du chargement de la séquence:', error);
        this.errorMessage = 'Impossible de charger les détails de la séquence';
      }
    },
    async loadExistingOrders() {
      try {
        // Récupérer toutes les scènes de cette séquence pour vérifier les ordres existants
        const response = await axios.get(`/api/scenes/sequences/${this.sequenceId}`);
        this.existingOrders = response.data.map(scene => scene.ordre);
        
        // Calculer le prochain ordre disponible
        this.calculateSuggestedOrder();
      } catch (error) {
        console.error('Erreur lors du chargement des ordres existants:', error);
        this.errorMessage = 'Impossible de vérifier les ordres existants';
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
      
      // Pré-remplir le champ avec la suggestion
      this.scene.ordre = this.suggestedOrder;
    },
    validateOrder() {
      if (!this.scene.ordre) {
        this.orderError = 'L\'ordre est requis';
        return;
      }
      
      if (this.scene.ordre < 1) {
        this.orderError = 'L\'ordre doit être un nombre positif';
        return;
      }
      
      // Vérifier si l'ordre existe déjà
      if (this.existingOrders.includes(this.scene.ordre)) {
        this.orderError = `L'ordre ${this.scene.ordre} existe déjà dans cette séquence`;
        return;
      }
      
      // Si tout est valide, effacer l'erreur
      this.orderError = '';
    },
    useSuggestedOrder() {
      this.scene.ordre = this.suggestedOrder;
      this.validateOrder();
    },
    async submitScene() {
      // Valider à nouveau avant soumission
      this.validateOrder();
      
      if (this.orderError) {
        this.errorMessage = 'Veuillez corriger les erreurs avant de soumettre';
        return;
      }
      
      this.loading = true;
      this.errorMessage = '';
      
      try {
        // CORRECTION : Utiliser le bon endpoint selon SceneController
        await axios.post(`/api/scenes/sequences/${this.sequenceId}`, this.scene);
       
        this.$router.push(`/sequence/${this.sequenceId}/detail-sequence`);
      } catch (error) {
        console.error('Erreur lors de la création de la scène:', error);
        
        // Vérifier si l'erreur est due à un doublon d'ordre
        if (error.response?.status === 400 && 
            error.response?.data?.message?.includes('ordre') &&
            error.response?.data?.message?.includes('existe')) {
          this.orderError = 'Cet ordre existe déjà dans la séquence';
          this.errorMessage = 'Erreur de validation: ' + this.orderError;
          
          // Recharger les ordres existants au cas où ils auraient changé
          await this.loadExistingOrders();
        } else {
          this.errorMessage = error.response?.data?.message || 'Erreur lors de la création de la scène';
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
.add-scene-container {
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
.scene-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.scene-header h2 {
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
.scene-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.scene-form form {
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

.form-group input.error-input {
  border: 2px solid #ef4444;
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

.suggestion-text {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #93c5fd; /* Bleu clair */
  display: flex;
  align-items: center;
  gap: 0.5rem;
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

.error-text {
  margin-top: 0.5rem;
  color: #ef4444; /* Rouge */
  font-size: 0.9rem;
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

.error-message {
  background: rgba(239, 68, 68, 0.2); /* Rouge semi-transparent */
  color: #ef4444;
  padding: 1rem;
  border-radius: 10px;
  border: 1px solid rgba(239, 68, 68, 0.4);
  margin-top: 1rem;
  animation: shake 0.3s ease-in-out;
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
  .add-scene-container {
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
  
  .suggestion-text {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.3rem;
  }
}
</style>