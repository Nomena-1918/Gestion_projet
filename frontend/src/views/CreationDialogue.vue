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
            <div class="combobox-container">
              <input
                type="text"
                v-model="sceneSearch"
                @focus="showSceneSuggestions = true"
                @blur="hideSceneSuggestions"
                @input="filterScenes"
                :placeholder="formData.sceneId ? getSceneName(formData.sceneId) : 'Rechercher une scène...'"
                required
                class="combobox-input"
              />
              <ul v-if="showSceneSuggestions && filteredScenes.length" class="suggestions-list">
                <li
                  v-for="scene in filteredScenes"
                  :key="scene.idScene"
                  @mousedown="selectScene(scene)"
                  class="suggestion-item"
                >
                  {{ scene.titre }} ({{ scene.sequenceTitre }})
                </li>
              </ul>
              <ul v-if="showSceneSuggestions && filteredScenes.length === 0" class="suggestions-list">
                <li class="suggestion-item no-results">Aucune scène trouvée</li>
              </ul>
            </div>
          </div>
          
          <div class="form-group">
            <label>Personnage</label>
            <div class="combobox-container">
              <input
                type="text"
                v-model="personnageSearch"
                @focus="showPersonnageSuggestions = true"
                @blur="hidePersonnageSuggestions"
                @input="filterPersonnages"
                :placeholder="formData.personnageId ? getPersonnageName(formData.personnageId) : 'Rechercher un personnage...'"
                class="combobox-input"
              />
              <ul v-if="showPersonnageSuggestions && filteredPersonnages.length" class="suggestions-list">
                <li
                  v-for="personnage in filteredPersonnages"
                  :key="personnage.id"
                  @mousedown="selectPersonnage(personnage)"
                  class="suggestion-item"
                >
                  {{ personnage.nom }} ({{ personnage.projetTitre }})
                </li>
              </ul>
              <ul v-if="showPersonnageSuggestions && filteredPersonnages.length === 0" class="suggestions-list">
                <li class="suggestion-item no-results">Aucun personnage trouvé</li>
              </ul>
            </div>
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

      <!-- Liste des dialogues -->
      <div class="dialogues-list">
        <h3>Liste des dialogues</h3>
        
        <div class="filters">
          <div class="filter-group">
            <input v-model="searchTerm" type="text" placeholder="Rechercher par texte..." />
          </div>
          
          <div class="filter-group">
            <div class="combobox-container">
              <input
                type="text"
                v-model="filterSceneSearch"
                @focus="showFilterSceneSuggestions = true"
                @blur="hideFilterSceneSuggestions"
                @input="filterFilterScenes"
                :placeholder="filterSceneId ? getSceneName(filterSceneId) : 'Toutes les scènes'"
                class="combobox-input"
              />
              <ul v-if="showFilterSceneSuggestions && filteredFilterScenes.length" class="suggestions-list">
                <li
                  v-for="scene in filteredFilterScenes"
                  :key="scene.idScene"
                  @mousedown="selectFilterScene(scene)"
                  class="suggestion-item"
                >
                  {{ scene.titre }}
                </li>
                <li @mousedown="clearFilterScene" class="suggestion-item clear-filter">
                  ✕ Effacer le filtre
                </li>
              </ul>
            </div>
          </div>
          
          <div class="filter-group">
            <div class="combobox-container">
              <input
                type="text"
                v-model="filterPersonnageSearch"
                @focus="showFilterPersonnageSuggestions = true"
                @blur="hideFilterPersonnageSuggestions"
                @input="filterFilterPersonnages"
                :placeholder="filterPersonnageId ? (filterPersonnageId === 'null' ? 'Narration' : getPersonnageName(filterPersonnageId)) : 'Tous les personnages'"
                class="combobox-input"
              />
              <ul v-if="showFilterPersonnageSuggestions && filteredFilterPersonnages.length" class="suggestions-list">
                <li @mousedown="selectFilterPersonnage('null')" class="suggestion-item">
                  Narration
                </li>
                <li
                  v-for="personnage in filteredFilterPersonnages"
                  :key="personnage.id"
                  @mousedown="selectFilterPersonnage(personnage.id)"
                  class="suggestion-item"
                >
                  {{ personnage.nom }}
                </li>
                <li @mousedown="clearFilterPersonnage" class="suggestion-item clear-filter">
                  ✕ Effacer le filtre
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div v-if="loading" class="loading">Chargement des dialogues...</div>
        
        <div v-else-if="filteredDialogues.length === 0" class="no-data">
          Aucun dialogue trouvé.
        </div>
        
        <div v-else class="dialogues-grid">
          <div v-for="dialogue in filteredDialogues" :key="dialogue.id" class="dialogue-card">
            <div class="dialogue-header">
              <h4>{{ dialogue.personnageNom || 'Narration' }}</h4>
              <div class="dialogue-actions">
                <!-- Icône de commentaire -->
                <span class="icon-comment" @click="openDialogueComments(dialogue)" 
                      title="Commentaires">
                  💬 {{ dialogue.commentCount || 0 }}
                </span>
                <span class="icon-edit" @click="editDialogue(dialogue)">✏️</span>
                <span class="icon-delete" @click="deleteDialogue(dialogue.id)">🗑️</span>
              </div>
            </div>
            
            <div class="dialogue-info">
              <p><strong>Scène:</strong> {{ dialogue.sceneTitre }}</p>
              <p><strong>Ordre:</strong> {{ dialogue.ordre }}</p>
              <p v-if="dialogue.observation"><strong>Observation:</strong> {{ dialogue.observation }}</p>
              <p><strong>Créé le:</strong> {{ formatDate(dialogue.creeLe) }}</p>
            </div>
            
            <div class="dialogue-content">
              <p>{{ dialogue.texte }}</p>
            </div>
          </div>
        </div>

        <!-- Modale pour afficher les commentaires du dialogue -->
        <div v-if="showDialogueCommentModal" class="modal-overlay" @click="showDialogueCommentModal = false">
          <div class="modal-content" @click.stop>
            <h3>Commentaires - {{ selectedDialogue.personnageNom || 'Narration' }}</h3>
            
            <!-- Formulaire d'ajout de commentaire -->
            <div class="add-comment">
              <textarea v-model="newDialogueComment" placeholder="Ajouter un commentaire..." rows="3"></textarea>
              <button @click="addDialogueComment" class="add-comment-btn">Ajouter</button>
            </div>
            
            <!-- Liste des commentaires -->
            <div class="comments-list">
              <div v-for="comment in dialogueComments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.utilisateurNom }}</span>
                  <span class="comment-date">{{ formatDate(comment.creeLe) }}</span>
                </div>
                <div class="comment-content">
                  {{ comment.contenu }}
                </div>
                <div class="comment-actions" v-if="comment.utilisateurId === user.id">
                  <button @click="deleteDialogueComment(comment.id)" class="delete-comment-btn">Supprimer</button>
                </div>
              </div>
              
              <div v-if="dialogueComments.length === 0" class="no-comments">
                Aucun commentaire pour ce dialogue.
              </div>
            </div>
            
            <div class="modal-actions">
              <button type="button" @click="showDialogueCommentModal = false" class="cancel-btn">Fermer</button>
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
  name: 'CreationDialogue',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user')) || null,
      showProfileMenu: false,
      formData: {
        sceneId: '',
        personnageId: null,
        texte: '',
        ordre: 1,
        observation: ''
      },
      isEditing: false,
      editingId: null,
      scenes: [],
      personnages: [],
      searchTerm: '',
      filterSceneId: '',
      filterPersonnageId: '',
      loading: true,
      dialogues: [],
      showDialogueCommentModal: false,
      dialogueComments: [],
      newDialogueComment: '',
      selectedDialogue: {},
      existingOrders: [], 
      suggestedOrder: null,
      orderError: '',
      
      // Nouvelles données pour les zones de liste modifiable
      sceneSearch: '',
      personnageSearch: '',
      filterSceneSearch: '',
      filterPersonnageSearch: '',
      showSceneSuggestions: false,
      showPersonnageSuggestions: false,
      showFilterSceneSuggestions: false,
      showFilterPersonnageSuggestions: false,
      filteredScenes: [],
      filteredPersonnages: [],
      filteredFilterScenes: [],
      filteredFilterPersonnages: []
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
    }
  },
  async created() {
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

    await this.loadScenes();
    await this.loadPersonnages();
    await this.loadDialogues();
    document.addEventListener('click', this.handleClickOutside);
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
    },
    // Mettre à jour les listes filtrées quand les données changent
    scenes: {
      handler(newVal) {
        this.filteredScenes = [...newVal];
        this.filteredFilterScenes = [...newVal];
      },
      deep: true
    },
    personnages: {
      handler(newVal) {
        this.filteredPersonnages = [...newVal];
        this.filteredFilterPersonnages = [...newVal];
      },
      deep: true
    }
  },

  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    async loadScenes() {
      try {
        const response = await axios.get('/api/scenes');
        this.scenes = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des scènes:', error);
        alert('Erreur lors du chargement des scènes');
      }
    },
    async loadPersonnages() {
      try {
        const response = await axios.get('/api/personnages');
        this.personnages = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des personnages:', error);
        alert('Erreur lors du chargement des personnages');
      }
    },
    async loadDialogues() {
      this.loading = true;
      try {
        const response = await axios.get('/api/dialogues');
        this.dialogues = response.data;
        await this.loadCommentCounts();
      } catch (error) {
        console.error('Erreur lors du chargement des dialogues:', error);
        alert('Erreur lors du chargement des dialogues: ' + (error.response?.data?.message || error.message));
      } finally {
        this.loading = false;
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
      // Valider à nouveau avant soumission
      this.validateOrder();
      
      if (this.orderError) {
        alert('Veuillez corriger les erreurs avant de soumettre');
        return;
      }
      
      try {
        const payload = {
          ...this.formData,
          sceneId: parseInt(this.formData.sceneId),
          personnageId: this.formData.personnageId ? parseInt(this.formData.personnageId) : null,
          ordre: parseInt(this.formData.ordre)
        };
        
        if (this.isEditing) {
          await axios.put(`/api/dialogues/${this.editingId}`, payload);
          alert('Dialogue modifié avec succès!');
        } else {
          await axios.post('/api/dialogues', payload);
          alert('Dialogue créé avec succès!');
        }
        
        this.resetForm();
        await this.loadDialogues();
      } catch (error) {
        console.error('Erreur lors de la sauvegarde du dialogue:', error);
        
        // Vérifier si l'erreur est due à un doublon d'ordre
        if (error.response?.status === 400 && 
            error.response?.data?.message?.includes('ordre') &&
            error.response?.data?.message?.includes('existe')) {
          this.orderError = 'Cet ordre existe déjà dans la scène';
          alert('Erreur de validation: ' + this.orderError);
          
          // Recharger les ordres existants au cas où ils auraient changé
          await this.loadExistingOrders();
        } else {
          alert('Erreur: ' + (error.response?.data?.message || error.message));
        }
      }
    }, 

    editDialogue(dialogue) {
      this.formData = {
        sceneId: dialogue.sceneId.toString(),
        personnageId: dialogue.personnageId ? dialogue.personnageId.toString() : null,
        texte: dialogue.texte,
        ordre: dialogue.ordre,
        observation: dialogue.observation || ''
      };
      this.isEditing = true;
      this.editingId = dialogue.id;
      this.originalOrder = dialogue.ordre; // Conserver l'ordre original pour la validation
      
      // Mettre à jour les recherches avec les valeurs actuelles
      this.sceneSearch = this.getSceneName(dialogue.sceneId);
      if (dialogue.personnageId) {
        this.personnageSearch = this.getPersonnageName(dialogue.personnageId);
      } else {
        this.personnageSearch = '';
      }
      
      // Charger les ordres existants pour la scène sélectionnée
      this.loadExistingOrders();
      
      document.querySelector('.creation-form').scrollIntoView({ behavior: 'smooth' });
    },
    
    async deleteDialogue(dialogueId) {
      if (!confirm('Êtes-vous sûr de vouloir supprimer ce dialogue ?')) {
        return;
      }
      
      try {
        await axios.delete(`/api/dialogues/${dialogueId}`);
        await this.loadDialogues();
        alert('Dialogue supprimé avec succès!');
      } catch (error) {
        console.error('Erreur lors de la suppression du dialogue:', error);
        alert('Erreur: ' + (error.response?.data?.message || error.message));
      }
    },
    resetForm() {
      this.formData = {
        sceneId: '',
        personnageId: null,
        texte: '',
        ordre: 1,
        observation: ''
      };
      this.sceneSearch = '';
      this.personnageSearch = '';
      this.isEditing = false;
      this.editingId = null;
      this.originalOrder = null;
      this.existingOrders = [];
      this.suggestedOrder = null;
      this.orderError = '';
      this.showSceneSuggestions = false;
      this.showPersonnageSuggestions = false;
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
    },
    async openDialogueComments(dialogue) {
      this.selectedDialogue = dialogue;
      await this.loadDialogueComments(dialogue.id);
      this.showDialogueCommentModal = true;
    },
    async loadDialogueComments(dialogueId) {
      try {
        const response = await axios.get(`/api/dialogues/commentaires/dialogue/${dialogueId}`);
        this.dialogueComments = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des commentaires de dialogue:', error);
        this.dialogueComments = [];
        alert('Erreur lors du chargement des commentaires');
      }
    },
    async addDialogueComment() {
      if (!this.newDialogueComment.trim()) {
        alert('Le commentaire ne peut pas être vide');
        return;
      }
      
      try {
        await axios.post('/api/dialogues/commentaires', {
          contenu: this.newDialogueComment,
          dialogueId: this.selectedDialogue.id
        }, {
          headers: {
            'X-User-Id': this.user.id
          }
        });
        
        this.newDialogueComment = '';
        await this.loadDialogueComments(this.selectedDialogue.id);
        await this.loadDialogues();
        alert('Commentaire ajouté avec succès!');
      } catch (error) {
        console.error('Erreur lors de l\'ajout du commentaire:', error);
        alert('Erreur lors de l\'ajout du commentaire: ' + (error.response?.data?.message || error.message));
      }
    },
    async deleteDialogueComment(commentId) {
      if (!confirm('Êtes-vous sûr de vouloir supprimer ce commentaire ?')) {
        return;
      }
      
      try {
        await axios.delete(`/api/dialogues/commentaires/${commentId}`);
        await this.loadDialogueComments(this.selectedDialogue.id);
        await this.loadDialogues();
        alert('Commentaire supprimé avec succès!');
      } catch (error) {
        console.error('Erreur lors de la suppression du commentaire:', error);
        alert('Erreur lors de la suppression du commentaire: ' + (error.response?.data?.message || error.message));
      }
    },
    async loadCommentCounts() {
      for (let dialogue of this.dialogues) {
        try {
          const response = await axios.get(`/api/dialogues/commentaires/dialogue/${dialogue.id}/count`);
          dialogue.commentCount = response.data;
        } catch (error) {
          console.error('Erreur lors du chargement du nombre de commentaires:', error);
          dialogue.commentCount = 0;
        }
      }
    },
    
    // Méthodes pour les zones de liste modifiable
    filterScenes() {
      const searchTerm = this.sceneSearch.toLowerCase();
      if (!searchTerm) {
        this.filteredScenes = [...this.scenes];
        return;
      }
      this.filteredScenes = this.scenes.filter(scene => 
        scene.titre.toLowerCase().includes(searchTerm) || 
        scene.sequenceTitre.toLowerCase().includes(searchTerm)
      );
    },
    
    filterPersonnages() {
      const searchTerm = this.personnageSearch.toLowerCase();
      if (!searchTerm) {
        this.filteredPersonnages = [...this.personnages];
        return;
      }
      this.filteredPersonnages = this.personnages.filter(personnage => 
        personnage.nom.toLowerCase().includes(searchTerm) || 
        personnage.projetTitre.toLowerCase().includes(searchTerm)
      );
    },
    
    filterFilterScenes() {
      const searchTerm = this.filterSceneSearch.toLowerCase();
      if (!searchTerm) {
        this.filteredFilterScenes = [...this.scenes];
        return;
      }
      this.filteredFilterScenes = this.scenes.filter(scene => 
        scene.titre.toLowerCase().includes(searchTerm)
      );
    },
    
    filterFilterPersonnages() {
      const searchTerm = this.filterPersonnageSearch.toLowerCase();
      if (!searchTerm) {
        this.filteredFilterPersonnages = [...this.personnages];
        return;
      }
      this.filteredFilterPersonnages = this.personnages.filter(personnage => 
        personnage.nom.toLowerCase().includes(searchTerm)
      );
    },
    
    selectScene(scene) {
      this.formData.sceneId = scene.idScene;
      this.sceneSearch = scene.titre;
      this.showSceneSuggestions = false;
    },
    
    selectPersonnage(personnage) {
      this.formData.personnageId = personnage.id;
      this.personnageSearch = personnage.nom;
      this.showPersonnageSuggestions = false;
    },
    
    selectFilterScene(scene) {
      this.filterSceneId = scene.idScene;
      this.filterSceneSearch = scene.titre;
      this.showFilterSceneSuggestions = false;
    },
    
    selectFilterPersonnage(personnageId) {
      this.filterPersonnageId = personnageId;
      if (personnageId === 'null') {
        this.filterPersonnageSearch = 'Narration';
      } else {
        const personnage = this.personnages.find(p => p.id === parseInt(personnageId));
        this.filterPersonnageSearch = personnage ? personnage.nom : '';
      }
      this.showFilterPersonnageSuggestions = false;
    },
    
    clearFilterScene() {
      this.filterSceneId = '';
      this.filterSceneSearch = '';
      this.showFilterSceneSuggestions = false;
    },
    
    clearFilterPersonnage() {
      this.filterPersonnageId = '';
      this.filterPersonnageSearch = '';
      this.showFilterPersonnageSuggestions = false;
    },
    
    hideSceneSuggestions() {
      setTimeout(() => {
        this.showSceneSuggestions = false;
      }, 200);
    },
    
    hidePersonnageSuggestions() {
      setTimeout(() => {
        this.showPersonnageSuggestions = false;
      }, 200);
    },
    
    hideFilterSceneSuggestions() {
      setTimeout(() => {
        this.showFilterSceneSuggestions = false;
      }, 200);
    },
    
    hideFilterPersonnageSuggestions() {
      setTimeout(() => {
        this.showFilterPersonnageSuggestions = false;
      }, 200);
    },
    
    getSceneName(id) {
      const scene = this.scenes.find(s => s.idScene === parseInt(id));
      return scene ? scene.titre : '';
    },
    
    getPersonnageName(id) {
      const personnage = this.personnages.find(p => p.id === parseInt(id));
      return personnage ? personnage.nom : '';
    }
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

.header {
  background-color: #ffffff;
  border-bottom: 1px solid #e1e4e8;
  padding: 15px 0;
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  color: #2c3e50;
  font-size: 24px;
  margin: 0;
}

.profile-section {
  position: relative;
}

.profile-icon {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.profile-avatar {
  width: 32px;
  height: 32px;
  background-color: #3498db;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.profile-name {
  color: #2c3e50;
  font-size: 16px;
}

.profile-arrow {
  color: #7f8c8d;
  font-size: 12px;
}

.profile-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: white;
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  z-index: 100;
  padding: 15px;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.profile-avatar-large {
  width: 40px;
  height: 40px;
  background-color: #3498db;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.profile-details h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
}

.profile-details p {
  margin: 5px 0 0;
  color: #7f8c8d;
  font-size: 14px;
}

.role-badge {
  background-color: #e1e4e8;
  color: #2c3e50;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  display: inline-block;
}

.logout-btn {
  width: 100%;
  padding: 8px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.logout-btn:hover {
  background-color: #c0392b;
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


.cancel-btn {
  background-color: #95a5a6;
  color: white;
}

.cancel-btn:hover {
  background-color: #7f8c8d;
}

.dialogues-list h3 {
  color: #2c3e50;
  font-size: 20px;
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-group {
  flex: 1;
  min-width: 200px;
}

.filter-group input,
.filter-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.filter-group input:focus,
.filter-group select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.loading,
.no-data {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
  font-size: 16px;
  font-style: italic;
}

.dialogues-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.dialogue-card {
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  padding: 15px;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.dialogue-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.dialogue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e1e4e8;
}

.dialogue-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
}

.dialogue-actions {
  display: flex;
  gap: 10px;
}

.icon-comment,
.icon-edit,
.icon-delete {
  cursor: pointer;
  font-size: 16px;
}

.icon-comment:hover {
  color: #3498db;
}

.icon-edit:hover {
  color: #f39c12;
}

.icon-delete:hover {
  color: #e74c3c;
}

.dialogue-info {
  margin-bottom: 15px;
  font-size: 14px;
  color: #7f8c8d;
}

.dialogue-info p {
  margin: 5px 0;
}

.dialogue-info strong {
  color: #2c3e50;
}

.dialogue-content p {
  margin: 0;
  line-height: 1.5;
  color: #2c3e50;
  font-size: 14px;
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
  padding: 20px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  margin-top: 0;
  color: #2c3e50;
  border-bottom: 1px solid #e1e4e8;
  padding-bottom: 10px;
}

.add-comment {
  margin-bottom: 20px;
}

.add-comment textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  min-height: 80px;
  margin-bottom: 10px;
  box-sizing: border-box;
}

.add-comment-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.add-comment-btn:hover {
  background-color: #2980b9;
}

.comments-list {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 20px;
}

.comment-item {
  border: 1px solid #e1e4e8;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 10px;
  background-color: #f8f9fa;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.comment-author {
  font-weight: 600;
  color: #2c3e50;
}

.comment-date {
  color: #7f8c8d;
}

.comment-content {
  line-height: 1.4;
  color: #2c3e50;
}

.comment-actions {
  margin-top: 8px;
  text-align: right;
}

.delete-comment-btn {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.2s;
}

.delete-comment-btn:hover {
  background-color: #c0392b;
}

.no-comments {
  text-align: center;
  color: #7f8c8d;
  font-style: italic;
  padding: 20px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
}

.cancel-btn {
  background-color: #95a5a6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn:hover {
  background-color: #7f8c8d;
}

@media (max-width: 768px) {
  .creation-dialogue-container {
    padding: 15px;
  }

  .header h1 {
    font-size: 20px;
  }

  .filters {
    flex-direction: column;
    gap: 15px;
  }

  .dialogues-grid {
    grid-template-columns: 1fr;
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

  .modal-content {
    width: 95%;
    padding: 15px;
  }
  
  .comment-item {
    padding: 10px;
  }
}

@media (max-width: 480px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .profile-icon {
    width: 100%;
    justify-content: space-between;
  }

  .profile-menu {
    width: 100%;
    right: 0;
  }

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