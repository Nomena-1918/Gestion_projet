<template>
  <div class="add-episode-container">
    <!-- Contenu principal -->
    <main class="main-content">
      <div class="form-header">
        <button @click="goBack" class="back-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Retour
        </button>
        <h2>Créer votre épisode</h2>
      </div>

      <form @submit.prevent="submitForm" class="episode-form">
        <div class="form-group">
          <label for="titre">Titre de l'épisode</label>
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
          <label for="ordre">Ordre dans le projet</label>
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
          <label for="realisateur">Réalisateur *</label>
          <select 
            id="realisateur"
            v-model="form.realisateurId" 
            required
            class="form-select"
          >
            <option value="">Sélectionner un réalisateur</option>
            <option v-for="realisateur in realisateurs" :key="realisateur.idRealisateur" :value="realisateur.idRealisateur">
              {{ realisateur.nom }} - {{ realisateur.specialite }}
            </option>
          </select>
        </div>

        <!-- NOUVEAU : Champ Scénariste -->
        <div class="form-group">
          <label for="scenariste">Scénariste *</label>
          <select 
            id="scenariste"
            v-model="form.scenaristeId" 
            required
            class="form-select"
          >
            <option value="">Sélectionner un scénariste</option>
            <option v-for="scenariste in scenaristes" :key="scenariste.idScenariste" :value="scenariste.idScenariste">
              {{ scenariste.nom }} - {{ scenariste.specialite }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="projet">Titre du projet</label>
          <input 
            id="projet"
            :value="projetTitre" 
            type="text"
            disabled
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="statut">Statut</label>
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
import '../assets/css/episode.css';

export default {
  name: 'AddEpisodeView',
  data() {
    return {
      showProfileMenu: false,
      user: null,
      projetTitre: '',
      statutsEpisode: [],
      existingOrders: [],
      realisateurs: [],
      scenaristes: [],
      form: {
        titre: '',
        ordre: '',
        projetId: this.$route.params.id || this.$route.params.projetId || '',
        statutId: '',
        synopsis: '',
        realisateurId: '',
        scenaristeId: ''
      },
      loading: false,
      errorMessage: '',
      ordreError: '', // Erreur spécifique à l'ordre
      suggestedOrdre: null, // Ordre suggéré
      newEpisodeId: null // ID du nouvel épisode créé
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
    this.fetchRealisateurs();
    this.fetchScenaristes();
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

     async fetchRealisateurs() {
      try {
        const response = await axios.get('/api/realisateurs');
        this.realisateurs = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des réalisateurs:', error);
      }
    },

      async fetchScenaristes() {
      try {
        const response = await axios.get('/api/scenaristes');
        this.scenaristes = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des scénaristes:', error);
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
      this.form.ordre = this.suggestedOrdre;
      this.validateOrdre();
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
          this.newEpisodeId = response.data.idEpisode; // Stocker l'ID du nouvel épisode
          this.$router.push({
            path: `/projet/${this.form.projetId}/ecran-travail`,
            query: { episodeId: this.newEpisodeId }
          });
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
      if (this.newEpisodeId) {
        // Si un nouvel épisode a été créé, rediriger vers la page ecran-travail avec l'ID de l'épisode
        this.$router.push({
          path: `/projet/${this.form.projetId}/ecran-travail`,
          query: { episodeId: this.newEpisodeId }
        });
      } else {
        // Sinon, revenir à la page précédente
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
    }
  }
};
</script>


