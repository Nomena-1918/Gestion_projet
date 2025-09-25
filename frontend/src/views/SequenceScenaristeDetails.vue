<template>
  <div class="sequence-details-container">

    <!-- Contenu principal -->
    <main class="main-content">
      <div class="sequence-header">
        <button @click="goBack" class="back-btn">← Retour</button>
        <h2>Détails de la Séquence</h2>
      </div>

      <!-- Section détails de la séquence -->
      <div class="sequence-details">
        <div class="sequence-info">
          <div class="sequence-main">
            <h3>{{ sequence.titre }}</h3>
            <div class="sequence-synopsis">
              <h4>Synopsis
                <span class="comment-icon" @click="toggleCommentSection">
                  💬 {{ commentCount }}
                </span>
              </h4>
              <p>{{ sequence.synopsis || 'Aucun synopsis disponible' }}</p>
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
          
          <div class="sequence-sidebar">
            <div class="sequence-statut">
              <span class="statut-label">Statut:</span>
              <span class="statut-value">{{ sequence.statutNom }}</span>
            </div>
            <div class="sequence-date">
              <span class="date-label">Ordre:</span>
              <span class="date-value">{{ sequence.ordre }}</span>
            </div>
            <div class="sequence-date">
              <span class="date-label">Créé le:</span>
              <span class="date-value">{{ formatDate(sequence.creeLe) }}</span>
            </div>
            <div class="sequence-date">
              <span class="date-label">Modifié le:</span>
              <span class="date-value">{{ formatDate(sequence.modifieLe) }}</span>
            </div>
            <div class="sequence-date" v-if="sequence.dateFin">
              <span class="date-label">Terminé le:</span>
              <span class="date-value">{{ formatDate(sequence.dateFin) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Section scènes -->
      <div class="scenes-section">
        <div class="section-header">
          <h3>Scènes</h3>
          <button class="add-scene-btn" @click="goToAddScene">+ Scène</button>
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
              <option v-for="statut in statutsScene" :key="statut.id" :value="statut.nomStatutsScene">
                {{ statut.nomStatutsScene }}
              </option>
            </select>
          </div>
        </div>

        <!-- Liste des scènes -->
        <div class="scenes-list">
          <div v-for="scene in filteredScenes" :key="scene.idScene" class="scene-card">
            <div class="scene-header">
              <div class="scene-statut">
                {{ scene.statutNom }}
              </div>
              <div class="scene-actions">
                <span class="icon-comment" @click="openSceneComments(scene)" title="Commentaires">
                  💬 {{ scene.commentCount || 0 }}
                </span>
                <span class="icon-edit" @click="startEditScene(scene)">✏️</span>
                <span class="icon-delete" @click="confirmDeleteScene(scene.idScene)">🗑️</span>
                 <span class="icon-lieu" @click="openAddLieuModal(scene)" title="Ajouter un lieu">
                📍
              </span>
              </div>
            </div>
            
            <div class="scene-content">
              <h4>{{ scene.titre }}</h4>
              <p class="scene-order">Ordre: {{ scene.ordre }}</p>
              
              <div class="scene-dates">
                <p>Créé le: {{ formatDate(scene.creeLe) }}</p>
                <p>Modifié le: {{ formatDate(scene.modifieLe) }}</p>
              </div>
              
              <div class="scene-synopsis" v-if="scene.synopsis">
                <p>{{ truncateText(scene.synopsis, 100) }}</p>
              </div>
              
              <div class="scene-footer">
                <button class="details-btn" @click="openSynopsisModal(scene)">Détails</button>
              </div>
            </div>
          </div>
        </div>
      </div>

     <!-- Modale pour modifier la scène -->
      <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
        <div class="modal-content" @click.stop>
          <h3>Modifier la Scène</h3>
          
          <!-- Message d'erreur général -->
          <div v-if="editError" class="error-message">
            {{ editError }}
          </div>
          
          <form @submit.prevent="saveEditedScene">
            <div class="form-group">
              <label>Titre de la scène:</label>
              <input v-model="editingScene.titre" type="text" required />
            </div>
            <div class="form-group">
              <label>Synopsis:</label>
              <textarea v-model="editingScene.synopsis"></textarea>
            </div>
            <div class="form-group">
              <label>Ordre dans la séquence:</label>
              <input 
                v-model="editingScene.ordre" 
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
              <div v-if="suggestedOrder && !editingScene.ordre" class="suggestion-text">
                Suggestion: Le prochain ordre disponible est {{ suggestedOrder }}
                <button type="button" @click="useSuggestedOrder" class="suggestion-btn">
                  Utiliser cette valeur
                </button>
              </div>
            </div>
            <div class="form-group">
              <label>Titre de la séquence:</label>
              <input :value="sequence.titre" type="text" disabled />
            </div>
            <div class="form-group">
              <label>Statut:</label>
              <select v-model="editingScene.statutId" required>
                <option v-for="statut in statutsScene" :key="statut.id" :value="statut.id">
                  {{ statut.nomStatutsScene }}
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

     
 <!-- Modale pour afficher le synopsis complet -->
<div v-if="showSynopsisModal" class="modal-overlay" @click="closeSynopsisModal">
  <div class="modal-content synopsis-modal" @click.stop>
    <h3>{{ selectedScene.titre }}</h3>
    
    <!-- Informations sur les comédiens -->
    <div class="scene-info-section" v-if="selectedSceneComediens.length > 0">
      <h4>Comédiens dans cette scène</h4>
      <div class="comedien-list">
        <div v-for="comedien in selectedSceneComediens" :key="comedien.id" class="comedien-item">
          <span class="comedien-name">{{ comedien.comedienNom }}</span>
          <span v-if="comedien.personnageNom" class="comedien-character">({{ comedien.personnageNom }})</span>
        </div>
      </div>
    </div>

    <!-- Informations sur les lieux et plateaux -->
    <div class="scene-info-section" v-if="selectedSceneLieus.length > 0">
      <h4>Lieux et plateaux</h4>
      <div class="location-list">
        <div v-for="sceneLieu in selectedSceneLieus" :key="sceneLieu.id" class="location-item">
          <div class="location-info">
            <strong>{{ sceneLieu.lieuNom }}</strong>
            <span v-if="sceneLieu.plateauNom"> - Plateau: {{ sceneLieu.plateauNom }}</span>
            <span v-if="sceneLieu.descriptionUtilisation"> ({{ sceneLieu.descriptionUtilisation }})</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Synopsis -->
    <div class="synopsis-content">
      <h4>Synopsis</h4>
      <p>{{ selectedScene.synopsis || 'Aucun synopsis disponible' }}</p>
    </div>
    
    <div class="modal-actions">
      <button type="button" @click="closeSynopsisModal" class="cancel-btn">Fermer</button>
    </div>
  </div>
</div>

      <!-- Modale pour afficher les commentaires de la scène -->
       <div v-if="showSceneCommentModal" class="modal-overlay" @click="showSceneCommentModal = false">
        <div class="modal-content" @click.stop>
          <h3>Commentaires - {{ selectedScene.titre }}</h3>
          
          <!-- Formulaire d'ajout de commentaire -->
          <div class="add-comment">
            <textarea v-model="newSceneComment" placeholder="Ajouter un commentaire..." rows="3"></textarea>
            <button @click="addSceneComment" class="add-comment-btn">Ajouter</button>
          </div>
          
          <!-- Liste des commentaires -->
          <div class="comments-list">
            <div v-for="comment in sceneComments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="comment-author">{{ comment.utilisateurNom }}</span>
                <span class="comment-date">{{ formatDate(comment.creeLe) }}</span>
              </div>
              <div class="comment-content">
                {{ comment.contenu }}
              </div>
              <div class="comment-actions" v-if="comment.utilisateurId === user.id">
                <button @click="deleteSceneComment(comment.id)" class="delete-comment-btn">Supprimer</button>
              </div>
            </div>
            
            <div v-if="sceneComments.length === 0" class="no-comments">
              Aucun commentaire pour cette scène.
            </div>
          </div>
          
          <div class="modal-actions">
            <button type="button" @click="showSceneCommentModal = false" class="cancel-btn">Fermer</button>
          </div>
        </div>
      </div>


       <!-- Nouvelle modale pour ajouter un lieu à la scène -->
  <div v-if="showAddLieuModal" class="modal-overlay" @click="closeAddLieuModal">
      <div class="modal-content" @click.stop>
        <h3>Ajouter un lieu à la scène: {{ selectedScene.titre }}</h3>
        
        <form @submit.prevent="addLieuToScene">
          <div class="form-group">
            <label>Sélectionner un lieu:</label>
            <select v-model="selectedLieuId" @change="onLieuChange" required>
              <option value="" disabled>Sélectionner un lieu</option>
              <option v-for="lieu in lieuxDisponibles" :key="lieu.id" :value="lieu.id">
                {{ lieu.nomLieu }} ({{ lieu.typeLieu }})
              </option>
            </select>
          </div>
          
          <div class="form-group" v-if="plateauxDisponibles.length > 0">
            <label>Sélectionner un plateau (optionnel):</label>
            <select v-model="selectedPlateauId">
              <option value="" disabled>Sélectionner un plateau</option>
              <option v-for="plateau in plateauxDisponibles" :key="plateau.id" :value="plateau.id">
                {{ plateau.nom }} ({{ plateau.typePlateau }})
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label>Description d'utilisation:</label>
            <textarea v-model="lieuDescription" placeholder="Ex: Jour, Nuit, Intérieur, Extérieur..." rows="3"></textarea>
          </div>
          
          <div class="modal-actions">
            <button type="submit" class="save-btn">Ajouter</button>
            <button type="button" @click="closeAddLieuModal" class="cancel-btn">Annuler</button>
          </div>
        </form>
      
      <!-- Liste des lieux déjà associés à cette scène -->
      <div class="associated-lieux" v-if="sceneLieus.length > 0">
        <h4>Lieux déjà associés:</h4>
        <div v-for="sceneLieu in sceneLieus" :key="sceneLieu.id" class="scene-lieu-item">
          <div class="scene-lieu-info">
            <strong>{{ sceneLieu.lieuNom }}</strong>
            <span v-if="sceneLieu.plateauNom"> - Plateau: {{ sceneLieu.plateauNom }}</span>
            - {{ sceneLieu.descriptionUtilisation || 'Aucune description' }}
          </div>
          <button @click="removeLieuFromScene(sceneLieu.id)" class="delete-btn" title="Supprimer">
            🗑️
          </button>
        </div>
      </div>
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
      sequence: {},
      scenes: [],
      statutsScene: [],
      filterTimePeriod: 'all',
      filterStatut: '',
      showEditModal: false,
      editingScene: {
        idScene: null,
        titre: '',
        synopsis: '',
        ordre: null,
        statutId: null,
      },
      showCommentSection: false,
      newComment: '',
      comments: [],
      commentCount: 0,
      showSynopsisModal: false,
      selectedScene: {},
      showSceneCommentModal: false,
      sceneComments: [],
      newSceneComment: '',
      selectedScene: {},

      showAddLieuModal: false,
      selectedSceneForLieu: null,
      lieux: [],
      plateaux: [],
      selectedLieu: null,
      selectedPlateau: null,
      descriptionUtilisation: '',
      newLieuModal: false,
      newPlateauModal: false,
      newLieu: {
        nomLieu: '',
        typeLieu: '',
        adresse: ''
      },
      newPlateau: {
        nom: '',
        typePlateau: '',
        description: ''
      },
      showAddLieuModal: false,
      selectedLieuId: null,
       selectedPlateauId: null,
      plateauxDisponibles: [], 
      lieuDescription: '',
      lieuxDisponibles: [],
      sceneLieus: [],
      selectedScene: {},
      selectedSceneComediens: [],
      selectedSceneLieus: [],
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
    filteredScenes() {
      let filtered = this.scenes;

      // Filtrer par statut
      if (this.filterStatut) {
        filtered = filtered.filter(scene => scene.statutNom === this.filterStatut);
      }

      // Filtrer par période
      const now = new Date();
      filtered = filtered.filter(scene => {
        const modifieLe = new Date(scene.modifieLe);
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
    await this.loadSequence();
    await this.loadScenes();
    await this.loadStatutsScene();
    await this.loadCommentCount();
    document.addEventListener('click', this.handleClickOutside);

      // Charger les lieux disponibles pour le projet
    await this.loadLieuxDisponibles();
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    async loadSequence() {
      try {
        const response = await axios.get(`/api/sequences/${this.$route.params.id}`);
        this.sequence = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement de la séquence:', error);
      }
    },

    async loadEpisode() {
      try {
        const response = await axios.get(`/api/episodes/${this.$route.params.id}`);
        this.episode = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement de l\'épisode:', error);
      }
    },
    
   async loadScenes() {
  try {
    const response = await axios.get(`/api/scenes/sequences/${this.$route.params.id}`);
    this.scenes = response.data;
    
    // Charger le nombre de commentaires pour chaque scène
    for (let scene of this.scenes) {
      try {
        const countResponse = await axios.get(`/api/scene-commentaires/scene/${scene.idScene}/count`);
        scene.commentCount = countResponse.data;
      } catch (error) {
        console.error('Erreur lors du chargement du nombre de commentaires pour la scène:', error);
        scene.commentCount = 0;
      }
    }
  } catch (error) {
    console.error('Erreur lors du chargement des scènes:', error);
  }
},
    async loadStatutsScene() {
      try {
        const response = await axios.get('/api/statuts-scene');
        this.statutsScene = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des statuts:', error);
      }
    },

    async loadComments() {
      try {
        const response = await axios.get(`/api/sequence-commentaires/sequence/${this.$route.params.id}`);
        this.comments = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des commentaires:', error);
      }
    },
    
    async loadCommentCount() {
      try {
        const response = await axios.get(`/api/sequence-commentaires/sequence/${this.$route.params.id}/count`);
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
        await axios.post('/api/sequence-commentaires', {
          contenu: this.newComment,
          sequenceId: this.$route.params.id
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
          await axios.delete(`/api/sequence-commentaires/${commentId}`);
          await this.loadComments();
          await this.loadCommentCount();
        } catch (error) {
          console.error('Erreur lors de la suppression du commentaire:', error);
        }
      }
    },
    
    async openSceneComments(scene) {
      this.selectedScene = scene;
      await this.loadSceneComments(scene.idScene);
      this.showSceneCommentModal = true;
    },

    async loadSceneComments(sceneId) {
      try {
        const response = await axios.get(`/api/scene-commentaires/scene/${sceneId}`);
        this.sceneComments = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des commentaires de scène:', error);
      }
    },

    async addSceneComment() {
      if (!this.newSceneComment.trim()) return;
      
      try {
        await axios.post('/api/scene-commentaires', {
          contenu: this.newSceneComment,
          sceneId: this.selectedScene.idScene
        }, {
          headers: {
            'X-User-Id': this.user.id
          }
        });
        
        this.newSceneComment = '';
        await this.loadSceneComments(this.selectedScene.idScene);
        await this.loadScenes(); // Recharger pour mettre à jour le compteur
      } catch (error) {
        console.error('Erreur lors de l\'ajout du commentaire:', error);
        alert('Erreur lors de l\'ajout du commentaire');
      }
    },

    async deleteSceneComment(commentId) {
      if (confirm('Êtes-vous sûr de vouloir supprimer ce commentaire ?')) {
        try {
          await axios.delete(`/api/scene-commentaires/${commentId}`);
          await this.loadSceneComments(this.selectedScene.idScene);
          await this.loadScenes(); // Recharger pour mettre à jour le compteur
        } catch (error) {
          console.error('Erreur lors de la suppression du commentaire:', error);
        }
      }
    },

    startEditScene(scene) {
      this.editingScene = {
        idScene: scene.idScene,
        titre: scene.titre,
        synopsis: scene.synopsis,
        ordre: scene.ordre,
        statutId: this.getStatutIdByNom(scene.statutNom),
      };
      this.originalOrder = scene.ordre; // Sauvegarder l'ordre original
      this.showEditModal = true;
      
      // Charger les ordres existants et calculer la suggestion
      this.loadExistingOrders();
    },

      async loadExistingOrders() {
      try {
        // Récupérer toutes les scènes de la séquence
        const response = await axios.get(`/api/scenes/sequences/${this.$route.params.id}`);
        
        // Filtrer les ordres existants (exclure la scène en cours d'édition)
        this.existingOrders = response.data
          .filter(scene => scene.idScene !== this.editingScene.idScene)
          .map(scene => scene.ordre);
        
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
      if (!this.editingScene.ordre) {
        this.orderError = 'L\'ordre est requis';
        return;
      }
      
      const orderNum = parseInt(this.editingScene.ordre);
      
      if (orderNum < 1) {
        this.orderError = 'L\'ordre doit être un nombre positif';
        return;
      }
      
      // Vérifier si l'ordre existe déjà (sauf si c'est l'ordre original de cette scène)
      if (this.existingOrders.includes(orderNum) && orderNum !== this.originalOrder) {
        this.orderError = `L'ordre ${orderNum} existe déjà pour cette séquence. Veuillez choisir un autre numéro.`;
        return;
      }
      
      this.orderError = '';
    },
    
    useSuggestedOrder() {
      this.editingScene.ordre = this.suggestedOrder;
      this.validateOrder();
    },

    getStatutIdByNom(nom) {
      const statut = this.statutsScene.find(s => s.nomStatutsScene === nom);
      return statut ? statut.id : null;
    },
    
   async saveEditedScene() {
      // Valider l'ordre avant soumission
      this.validateOrder();
      if (this.orderError) {
        return;
      }
      
      try {
        const response = await axios.put(`/api/scenes/${this.editingScene.idScene}`, {
          titre: this.editingScene.titre,
          synopsis: this.editingScene.synopsis,
          ordre: parseInt(this.editingScene.ordre),
          statutId: this.editingScene.statutId,
        });
        
        this.showEditModal = false;
        this.editError = '';
        this.orderError = '';
        await this.loadScenes();
      } catch (error) {
        console.error('Erreur lors de la mise à jour de la scène:', error);
        
        // Gestion spécifique des erreurs de duplication d'ordre
        if (error.response?.status === 400 && 
            error.response?.data?.message?.includes('ordre')) {
          this.orderError = 'Cet ordre existe déjà pour cette séquence. Veuillez choisir un autre numéro.';
          this.editError = 'Erreur de validation: ' + this.orderError;
        } else {
          this.editError = error.response?.data?.message || 'Erreur lors de la mise à jour de la scène';
        }
      }
    },
    
    closeEditModal() {
      this.showEditModal = false;
      this.editingScene = {
        idScene: null,
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
    
    async openAddLieuModal(scene) {
      this.selectedScene = scene;
      this.showAddLieuModal = true;
      await this.loadSceneLieus(scene.idScene);
    },
    
    closeAddLieuModal() {
      this.showAddLieuModal = false;
      this.selectedLieuId = null;
      this.lieuDescription = '';
      this.sceneLieus = [];
      this.selectedScene = {};
    },
    
async loadLieuxDisponibles() {
  try {
    // Si vous ne pouvez pas obtenir le projetId, chargez tous les lieux
    console.warn('Impossible de trouver le projetId, chargement de tous les lieux');
    const response = await axios.get('/api/lieux');
    this.lieuxDisponibles = response.data || [];
    
  } catch (error) {
    console.error('Erreur même avec chargement de tous les lieux:', error);
    this.lieuxDisponibles = [];
  }
},
    async loadSceneLieus(sceneId) {
      try {
        const response = await axios.get(`/api/scene-lieux/scenes/${sceneId}`);
        this.sceneLieus = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des lieux de la scène:', error);
      }
    },
    
async addLieuToScene() {
  if (!this.selectedLieuId) {
    alert('Veuillez sélectionner un lieu');
    return;
  }
  
  try {
    // Vérifier que les IDs sont bien des nombres
    const sceneId = Number(this.selectedScene.idScene);
    const lieuId = Number(this.selectedLieuId);
    const plateauId = this.selectedPlateauId ? Number(this.selectedPlateauId) : null;
    
    const payload = {
      sceneId: sceneId,
      lieuId: lieuId,
      plateauId: plateauId, 
      descriptionUtilisation: this.lieuDescription || ''
    };
    
    console.log('Payload envoyé (avec vérification):', payload);
    console.log('Types des données:', {
      sceneId: typeof payload.sceneId,
      lieuId: typeof payload.lieuId,
      plateauId: typeof payload.plateauId
    });

    const response = await axios.post('/api/scene-lieux', payload, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    console.log('Réponse du serveur:', response.data);
    
    await this.loadSceneLieus(this.selectedScene.idScene);
    
    this.selectedLieuId = null;
    this.selectedPlateauId = null;
    this.lieuDescription = '';
    this.plateauxDisponibles = [];
    
    alert('Lieu et plateau ajoutés avec succès à la scène!');
  } catch (error) {
    console.error('Erreur détaillée:', error);
    console.error('Données de l\'erreur:', error.response?.data);
    console.error('Status de l\'erreur:', error.response?.status);
    
    let errorMessage = 'Erreur lors de l\'ajout du lieu';
    if (error.response?.data) {
      // Si le serveur retourne un message d'erreur détaillé
      errorMessage += ': ' + (error.response.data.message || JSON.stringify(error.response.data));
    }
    alert(errorMessage);
  }
},

    async removeLieuFromScene(sceneLieuId) {
      if (confirm('Êtes-vous sûr de vouloir supprimer ce lieu de la scène ?')) {
        try {
          await axios.delete(`/api/scene-lieux/${sceneLieuId}`);
          await this.loadSceneLieus(this.selectedScene.idScene);
          alert('Lieu retiré de la scène avec succès!');
        } catch (error) {
          console.error('Erreur lors de la suppression du lieu:', error);
          alert('Erreur lors de la suppression du lieu');
        }
      }
    },

    
    async loadLieux() {
      try {
        const response = await axios.get(`/api/lieux/projets/${this.sequence.projetId}`);
        this.lieux = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des lieux:', error);
      }
    },
    
   async onLieuChange() {
      if (this.selectedLieuId) {
        try {
          const response = await axios.get(`/api/scene-lieux/lieux/${this.selectedLieuId}/plateaux`);
          this.plateauxDisponibles = response.data;
        } catch (error) {
          console.error('Erreur lors du chargement des plateaux:', error);
          this.plateauxDisponibles = [];
        }
      } else {
        this.plateauxDisponibles = [];
        this.selectedPlateauId = null;
      }
    },
    
    async addSceneLieu() {
      if (!this.selectedLieu || !this.selectedSceneForLieu) {
        alert('Veuillez sélectionner un lieu');
        return;
      }
      
      try {
        const sceneLieuData = {
          sceneId: this.selectedSceneForLieu.idScene,
          lieuId: this.selectedLieu,
          descriptionUtilisation: this.descriptionUtilisation
        };
        
        await axios.post('/api/scene-lieux', sceneLieuData);
        
        // Si un plateau est sélectionné, l'ajouter aussi
        if (this.selectedPlateau) {
          const scenePlateauData = {
            sceneId: this.selectedSceneForLieu.idScene,
            plateauId: this.selectedPlateau,
            descriptionUtilisation: this.descriptionUtilisation
          };
          
          await axios.post('/api/scene-plateaux', scenePlateauData);
        }
        
        this.closeAddLieuModal();
        alert('Lieu et plateau ajoutés avec succès à la scène');
      } catch (error) {
        console.error('Erreur lors de l\'ajout du lieu/plateau:', error);
        alert('Erreur lors de l\'ajout');
      }
    },
    
    closeAddLieuModal() {
      this.showAddLieuModal = false;
      this.selectedLieuId = null;
      this.selectedPlateauId = null;
      this.lieuDescription = '';
      this.plateauxDisponibles = [];
      this.sceneLieus = [];
      this.selectedScene = {};
    },
    
    async createNewLieu() {
      try {
        const response = await axios.post('/api/lieux', this.newLieu);
        await this.loadLieux();
        this.selectedLieu = response.data.id;
        this.newLieuModal = false;
        this.newLieu = { nomLieu: '', typeLieu: '', adresse: '' };
      } catch (error) {
        console.error('Erreur lors de la création du lieu:', error);
        alert('Erreur lors de la création du lieu');
      }
    },
    
    async createNewPlateau() {
      if (!this.selectedLieu) {
        alert('Veuillez d\'abord sélectionner un lieu');
        return;
      }
      
      try {
        const plateauData = {
          ...this.newPlateau,
          lieuId: this.selectedLieu
        };
        
        const response = await axios.post('/api/plateaux', plateauData);
        await this.onLieuChange(); // Recharger les plateaux
        this.selectedPlateau = response.data.id;
        this.newPlateauModal = false;
        this.newPlateau = { nom: '', typePlateau: '', description: '' };
      } catch (error) {
        console.error('Erreur lors de la création du plateau:', error);
        alert('Erreur lors de la création du plateau');
      }
    },
    
    closeEditModal() {
      this.showEditModal = false;
      this.editingScene = {
        idScene: null,
        titre: '',
        synopsis: '',
        ordre: null,
        statutId: null,
      };
    },
    
    async confirmDeleteScene(sceneId) {
      if (confirm('Êtes-vous sûr de vouloir supprimer cette scène ?')) {
        try {
          await axios.delete(`/api/scenes/${sceneId}`);
          await this.loadScenes();
        } catch (error) {
          console.error('Erreur lors de la suppression de la scène:', error);
        }
      }
    },

    async openSynopsisModal(scene) {
      this.selectedScene = scene;
      this.showSynopsisModal = true;
      
      // Charger les comédiens de la scène
      await this.loadSceneComediens(scene.idScene);
      
      // Charger les lieux de la scène
      await this.loadSceneLieus(scene.idScene);
    },

    async loadSceneComediens(sceneId) {
      try {
        const response = await axios.get(`/api/comedien-scene/scene/${sceneId}`);
        console.log('Données comédiens:', response.data);
        this.selectedSceneComediens = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des comédiens de la scène:', error);
        this.selectedSceneComediens = [];
      }
    },

async loadSceneLieus(sceneId) {
  try {
    const response = await axios.get(`/api/scene-lieux/scenes/${sceneId}`);
    this.selectedSceneLieus = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des lieux de la scène:', error);
    this.selectedSceneLieus = [];
  }
},



    goToAddScene() {
      this.$router.push(`/sequence/${this.$route.params.id}/add-scene`);
    },
    
    
    closeSynopsisModal() {
      this.showSynopsisModal = false;
      this.selectedScene = {};
      this.selectedSceneComediens = [];
      this.selectedSceneLieus = [];
    },
    
    goBack() {
      this.$router.push(`/episode/${this.sequence.episodeId}/detail-episode`);
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
    }
  },
};
</script>

<style scoped>
.sequence-details-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: 'Arial', sans-serif;
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

.sequence-header {
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

.sequence-details {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.sequence-info {
  display: flex;
  gap: 2rem;
}

.sequence-main {
  flex: 1;
}

.sequence-main h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.sequence-synopsis h4 {
  margin: 1.5rem 0 0.5rem;
  color: #555;
}

.sequence-synopsis p {
  line-height: 1.6;
  color: #666;
}

.sequence-sidebar {
  width: 250px;
  border-left: 1px solid #eee;
  padding-left: 2rem;
}

.sequence-statut,
.sequence-date {
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

.scenes-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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

.add-scene-btn {
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

.scenes-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.scene-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.scene-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.scene-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.scene-statut {
  background: #d8bfd8;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.scene-actions {
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

.scene-content {
  padding: 1rem;
}

.scene-content h4 {
  margin: 0 0 0.5rem;
  color: #333;
}

.scene-order {
  color: #666;
  margin: 0 0 0.5rem;
  font-size: 0.9rem;
}

.scene-dates {
  margin-bottom: 1rem;
}

.scene-dates p {
  margin: 0.25rem 0;
  color: #666;
  font-size: 0.9rem;
}

.scene-synopsis p {
  color: #555;
  line-height: 1.5;
  margin: 0 0 1rem;
}

.scene-footer {
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
.no-lieux-message {
  padding: 1rem;
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 4px;
  color: #856404;
  margin-top: 1rem;
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
  background-color: #ffebee;
  color: #c62828;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  border: 1px solid #ffcdd2;
}

.error-input {
  border-color: #c62828 !important;
}

.error-text {
  color: #c62828;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.suggestion-text {
  color: #666;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.suggestion-btn {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;
  margin-left: 0.5rem;
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

.date-label {
  display: block;
  font-weight: 600;
  color: #555;
  margin-bottom: 0.25rem;
}

.date-value {
  color: #333;
}

.sequence-date:last-child .date-value {
  color: #28a745;
  font-weight: 600;
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

.synopsis-content h4 {
  margin: 1rem 0 0.5rem;
  color: #555;
}

.synopsis-content p {
  line-height: 1.6;
  color: #666;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .sequence-info {
    flex-direction: column;
  }
  
  .sequence-sidebar {
    width: 100%;
    border-left: none;
    border-top: 1px solid #eee;
    padding-left: 0;
    padding-top: 1.5rem;
  }
  
  .filters {
    flex-direction: column;
  }
  
  .scenes-list {
    grid-template-columns: 1fr;
  }
}

.associated-lieux {
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.associated-lieux h4 {
  margin-bottom: 1rem;
  color: #555;
}

.scene-lieu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  margin-bottom: 0.5rem;
  background: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #eee;
}

.scene-lieu-info {
  flex: 1;
}

.delete-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 3px;
  transition: background-color 0.3s;
}

.delete-btn:hover {
  background-color: rgba(239, 68, 68, 0.1);
}

/* Style pour l'icône lieu */
.icon-lieu {
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.icon-lieu:hover {
  background-color: rgba(34, 197, 94, 0.1);
}

.scene-actions {
  display: flex;
  gap: 0.5rem;
}

.icon-lieu {
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.3s;
  font-size: 1.1em;
}

.icon-lieu:hover {
  background-color: rgba(34, 197, 94, 0.1);
  transform: scale(1.1);
}

.synopsis-modal {
  max-width: 600px;
}

.scene-info-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.scene-info-section h4 {
  margin: 0 0 0.5rem 0;
  color: #555;
  font-size: 1.1rem;
}

.comedien-list,
.location-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.comedien-item,
.location-item {
  padding: 0.5rem;
  background: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #eee;
}

.comedien-name {
  font-weight: 600;
  color: #333;
}

.comedien-character {
  color: #666;
  font-style: italic;
}

.location-info {
  color: #333;
  line-height: 1.4;
}
</style>