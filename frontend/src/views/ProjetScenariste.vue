<template>
  <div class="projet-scenariste-container">

    <!-- Contenu principal -->
    <main class="main-content">
      <div class="project-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Détails du Projet</h2>
      </div>

      <!-- Section détails du projet -->
      <div class="project-details">
        <div class="project-info">
          <div class="project-main">
            <h3>{{ projet.titre }}</h3>
            <div class="project-synopsis">
              <h4>Synopsis</h4>
              <p>{{ projet.synopsis || 'Aucun synopsis disponible' }}</p>
            </div>
          </div>
          
          <div class="project-sidebar">
            <div class="project-statut">
              <span class="statut-label">Statut:</span>
              <span class="statut-value">{{ projet.statutNom }}</span>
            </div>
            <div class="project-date">
              <span class="date-label">Créé le:</span>
              <span class="date-value">{{ formatDate(projet.creeLe) }}</span>
            </div>
            <div class="project-date">
              <span class="date-label">Modifié le:</span>
              <span class="date-value">{{ formatDate(projet.modifieLe) }}</span>
            </div>
            <!-- Dans la section project-sidebar, ajouter après les autres divs de date -->
            <div class="project-date" v-if="projet.dateFin">
              <span class="date-label">Terminé le:</span>
              <span class="date-value">{{ formatDate(projet.dateFin) }}</span>
            </div>
            <div class="project-genre">
              <span class="genre-label">Genre:</span>
              <span class="genre-value">{{ projet.genreNom }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Section épisodes -->
      <div class="episodes-section">
        <div class="section-header">
          <h3>Épisodes</h3>
          <button class="add-episode-btn" @click="goToAddEpisode">+ Épisode</button>
        </div>

        <!-- Filtres -->
        <div class="filters">
          <div class="filter-group">
            <label>Période de mise à jour:</label>
            <select v-model="filterTimePeriod">
              <option value="all">Toutes les périodes</option>
              <option value="today">Aujourd'hui</option>
              <option value="this_week">Cette semaine</option>
              <option value="this_month">Ce mois-ci</option>
              <option value="this_year">Cette année</option>
              <option value="recent">Récent (7 derniers jours)</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Statut:</label>
            <select v-model="filterStatut">
              <option value="">Tous</option>
              <option v-for="statut in statutsEpisode" :key="statut.idStatutEpisode" :value="statut.nomStatutsEpisode">
                {{ statut.nomStatutsEpisode }}
              </option>
            </select>
          </div>
        </div>

        <!-- Liste des épisodes -->
        <div class="episodes-list">
          <div v-for="episode in filteredEpisodes" :key="episode.idEpisode" class="episode-card">
            <div class="episode-header">
              <div class="episode-statut">
                {{ episode.statutNom }}
              </div>
              <div class="episode-actions">
                <span class="icon-edit" @click="startEditEpisode(episode)">✏️</span>
                <span class="icon-delete" @click="confirmDeleteEpisode(episode.idEpisode)">🗑️</span>
              </div>
            </div>
            
            <div class="episode-content">
              <h4>{{ episode.titre }}</h4>
              <p class="episode-order">Ordre: {{ episode.ordre }}</p>
              
              <div class="episode-dates">
                <p>Créé le: {{ formatDate(episode.creeLe) }}</p>
                <p>Modifié le: {{ formatDate(episode.modifieLe) }}</p>
                <p>Nombre de séquences: {{ episode.nombreSequences || 0 }}</p>
              </div>
              
              <div class="episode-synopsis" v-if="episode.synopsis">
                <p>{{ truncateText(episode.synopsis, 100) }}</p>
              </div>
              
              <div class="episode-footer">
                <button class="details-btn" @click="goToDetails(episode.idEpisode)">Détails</button>
                <button class="add-sequence-btn" @click="goToAddSequence(episode.idEpisode)">+ Séquence</button>
              </div>
            </div>

          </div>
        </div>
      </div>

     <!-- Modale pour modifier l'épisode -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <h3>Modifier l'Épisode</h3>
        
        <!-- Message d'erreur général -->
        <div v-if="editError" class="error-message">
          {{ editError }}
        </div>
        
        <form @submit.prevent="saveEditedEpisode">
          <div class="form-group">
            <label>Titre de l'épisode:</label>
            <input v-model="editingEpisode.titre" type="text" required />
          </div>
          <div class="form-group">
            <label>Synopsis:</label>
            <textarea v-model="editingEpisode.synopsis" required></textarea>
          </div>
          <div class="form-group">
            <label>Ordre dans le projet:</label>
            <input 
              v-model="editingEpisode.ordre" 
              type="number" 
              required 
              :class="{ 'error-input': orderError }"
              @blur="validateOrder"
            />
            <!-- Message d'erreur spécifique pour l'ordre -->
            <div v-if="orderError" class="error-text">
              {{ orderError }}
            </div>
            <!-- Suggestion d'ordre -->
            <div v-if="suggestedOrder && !editingEpisode.ordre" class="suggestion-text">
              Suggestion: Le prochain ordre disponible est {{ suggestedOrder }}
              <button type="button" @click="useSuggestedOrder" class="suggestion-btn">
                Utiliser cette valeur
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>Titre du projet:</label>
            <input :value="projet.titre" type="text" disabled />
          </div>
          <div class="form-group">
            <label>Statut:</label>
            <select v-model="editingEpisode.statutId" required>
              <option v-for="statut in statutsEpisode" :key="statut.idStatutEpisode" :value="statut.idStatutEpisode">
                {{ statut.nomStatutsEpisode }}
              </option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="submit" class="save-btn" :disabled="orderError !== ''">Sauvegarder</button>
            <button type="button" @click="closeEditModal" class="cancel-btn">Annuler</button>
          </div>
        </form>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios';
import '../assets/css/projet_scenariste.css';

export default {
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user')) || null,
      showProfileMenu: false,
      projet: {},
      episodes: [],
      statutsEpisode: [],
       allEpisodes: [], 
      userEpisodes: [],
      filterTimePeriod: 'all',
      filterStatut: '',
      showEditModal: false,
      editingEpisode: {
        idEpisode: null,
        titre: '',
        synopsis: '',
        ordre: null,
        statutId: null,
      },
      editError: '',
      orderError: '', 
      suggestedOrder: null, 
      existingOrders: [],
      originalOrder: null 
    };
  },
  computed: {
    userInitials() {
      if (!this.user?.nom) return '';
      const names = this.user.nom.split(' ');
      return names.map(n => n[0]).join('').toUpperCase();
    },
    filteredEpisodes() {
      let filtered = this.episodes;

      // Filtrer par statut
      if (this.filterStatut) {
        filtered = filtered.filter(episode => episode.statutNom === this.filterStatut);
      }

      // Filtrer par période
      const now = new Date();
      filtered = filtered.filter(episode => {
        const modifieLe = new Date(episode.modifieLe);
        switch (this.filterTimePeriod) {
          case 'today':
            return modifieLe.toDateString() === now.toDateString();
          case 'this_week':
            const oneWeekAgo = new Date(now);
            oneWeekAgo.setDate(now.getDate() - 7);
            return modifieLe >= oneWeekAgo;
          case 'this_month':
            return modifieLe.getMonth() === now.getMonth() && modifieLe.getFullYear() === now.getFullYear();
          case 'this_year':
            return modifieLe.getFullYear() === now.getFullYear();
          case 'recent':
            const sevenDaysAgo = new Date(now);
            sevenDaysAgo.setDate(now.getDate() - 7);
            return modifieLe >= sevenDaysAgo;
          default:
            return true;
        }
      });

      return filtered;
    },
  },
 async created() {
    await this.loadProjet();
    await this.loadUserEpisodes(); 
    await this.loadAllEpisodes();
    await this.loadStatutsEpisode();
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    async loadProjet() {
      try {
        const response = await axios.get(`/api/projets/${this.$route.params.id}`);
        this.projet = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement du projet:', error);
      }
    },

  async loadUserEpisodes() {
    try {
      
      const response = await axios.get(`/api/episodes/utilisateur/${this.user.id}`);
      this.userEpisodes = response.data;
    } catch (error) {
      console.error('Erreur lors du chargement des épisodes utilisateur:', error);
    }
  },
    async loadAllEpisodes() {
      try {
        const response = await axios.get(`/api/episodes/projet/${this.$route.params.id}`);
        this.allEpisodes = response.data;
        
        // Filtrer pour n'afficher que les épisodes accessibles
        this.episodes = this.allEpisodes.filter(episode => 
            this.userEpisodes.some(userEp => userEp.idEpisode === episode.idEpisode) ||
            this.user.role === 'ADMIN'
        );
      } catch (error) {
        console.error('Erreur lors du chargement des épisodes:', error);
      }
    },


    async loadEpisodes() {
      try {
        const response = await axios.get(`/api/episodes/projet/${this.$route.params.id}`);
        this.episodes = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des épisodes:', error);
      }
    },
    async loadStatutsEpisode() {
      try {
        const response = await axios.get('/api/statuts-episode');
        this.statutsEpisode = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des statuts:', error);
      }
    }, 
   startEditEpisode(episode) {
      const hasAccess = this.userEpisodes.some(ep => ep.idEpisode === episode.idEpisode) || 
                      this.user.role === 'ADMIN';
    
      if (hasAccess) {  
        this.editingEpisode = {
          idEpisode: episode.idEpisode,
          titre: episode.titre,
          synopsis: episode.synopsis,
          ordre: episode.ordre,
          statutId: this.getStatutIdByNom(episode.statutNom),
        };
        this.originalOrder = episode.ordre;
        this.showEditModal = true;
        this.loadExistingOrders();
      } else {
        alert('Vous n\'avez pas les droits pour modifier cet épisode');
      }
    },
     async loadExistingOrders() {
      try {
        // Récupérer tous les épisodes du projet
        const response = await axios.get(`/api/episodes/projet/${this.$route.params.id}`);
        
        // Filtrer les ordres existants (exclure l'épisode en cours d'édition)
        this.existingOrders = response.data
          .filter(episode => episode.idEpisode !== this.editingEpisode.idEpisode)
          .map(episode => episode.ordre);
        
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
    },
    
    validateOrder() {
      if (!this.editingEpisode.ordre) {
        this.orderError = 'L\'ordre est requis';
        return;
      }
      
      const orderNum = parseInt(this.editingEpisode.ordre);
      
      if (orderNum < 1) {
        this.orderError = 'L\'ordre doit être un nombre positif';
        return;
      }
      
      // Vérifier si l'ordre existe déjà (sauf si c'est l'ordre original de cet épisode)
      if (this.existingOrders.includes(orderNum) && orderNum !== this.originalOrder) {
        this.orderError = `L'ordre ${orderNum} existe déjà pour ce projet. Veuillez choisir un autre numéro.`;
        return;
      }
      
      this.orderError = '';
    },
    
    useSuggestedOrder() {
      this.editingEpisode.ordre = this.suggestedOrder;
      this.validateOrder();
    },

    getStatutIdByNom(nom) {
      const statut = this.statutsEpisode.find(s => s.nomStatutsEpisode === nom);
      return statut ? statut.idStatutEpisode : null;
    },
    async saveEditedEpisode() {
      this.validateOrder();
      if (this.orderError) {
        return;
      }
      
      try {
        const response = await axios.put(`/api/episodes/${this.editingEpisode.idEpisode}`, {
          titre: this.editingEpisode.titre,
          synopsis: this.editingEpisode.synopsis,
          ordre: parseInt(this.editingEpisode.ordre),
          statutId: this.editingEpisode.statutId,
        });
        
        this.showEditModal = false;
        this.editError = '';
        this.orderError = '';
        
        // CORRIGER : utiliser loadAllEpisodes au lieu de loadEpisodes
        await this.loadAllEpisodes(); // ou await this.loadEpisodes() si vous l'ajoutez
        
      } catch (error) {
        console.error('Erreur lors de la mise à jour de l\'épisode:', error);
        
        if (error.response?.status === 400 && 
            error.response?.data?.message?.includes('ordre')) {
          this.orderError = 'Cet ordre existe déjà pour ce projet. Veuillez choisir un autre numéro.';
          this.editError = 'Erreur de validation: ' + this.orderError;
        } else {
          this.editError = error.response?.data?.message || 'Erreur lors de la mise à jour de l\'épisode';
        }
      }
    },

    closeEditModal() {
      this.showEditModal = false;
      this.editingEpisode = {
        idEpisode: null,
        titre: '',
        synopsis: '',
        ordre: null,
        statutId: null,
      };
      this.editError = '';
      this.orderError = '';
      this.suggestedOrder = null;
      this.existingOrders = [];
      this.originalOrder = null;
    },
    async confirmDeleteEpisode(episodeId) {
      const hasAccess = this.userEpisodes.some(ep => ep.idEpisode === episodeId) || 
                       this.user.role === 'ADMIN';
      if (!hasAccess) {
        alert('Vous n\'avez pas les droits pour supprimer cet épisode');
        return;
      }

      if (confirm('Êtes-vous sûr de vouloir supprimer cet épisode ?')) {
        try {
          await axios.delete(`/api/episodes/${episodeId}`);
          await this.loadEpisodes();
        } catch (error) {
          console.error('Erreur lors de la suppression de l\'épisode:', error);
        }
      }
    },
    goToAddEpisode() {
      this.$router.push(`/projet/${this.$route.params.id}/add-episode`);
    },
    goToDetails(episodeId) {
      // Vérifier si l'utilisateur a accès à cet épisode
      const hasAccess = this.userEpisodes.some(ep => ep.idEpisode === episodeId) || 
                      this.user.role === 'ADMIN';
      
      if (hasAccess) {
        this.$router.push(`/episode/${episodeId}/detail-episode`);
      } else {
        alert('Vous n\'avez pas accès à cet épisode');
      }
    },
    goToAddSequence(episodeId) {
      const hasAccess = this.userEpisodes.some(ep => ep.idEpisode === episodeId) || 
                       this.user.role === 'ADMIN';
      
      if (hasAccess) {
        this.$router.push(`/episode/${episodeId}/add-sequence`);
      } else {
        alert('Vous n\'avez pas accès à cet épisode');
      }
    },
    goBack() {
      this.$router.push('/scenariste');
    },
    formatDate(date) {
      return new Date(date).toLocaleString();
    },
    truncateText(text, length) {
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
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
  },
};
</script>
<style scoped>
</style>