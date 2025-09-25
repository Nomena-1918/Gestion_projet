<template>
  <div class="app-wrapper">
    <div class="ecran-travail">
      <!-- Header avec titre de l'épisode -->
      <header class="">
        <div class="navigation">
          <button class="nav-btn" @click="goToPrevPage" :disabled="!hasPrev || isLoading">Précédent</button>
          <button class="nav-btn" @click="goToNextPage" :disabled="!hasNext || isLoading">Suivant</button>
        </div>

        <h2> Épisode {{ currentEpisode?.ordre }} : </h2><br>     

       <div class="title-episode">
          <label> {{ currentEpisode?.titre || 'Chargement...' }} </label>

          <!-- Afficher le bouton modifier seulement si l'utilisateur a la permission -->
          <span v-if="userPermissions.canEditEpisode" class="icon-edit" @click="startEditEpisode">
            <i class="fas fa-pen icon" style="background: none;"></i>
          </span> <br>
        </div>
        
        <div class="syno-episode">
          <label><strong>Synopsis :</strong> {{ currentEpisode?.synopsis || 'Chargement...' }} </label><br>
          <label><strong>Statut :</strong> {{ currentEpisode?.statutNom || 'Chargement...' }} </label>
        </div>

        <div class="episode-equipe" v-if="currentEpisode">
          <div class="equipe-info">
            <strong>Équipe de l'épisode :</strong>
            <span v-if="currentEpisode.realisateur" class="realisateur-info">
              Réalisateur : {{ currentEpisode.realisateur.nom }}
            </span>
            <span v-if="currentEpisode.scenariste" class="scenariste-info">
              Scénariste : {{ currentEpisode.scenariste.nom }}
            </span>
          </div>
        </div>
      </header>

      <!-- Navigation par numéros d'épisodes -->
        <div class="episode-navigation">
          <span
            v-for="episode in episodes"
            :key="episode.idEpisode"
            class="episode-number"
            :class="{ 'active': episode.idEpisode === currentEpisode?.idEpisode, 'new-episode': episode.idEpisode === newlyCreatedEpisodeId }"
            @click="selectEpisode(episode.idEpisode)"
          >
            {{ episode.ordre }}
            <span v-if="episode.idEpisode === newlyCreatedEpisodeId" class="blinking-icon">✨</span>
          </span>
        </div>

      <!-- Indicateur de chargement -->
      <div v-if="isLoading" class="loading">Chargement en cours...</div>

      <!-- Message d'erreur -->
      <div v-if="error && !isLoading" class="error-message">
        {{ error }}
        <button class="retry-btn" @click="retryFetch">Réessayer</button>
      </div>

       <!-- Liens de création - Masquer ceux non autorisés -->
      <div class="liens">
        <button v-if="userPermissions.canEditEpisode" class="add-scene-btn" @click="goToAddEpisode">
          <i class="fas fa-plus-circle icon" style="color: #21294F;"></i> Episode
        </button>     
        <button v-if="userPermissions.canCreateSequence" class="add-scene-btn" @click="goToAddSequence">
          <i class="fas fa-plus-circle icon" style="color: #21294F;"></i> Séquence
        </button>
        <button v-if="userPermissions.canCreateLieu" class="add-scene-btn" @click="goToAddLieu">
          <i class="fas fa-plus-circle icon" style="color: #21294F;"></i> Lieu
        </button>
        <button v-if="userPermissions.canCreatePlateau" class="add-scene-btn" @click="goToAddPlateau">
          <i class="fas fa-plus-circle icon" style="color: #21294F;"></i> Plateau
        </button>
        <button v-if="userPermissions.canCreateComedien" class="add-scene-btn" @click="goToAddComedien">
          <i class="fas fa-plus-circle icon" style="color: #21294F;"></i> Comedien
        </button>
        <button v-if="userPermissions.canCreatePersonnage" class="add-scene-btn" @click="goToAddPersonnage">
          <i class="fas fa-plus-circle icon" style="color: #21294F;"></i> Personnage
        </button>
      </div>

      <div class="#">
        <h2>Les séquences :</h2>
      </div>

      <div class="#">
        <!-- Navigation par numéros de séquences -->
        <div v-if="currentEpisode && !isLoading" class="sequence-navigation">
          <span
            v-for="(sequence, index) in sequences"
            :key="sequence.idSequence"
            class="sequence-number"
            :class="{ 'active': sequence.idSequence === currentSequence?.idSequence, 'new-sequence': sequence.idSequence === newlyCreatedSequenceId }"
            @click="selectSequence(sequence.idSequence)"
          >
            <span v-if="index > 0"></span>
            {{ sequence.ordre }}
            <span v-if="sequence.idSequence === newlyCreatedSequenceId" class="blinking-icon">✨</span>
          </span>
        </div>
      </div>

      

      <!-- Contenu de la séquence -->
       <main class="sequence-page" v-if="currentSequence && !isLoading">
        <h2>
          Séquence 0{{ currentSequence.ordre }} : {{ currentSequence.titre }}
          <span v-if="userPermissions.canCreateSequence" class="icon-edit" @click="startEditSequence(currentSequence)">
            <i class="fas fa-pen icon" style="color: #17a2b8;"></i>
          </span>
          <span v-if="userPermissions.canCreateSequence" class="icon-delete" @click="deleteSequence(currentSequence.idSequence)">
            <i class="fas fa-trash icon" style="color: #dc3545;"></i>
          </span>
          <span class="comment-icon" @click="toggleSequenceCommentSection">
            <h3><i class="fas fa-comments icon" style="color: #21294F;"></i>{{ sequenceCommentCount }}</h3>
          </span>
        </h2>

        <!-- Section commentaires séquence -->
        <div v-if="showSequenceCommentSection" class="comment-section">
          <h4><i class="fas fa-comments icon" style="color: #21294F;"></i>Commentaires sur la séquence</h4>
          <div class="add-comment">
            <textarea v-model="newSequenceComment" placeholder="Ajouter un commentaire..." rows="3"></textarea>
            <button @click="addSequenceComment" class="add-comment-btn"><i class="fas fa-plus-circle icon"></i>Ajouter</button>
          </div>
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
                <button @click="deleteSequenceComment(comment.id)" class="delete-comment-btn"><i class="fas fa-trash icon"></i>Supprimer</button>
              </div>
            </div>
          </div>
        </div>
        
        <p><strong>Synopsis:</strong> {{ currentSequence.synopsis || 'Aucun synopsis' }}</p>
        <p><strong>Statut:</strong> {{ currentSequence.statutNom || 'Non défini' }}</p>

        <!-- Section scènes -->
        <div class="scenes-section">
          <div class="section-header">
            <h3>Scènes</h3>
            <button class="add-scene-btn" @click="goToAddScene"><i class="fas fa-plus-circle icon" style="color: #21294F;"></i> Scène</button>
          </div>


          <!-- Liste des scènes -->
          <div class="scenes-list">
            <div v-for="scene in currentSequence.scenes" :key="scene.idScene" class="scene-card">
              <h3>
                Scène {{ scene.ordre }}: {{ scene.titre }}
                <span class="icon-edit" @click="startEditScene(scene)"><i class="fas fa-pen icon" style="color: #17a2b8;"></i></span>
                <span class="icon-delete" @click="deleteScene(scene.idScene)"><i class="fas fa-trash icon" style="color: #dc3545;"></i></span>
                <span class="comment-icon" @click="toggleSceneCommentSection(scene)">
                  <i class="fas fa-comments icon" style="color: #21294F;"></i> {{ getSceneCommentCount(scene.idScene) }}
                </span>
              </h3>

              <!-- Section commentaires scène -->
              <div v-if="showSceneCommentModal && selectedScene?.idScene === scene.idScene" class="comment-section">
                <h4>Commentaires sur la scène</h4>
                <div class="add-comment">
                  <textarea v-model="newSceneComment" placeholder="Ajouter un commentaire..." rows="3"></textarea>
                  <button @click="addSceneComment" class="add-comment-btn"><i class="fas fa-plus-circle icon"></i>Ajouter</button>
                </div>
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
                      <button @click="deleteSceneComment(comment.id)" class="delete-comment-btn"><i class="fas fa-trash icon"></i>Supprimer</button>
                    </div>
                  </div>
                </div>
                <button @click="closeSceneCommentModal" class="close-comments-btn">Fermer</button>
              </div>

              <p><strong>Synopsis:</strong> {{ scene.synopsis || 'Aucun synopsis' }}</p>
              <p><strong>Statut:</strong> {{ scene.statutNom || 'Non défini' }}</p>

              <div class="section-header">
                <h4><i class="fas fa-map-pin icon" style="color: #dc3545;"></i>Lieux et Plateaux:</h4>
                <button class="add-lieu-btn" @click="openAddLieuModal(scene)"><i class="fas fa-plus-circle icon" style="color: #21294F;"></i>Lieu/Plateau</button>
              </div>
              <!-- Lieux et Plateaux -->
              <div class="lieux-plateaux" v-if="scene.sceneLieus?.length">
                <ul>
                  <li v-for="sceneLieu in scene.sceneLieus" :key="sceneLieu.id">
                    <strong>{{ sceneLieu.lieuNom || 'Lieu inconnu' }}</strong>
                    <span v-if="sceneLieu.plateauNom"> - <strong>Plateau:</strong> {{ sceneLieu.plateauNom }}</span>
                    <span class="icon-delete" @click="deleteSceneLieu(sceneLieu.id)"><i class="fas fa-trash icon" style="color: #dc3545;"></i></span>
                    <p v-if="sceneLieu.descriptionUtilisation">Description: {{ sceneLieu.descriptionUtilisation }}</p>
                  </li>
                </ul>
              </div>
              <p v-else>Aucun lieu ou plateau associé.</p>

              <div class="section-header">
                    <h4><i class="fas fa-comments icon" ></i>Dialogues:</h4> 
                </div>
              <!-- Dialogues -->
            

              <!-- Modifier la section des dialogues dans le template -->
              <div class="dialogues" v-if="scene.dialogues?.length">
                <ul>
                  <li v-for="dialogue in scene.dialogues" :key="dialogue.id" class="dialogue-item">
                    <div 
                      class="dialogue-text" 
                      @mouseup="openHighlightModal(dialogue, $event)"
                      :data-dialogue-id="dialogue.id"
                    >
                      <strong>{{ dialogue.personnageNom || 'Narrateur' }}:</strong> 
                      <span class="dialogue-content">{{ dialogue.texte }}</span>
                      
                      <!-- Afficher les surlignages -->
                      <template v-if="dialogueHighlights[dialogue.id]">
                        <span 
                          v-for="highlight in dialogueHighlights[dialogue.id]" 
                          :key="highlight.id"
                          class="text-highlight"
                          :style="{ backgroundColor: highlight.couleur.valeurHex }"
                          :title="`Surligné par ${highlight.utilisateurNom}`"
                        >
                          {{ highlight.texteSurligne }}
                        </span>
                      </template>
                    </div>
                    
                    <span v-if="dialogue.observation" class="dialogue-observation">
                      {{ dialogue.observation }}
                    </span>
                    
                    <div class="dialogue-actions">
                      <span class="icon-edit" @click="startEditDialogue(dialogue)">
                        <i class="fas fa-pen icon" style="color: #17a2b8;"></i>
                      </span>
                      <span class="icon-delete" @click="deleteDialogue(dialogue.id)">
                        <i class="fas fa-trash icon" style="color: #dc3545;"></i>
                      </span>
                      <span class="comment-icon" @click="toggleDialogueCommentSection(dialogue)">
                        <i class="fas fa-comment icon" style="color: #21294F;"></i> 
                        {{ getDialogueCommentCount(dialogue.id) }}
                      </span>
                      <span class="highlight-icon" @click="openHighlightModal(dialogue, $event)" title="Surligner">
                        <i class="fas fa-highlighter icon" style="color: #ffeb3b;"></i>
                      </span>
                    </div>
                  </li>
                </ul>
              </div>

              <div class="section-header">
                  <h4><i class="fas fa-comments icon" ></i></h4> 
                  <button class="add-dialogue-btn" @click="goToAddDialogue(scene.idScene)"><i class="fas fa-plus-circle icon" style="color: #21294F;"></i>Dialogue</button>
              </div>                    
            
            </div>
          </div>
        </div>
      </main>
      <div v-else-if="!isLoading" class="no-data">
        <p>Aucune séquence disponible pour cet épisode.</p>
      </div>

      <!-- Ajouter cette modale après les autres modales -->
      <div v-if="showHighlightModal" class="modal-overlay" @click="closeHighlightModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>
              <i class="fas fa-highlighter icon"></i>
              Surligner le texte
            </h3>
            <button @click="closeHighlightModal" class="close-btn">
              <i class="fas fa-times icon"></i>
            </button>
          </div>
          
          <div class="highlight-preview">
            <p><strong>Texte sélectionné :</strong></p>
            <div class="selected-text-preview">{{ selectedText }}</div>
          </div>
          
          <div class="color-selection">
            <h4>Choisir une couleur :</h4>
            <div class="color-palette">
              <div 
                v-for="color in availableColors" 
                :key="color.id"
                class="color-option"
                :class="{ 'selected': selectedColor?.id === color.id }"
                :style="{ backgroundColor: color.valeurHex }"
                @click="selectedColor = color"
                :title="color.nom"
              ></div>
            </div>
          </div>
          
          <div v-if="dialogueHighlights[selectedDialogueForHighlight?.id]?.length" class="existing-highlights">
            <h4>Surlignages existants :</h4>
            <div 
              v-for="highlight in dialogueHighlights[selectedDialogueForHighlight?.id]" 
              :key="highlight.id"
              class="highlight-item"
            >
              <span 
                class="highlight-sample"
                :style="{ backgroundColor: highlight.couleur.valeurHex }"
              ></span>
              <span class="highlight-text">{{ highlight.texteSurligne }}</span>
              <span class="highlight-info">par {{ highlight.utilisateurNom }}</span>
              <button 
                v-if="highlight.utilisateurId === user.id"
                @click="removeHighlight(highlight.id)"
                class="delete-highlight-btn"
              >
                <i class="fas fa-trash"></i>
              </button>
            </div>
          </div>
          
          <div class="modal-actions">
            <button type="button" @click="closeHighlightModal" class="cancel-btn">
              Annuler
            </button>
            <button 
              type="button" 
              @click="applyHighlight" 
              class="save-btn"
              :disabled="!selectedColor"
            >
              <i class="fas fa-highlighter"></i> Appliquer
            </button>
          </div>
        </div>
      </div>

      <!-- Modale pour éditer l'épisode -->
      <div v-if="showEditEpisodeModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>
              <i class="fas fa-edit icon"></i>
              Modifier l'épisode
            </h3>
            <button @click="closeEditEpisodeModal" class="close-btn"><i class="fas fa-times icon"></i></button>
          </div>
          <form @submit.prevent="saveEditedEpisode" class="edit-form">
            <div class="form-group">
              <label for="edit-episode-titre">Titre</label>
              <input
                type="text"
                id="edit-episode-titre"
                v-model="editingEpisode.titre"
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="edit-episode-synopsis">Synopsis</label>
              <textarea
                id="edit-episode-synopsis"
                v-model="editingEpisode.synopsis"
                rows="4"
                class="form-textarea"
              ></textarea>
            </div>
            <div class="form-group">
              <label for="edit-episode-ordre">Ordre</label>
              <input
                type="number"
                id="edit-episode-ordre"
                v-model="editingEpisode.ordre"
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="edit-episode-statut">Statut</label>
              <select
                id="edit-episode-statut"
                v-model="editingEpisode.statutId"
                required
                class="form-select"
              >
                <option value="">Sélectionnez un statut</option>
                <option v-for="statut in statutsEpisode" :key="statut.idStatutEpisode" :value="statut.idStatutEpisode">
                  {{ statut.nomStatutsEpisode }}
                </option>
              </select>
            </div>
            <div v-if="editEpisodeError" class="error-message">
              {{ editEpisodeError }}
            </div>
            <div class="modal-actions">
              <button type="button" @click="closeEditEpisodeModal" class="cancel-btn">Annuler</button>
              <button type="submit" class="save-btn" :disabled="editEpisodeLoading">
                {{ editEpisodeLoading ? 'Sauvegarde...' : 'Sauvegarder' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Modale pour éditer la séquence -->
      <div v-if="showEditSequenceModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>
              <i class="fas fa-edit icon"></i>
              Modifier la séquence
            </h3>
            <button @click="closeEditSequenceModal" class="close-btn"><i class="fas fa-times icon"></i></button>
          </div>
          <form @submit.prevent="saveEditedSequence" class="edit-form">
            <div class="form-group">
              <label for="edit-sequence-titre">Titre</label>
              <input
                type="text"
                id="edit-sequence-titre"
                v-model="editingSequence.titre"
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="edit-sequence-synopsis">Synopsis</label>
              <textarea
                id="edit-sequence-synopsis"
                v-model="editingSequence.synopsis"
                rows="4"
                class="form-textarea"
              ></textarea>
            </div>
            <div class="form-group">
              <label for="edit-sequence-ordre">Ordre</label>
              <input
                type="number"
                id="edit-sequence-ordre"
                v-model="editingSequence.ordre"
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="edit-sequence-statut">Statut</label>
              <select
                id="edit-sequence-statut"
                v-model="editingSequence.statutId"
                required
                class="form-select"
              >
                <option value="">Sélectionnez un statut</option>
                <option v-for="statut in statutsSequence" :key="statut.id" :value="statut.id">
                  {{ statut.nomStatutsSequence }}
                </option>
              </select>
            </div>
            <div v-if="editSequenceError" class="error-message">
              {{ editSequenceError }}
            </div>
            <div class="modal-actions">
              <button type="button" @click="closeEditSequenceModal" class="cancel-btn">Annuler</button>
              <button type="submit" class="save-btn" :disabled="editSequenceLoading">
                {{ editSequenceLoading ? 'Sauvegarde...' : 'Sauvegarder' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Modale pour éditer la scène -->
      <div v-if="showEditSceneModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>
              <i class="fas fa-edit icon"></i>
              Modifier la scène
            </h3>
            <button @click="closeEditSceneModal" class="close-btn"><i class="fas fa-times icon"></i></button>
          </div>
          <form @submit.prevent="saveEditedScene" class="edit-form">
            <div class="form-group">
              <label for="edit-scene-titre">Titre</label>
              <input
                type="text"
                id="edit-scene-titre"
                v-model="editingScene.titre"
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="edit-scene-synopsis">Synopsis</label>
              <textarea
                id="edit-scene-synopsis"
                v-model="editingScene.synopsis"
                rows="4"
                class="form-textarea"
              ></textarea>
            </div>
            <div class="form-group">
              <label for="edit-scene-ordre">Ordre</label>
              <input
                type="number"
                id="edit-scene-ordre"
                v-model="editingScene.ordre"
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="edit-scene-statut">Statut</label>
              <select
                id="edit-scene-statut"
                v-model="editingScene.statutId"
                required
                class="form-select"
              >
                <option value="">Sélectionnez un statut</option>
                <option v-for="statut in statutsScene" :key="statut.id" :value="statut.id">
                  {{ statut.nomStatutsScene }}
                </option>
              </select>
            </div>
            <div v-if="editSceneError" class="error-message">
              {{ editSceneError }}
            </div>
            <div class="modal-actions">
              <button type="button" @click="closeEditSceneModal" class="cancel-btn">Annuler</button>
              <button type="submit" class="save-btn" :disabled="editSceneLoading">
                {{ editSceneLoading ? 'Sauvegarde...' : 'Sauvegarder' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Modale pour éditer le dialogue -->
      <div v-if="showEditDialogueModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>
              <i class="fas fa-edit icon"></i>
              Modifier le dialogue
            </h3>
            <button @click="closeEditDialogueModal" class="close-btn"><i class="fas fa-times icon"></i></button>
          </div>
          <form @submit.prevent="saveEditedDialogue" class="edit-form">
            <div class="form-group">
              <label for="edit-dialogue-personnage">Personnage</label>
              <select
                id="edit-dialogue-personnage"
                v-model="editingDialogue.personnageId"
                required
                class="form-select"
              >
                <option :value="null">Narration (sans personnage)</option>
                <option v-for="personnage in personnages" :key="personnage.id" :value="personnage.id">
                  {{ personnage.nom }} ({{ personnage.projetTitre }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="edit-dialogue-texte">Texte</label>
              <textarea
                id="edit-dialogue-texte"
                v-model="editingDialogue.texte"
                rows="4"
                class="form-textarea"
                required
              ></textarea>
            </div>
            <div class="form-group">
              <label for="edit-dialogue-ordre">Ordre</label>
              <input
                type="number"
                id="edit-dialogue-ordre"
                v-model="editingDialogue.ordre"
                required
                class="form-input"
                @blur="validateOrder"
              />
              <div v-if="suggestedOrder" class="suggestion">Suggestion: {{ suggestedOrder }}</div>
              <div v-if="orderError" class="error-message">{{ orderError }}</div>
            </div>
            <div class="form-group">
              <label for="edit-dialogue-observation">Observation</label>
              <textarea
                id="edit-dialogue-observation"
                v-model="editingDialogue.observation"
                rows="3"
                class="form-textarea"
                placeholder="Observation optionnelle"
              ></textarea>
            </div>
            <div v-if="editDialogueError" class="error-message">
              {{ editDialogueError }}
            </div>
            <div class="modal-actions">
              <button type="button" @click="closeEditDialogueModal" class="cancel-btn">Annuler</button>
              <button type="submit" class="save-btn" :disabled="editDialogueLoading">
                {{ editDialogueLoading ? 'Sauvegarde...' : 'Sauvegarder' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Modale pour commentaires de dialogue -->
      <div v-if="showDialogueCommentModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>Commentaires du dialogue</h3>
            <button @click="closeDialogueCommentModal" class="close-btn"><i class="fas fa-times icon"></i></button>
          </div>
          <div class="add-comment">
            <textarea v-model="newDialogueComment" placeholder="Ajouter un commentaire..." rows="3"></textarea>
            <button @click="addDialogueComment" class="add-comment-btn">Ajouter</button>
          </div>
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
          </div>
        </div>
      </div>

      <!-- Modale pour ajouter un lieu/plateau -->
      <div v-if="showAddLieuModal" class="modal-overlay" @click="closeAddLieuModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>Ajouter un Lieu/Plateau à la scène: {{ selectedSceneForLieu?.titre }}</h3>
            <button @click="closeAddLieuModal" class="close-btn"><i class="fas fa-times icon"></i></button>
          </div>
          
          <form @submit.prevent="addSceneLieu" class="edit-form">
            <div class="form-group">
              <label for="lieu-select">Sélectionner un lieu existant</label>
              <select id="lieu-select" v-model="selectedLieuId" @change="loadAvailablePlateaux" class="form-select" required>
                <option value="">Choisir un lieu</option>
                <option v-if="availableLieux.length === 0" disabled>Aucun lieu disponible pour ce projet</option>
                <option v-for="lieu in availableLieux" :key="lieu.id" :value="lieu.id">
                  {{ lieu.nomLieu }} ({{ lieu.typeLieu }})
                </option>
              </select>
            </div>
            
            <div class="form-group" v-if="availablePlateaux.length > 0">
              <label for="plateau-select">Sélectionner un plateau existant (optionnel)</label>
              <select id="plateau-select" v-model="selectedPlateauId" class="form-select">
                <option value="">Aucun plateau</option>
                <option v-for="plateau in availablePlateaux" :key="plateau.id" :value="plateau.id">
                  {{ plateau.nom }} ({{ plateau.typePlateau }})
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label for="description-utilisation">Description d'utilisation</label>
              <textarea 
                id="description-utilisation" 
                v-model="descriptionUtilisation" 
                rows="3" 
                class="form-textarea" 
                placeholder="Description de l'utilisation de ce lieu/plateau dans la scène..."
              ></textarea>
            </div>
            
            <div v-if="addLieuError" class="error-message">
              {{ addLieuError }}
            </div>
            
            <div class="modal-actions">
              <button type="button" @click="closeAddLieuModal" class="cancel-btn">Annuler</button>
              <button type="submit" class="save-btn" :disabled="addLieuLoading">
                {{ addLieuLoading ? 'Ajout en cours...' : 'Ajouter' }}
              </button>
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
    </div>
  </div>
</template>

<script setup>
import { useEcranTravailStore } from '../stores/ecranTravailStore';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import '../assets/css/ecran_travail.css';

const route = useRoute();
const router = useRouter();
const store = useEcranTravailStore();

const showHighlightModal = ref(false);
const selectedDialogueForHighlight = ref(null);
const selectedText = ref('');
const selectedColor = ref(null);
const availableColors = ref([]);
const dialogueHighlights = ref({});

const userPermissions = ref({
    canEditEpisode: false,
    canCreateSequence: false,
    canCreateScene: false,
    canCreateDialogue: false,
    canCreateLieu: false,
    canCreatePlateau: false,
    canCreateComedien: false,
    canCreatePersonnage: false
});

// Variables réactives pour validation ordre
const existingOrders = ref([]);
const suggestedOrder = ref(null);
const orderError = ref('');

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

const showDialogueCommentModal = ref(false);
const selectedDialogue = ref(null);
const newDialogueComment = ref('');
const dialogueComments = ref([]);
const dialogueCommentCounts = ref({});

// Données pour la modale d'ajout de lieu
const showAddLieuModal = ref(false);
const selectedSceneForLieu = ref(null);
const selectedLieuId = ref(null);
const selectedPlateauId = ref(null);
const descriptionUtilisation = ref('');
const availableLieux = ref([]);
const availablePlateaux = ref([]);
const sceneLieus = ref([]);
const addLieuError = ref('');
const addLieuLoading = ref(false);

// Dans les données réactives
const personnages = ref([]);

// Données pour l'édition des épisodes
const showEditEpisodeModal = ref(false);
const editingEpisode = ref({
  id: null,
  titre: '',
  synopsis: '',
  ordre: null,
  statutId: null
});
const statutsEpisode = ref([]);
const editEpisodeError = ref('');
const editEpisodeLoading = ref(false);

// Données pour l'édition des séquences
const showEditSequenceModal = ref(false);
const editingSequence = ref({
  id: null,
  titre: '',
  synopsis: '',
  ordre: null,
  statutId: null
});
const statutsSequence = ref([]);
const editSequenceError = ref('');
const editSequenceLoading = ref(false);

// Données pour l'édition des scènes
const showEditSceneModal = ref(false);
const editingScene = ref({
  id: null,
  titre: '',
  synopsis: '',
  ordre: null,
  statutId: null
});
const statutsScene = ref([]);
const editSceneError = ref('');
const editSceneLoading = ref(false);

// Données pour l'édition des dialogues
const showEditDialogueModal = ref(false);
const editingDialogue = ref({
  id: null,
  personnageNom: '',
  texte: '',
  observation: ''
});
const editDialogueError = ref('');
const editDialogueLoading = ref(false);

// Projet ID
const projetId = ref(route.params.idProjet || '1');

// Variable pour suivre l'épisode et la séquence nouvellement créés
const newlyCreatedEpisodeId = ref(null);
const newlyCreatedSequenceId = ref(null);

// Propriétés calculées
const episodes = computed(() => store.episodes);
const sequences = computed(() => store.sequences);

// onMounted(async () => {
//   const projetIdLocal = route.params.idProjet || '1';
//   projetId.value = projetIdLocal;
//   await store.fetchEpisodes(projetIdLocal);
//   if (store.currentSequence) {
//     await loadSequenceCommentCount();
//     await loadSceneCommentCounts();
//     await loadDialogueCommentCounts();
//     await loadAvailableLieux();
//   }
//   await Promise.all([
//     loadStatutsEpisode(),
//     loadStatutsSequence(),
//     loadStatutsScene()
//   ]);
//   await loadPersonnages();

//   const episodeId = route.query.episodeId;
//   const sequenceId = route.query.sequenceId;

//   if (episodeId) {
//     await store.selectEpisodeById(episodeId);
//   }

//   if (sequenceId) {
//     await store.selectSequenceById(sequenceId);
//   }
// });

// Dans la section onMounted, charger les couleurs AVANT d'essayer de les utiliser
onMounted(async () => {
  const projetIdLocal = route.params.idProjet || '1';
  projetId.value = projetIdLocal;
  
  // Charger les couleurs disponibles en premier
  await loadAvailableColors();
  
  await store.fetchEpisodes(projetIdLocal);
  if (store.currentSequence) {
    await loadSequenceCommentCount();
    await loadSceneCommentCounts();
    await loadDialogueCommentCounts();
    await loadAvailableLieux();
  }
  await Promise.all([
    loadStatutsEpisode(),
    loadStatutsSequence(),
    loadStatutsScene()
  ]);
  await loadPersonnages();

  const episodeId = route.query.episodeId;
  const sequenceId = route.query.sequenceId;

  if (episodeId) {
    await store.selectEpisodeById(episodeId);
  }

  if (sequenceId) {
    await store.selectSequenceById(sequenceId);
  }
});

const checkUserPermissions = async (episodeId) => {
    if (!user.value) return;
    
    try {
        const response = await axios.get(`/api/episodes/${episodeId}/permissions`, {
            headers: {
                'X-User-Id': user.value.id
            }
        });
        
        userPermissions.value = response.data;
    } catch (error) {
        console.error('Erreur lors de la vérification des permissions:', error);
        // Par défaut, tout à false pour la sécurité
        Object.keys(userPermissions.value).forEach(key => {
            userPermissions.value[key] = false;
        });
    }
};

watch(() => store.currentEpisode, async (newEpisode) => {
    if (newEpisode) {
        await checkUserPermissions(newEpisode.idEpisode);
        
        // Afficher les noms du réalisateur et scénariste
        if (newEpisode.realisateur) {
            console.log('Réalisateur:', newEpisode.realisateur.nom);
        }
        if (newEpisode.scenariste) {
            console.log('Scénariste:', newEpisode.scenariste.nom);
        }
    }
});

const loadPersonnages = async () => {
  try {
    const response = await axios.get('/api/personnages');
    personnages.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des personnages:', error);
  }
};

// Méthodes pour le surlignage
const applyHighlight = async () => {
  if (!selectedColor.value || !selectedDialogueForHighlight.value || !selectedText.value) {
    alert('Veuillez sélectionner une couleur et du texte.');
    return;
  }

  try {
    const highlightData = {
      dialogueId: selectedDialogueForHighlight.value.id,
      couleur: {
        id: selectedColor.value.id,
        nom: selectedColor.value.nom,
        valeurHex: selectedColor.value.valeurHex
      },
      utilisateurId: user.value.id,
      texteSurligne: selectedText.value,
      debutIndex: 0,
      finIndex: selectedText.value.length
    };

    const response = await axios.post('/api/dialogue-surlignages', highlightData);
    
    if (response.status === 200 || response.status === 201) {
      // Recharger les surlignages
      await loadDialogueHighlights(selectedDialogueForHighlight.value.id);
      
      // Fermer la modale
      closeHighlightModal();
      
      alert('Texte surligné avec succès !');
    }
  } catch (error) {
    console.error('Erreur lors du surlignage:', error);
    alert('Erreur lors du surlignage: ' + (error.response?.data?.message || error.message));
  }
};

const removeHighlight = async (highlightId) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer ce surlignage ?')) {
    try {
      await axios.delete(`/api/dialogue-surlignages/${highlightId}`);
      await loadDialogueHighlights(selectedDialogueForHighlight.value.id);
    } catch (error) {
      console.error('Erreur lors de la suppression du surlignage:', error);
    }
  }
};

const closeHighlightModal = () => {
  showHighlightModal.value = false;
  selectedDialogueForHighlight.value = null;
  selectedText.value = '';
  selectedColor.value = null;
};


onMounted(async () => {
  // Récupération correcte de l'ID du projet depuis les paramètres de route
  projetId.value = route.params.idProjet;
  
  if (!projetId.value) {
    console.error('ID du projet non trouvé dans les params de route !');
    // Essayez de récupérer depuis query params ou store si nécessaire
    projetId.value = route.query.projetId || store.projetId;
    
    if (!projetId.value) {
      console.error('ID du projet non trouvable !');
      return;
    }
  }

  await store.fetchEpisodes(projetId.value);
  
  // Charger les données supplémentaires
  if (store.currentSequence) {
    await loadSequenceCommentCount();
    await loadSceneCommentCounts();
    await loadDialogueCommentCounts();
    await loadAvailableLieux();
  }
  
  await Promise.all([
    loadStatutsEpisode(),
    loadStatutsSequence(),
    loadStatutsScene()
  ]);
  
  await loadPersonnages();

  // Gestion de la navigation vers un épisode/spécifique
  const episodeId = route.query.episodeId;
  const sequenceId = route.query.sequenceId;

  if (episodeId) {
    await store.selectEpisodeById(episodeId);
  }

  if (sequenceId) {
    await store.selectSequenceById(sequenceId);
  }
});


// Watchers
watch(
  () => route.query.episodeId,
  async (newId) => {
    if (newId) {
      await store.selectEpisodeById(newId);
    }
  },
  { immediate: true }
);

watch(() => route.params.idProjet, async (newProjetId) => {
  if (newProjetId) {
    projetId.value = newProjetId;
    await loadAvailableLieux();
  }
});

watch(() => store.currentSequence, async (newSequence) => {
  if (newSequence) {
    await loadSequenceCommentCount();
    await loadSceneCommentCounts();
    await loadDialogueCommentCounts();
  }
});

// Watcher pour les changements de paramètres de route
watch(
  () => route.params.idProjet,
  async (newProjetId) => {
    if (newProjetId && newProjetId !== projetId.value) {
      projetId.value = newProjetId;
      await store.fetchEpisodes(projetId.value);
      await loadAvailableLieux();
    }
  },
  { immediate: true }
);

// Charger les statuts
const loadStatutsEpisode = async () => {
  try {
    const response = await axios.get('/api/statuts-episode');
    statutsEpisode.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des statuts d\'épisode:', error);
  }
};

const loadStatutsSequence = async () => {
  try {
    const response = await axios.get('/api/statuts-sequence');
    statutsSequence.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des statuts de séquence:', error);
  }
};

const loadStatutsScene = async () => {
  try {
    const response = await axios.get('/api/statuts-scene');
    statutsScene.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des statuts de scène:', error);
  }
};

// Charger les lieux et plateaux disponibles
const loadAvailableLieux = async () => {
  try {
    // Priorité 1: ID du projet depuis les paramètres de route
    if (route.params.idProjet) {
      projetId.value = route.params.idProjet;
    }
    // Priorité 2: ID du projet depuis le store
    else if (store.projetId) {
      projetId.value = store.projetId;
    }
    // Priorité 3: ID du projet depuis l'épisode courant (si disponible)
    else if (store.currentEpisode?.idEpisode) {
      try {
        const episodeResponse = await axios.get(`/api/episodes/${store.currentEpisode.idEpisode}`);
        projetId.value = episodeResponse.data.projetId;
      } catch (error) {
        console.warn('Impossible de récupérer l\'ID du projet depuis l\'épisode:', error);
      }
    }

    if (!projetId.value) {
      console.error('Impossible de déterminer l\'ID du projet');
      availableLieux.value = [];
      return;
    }

    const response = await axios.get(`/api/lieux/projets/${projetId.value}`);
    availableLieux.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des lieux:', error);
    availableLieux.value = [];
  }
};

const loadAvailablePlateaux = async () => {
  if (!selectedLieuId.value) {
    availablePlateaux.value = [];
    return;
  }
  
  try {
    const response = await axios.get(`/api/scene-lieux/lieux/${selectedLieuId.value}/plateaux`);
    availablePlateaux.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des plateaux:', error);
    availablePlateaux.value = [];
  }
};

// Helpers
const capitalize = (str) => str.charAt(0).toUpperCase() + str.slice(1);

const getStatuts = (type) => {
  if (type === 'episode') return statutsEpisode.value;
  if (type === 'sequence') return statutsSequence.value;
  if (type === 'scene') return statutsScene.value;
  return [];
};

const getNomField = (type) => `nomStatuts${capitalize(type)}`;

const getIdField = (type) => {
  if (type === 'episode') return 'idStatutEpisode';
  return 'id';
};

const getStatutIdByNom = (type, nom) => {
  const statuts = getStatuts(type);
  const nomField = getNomField(type);
  const statut = statuts.find(s => s[nomField] === nom);
  const idField = getIdField(type);
  return statut ? statut[idField] : null;
};

const getStatutNomById = (type, id) => {
  const statuts = getStatuts(type);
  const idField = getIdField(type);
  const nomField = getNomField(type);
  const statut = statuts.find(s => s[idField] === id);
  return statut ? statut[nomField] : '';
};

// Charger les couleurs disponibles
const loadAvailableColors = async () => {
  try {
    const response = await axios.get('/api/couleurs');
    availableColors.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des couleurs:', error);
  }
};

// Méthode pour ouvrir la modale de surlignage
const openHighlightModal = async (dialogue, event) => {
  // S'assurer que les couleurs sont chargées
  if (availableColors.value.length === 0) {
    await loadAvailableColors();
  }
  
  selectedDialogueForHighlight.value = dialogue;
  const selection = window.getSelection();
  const selectedTextContent = selection.toString().trim();
  
  if (selectedTextContent) {
    selectedText.value = selectedTextContent;
    selectedColor.value = null;
    showHighlightModal.value = true;
    
    // Charger les surlignages existants
    await loadDialogueHighlights(dialogue.id);
  } else {
    alert('Veuillez sélectionner du texte à surligner.');
  }
};

// Charger les surlignages d'un dialogue
const loadDialogueHighlights = async (dialogueId) => {
  try {
    const response = await axios.get(`/api/dialogue-surlignages/dialogue/${dialogueId}`);
    dialogueHighlights.value[dialogueId] = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des surlignages:', error);
  }
};

// Méthodes pour l'édition
const startEditEpisode = () => {
  if (!store.currentEpisode) return;
  editingEpisode.value = {
    id: store.currentEpisode.idEpisode,
    titre: store.currentEpisode.titre,
    synopsis: store.currentEpisode.synopsis || '',
    ordre: store.currentEpisode.ordre,
    statutId: getStatutIdByNom('episode', store.currentEpisode.statutNom)
  };
  editEpisodeError.value = '';
  showEditEpisodeModal.value = true;
};

const saveEditedEpisode = async () => {
  editEpisodeLoading.value = true;
  editEpisodeError.value = '';

  try {
    const url = `/api/episodes/${editingEpisode.value.id}`;
    const updateData = {
      titre: editingEpisode.value.titre,
      synopsis: editingEpisode.value.synopsis,
      ordre: parseInt(editingEpisode.value.ordre),
      statutId: editingEpisode.value.statutId
    };
    const response = await axios.put(url, updateData);
    if (response.status === 200) {
      store.currentEpisode.titre = editingEpisode.value.titre;
      store.currentEpisode.synopsis = editingEpisode.value.synopsis;
      store.currentEpisode.ordre = editingEpisode.value.ordre;
      store.currentEpisode.statutNom = getStatutNomById('episode', editingEpisode.value.statutId);
      const episodeIndex = store.episodes.findIndex(e => e.idEpisode === editingEpisode.value.id);
      if (episodeIndex !== -1) {
        store.episodes[episodeIndex] = { ...store.episodes[episodeIndex], ...editingEpisode.value };
      }
      closeEditEpisodeModal();
    }
  } catch (error) {
    console.error('Erreur lors de la mise à jour de l\'épisode:', error);
    editEpisodeError.value = error.response?.data?.message || 'Erreur lors de la mise à jour de l\'épisode';
  } finally {
    editEpisodeLoading.value = false;
  }
};

const closeEditEpisodeModal = () => {
  showEditEpisodeModal.value = false;
  editingEpisode.value = { id: null, titre: '', synopsis: '', ordre: null, statutId: null };
  editEpisodeError.value = '';
};

const startEditSequence = (sequence) => {
  editingSequence.value = {
    id: sequence.idSequence,
    titre: sequence.titre,
    synopsis: sequence.synopsis || '',
    ordre: sequence.ordre,
    statutId: getStatutIdByNom('sequence', sequence.statutNom)
  };
  editSequenceError.value = '';
  showEditSequenceModal.value = true;
};

const saveEditedSequence = async () => {
  editSequenceLoading.value = true;
  editSequenceError.value = '';

  try {
    const url = `/api/sequences/${editingSequence.value.id}`;
    const updateData = {
      titre: editingSequence.value.titre,
      synopsis: editingSequence.value.synopsis,
      ordre: parseInt(editingSequence.value.ordre),
      statutId: editingSequence.value.statutId
    };
    const response = await axios.put(url, updateData);
    if (response.status === 200) {
      store.currentSequence.titre = editingSequence.value.titre;
      store.currentSequence.synopsis = editingSequence.value.synopsis;
      store.currentSequence.ordre = editingSequence.value.ordre;
      store.currentSequence.statutNom = getStatutNomById('sequence', editingSequence.value.statutId);
      await store.fetchSequences(store.currentEpisode.idEpisode);
      closeEditSequenceModal();
    }
  } catch (error) {
    console.error('Erreur lors de la mise à jour de la séquence:', error);
    editSequenceError.value = error.response?.data?.message || 'Erreur lors de la mise à jour de la séquence';
  } finally {
    editSequenceLoading.value = false;
  }
};

const closeEditSequenceModal = () => {
  showEditSequenceModal.value = false;
  editingSequence.value = { id: null, titre: '', synopsis: '', ordre: null, statutId: null };
  editSequenceError.value = '';
};

const startEditScene = (scene) => {
  editingScene.value = {
    id: scene.idScene,
    titre: scene.titre,
    synopsis: scene.synopsis || '',
    ordre: scene.ordre,
    statutId: getStatutIdByNom('scene', scene.statutNom)
  };
  editSceneError.value = '';
  showEditSceneModal.value = true;
};

const saveEditedScene = async () => {
  editSceneLoading.value = true;
  editSceneError.value = '';

  try {
    const url = `/api/scenes/${editingScene.value.id}`;
    const updateData = {
      titre: editingScene.value.titre,
      synopsis: editingScene.value.synopsis,
      ordre: parseInt(editingScene.value.ordre),
      statutId: editingScene.value.statutId
    };
    const response = await axios.put(url, updateData);
    if (response.status === 200) {
      const sceneIndex = store.currentSequence.scenes.findIndex(s => s.idScene === editingScene.value.id);
      if (sceneIndex !== -1) {
        store.currentSequence.scenes[sceneIndex] = {
          ...store.currentSequence.scenes[sceneIndex],
          titre: editingScene.value.titre,
          synopsis: editingScene.value.synopsis,
          ordre: editingScene.value.ordre,
          statutNom: getStatutNomById('scene', editingScene.value.statutId)
        };
      }
      await store.fetchSequenceDetails(store.currentSequence.idSequence);
      closeEditSceneModal();
    }
  } catch (error) {
    console.error('Erreur lors de la mise à jour de la scène:', error);
    editSceneError.value = error.response?.data?.message || 'Erreur lors de la mise à jour de la scène';
  } finally {
    editSceneLoading.value = false;
  }
};

const closeEditSceneModal = () => {
  showEditSceneModal.value = false;
  editingScene.value = { id: null, titre: '', synopsis: '', ordre: null, statutId: null };
  editSceneError.value = '';
};

// Méthodes pour l'édition du dialogue
// Méthodes pour l'édition du dialogue
const startEditDialogue = async (dialogue) => {
  editingDialogue.value = {
    id: dialogue.id,
    personnageId: dialogue.personnageId || null,
    texte: dialogue.texte,
    observation: dialogue.observation || '',
    ordre: dialogue.ordre || 1,
    sceneId: dialogue.sceneId  // Gardé et envoyé dans update
  };
  editDialogueError.value = '';
  orderError.value = '';
  await loadExistingOrders();  // Charge les ordres existants pour validation
  showEditDialogueModal.value = true;
};

const loadExistingOrders = async () => {
  if (!editingDialogue.value.sceneId) return;
  try {
    const response = await axios.get(`/api/dialogues/scene/${editingDialogue.value.sceneId}`);
    existingOrders.value = response.data.map(d => d.ordre);
    calculateSuggestedOrder();
  } catch (error) {
    console.error('Erreur lors du chargement des ordres existants:', error);
  }
};

const calculateSuggestedOrder = () => {
  if (existingOrders.value.length === 0) {
    suggestedOrder.value = 1;
  } else {
    const maxOrder = Math.max(...existingOrders.value);
    suggestedOrder.value = maxOrder + 1;
  }
};

const validateOrder = () => {
  const ordre = parseInt(editingDialogue.value.ordre);
  if (isNaN(ordre) || ordre < 1) {
    orderError.value = 'L\'ordre doit être un nombre positif.';
    return false;
  }
  // Check doublon (exclure l'ordre actuel si édition)
  if (existingOrders.value.includes(ordre) && ordre !== parseInt(editingDialogue.value.ordreOriginal || editingDialogue.value.ordre)) {
    orderError.value = 'Cet ordre existe déjà dans la scène.';
    return false;
  }
  orderError.value = '';
  return true;
};

const saveEditedDialogue = async () => {
  if (!editingDialogue.value.texte.trim()) {
    editDialogueError.value = 'Le texte du dialogue est requis.';
    return;
  }
  if (!validateOrder()) {
    return;  // Bloque si ordre invalide
  }

  editDialogueLoading.value = true;
  editDialogueError.value = '';

  try {
    const updateData = {
      sceneId: editingDialogue.value.sceneId,  // Ajouté comme dans CreationDialogue
      personnageId: editingDialogue.value.personnageId,
      texte: editingDialogue.value.texte,
      observation: editingDialogue.value.observation || null,
      ordre: parseInt(editingDialogue.value.ordre)
    };

    const response = await axios.put(`/api/dialogues/${editingDialogue.value.id}`, updateData, {
      headers: {
        'X-User-Id': user.value.id  // Ajouté pour cohérence avec commentaires
      }
    });

    if (response.status === 200) {
      // Mise à jour locale optionnelle, mais recharge complet comme CreationDialogue
      await store.fetchSequenceDetails(store.currentSequence.idSequence);
      closeEditDialogueModal();
      // Alert comme dans CreationDialogue
      alert('Dialogue modifié avec succès!');
    }
  } catch (error) {
    console.error('Erreur lors de la mise à jour du dialogue:', error);
    editDialogueError.value = error.response?.data?.message || 'Erreur lors de la mise à jour du dialogue';
    alert(editDialogueError.value);  // Aligné sur CreationDialogue
  } finally {
    editDialogueLoading.value = false;
  }
};

const closeEditDialogueModal = () => {
  showEditDialogueModal.value = false;
  editingDialogue.value = { id: null, personnageId: null, texte: '', observation: '', ordre: 1, sceneId: null };
  editDialogueError.value = '';
  orderError.value = '';
  existingOrders.value = [];
  suggestedOrder.value = null;
};

// Méthodes pour la navigation
const goToNextPage = () => store.goToNextPage();
const goToPrevPage = () => store.goToPrevPage();
const retryFetch = () => store.fetchEpisodes(projetId.value);

const selectEpisode = async (episodeId) => {
  try {
    await store.selectEpisodeById(episodeId);
    router.push({ query: { ...route.query, episodeId } });
  } catch (error) {
    console.error('Erreur lors de la sélection de l\'épisode:', error);
  }
};

// const selectSequence = async (sequenceId) => {
//   try {
//     await store.selectSequenceById(sequenceId);
//     router.push({ query: { ...route.query, sequenceId } });
//   } catch (error) {
//     console.error('Erreur lors de la sélection de la séquence:', error);
//   }
// };

const selectSequence = async (sequenceId) => {
  await store.selectSequenceById(sequenceId);
  // Si newlyCreatedSequenceId n'est pas défini, supprimez cette ligne ou ajoutez const newlyCreatedSequenceId = ref(null);
  newlyCreatedSequenceId.value = null;
};

// Méthodes pour l'ajout
const goToAddEpisode = async () => {
  // Utiliser la variable projetId qui est maintenant correctement initialisée
  if (!projetId.value) {
    console.error('ID du projet non trouvé !');
    
    // Tentative de récupération alternative
    const alternativeId = route.params.idProjet || route.query.projetId || store.projetId;
    if (alternativeId) {
      projetId.value = alternativeId;
    } else {
      alert('ID du projet manquant. Veuillez réessayer.');
      return;
    }
  }
  
  try {
    // Navigation directe vers la page de création d'épisode
    router.push(`/projet/${projetId.value}/add-episode-ecran-travail`);
  } catch (error) {
    console.error('Erreur lors de la navigation:', error);
    alert('Erreur lors de la navigation');
  }
};

const goToAddSequence = async () => {
   if (!userPermissions.value.canCreateSequence) {
        alert('Vous n\'êtes pas autorisé à créer des séquences pour cet épisode.');
        return;
    }
  if (!store.currentEpisode?.idEpisode) {
    console.error('ID de l\'épisode non trouvé !');
    alert('ID de l\'épisode manquant. Veuillez réessayer.');
    return;
  }
  try {
    const response = await axios.post(`/api/sequences`, {
      episodeId: store.currentEpisode.idEpisode,
      titre: 'Nouvelle Séquence',
      ordre: store.sequences.length + 1,
      synopsis: '',
      statutId: getStatutIdByNom('sequence', 'Brouillon')
    });
    if (response.status === 201) { 
      const newSequenceId = response.data.idSequence;
      await store.fetchSequences(store.currentEpisode.idEpisode);
      newlyCreatedSequenceId.value = newSequenceId;
      await selectSequence(newSequenceId);
      setTimeout(() => {
        newlyCreatedSequenceId.value = null;
      }, 5000);
    }
  } catch (error) {
    console.error('Erreur lors de la création de la séquence:', error);
  }
  router.push(`/episode/${store.currentEpisode?.idEpisode}/add-sequence-ecran-travail`);
};

const goToAddScene = () => {
  if (!userPermissions.value.canCreateScene) {
        alert('Vous n\'êtes pas autorisé à créer des scènes pour cette séquence.');
        return;
    }
  
  const currentUrl = window.location.pathname + window.location.search;
  localStorage.setItem('lastEcranTravailUrl', currentUrl);
  
  router.push({
    path: `/sequence/${store.currentSequence?.idSequence}/add-scene-ecran-travail`,
    query: { 
      backUrl: currentUrl 
    }
  });
};

const goToAddDialogue = (sceneId) => {
  router.push(`/scene/${sceneId}/add-dialogue-scene-ecran-travail`);
};

const goToAddComedien = () => {
  
  const projetIdToUse = projetId.value || store.projetId || route.params.idProjet;
  
  if (!projetIdToUse) {
    console.error('ID du projet non trouvé !');
    alert('ID du projet manquant. Veuillez réessayer.');
    return;
  }
  
  console.log('Navigation vers création comédien avec projet ID:', projetIdToUse);
  router.push(`/projet/${projetIdToUse}/add-comedien-ecran-travail`);
};

const goToAddLieu = () => {
  // Priorité 1: ID du projet depuis les paramètres de route
  if (route.params.idProjet) {
    projetId.value = route.params.idProjet;
  }
  // Priorité 2: ID du projet depuis le store
  else if (store.projetId) {
    projetId.value = store.projetId;
  }
  // Priorité 3: ID du projet depuis l'épisode courant (si disponible)
  else if (store.currentEpisode?.idEpisode) {
    // On essaie de récupérer l'ID du projet depuis l'épisode
    axios.get(`/api/episodes/${store.currentEpisode.idEpisode}`)
      .then(response => {
        projetId.value = response.data.projetId;
        router.push(`/projet/${projetId.value}/add-lieu-scene-ecran-travail`);
      })
      .catch(error => {
        console.error('Erreur lors de la récupération du projet:', error);
        fallbackToProjectSelection();
      });
    return;
  } else {
    fallbackToProjectSelection();
    return;
  }

  if (!projetId.value) {
    fallbackToProjectSelection();
    return;
  }

  router.push(`/projet/${projetId.value}/add-lieu-scene-ecran-travail`);
};

// Méthode de fallback si l'ID du projet n'est pas trouvé
const fallbackToProjectSelection = () => {
  if (confirm('Impossible de déterminer le projet actuel. Souhaitez-vous sélectionner un projet manuellement ?')) {
    // Rediriger vers une page de sélection de projet ou utiliser une modale
    router.push('/projets'); // Adaptez cette route selon votre application
  }
};

const goToAddPlateau = () => {
  if (!projetId.value) {
    console.error('ID du projet non trouvé !');
    alert('ID du projet manquant. Veuillez réessayer.');
    return;
  }
  router.push(`/projet/${projetId.value}/add-plateau-scene`);
};

const goToAddPersonnage = () => {
  if (!projetId.value) {
    console.error('ID du projet non trouvé !');
    alert('ID du projet manquant. Veuillez réessayer.');
    return;
  }
  router.push(`/projet/${projetId.value}/add-personnage-ecran-travail`);
};

// Méthodes pour la modale d'ajout de lieu
const openAddLieuModal = async (scene) => {
  selectedSceneForLieu.value = scene;
  await loadAvailableLieux();
  await loadSceneLieus(scene.idScene);
  addLieuError.value = '';
  selectedLieuId.value = null;
  selectedPlateauId.value = null;
  descriptionUtilisation.value = '';
  availablePlateaux.value = [];
  showAddLieuModal.value = true;
};

const closeAddLieuModal = () => {
  showAddLieuModal.value = false;
  selectedSceneForLieu.value = null;
  selectedLieuId.value = null;
  selectedPlateauId.value = null;
  descriptionUtilisation.value = '';
  sceneLieus.value = [];
  addLieuError.value = '';
};

const loadSceneLieus = async (sceneId) => {
  try {
    const response = await axios.get(`/api/scene-lieux/scenes/${sceneId}`);
    sceneLieus.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des lieux de la scène:', error);
    sceneLieus.value = [];
  }
};

const addSceneLieu = async () => {
  if (!selectedLieuId.value) {
    addLieuError.value = 'Veuillez sélectionner un lieu.';
    return;
  }

  addLieuLoading.value = true;
  addLieuError.value = '';

  try {
    const lieuData = {
      sceneId: selectedSceneForLieu.value.idScene,
      lieuId: selectedLieuId.value,
      plateauId: selectedPlateauId.value || null,
      descriptionUtilisation: descriptionUtilisation.value
    };

    const response = await axios.post('/api/scene-lieux', lieuData);
    
    if (response.status === 201) {
      await loadSceneLieus(selectedSceneForLieu.value.idScene);
      await store.fetchSequenceDetails(store.currentSequence.idSequence);
      
      selectedLieuId.value = null;
      selectedPlateauId.value = null;
      descriptionUtilisation.value = '';
      availablePlateaux.value = [];
    }
  } catch (error) {
    console.error('Erreur lors de l\'ajout du lieu/plateau:', error);
    addLieuError.value = error.response?.data?.message || 'Erreur lors de l\'ajout du lieu/plateau';
  } finally {
    addLieuLoading.value = false;
  }
};

const removeLieuFromScene = async (sceneLieuId) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer ce lieu/plateau de la scène ?')) {
    try {
      await axios.delete(`/api/scene-lieux/${sceneLieuId}`);
      await loadSceneLieus(selectedSceneForLieu.value.idScene);
      await store.fetchSequenceDetails(store.currentSequence.idSequence);
    } catch (error) {
      console.error('Erreur lors de la suppression du lieu/plateau:', error);
      alert('Erreur lors de la suppression du lieu/plateau');
    }
  }
};

// Méthodes pour les commentaires
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
  if (!store.currentSequence) return;
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

const toggleDialogueCommentSection = async (dialogue) => {
  selectedDialogue.value = dialogue;
  showDialogueCommentModal.value = true;
  await loadDialogueComments(dialogue.id);
};

const closeDialogueCommentModal = () => {
  showDialogueCommentModal.value = false;
  selectedDialogue.value = null;
  dialogueComments.value = [];
};

const loadDialogueComments = async (dialogueId) => {
  try {
    const response = await axios.get(`/api/dialogues/commentaires/dialogue/${dialogueId}`);
    dialogueComments.value = response.data;
  } catch (error) {
    console.error('Erreur lors du chargement des commentaires de dialogue:', error);
  }
};

const getDialogueCommentCount = (dialogueId) => {
  return dialogueCommentCounts.value[dialogueId] || 0;
};

const loadDialogueCommentCounts = async () => {
  if (!store.currentSequence?.scenes) return;
  for (const scene of store.currentSequence.scenes) {
    if (scene.dialogues && scene.dialogues.length > 0) {
      for (const dialogue of scene.dialogues) {
        try {
          const response = await axios.get(`/api/dialogues/commentaires/dialogue/${dialogue.id}/count`);
          dialogueCommentCounts.value[dialogue.id] = response.data;
        } catch (error) {
          console.error('Erreur lors du chargement du nombre de commentaires pour le dialogue:', error);
          dialogueCommentCounts.value[dialogue.id] = 0;
        }
      }
    }
  }
};

const addDialogueComment = async () => {
  if (!newDialogueComment.value.trim() || !selectedDialogue.value) return;
  try {
    await axios.post('/api/dialogues/commentaires', {
      contenu: newDialogueComment.value,
      dialogueId: selectedDialogue.value.id
    }, {
      headers: {
        'X-User-Id': user.value.id
      }
    });
    newDialogueComment.value = '';
    await loadDialogueComments(selectedDialogue.value.id);
    await loadDialogueCommentCounts();
  } catch (error) {
    console.error('Erreur lors de l\'ajout du commentaire:', error);
    alert('Erreur lors de l\'ajout du commentaire');
  }
};

const deleteDialogueComment = async (commentId) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer ce commentaire ?')) {
    try {
      await axios.delete(`/api/dialogues/commentaires/${commentId}`);
      await loadDialogueComments(selectedDialogue.value.id);
      await loadDialogueCommentCounts();
    } catch (error) {
      console.error('Erreur lors de la suppression du commentaire:', error);
    }
  }
};

// Méthodes de suppression
const deleteSequence = async (sequenceId) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer cette séquence ?')) {
    try {
      await axios.delete(`/api/sequences/${sequenceId}`);
      await store.fetchSequences(store.currentEpisode.idEpisode);
    } catch (error) {
      console.error('Erreur lors de la suppression de la séquence:', error);
      alert('Erreur lors de la suppression de la séquence');
    }
  }
};

const deleteScene = async (sceneId) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer cette scène ?')) {
    try {
      await axios.delete(`/api/scenes/${sceneId}`);
      await store.fetchSequenceDetails(store.currentSequence.idSequence);
    } catch (error) {
      console.error('Erreur lors de la suppression de la scène:', error);
      alert('Erreur lors de la suppression de la scène');
    }
  }
};

const deleteDialogue = async (dialogueId) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer ce dialogue ?')) {
    try {
      await axios.delete(`/api/dialogues/${dialogueId}`);
      await store.fetchSequenceDetails(store.currentSequence.idSequence);
    } catch (error) {
      console.error('Erreur lors de la suppression du dialogue:', error);
      alert('Erreur lors de la suppression du dialogue');
    }
  }
};

const deleteSceneLieu = async (sceneLieuId) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer ce lieu/plateau ?')) {
    try {
      await axios.delete(`/api/scene-lieux/${sceneLieuId}`);
      await store.fetchSequenceDetails(store.currentSequence.idSequence);
    } catch (error) {
      console.error('Erreur lors de la suppression du lieu/plateau:', error);
      alert('Erreur lors de la suppression du lieu/plateau');
    }
  }
};

// Méthode utilitaire pour formater les dates
const formatDate = (date) => {
  return new Date(date).toLocaleString();
};

// Propriétés calculées
const currentEpisode = computed(() => store.currentEpisode);
const currentSequence = computed(() => store.currentSequence);
const error = computed(() => store.error);
const isLoading = computed(() => store.isLoading);
const hasNext = computed(() => store.hasNext);
const hasPrev = computed(() => store.hasPrev);
</script>

<style>
.sequence-navigation {
  display: flex;
  gap: 5px;
  margin: 10px 0;
  justify-content: center;
  align-items: center;
}

.sequence-number {
  cursor: pointer;
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #f9f9f9;
  font-size: 16px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
}

.sequence-number:hover {
  background-color: #e0e0e0;
}

.sequence-number.active {
  background-color: #007bff;
  color: white;
  border-color: #007bff;
}

.new-sequence {
  position: relative;
}

.blinking-icon {
  position: absolute;
  top: -5px;
  right: -5px;
  font-size: 12px;
  animation: blink 1s infinite;
}

.separator {
  margin: 0 5px;
  color: #000;
  font-weight: bold;
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0; }
  100% { opacity: 1; }
}

/* Ajouter ces styles */
.dialogue-text {
  cursor: text;
  user-select: text;
  position: relative;
  line-height: 1.6;
}

.text-highlight {
  padding: 2px 1px;
  border-radius: 3px;
  margin: 0 1px;
}

.highlight-icon {
  cursor: pointer;
  margin-left: 8px;
}

.color-palette {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 10px 0;
}

.color-option {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: transform 0.2s;
}

.color-option:hover {
  transform: scale(1.1);
}

.color-option.selected {
  border-color: #333;
  transform: scale(1.2);
}

.selected-text-preview {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  margin: 10px 0;
  border-left: 4px solid #007bff;
}

.existing-highlights {
  margin-top: 15px;
  max-height: 200px;
  overflow-y: auto;
}

.highlight-item {
  display: flex;
  align-items: center;
  padding: 5px;
  margin: 5px 0;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.highlight-sample {
  width: 20px;
  height: 20px;
  border-radius: 3px;
  margin-right: 10px;
}

.highlight-text {
  flex: 1;
  font-size: 0.9em;
}

.highlight-info {
  font-size: 0.8em;
  color: #666;
  margin-right: 10px;
}

.delete-highlight-btn {
  background: none;
  border: none;
  color: #dc3545;
  cursor: pointer;
  padding: 5px;
}
.episode-number.accessible {
    border: 2px solid #4CAF50;
}

.episode-number:not(.accessible) {
    opacity: 0.6;
    cursor: not-allowed;
}

.equipe-info {
    margin-top: 10px;
    padding: 10px;
    background-color: #f5f5f5;
    border-radius: 5px;
}

.realisateur-info, .scenariste-info {
    display: block;
    margin: 5px 0;
    font-style: italic;
}

/* Masquer les boutons non autorisés */
button[disabled] {
    opacity: 0.5;
    cursor: not-allowed;
}
</style>
