<template>
  <div class="add-project-container">
    <!-- Contenu principal -->
    <main class="main-content">
      <div class="form-header">
        <h2>Ajouter un nouveau projet</h2>
        <button @click="goBack" class="back-btn">← Retour</button>
      </div>

      <form @submit.prevent="submitProject" class="project-form">
        <div class="form-group">
          <label for="titre">Titre du projet *</label>
          <input 
            type="text" 
            id="titre"
            v-model="project.titre" 
            required 
            placeholder="Entrez le titre du projet"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="synopsis">Synopsis</label>
          <textarea 
            id="synopsis"
            v-model="project.synopsis" 
            placeholder="Décrivez le synopsis du projet"
            rows="4"
            class="form-textarea"
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="genre">Genre *</label>
            <select 
              id="genre"
              v-model="project.genreId" 
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
            <label for="statut">Statut *</label>
            <select 
              id="statut"
              v-model="project.statutId" 
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

        <div class="form-row">
          <div class="form-group">
            <label for="dateDebut">Date de début estimée *</label>
            <input 
              type="date" 
              id="dateDebut"
              v-model="project.dateDebut" 
              required
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="dateFin">Date de fin estimée *</label>
            <input 
              type="date" 
              id="dateFin"
              v-model="project.dateFin" 
              required
              class="form-input"
            />
          </div>
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="cancel-btn">Annuler</button>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? 'Création en cours...' : 'Créer le projet' }}
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
  name: 'AddProjectView',
  data() {
    return {
      showProfileMenu: false,
      user: null,
      project: {
        titre: '',
        synopsis: '',
        genreId: '',
        statutId: '',
        dateDebut: '',
        dateFin: ''
      },
      genres: [],
      statuts: [],
      loading: false,
      error: ''
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
    this.fetchGenres();
    this.fetchStatuts();
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
    async submitProject() {
      this.loading = true;
      this.error = '';

      try {
        const response = await axios.post('/api/projets', this.project);
        
        if (response.status === 201) {
          this.$router.push('/scenariste');
        }
      } catch (error) {
        console.error('Erreur lors de la création du projet:', error);
        this.error = error.response?.data?.message || 'Erreur lors de la création du projet';
      } finally {
        this.loading = false;
      }
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
    }
  }
};
</script>

<style scoped>
/* Import Font Awesome pour les icônes */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

/* Conteneur principal */
.add-project-container {
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
.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.form-header h2 {
  color: #e2e8f0; /* Gris clair pour contraste */
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
.project-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
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
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  background: rgba(30, 41, 59, 0.7);
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.5); /* Halo bleu */
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.cancel-btn {
  background: linear-gradient(135deg, #4b5563 0%, #374151 100%); /* Gris bleu */
  color: #ffffff;
  padding: 0.8rem 1.8rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  transform: scale(1.05);
  background: linear-gradient(135deg, #374151 0%, #4b5563 100%);
}

.submit-btn {
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%); /* Bleu nuit */
  color: #ffffff;
  padding: 0.8rem 1.8rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: scale(1.05);
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  background: rgba(239, 68, 68, 0.2); /* Rouge semi-transparent */
  color: #ef4444;
  padding: 1rem;
  border-radius: 10px;
  border: 1px solid rgba(239, 68, 68, 0.4);
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
  .add-project-container {
    padding: 1rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .cancel-btn,
  .submit-btn {
    width: 100%;
  }

  .main-content {
    padding: 1.2rem;
  }
}
</style>