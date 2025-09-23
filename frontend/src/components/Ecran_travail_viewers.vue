<template>
  <div class="ecran-travail">
    <!-- Header avec titre de l'épisode -->
    <header class="header">
      <h2>Viewers</h2>
      <h1>Épisode: {{ currentEpisode?.titre || 'Chargement...' }}</h1>
      <div class="navigation">
        <button class="nav-btn" @click="goToPrevPage" :disabled="!currentSequence || isLoading">Précédent</button>
        <button class="nav-btn" @click="goToNextPage" :disabled="!currentSequence || isLoading">Suivant</button>
      </div>
    </header>

    <!-- Indicateur de chargement -->
    <div v-if="isLoading" class="loading">Chargement en cours...</div>

    <!-- Message d'erreur -->
    <div v-if="error && !isLoading" class="error-message">
      {{ error }}
      <button class="retry-btn" @click="retryFetch">Réessayer</button>
    </div>

    <!-- Contenu de la séquence -->
    <main class="sequence-page" v-if="currentSequence && !isLoading">
      <h2>Séquence {{ currentSequence.ordre }}: {{ currentSequence.titre }}
        <span class="comment-icon" @click="toggleSequenceCommentSection">
          💬 {{ sequenceCommentCount }}
        </span>
      </h2>
      
      <!-- Section commentaires séquence -->
      <div v-if="showSequenceCommentSection" class="comment-section">
        <h4>Commentaires sur la séquence</h4>
        
        <!-- Formulaire d'ajout de commentaire -->
        <div class="add-comment">
          <textarea v-model="newSequenceComment" placeholder="Ajouter un commentaire..." rows="3"></textarea>
          <button @click="addSequenceComment" class="add-comment-btn">Ajouter</button>
        </div>
        
        <!-- Liste des commentaires -->
        <div class="comments-list">
          <div v-for="comment in sequenceComments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <span class="comment-author">{{ comment.utilisateurNom }}</span>
              <span class="comment-date">{{ formatDate(comment.creeLe) }}</span>
            </div>
            <div class="comment-content">
              {{ comment.contenu }}
            </div>
            <div class="comment-actions" v-if="comment.utilisateurId === user.id">
              <button @click="deleteSequenceComment(comment.id)" class="delete-comment-btn">Supprimer</button>
            </div>
          </div>
        </div>
      </div>

      <p><strong>Synopsis:</strong> {{ currentSequence.synopsis || 'Aucun synopsis' }}</p>
      <p><strong>Statut:</strong> {{ currentSequence.statutNom || 'Non défini' }}</p>

      <!-- Liste des scènes -->
      <div class="scenes-list">
        <div v-for="scene in currentSequence.scenes" :key="scene.idScene" class="scene-card">
          <h3>Scène {{ scene.ordre }}: {{ scene.titre }}
            <span class="comment-icon" @click="toggleSceneCommentSection(scene)">
              💬 {{ getSceneCommentCount(scene.idScene) }}
            </span>
          </h3>
          
          <!-- Section commentaires scène -->
          <div v-if="showSceneCommentModal && selectedScene?.idScene === scene.idScene" class="comment-section">
            <h4>Commentaires sur la scène</h4>
            
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
            </div>
            
            <button @click="closeSceneCommentModal" class="close-comments-btn">Fermer</button>
          </div>

          <p><strong>Synopsis:</strong> {{ scene.synopsis || 'Aucun synopsis' }}</p>
          <p><strong>Statut:</strong> {{ scene.statutNom || 'Non défini' }}</p>

          <!-- Lieux et Plateaux -->
          <div class="lieux-plateaux" v-if="scene.sceneLieus?.length">
            <h4>Lieux et Plateaux:</h4>
            <ul>
              <li v-for="sceneLieu in scene.sceneLieus" :key="sceneLieu.id">
                <strong>Lieu:</strong> {{ sceneLieu.lieuNom }} (Type: {{ sceneLieu.lieu?.typeLieu || 'Non spécifié' }})
                <span v-if="sceneLieu.plateauNom"> - <strong>Plateau:</strong> {{ sceneLieu.plateauNom }}</span>
                <p v-if="sceneLieu.descriptionUtilisation">Description: {{ sceneLieu.descriptionUtilisation }}</p>
              </li>
            </ul>
          </div>
          <p v-else>Aucun lieu ou plateau associé.</p>

          <!-- Dialogues -->
          <div class="dialogues" v-if="scene.dialogues?.length">
            <h4>Dialogues:</h4>
            <ul>
              <li v-for="dialogue in scene.dialogues" :key="dialogue.id">
                <strong>{{ dialogue.personnageNom || 'Narrateur' }}:</strong> {{ dialogue.texte }} <br><br>
                <span v-if="dialogue.observation">{{ dialogue.observation }}</span>
              </li>
            </ul>
          </div>
          <p v-else>Aucun dialogue associé.</p>
        </div>
      </div>
    </main>
    <div v-else-if="!isLoading" class="no-data">
      <p>Aucune séquence disponible pour cet épisode.</p>
    </div>
  </div>
</template>

<script setup>
import { useEcranTravailStore } from '../stores/ecranTravailStore';
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { watch } from 'vue';
import axios from 'axios';
import '../assets/css/ecran_travail.css';

const route = useRoute();
const store = useEcranTravailStore();

// Données pour les commentaires
const user = ref(JSON.parse(localStorage.getItem('user')) || null);
const showSequenceCommentSection = ref(false);
const newSequenceComment = ref('');
const sequenceComments = ref([]);
const sequenceCommentCount = ref(0);

const showSceneCommentModal = ref(false);
const selectedScene = ref(null);
const newSceneComment = ref('');
const sceneComments = ref([]);
const sceneCommentCounts = ref({});

onMounted(async () => {
  const projetId = route.params.idProjet || '1';
  await store.fetchEpisodes(projetId);
});

// Méthodes pour les commentaires de séquence
const toggleSequenceCommentSection = async () => {
  showSequenceCommentSection.value = !showSequenceCommentSection.value;
  if (showSequenceCommentSection.value) {
    await loadSequenceComments();
    await loadSequenceCommentCount();
  }
};

const loadSequenceComments = async () => {
  try {
    const response = await axios.get(`/api/sequence-commentaires/sequence/${store.currentSequence.idSequence}`);
    sequenceComments.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des commentaires de séquence:', error);
  }
};

const loadSequenceCommentCount = async () => {
  try {
    const response = await axios.get(`/api/sequence-commentaires/sequence/${store.currentSequence.idSequence}/count`);
    sequenceCommentCount.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement du nombre de commentaires:', error);
  }
};

const addSequenceComment = async () => {
  if (!newSequenceComment.value.trim()) return;
  
  try {
    await axios.post('/api/sequence-commentaires', {
      contenu: newSequenceComment.value,
      sequenceId: store.currentSequence.idSequence
    }, {
      headers: {
        'X-User-Id': user.value.id
      }
    });
    
    newSequenceComment.value = '';
    await loadSequenceComments();
    await loadSequenceCommentCount();
  } catch (error) {
    console.error('Erreur lors de l\'ajout du commentaire:', error);
    alert('Erreur lors de l\'ajout du commentaire');
  }
};

const deleteSequenceComment = async (commentId) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer ce commentaire ?')) {
    try {
      await axios.delete(`/api/sequence-commentaires/${commentId}`);
      await loadSequenceComments();
      await loadSequenceCommentCount();
    } catch (error) {
      console.error('Erreur lors de la suppression du commentaire:', error);
    }
  }
};

// Méthodes pour les commentaires de scène
const toggleSceneCommentSection = async (scene) => {
  selectedScene.value = scene;
  showSceneCommentModal.value = true;
  await loadSceneComments(scene.idScene);
};

const closeSceneCommentModal = () => {
  showSceneCommentModal.value = false;
  selectedScene.value = null;
  sceneComments.value = [];
};

const loadSceneComments = async (sceneId) => {
  try {
    const response = await axios.get(`/api/scene-commentaires/scene/${sceneId}`);
    sceneComments.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des commentaires de scène:', error);
  }
};

const getSceneCommentCount = (sceneId) => {
  return sceneCommentCounts.value[sceneId] || 0;
};

const loadSceneCommentCounts = async () => {
  if (!store.currentSequence?.scenes) return;
  
  for (const scene of store.currentSequence.scenes) {
    try {
      const response = await axios.get(`/api/scene-commentaires/scene/${scene.idScene}/count`);
      sceneCommentCounts.value[scene.idScene] = response.data;
    } catch (error) {
      console.error('Erreur lors du chargement du nombre de commentaires pour la scène:', error);
      sceneCommentCounts.value[scene.idScene] = 0;
    }
  }
};

const addSceneComment = async () => {
  if (!newSceneComment.value.trim() || !selectedScene.value) return;
  
  try {
    await axios.post('/api/scene-commentaires', {
      contenu: newSceneComment.value,
      sceneId: selectedScene.value.idScene
    }, {
      headers: {
        'X-User-Id': user.value.id
      }
    });
    
    newSceneComment.value = '';
    await loadSceneComments(selectedScene.value.idScene);
    await loadSceneCommentCounts();
  } catch (error) {
    console.error('Erreur lors de l\'ajout du commentaire:', error);
    alert('Erreur lors de l\'ajout du commentaire');
  }
};

const deleteSceneComment = async (commentId) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer ce commentaire ?')) {
    try {
      await axios.delete(`/api/scene-commentaires/${commentId}`);
      await loadSceneComments(selectedScene.value.idScene);
      await loadSceneCommentCounts();
    } catch (error) {
      console.error('Erreur lors de la suppression du commentaire:', error);
    }
  }
};

// Méthode utilitaire pour formater les dates
const formatDate = (date) => {
  return new Date(date).toLocaleString();
};

const goToNextPage = () => store.goToNextPage();
const goToPrevPage = () => store.goToPrevPage();
const retryFetch = () => store.fetchEpisodes(store.projetId || '1');

const currentEpisode = computed(() => store.currentEpisode);
const currentSequence = computed(() => store.currentSequence);
const error = computed(() => store.error);
const isLoading = computed(() => store.isLoading);

// Charger les compteurs de commentaires quand la séquence change
watch(() => store.currentSequence, async (newSequence) => {
  if (newSequence) {
    await loadSequenceCommentCount();
    await loadSceneCommentCounts();
  }
});
</script>

