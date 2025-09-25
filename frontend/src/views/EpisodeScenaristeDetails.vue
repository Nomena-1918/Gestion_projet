<template>
  <div class="episode-details-container">

     <!-- Afficher un message d'erreur si l'utilisateur n'a pas accès -->
    <div v-if="accessDenied" class="access-denied">
      <div class="access-denied-content">
        <i class="fas fa-exclamation-triangle"></i>
        <h3>Accès refusé</h3>
        <p>Vous n'avez pas les droits nécessaires pour accéder à cet épisode.</p>
        <button @click="goBack" class="back-btn">Retour au projet</button>
      </div>
    </div>

    <!-- Contenu principal -->
    <main v-else class="main-content">
      <div class="episode-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Détails de l'Épisode</h2>
      </div>

      <!-- Section détails de l'épisode -->
      <div class="episode-details">
        <div class="episode-info">
          <div class="episode-main">
            <h3>{{ episode.titre }}</h3>
            <div class="episode-synopsis">
              <h4>
                Synopsis
                <span class="comment-icon" @click="toggleCommentSection">
                  💬 {{ commentCount }}
                </span>
              </h4>
              <p>{{ episode.synopsis || 'Aucun synopsis disponible' }}</p>
              
              <!-- Section commentaires -->
              <div v-if="showCommentSection" class="comment-section">
                <h5>Commentaires</h5>
                
                <!-- Formulaire d'ajout de commentaire -->
                <div class="add-comment">
                  <textarea v-model="newComment" placeholder="Ajouter un commentaire..." rows="3"></textarea>
                  <button @click="addComment" class="add-comment-btn">Ajouter</button>
                </div>
                
                <!-- Liste des commentaires -->
                <div class="comments-list">
                  <div v-for="comment in comments" :key="comment.id" class="comment-item">
                    <div class="comment-header">
                      <span class="comment-author">{{ comment.utilisateurNom }}</span>
                      <span class="comment-date">{{ formatDate(comment.creeLe) }}</span>
                    </div>
                    <div class="comment-content">
                      {{ comment.contenu }}
                    </div>
                    <div class="comment-actions" v-if="comment.utilisateurId === user.id">
                      <button @click="deleteComment(comment.id)" class="delete-comment-btn">Supprimer</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="episode-sidebar">
            <div class="episode-statut">
              <span class="statut-label">Statut:</span>
              <span class="statut-value">{{ episode.statutNom }}</span>
            </div>
            <div class="episode-date">
              <span class="date-label">Ordre:</span>
              <span class="date-value">{{ episode.ordre }}</span>
            </div>
            
            <!-- Informations du Scénariste -->
            <div class="episode-team" v-if="episode.scenariste">
              <span class="team-label">Scénariste:</span>
              <div class="team-member">
                <span class="member-name">{{ episode.scenariste.nom }}</span>
                <span class="member-email">{{ episode.scenariste.email }}</span>
              </div>
            </div>
            <div class="episode-team" v-else>
              <span class="team-label">Scénariste:</span>
              <span class="team-value">Non assigné</span>
            </div>
            
            <!-- Informations du Réalisateur -->
            <div class="episode-team" v-if="episode.realisateur">
              <span class="team-label">Réalisateur:</span>
              <div class="team-member">
                <span class="member-name">{{ episode.realisateur.nom }}</span>
                <span class="member-email">{{ episode.realisateur.email }}</span>
              </div>
            </div>
            <div class="episode-team" v-else>
              <span class="team-label">Réalisateur:</span>
              <span class="team-value">Non assigné</span>
            </div>
            
            <div class="episode-date">
              <span class="date-label">Créé le:</span>
              <span class="date-value">{{ formatDate(episode.creeLe) }}</span>
            </div>
            <div class="episode-date">
              <span class="date-label">Modifié le:</span>
              <span class="date-value">{{ formatDate(episode.modifieLe) }}</span>
            </div>
            <!-- Dans la section episode-sidebar, ajouter après les autres divs de date -->
            <div class="episode-date" v-if="episode.dateFin">
              <span class="date-label">Terminé le:</span>
              <span class="date-value">{{ formatDate(episode.dateFin) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Section séquences -->
      <div class="sequences-section">
        <div class="section-header">
          <h3>Séquences</h3>
          <button class="add-sequence-btn" @click="goToAddSequence">+ Séquence</button>
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
              <option v-for="statut in statutsSequence" :key="statut.id" :value="statut.nomStatutsSequence">
                {{ statut.nomStatutsSequence }}
              </option>
            </select>
          </div>
        </div>

        <!-- Liste des séquences -->
        <div class="sequences-list">
          <div v-for="sequence in filteredSequences" :key="sequence.idSequence" class="sequence-card">
            <div class="sequence-header">
              <div class="sequence-statut">
                {{ sequence.statutNom }}
              </div>
              <div class="sequence-actions">
                <span class="icon-edit" @click="startEditSequence(sequence)">✏️</span>
                <span class="icon-delete" @click="confirmDeleteSequence(sequence.idSequence)">🗑️</span>
              </div>
            </div>
            
            <div class="sequence-content">
              <h4>{{ sequence.titre }}</h4>
              <p class="sequence-order">Ordre: {{ sequence.ordre }}</p>
              
              <div class="sequence-dates">
                <p>Créé le: {{ formatDate(sequence.creeLe) }}</p>
                <p>Modifié le: {{ formatDate(sequence.modifieLe) }}</p>
              </div>
              
              <div class="sequence-synopsis" v-if="sequence.synopsis">
                <p>{{ truncateText(sequence.synopsis, 100) }}</p>
              </div>
              
              <div class="sequence-footer">
                <button class="details-btn" @click="goToSequenceDetails(sequence.idSequence)">Détails</button>
              </div>
            </div>
          </div>
        </div>
      </div>

    <!-- Modale pour modifier la séquence -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <h3>Modifier la Séquence</h3>
        
        <!-- Message d'erreur général -->
        <div v-if="editError" class="error-message">
          {{ editError }}
        </div>
        
        <form @submit.prevent="saveEditedSequence">
          <div class="form-group">
            <label>Titre de la séquence:</label>
            <input v-model="editingSequence.titre" type="text" required />
          </div>
          <div class="form-group">
            <label>Synopsis:</label>
            <textarea v-model="editingSequence.synopsis" required></textarea>
          </div>
          <div class="form-group">
            <label>Ordre dans l'épisode:</label>
            <input 
              v-model="editingSequence.ordre" 
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
            <div v-if="suggestedOrder && !editingSequence.ordre" class="suggestion-text">
              Suggestion: Le prochain ordre disponible est {{ suggestedOrder }}
              <button type="button" @click="useSuggestedOrder" class="suggestion-btn">
                Utiliser cette valeur
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>Titre de l'épisode:</label>
            <input :value="episode.titre" type="text" disabled />
          </div>
          <div class="form-group">
            <label>Statut:</label>
            <select v-model="editingSequence.statutId" required>
              <option v-for="statut in statutsSequence" :key="statut.id" :value="statut.id">
                {{ statut.nomStatutsSequence }}
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

export default {
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user')) || null,
      showProfileMenu: false,
      episode: {
        scenariste: null,
        realisateur: null
      },
      sequences: [],
      statutsSequence: [],
      accessDenied: false, 
      userEpisodes: [],
      filterTimePeriod: 'all',
      filterStatut: '',
      showEditModal: false,
      editingSequence: {
        idSequence: null,
        titre: '',
        synopsis: '',
        ordre: null,
        statutId: null,
      },
      showCommentSection: false,
      newComment: '',
      comments: [],
      commentCount: 0,
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
    filteredSequences() {
      let filtered = this.sequences;

      // Filtrer par statut
      if (this.filterStatut) {
        filtered = filtered.filter(sequence => sequence.statutNom === this.filterStatut);
      }

      // Filtrer par période
      const now = new Date();
      filtered = filtered.filter(sequence => {
        const modifieLe = new Date(sequence.modifieLe);
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
    await this.loadUserEpisodes(); 
    await this.checkAccess(); 
    if (!this.accessDenied) {
      await this.loadEpisode();
      await this.loadSequences();
      await this.loadStatutsSequence();
      await this.loadCommentCount();
    }
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    async loadUserEpisodes() {
        try {
          const response = await axios.get(`/api/episodes/utilisateur/${this.user.id}`);
          this.userEpisodes = response.data;
        } catch (error) {
          console.error('Erreur lors du chargement des épisodes utilisateur:', error);
        }
    },
    async checkAccess() {
      const episodeId = parseInt(this.$route.params.id);
      
      // Les admins ont accès à tout
      if (this.user.role === 'ADMIN') {
        this.accessDenied = false;
        return;
      }

      // Vérifier si l'épisode actuel est dans la liste des épisodes accessibles
      const hasAccess = this.userEpisodes.some(ep => ep.idEpisode === episodeId);
      
      if (!hasAccess) {
        this.accessDenied = true;
      }
    },
     async loadEpisode() {
      try {
        const response = await axios.get(`/api/episodes/${this.$route.params.id}`, {
          headers: {
            'X-User-Id': this.user.id
          }
        });
        this.episode = response.data;
        
        // Vérifier et logger les données reçues pour debug
        console.log('Données de l\'épisode:', this.episode);
        console.log('Scénariste:', this.episode.scenariste);
        console.log('Réalisateur:', this.episode.realisateur);
        
      } catch (error) {
        if (error.response?.status === 403) {
          this.accessDenied = true;
        } else {
          console.error('Erreur lors du chargement de l\'épisode:', error);
        }
      }
    },
    async loadSequences() {
      try {
        const response = await axios.get(`/api/sequences/episodes/${this.$route.params.id}`);
        this.sequences = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des séquences:', error);
      }
    },

    async loadStatutsSequence() {
      try {
        const response = await axios.get('/api/statuts-sequence');
        this.statutsSequence = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des statuts:', error);
      }
    },
    async loadComments() {
      try {
        const response = await axios.get(`/api/commentaires/episode/${this.$route.params.id}`);
        this.comments = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des commentaires:', error);
      }
    },
    async loadCommentCount() {
      try {
        const response = await axios.get(`/api/commentaires/episode/${this.$route.params.id}/count`);
        this.commentCount = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement du nombre de commentaires:', error);
      }
    },
    async toggleCommentSection() {
      this.showCommentSection = !this.showCommentSection;
      if (this.showCommentSection) {
        await this.loadComments();
      }
    },
    async addComment() {
      if (!this.newComment.trim()) return;
      
      try {
        await axios.post('/api/commentaires', {
          contenu: this.newComment,
          episodeId: this.$route.params.id
        }, {
          headers: {
            'X-User-Id': this.user.id
          }
        });
        
        this.newComment = '';
        await this.loadComments();
        await this.loadCommentCount();
      } catch (error) {
        console.error('Erreur lors de l\'ajout du commentaire:', error);
        alert('Erreur lors de l\'ajout du commentaire');
      }
    },
    async deleteComment(commentId) {
      if (confirm('Êtes-vous sûr de vouloir supprimer ce commentaire ?')) {
        try {
          await axios.delete(`/api/commentaires/${commentId}`);
          await this.loadComments();
          await this.loadCommentCount();
        } catch (error) {
          console.error('Erreur lors de la suppression du commentaire:', error);
        }
      }
    },
    startEditSequence(sequence) {
      this.editingSequence = {
        idSequence: sequence.idSequence,
        titre: sequence.titre,
        synopsis: sequence.synopsis,
        ordre: sequence.ordre,
        statutId: this.getStatutIdByNom(sequence.statutNom),
      };
      this.originalOrder = sequence.ordre; // Sauvegarder l'ordre original
      this.showEditModal = true;
      
      // Charger les ordres existants et calculer la suggestion
      this.loadExistingOrders();
    },
    async loadExistingOrders() {
      try {
        // Récupérer toutes les séquences de l'épisode
        const response = await axios.get(`/api/sequences/episodes/${this.$route.params.id}`);
        
        // Filtrer les ordres existants (exclure la séquence en cours d'édition)
        this.existingOrders = response.data
          .filter(sequence => sequence.idSequence !== this.editingSequence.idSequence)
          .map(sequence => sequence.ordre);
        
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
      if (!this.editingSequence.ordre) {
        this.orderError = 'L\'ordre est requis';
        return;
      }
      
      const orderNum = parseInt(this.editingSequence.ordre);
      
      if (orderNum < 1) {
        this.orderError = 'L\'ordre doit être un nombre positif';
        return;
      }
      
      // Vérifier si l'ordre existe déjà (sauf si c'est l'ordre original de cette séquence)
      if (this.existingOrders.includes(orderNum) && orderNum !== this.originalOrder) {
        this.orderError = `L'ordre ${orderNum} existe déjà pour cet épisode. Veuillez choisir un autre numéro.`;
        return;
      }
      
      this.orderError = '';
    },
    
    useSuggestedOrder() {
      this.editingSequence.ordre = this.suggestedOrder;
      this.validateOrder();
    },

    getStatutIdByNom(nom) {
      const statut = this.statutsSequence.find(s => s.nomStatutsSequence === nom);
      return statut ? statut.id : null;
    },
   async saveEditedSequence() {
      // Valider l'ordre avant soumission
      this.validateOrder();
      if (this.orderError) {
        return;
      }
      
      try {
        const response = await axios.put(`/api/sequences/${this.editingSequence.idSequence}`, {
          titre: this.editingSequence.titre,
          synopsis: this.editingSequence.synopsis,
          ordre: parseInt(this.editingSequence.ordre),
          statutId: this.editingSequence.statutId,
        });
        
        this.showEditModal = false;
        this.editError = '';
        this.orderError = '';
        await this.loadSequences();
      } catch (error) {
        console.error('Erreur lors de la mise à jour de la séquence:', error);
        
        // Gestion spécifique des erreurs de duplication d'ordre
        if (error.response?.status === 400 && 
            error.response?.data?.message?.includes('ordre')) {
          this.orderError = 'Cet ordre existe déjà pour cet épisode. Veuillez choisir un autre numéro.';
          this.editError = 'Erreur de validation: ' + this.orderError;
        } else {
          this.editError = error.response?.data?.message || 'Erreur lors de la mise à jour de la séquence';
        }
      }
    },

  closeEditModal() {
      this.showEditModal = false;
      this.editingSequence = {
        idSequence: null,
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

    async confirmDeleteSequence(sequenceId) {
      if (confirm('Êtes-vous sûr de vouloir supprimer cette séquence ?')) {
        try {
          await axios.delete(`/api/sequences/${sequenceId}`);
          await this.loadSequences();
        } catch (error) {
          console.error('Erreur lors de la suppression de la séquence:', error);
        }
      }
    },
    goToAddSequence() {
      this.$router.push(`/episode/${this.$route.params.id}/add-sequence`);
    },
    goToSequenceDetails(sequenceId) {
      this.$router.push(`/sequence/${sequenceId}/detail-sequence`);
    },
    goBack() {
      this.$router.push(`/projet/${this.episode.projetId}`);
    },
    formatDate(date) {
      return new Date(date).toLocaleString();
    },
    truncateText(text, length) {
      if (!text) return '';
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
.episode-details-container {
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
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.profile-avatar-large {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.5rem;
  margin: 0 auto 1rem;
}

.profile-details {
  text-align: center;
}

.profile-details h3 {
  margin: 0 0 0.5rem;
  color: #333;
}

.profile-details p {
  margin: 0.25rem 0;
  color: #666;
}

.role-badge {
  display: inline-block;
  background: #f0f0f0;
  color: #333;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.profile-actions {
  padding: 1rem;
}

.logout-btn {
  width: 100%;
  padding: 0.75rem;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background: #ff3742;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.episode-header {
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

.episode-details {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.episode-info {
  display: flex;
  gap: 2rem;
}

.episode-main {
  flex: 1;
}

.episode-main h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.episode-synopsis h4 {
  margin: 1.5rem 0 0.5rem;
  color: #555;
  display: flex;
  align-items: center;
}

.comment-icon {
  cursor: pointer;
  font-size: 0.8em;
  margin-left: 10px;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: normal;
}

.episode-synopsis p {
  line-height: 1.6;
  color: #666;
}

.comment-section {
  margin-top: 1.5rem;
  border-top: 1px solid #eee;
  padding-top: 1rem;
}

.comment-section h5 {
  margin-bottom: 1rem;
  color: #555;
}

.add-comment {
  margin-bottom: 1.5rem;
}

.add-comment textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  margin-bottom: 0.5rem;
}

.add-comment-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.comments-list {
  max-height: 300px;
  overflow-y: auto;
}

.comment-item {
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 1rem;
  margin-bottom: 1rem;
  background: #fafafa;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-size: 0.9em;
}

.comment-author {
  font-weight: 600;
  color: #667eea;
}

.comment-date {
  color: #888;
}

.comment-content {
  line-height: 1.4;
  color: #333;
}

.comment-actions {
  margin-top: 0.5rem;
  text-align: right;
}

.delete-comment-btn {
  background: #ff4757;
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.8em;
}

.episode-sidebar {
  width: 250px;
  border-left: 1px solid #eee;
  padding-left: 2rem;
}

.episode-statut,
.episode-date {
  margin-bottom: 1rem;
}

.statut-label,
.date-label {
  display: block;
  font-weight: 600;
  color: #555;
  margin-bottom: 0.25rem;
}

.statut-value,
.date-value {
  color: #333;
}

.sequences-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 1rem;
}

.section-header h3 {
  margin: 0;
  color: #333;
}

.add-sequence-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
}

.filter-group label {
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #555;
}

.filter-group select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.sequences-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.sequence-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.sequence-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.sequence-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.sequence-statut {
  background: #d8bfd8;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.sequence-actions {
  display: flex;
  gap: 0.5rem;
}

.icon-edit,
.icon-delete {
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.icon-edit:hover {
  background-color: rgba(59, 130, 246, 0.1);
}

.icon-delete:hover {
  background-color: rgba(239, 68, 68, 0.1);
}

.sequence-content {
  padding: 1rem;
}

.sequence-content h4 {
  margin: 0 0 0.5rem;
  color: #333;
}

.sequence-order {
  color: #666;
  margin: 0 0 0.5rem;
  font-size: 0.9rem;
}

.sequence-dates {
  margin-bottom: 1rem;
}

.sequence-dates p {
  margin: 0.25rem 0;
  color: #666;
  font-size: 0.9rem;
}

.sequence-synopsis p {
  color: #555;
  line-height: 1.5;
  margin: 0 0 1rem;
}

.sequence-footer {
  display: flex;
  justify-content: flex-end;
}

.details-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 1rem;
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

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.error-message {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  padding: 1rem;
  border-radius: 10px;
  border: 1px solid rgba(239, 68, 68, 0.4);
  margin-bottom: 1rem;
  animation: shake 0.3s ease-in-out;
}

.error-input {
  border-color: #e74c3c !important;
  box-shadow: 0 0 0 2px rgba(231, 76, 60, 0.2) !important;
}

.error-text {
  color: #e74c3c;
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
}

.suggestion-text {
  color: #3498db;
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
  font-style: italic;
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
  margin-left: 0.5rem;
}

.suggestion-btn:hover {
  background: rgba(52, 152, 219, 0.1);
}

/* Animation pour le message d'erreur */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  50% { transform: translateX(5px); }
  75% { transform: translateX(-5px); }
}

.save-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.cancel-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

@media (max-width: 768px) {
  .episode-info {
    flex-direction: column;
  }
  
  .episode-sidebar {
    width: 100%;
    border-left: none;
    border-top: 1px solid #eee;
    padding-left: 0;
    padding-top: 1.5rem;
  }
  
  .filters {
    flex-direction: column;
  }
  
  .sequences-list {
    grid-template-columns: 1fr;
  }
}

.date-label {
  display: block;
  font-weight: 600;
  color: #555;
  margin-bottom: 0.25rem;
}

.date-value {
  color: #333;
}

/* Style spécifique pour la date de fin */
.project-date:last-child .date-value,
.episode-date:last-child .date-value {
  color: #28a745; /* Vert pour indiquer la fin */
  font-weight: 600;
}

.access-denied {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.access-denied-content {
  background: white;
  padding: 3rem;
  border-radius: 15px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  max-width: 500px;
  width: 100%;
}

.access-denied-content i {
  font-size: 4rem;
  color: #ff4757;
  margin-bottom: 1rem;
}

.access-denied-content h3 {
  color: #333;
  margin-bottom: 1rem;
}

.access-denied-content p {
  color: #666;
  margin-bottom: 2rem;
  line-height: 1.6;
}

.access-denied-content .back-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: transform 0.3s ease;
}

.access-denied-content .back-btn:hover {
  transform: translateY(-2px);
}

.episode-team {
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #667eea;
}

.team-label {
  display: block;
  font-weight: 600;
  color: #555;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.team-member {
  display: flex;
  flex-direction: column;
}

.member-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
}

.member-email {
  color: #666;
  font-size: 0.85rem;
  font-style: italic;
}

.team-value {
  color: #888;
  font-style: italic;
}

/* Adaptation responsive */
@media (max-width: 768px) {
  .episode-team {
    margin-bottom: 0.75rem;
    padding: 0.5rem;
  }
  
  .team-label {
    font-size: 0.85rem;
  }
  
  .member-name {
    font-size: 0.9rem;
  }
  
  .member-email {
    font-size: 0.8rem;
  }
}

</style>