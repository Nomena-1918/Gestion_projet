<template>
  <div class="creation-comedien">
    <!-- <h2>Création et Gestion des Comédiens</h2> -->
    
    <div class="form-header">
        <button @click="goBack" class="back-btn"> <i class="fas fa-sign icon"></i>
          Retour
        </button>
        <h2>Créer votre comédien</h2>
      </div>

    <!-- Formulaire de création -->
    <div class="form-container">
      <h3>{{ isEditing ? 'Modifier' : 'Créer' }} un comédien</h3>
      <form @submit.prevent="submitForm" class="comedien-form" enctype="multipart/form-data">
        <div class="form-group">
          <label for="nom">Nom du comédien *</label>
          <input
            type="text"
            id="nom"
            v-model="formData.nom"
            required
            placeholder="Entrez le nom du comédien"
          />
        </div>

        <div class="form-group">
          <label for="age">Âge</label>
          <input
            type="number"
            id="age"
            v-model="formData.age"
            placeholder="Âge du comédien"
          />
        </div>

        <div class="form-group">
          <label for="email">Email *</label>
          <input
            type="email"
            id="email"
            v-model="formData.email"
            required
            placeholder="email@exemple.com"
          />
        </div>

        <div class="form-group">
          <label for="photo">Photo</label>
          <div class="photo-upload">
            <input
              type="file"
              id="photo"
              ref="photoInput"
              @change="handlePhotoUpload"
              accept="image/*"
              class="photo-input"
            />
            <label for="photo" class="photo-label">
              <span v-if="!previewPhoto">📷 Choisir une photo</span>
              <span v-else>📷 Changer la photo</span>
            </label>
            <div v-if="previewPhoto" class="photo-preview">
              <img :src="previewPhoto" alt="Aperçu de la photo" class="preview-image" />
              <button type="button" @click="removePhoto" class="remove-photo-btn">×</button>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button
            type="submit"
            :disabled="isSubmitting"
            class="btn-primary"
          >
            {{ isEditing ? 'Modifier' : 'Créer' }} le comédien
          </button>
          <button
            v-if="isEditing"
            type="button"
            @click="resetForm"
            class="btn-secondary"
          >
            Annuler
          </button>
        </div>
      </form>
    </div>

    <!-- Liste des comédiens -->
    <div class="comediens-list">
      <h3>Liste des comédiens</h3>
      
      <div v-if="loading" class="loading">Chargement...</div>
      
      <div v-else-if="comediens.length === 0" class="empty-state">
        Aucun comédien créé pour le moment.
      </div>

      <div v-else class="comediens-grid">
        <div
          v-for="comedien in comediens"
          :key="comedien.id"
          class="comedien-card"
        >
          <div class="comedien-photo">
            <img 
              v-if="comedien.photoPath" 
              :src="getPhotoUrl(comedien.photoPath)" 
              :alt="comedien.nom"
              class="photo"
            />
            <div v-else class="photo-placeholder">📷</div>
          </div>
          
          <div class="comedien-info">
            <h4>{{ comedien.nom }}</h4>
            <p><strong>Âge:</strong> {{ comedien.age }} ans</p>
            <p><strong>Email:</strong> {{ comedien.email }}</p>
            <p class="date-info">
              Créé le: {{ formatDate(comedien.creeLe) }}
            </p>
          </div>

          <div class="comedien-actions">
            <button
              @click="editComedien(comedien)"
              class="btn-edit"
              title="Modifier"
            >
              ✏️
            </button>
            <button
              @click="deleteComedien(comedien.id)"
              class="btn-delete"
              title="Supprimer"
            >
              🗑️
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CreationComedien',
  data() {
    return {
      formData: {
        nom: '',
        age: null,
        email: '',
        photo: null
      },
      comediens: [],
      isEditing: false,
      editingId: null,
      isSubmitting: false,
      loading: false,
      previewPhoto: null,
      currentPhotoFile: null
    };
  },
  async mounted() {
    await this.loadComediens();
  },
  methods: {
    async loadComediens() {
      this.loading = true;
      try {
        const response = await axios.get('/api/comediens');
        this.comediens = response.data;
      } catch (error) {
        console.error('Erreur lors du chargement des comédiens:', error);
        alert('Erreur lors du chargement des comédiens');
      } finally {
        this.loading = false;
      }
    },

    getPhotoUrl(photoPath) {
      return `/api/comediens/photo/${photoPath}`;
    },

    handlePhotoUpload(event) {
      const file = event.target.files[0];
      if (file) {
        this.currentPhotoFile = file;
        
        // Créer un aperçu de l'image
        const reader = new FileReader();
        reader.onload = (e) => {
          this.previewPhoto = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    },

    removePhoto() {
      this.previewPhoto = null;
      this.currentPhotoFile = null;
      this.$refs.photoInput.value = '';
    },

    async submitForm() {
      this.isSubmitting = true;
      try {
        const formData = new FormData();
        formData.append('nom', this.formData.nom);
        formData.append('age', this.formData.age);
        formData.append('email', this.formData.email);
        
        if (this.currentPhotoFile) {
          formData.append('photo', this.currentPhotoFile);
        }

        let response;
        if (this.isEditing) {
          response = await axios.put(`/api/comediens/${this.editingId}`, formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });
          alert('Comédien modifié avec succès');
        } else {
          response = await axios.post('/api/comediens', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });
          alert('Comédien créé avec succès');
        }

        this.resetForm();
        await this.loadComediens();
      } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error);
        alert('Erreur lors de la sauvegarde: ' + (error.response?.data?.message || error.message));
      } finally {
        this.isSubmitting = false;
      }
    },

    goBack() 
    {
        this.$router.go(-1);
      },

    editComedien(comedien) {
      this.formData = {
        nom: comedien.nom,
        age: comedien.age,
        email: comedien.email
      };
      
      if (comedien.photoPath) {
        this.previewPhoto = this.getPhotoUrl(comedien.photoPath);
      } else {
        this.previewPhoto = null;
      }
      
      this.currentPhotoFile = null;
      this.isEditing = true;
      this.editingId = comedien.id;
      
      // Scroll to form
      document.querySelector('.form-container').scrollIntoView({ behavior: 'smooth' });
    },

    async deleteComedien(id) {
      if (!confirm('Êtes-vous sûr de vouloir supprimer ce comédien ?')) {
        return;
      }

      try {
        await axios.delete(`/api/comediens/${id}`);
        alert('Comédien supprimé avec succès');
        await this.loadComediens();
      } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        alert('Erreur lors de la suppression');
      }
    },

    resetForm() {
      this.formData = {
        nom: '',
        age: null,
        email: ''
      };
      this.previewPhoto = null;
      this.currentPhotoFile = null;
      this.isEditing = false;
      this.editingId = null;
      this.$refs.photoInput.value = '';
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
};
</script>

<style scoped>
.creation-comedien {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  color: #2c3e50;
  margin-bottom: 30px;
  text-align: center;
}

.form-container {
  background: #f8f9fa;
  padding: 25px;
  border-radius: 10px;
  margin-bottom: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.comedien-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group:last-child {
  grid-column: 1 / -1;
}

label {
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
}

input {
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

input:focus {
  outline: none;
  border-color: #3498db;
}

/* Styles pour l'upload de photo */
.photo-upload {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.photo-input {
  display: none;
}

.photo-label {
  padding: 12px;
  border: 2px dashed #ddd;
  border-radius: 6px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.photo-label:hover {
  border-color: #3498db;
}

.photo-preview {
  position: relative;
  width: 150px;
  height: 150px;
  margin-top: 10px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
  border: 2px solid #ddd;
}

.remove-photo-btn {
  position: absolute;
  top: -10px;
  right: -10px;
  background: #ff6b6b;
  color: white;
  border: none;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-photo-btn:hover {
  background: #ee5253;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}

.btn-primary {
  background: #3498db;
  color: white;
  padding: 12px 25px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s ease;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-primary:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
  padding: 12px 25px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s ease;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.comediens-list {
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.comediens-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.comedien-card {
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  padding: 20px;
  background: white;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.comedien-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.comedien-photo {
  text-align: center;
  margin-bottom: 15px;
}

.photo {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 50%;
  border: 3px solid #f8f9fa;
}

.photo-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  border: 3px solid #e9ecef;
}

.comedien-info {
  flex: 1;
  margin-bottom: 15px;
}

.comedien-info h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 18px;
}

.comedien-info p {
  margin: 5px 0;
  color: #555;
}

.date-info {
  font-size: 12px;
  color: #888;
  margin-top: 10px !important;
  font-style: italic;
}

.comedien-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-edit, .btn-delete {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 5px;
  border-radius: 4px;
  transition: background 0.2s ease;
}

.btn-edit:hover {
  background: #ffeaa7;
}

.btn-delete:hover {
  background: #ff7675;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #888;
  font-style: italic;
}

@media (max-width: 768px) {
  .comedien-form {
    grid-template-columns: 1fr;
  }
  
  .comediens-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn-primary, .btn-secondary {
    width: 100%;
  }
}
</style>