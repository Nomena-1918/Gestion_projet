<template>
  <div class="gestion-equipe">
    <div class="header">
      <h1>Gestion de l'Équipe Créative</h1>
      <p>Créez et gérez les scénaristes et réalisateurs de vos projets</p>
    </div>

    <!-- Onglets pour switcher entre les formulaires -->
    <div class="tabs">
      <button 
        @click="activeTab = 'scenariste'" 
        :class="{ 'active': activeTab === 'scenariste' }"
        class="tab-button"
      >
        <i class="fas fa-pen-fancy"></i> Scénariste
      </button>
      <button 
        @click="activeTab = 'realisateur'" 
        :class="{ 'active': activeTab === 'realisateur' }"
        class="tab-button"
      >
        <i class="fas fa-video"></i> Réalisateur
      </button>
    </div>

    <!-- Formulaire de création -->
    <div class="form-container">
      <!-- Formulaire Scénariste -->
      <div v-if="activeTab === 'scenariste'" class="form-section">
        <h2>Créer un nouveau Scénariste</h2>
        <form @submit.prevent="creerScenariste" class="creation-form">
          <div class="form-group">
            <label for="nomScenariste">Nom complet *</label>
            <input
              id="nomScenariste"
              v-model="nouveauScenariste.nom"
              type="text"
              required
              placeholder="Ex: Jean Dupont"
            />
          </div>

          <div class="form-group">
            <label for="emailScenariste">Email *</label>
            <input
              id="emailScenariste"
              v-model="nouveauScenariste.email"
              type="email"
              required
              placeholder="exemple@email.com"
            />
          </div>

          <div class="form-group">
            <label for="passwordScenariste">Mot de passe *</label>
            <input
              id="passwordScenariste"
              v-model="nouveauScenariste.motDePasse"
              type="password"
              required
              placeholder="Minimum 8 caractères"
            />
          </div>

          <div class="form-group">
            <label for="specialiteScenariste">Spécialité</label>
            <input
              id="specialiteScenariste"
              v-model="nouveauScenariste.specialite"
              type="text"
              placeholder="Ex: Comédie, Drame, etc."
            />
          </div>

          <div class="form-group">
            <label for="biographieScenariste">Biographie</label>
            <textarea
              id="biographieScenariste"
              v-model="nouveauScenariste.biographie"
              rows="4"
              placeholder="Description du scénariste..."
            ></textarea>
          </div>

          <button type="submit" class="btn-primary" :disabled="chargement">
            <span v-if="chargement">Création...</span>
            <span v-else>Créer le Scénariste</span>
          </button>
        </form>
      </div>

      <!-- Formulaire Réalisateur -->
      <div v-if="activeTab === 'realisateur'" class="form-section">
        <h2>Créer un nouveau Réalisateur</h2>
        <form @submit.prevent="creerRealisateur" class="creation-form">
          <div class="form-group">
            <label for="nomRealisateur">Nom complet *</label>
            <input
              id="nomRealisateur"
              v-model="nouveauRealisateur.nom"
              type="text"
              required
              placeholder="Ex: Sophie Martin"
            />
          </div>

          <div class="form-group">
            <label for="emailRealisateur">Email *</label>
            <input
              id="emailRealisateur"
              v-model="nouveauRealisateur.email"
              type="email"
              required
              placeholder="exemple@email.com"
            />
          </div>

          <div class="form-group">
            <label for="passwordRealisateur">Mot de passe *</label>
            <input
              id="passwordRealisateur"
              v-model="nouveauRealisateur.motDePasse"
              type="password"
              required
              placeholder="Minimum 8 caractères"
            />
          </div>

          <div class="form-group">
            <label for="specialiteRealisateur">Spécialité</label>
            <input
              id="specialiteRealisateur"
              v-model="nouveauRealisateur.specialite"
              type="text"
              placeholder="Ex: Thriller, Romance, etc."
            />
          </div>

          <div class="form-group">
            <label for="biographieRealisateur">Biographie</label>
            <textarea
              id="biographieRealisateur"
              v-model="nouveauRealisateur.biographie"
              rows="4"
              placeholder="Description du réalisateur..."
            ></textarea>
          </div>

          <button type="submit" class="btn-primary" :disabled="chargement">
            <span v-if="chargement">Création...</span>
            <span v-else>Créer le Réalisateur</span>
          </button>
        </form>
      </div>
    </div>

    <!-- Barre de recherche et filtres -->
    <div class="search-section">
      <div class="search-bar">
        <i class="fas fa-search"></i>
        <input
          v-model="recherche"
          type="text"
          placeholder="Rechercher par nom, email ou spécialité..."
          class="search-input"
        />
      </div>

      <div class="filters">
        <select v-model="filtreRole" class="filter-select">
          <option value="tous">Tous les rôles</option>
          <option value="SCENARISTE">Scénaristes</option>
          <option value="REALISATEUR">Réalisateurs</option>
        </select>
      </div>
    </div>

    <!-- Liste des membres de l'équipe -->
    <div class="liste-equipe">
      <!-- Scénaristes -->
      <div class="role-section">
        <h3>
          <i class="fas fa-pen-fancy"></i>
          Scénaristes ({{ scenaristesFiltres.length }})
        </h3>
        <div v-if="scenaristesFiltres.length === 0" class="empty-state">
          <p>Aucun scénariste trouvé</p>
        </div>
        <div v-else class="cards-container">
          <div
            v-for="scenariste in scenaristesFiltres"
            :key="scenariste.idScenariste"
            class="member-card"
          >
            <div class="member-header">
              <div class="member-avatar">
                <i class="fas fa-user"></i>
              </div>
              <div class="member-info">
                <h4>{{ scenariste.nom }}</h4>
                <p class="member-email">{{ scenariste.email }}</p>
                <span class="role-badge scenariste">Scénariste</span>
              </div>
            </div>
            <div class="member-details">
              <p><strong>Spécialité:</strong> {{ scenariste.specialite || 'Non spécifiée' }}</p>
              <p><strong>Biographie:</strong> {{ scenariste.biographie || 'Non renseignée' }}</p>
            </div>
            <div class="member-actions">
              <button @click="modifierScenariste(scenariste)" class="btn-edit">
                <i class="fas fa-edit"></i> Modifier
              </button>
              <button @click="supprimerScenariste(scenariste.idScenariste)" class="btn-delete">
                <i class="fas fa-trash"></i> Supprimer
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Réalisateurs -->
      <div class="role-section">
        <h3>
          <i class="fas fa-video"></i>
          Réalisateurs ({{ realisateursFiltres.length }})
        </h3>
        <div v-if="realisateursFiltres.length === 0" class="empty-state">
          <p>Aucun réalisateur trouvé</p>
        </div>
        <div v-else class="cards-container">
          <div
            v-for="realisateur in realisateursFiltres"
            :key="realisateur.idRealisateur"
            class="member-card"
          >
            <div class="member-header">
              <div class="member-avatar">
                <i class="fas fa-user"></i>
              </div>
              <div class="member-info">
                <h4>{{ realisateur.nom }}</h4>
                <p class="member-email">{{ realisateur.email }}</p>
                <span class="role-badge realisateur">Réalisateur</span>
              </div>
            </div>
            <div class="member-details">
              <p><strong>Spécialité:</strong> {{ realisateur.specialite || 'Non spécifiée' }}</p>
              <p><strong>Biographie:</strong> {{ realisateur.biographie || 'Non renseignée' }}</p>
            </div>
            <div class="member-actions">
              <button @click="modifierRealisateur(realisateur)" class="btn-edit">
                <i class="fas fa-edit"></i> Modifier
              </button>
              <button @click="supprimerRealisateur(realisateur.idRealisateur)" class="btn-delete">
                <i class="fas fa-trash"></i> Supprimer
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de modification -->
    <div v-if="membreAModifier" class="modal-overlay">
      <div class="modal">
        <h3>Modifier {{ membreAModifier.nom }}</h3>
        <form @submit.prevent="sauvegarderModification">
          <div class="form-group">
            <label>Nom complet</label>
            <input v-model="membreAModifier.nom" type="text" required />
          </div>
          <div class="form-group">
            <label>Email</label>
            <input v-model="membreAModifier.email" type="email" required />
          </div>
          <div class="form-group">
            <label>Spécialité</label>
            <input v-model="membreAModifier.specialite" type="text" />
          </div>
          <div class="form-group">
            <label>Biographie</label>
            <textarea v-model="membreAModifier.biographie" rows="4"></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" @click="annulerModification" class="btn-secondary">
              Annuler
            </button>
            <button type="submit" class="btn-primary">Sauvegarder</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GestionEquipe',
  data() {
    return {
      activeTab: 'scenariste',
      recherche: '',
      filtreRole: 'tous',
      chargement: false,
      scenaristes: [],
      realisateurs: [],
      membreAModifier: null,
      
      nouveauScenariste: {
        nom: '',
        email: '',
        motDePasse: '',
        specialite: '',
        biographie: ''
      },
      
      nouveauRealisateur: {
        nom: '',
        email: '',
        motDePasse: '',
        specialite: '',
        biographie: ''
      }
    };
  },
  
  computed: {
    scenaristesFiltres() {
      let filtres = this.scenaristes;
      
      if (this.recherche) {
        const terme = this.recherche.toLowerCase();
        filtres = filtres.filter(s => 
          s.nom.toLowerCase().includes(terme) ||
          s.email.toLowerCase().includes(terme) ||
          (s.specialite && s.specialite.toLowerCase().includes(terme))
        );
      }
      
      return filtres;
    },
    
    realisateursFiltres() {
      let filtres = this.realisateurs;
      
      if (this.recherche) {
        const terme = this.recherche.toLowerCase();
        filtres = filtres.filter(r => 
          r.nom.toLowerCase().includes(terme) ||
          r.email.toLowerCase().includes(terme) ||
          (r.specialite && r.specialite.toLowerCase().includes(terme))
        );
      }
      
      return filtres;
    }
  },
  
  async mounted() {
    await this.chargerEquipe();
  },
  
  methods: {
    async chargerEquipe() {
      try {
        // Charger depuis les bons endpoints
        const [scenaristesRes, realisateursRes] = await Promise.all([
          fetch('/api/utilisateurs/scenaristes'),
          fetch('/api/utilisateurs/realisateurs')
        ]);
        
        if (scenaristesRes.ok) {
          this.scenaristes = await scenaristesRes.json();
        } else {
          console.error('Erreur chargement scénaristes:', scenaristesRes.status);
        }
        
        if (realisateursRes.ok) {
          this.realisateurs = await realisateursRes.json();
        } else {
          console.error('Erreur chargement réalisateurs:', realisateursRes.status);
        }
      } catch (error) {
        console.error('Erreur lors du chargement de l\'équipe:', error);
        alert('Erreur lors du chargement des données');
      }
    },
    
    async creerScenariste() {
      this.chargement = true;
      try {
        const response = await fetch('/api/utilisateurs/creer-scenariste', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.nouveauScenariste)
        });
        
        if (response.ok) {
          await this.chargerEquipe();
          this.reinitialiserFormulaireScenariste();
          alert('Scénariste créé avec succès!');
        } else {
          const errorData = await response.json();
          throw new Error(errorData.message || 'Erreur lors de la création');
        }
      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors de la création du scénariste: ' + error.message);
      } finally {
        this.chargement = false;
      }
    },
    
    async creerRealisateur() {
      this.chargement = true;
      try {
        const response = await fetch('/api/utilisateurs/creer-realisateur', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.nouveauRealisateur)
        });
        
        if (response.ok) {
          await this.chargerEquipe();
          this.reinitialiserFormulaireRealisateur();
          alert('Réalisateur créé avec succès!');
        } else {
          const errorData = await response.json();
          throw new Error(errorData.message || 'Erreur lors de la création');
        }
      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors de la création du réalisateur: ' + error.message);
      } finally {
        this.chargement = false;
      }
    },
    
    modifierScenariste(scenariste) {
      this.membreAModifier = { ...scenariste, type: 'SCENARISTE' };
    },
    
    modifierRealisateur(realisateur) {
      this.membreAModifier = { ...realisateur, type: 'REALISATEUR' };
    },
    
    async sauvegarderModification() {
      try {
        const endpoint = this.membreAModifier.type === 'SCENARISTE' 
          ? `/api/utilisateurs/scenaristes/${this.membreAModifier.idScenariste}`
          : `/api/utilisateurs/realisateurs/${this.membreAModifier.idRealisateur}`;
          
        const response = await fetch(endpoint, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.membreAModifier)
        });
        
        if (response.ok) {
          await this.chargerEquipe();
          this.annulerModification();
          alert('Modification sauvegardée!');
        } else {
          const errorData = await response.json();
          throw new Error(errorData.message || 'Erreur lors de la modification');
        }
      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors de la modification: ' + error.message);
      }
    },
    
    async supprimerScenariste(id) {
      if (confirm('Êtes-vous sûr de vouloir supprimer ce scénariste ?')) {
        try {
          const response = await fetch(`/api/utilisateurs/scenaristes/${id}`, {
            method: 'DELETE'
          });
          
          if (response.ok) {
            await this.chargerEquipe();
            alert('Scénariste supprimé avec succès!');
          } else {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erreur lors de la suppression');
          }
        } catch (error) {
          console.error('Erreur:', error);
          alert('Erreur lors de la suppression: ' + error.message);
        }
      }
    },
    
    async supprimerRealisateur(id) {
      if (confirm('Êtes-vous sûr de vouloir supprimer ce réalisateur ?')) {
        try {
          const response = await fetch(`/api/utilisateurs/realisateurs/${id}`, {
            method: 'DELETE'
          });
          
          if (response.ok) {
            await this.chargerEquipe();
            alert('Réalisateur supprimé avec succès!');
          } else {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erreur lors de la suppression');
          }
        } catch (error) {
          console.error('Erreur:', error);
          alert('Erreur lors de la suppression: ' + error.message);
        }
      }
    },
    
    annulerModification() {
      this.membreAModifier = null;
    },
    
    reinitialiserFormulaireScenariste() {
      this.nouveauScenariste = {
        nom: '',
        email: '',
        motDePasse: '',
        specialite: '',
        biographie: ''
      };
    },
    
    reinitialiserFormulaireRealisateur() {
      this.nouveauRealisateur = {
        nom: '',
        email: '',
        motDePasse: '',
        specialite: '',
        biographie: ''
      };
    }
  }
};
</script>


<style scoped>
.gestion-equipe {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 2rem;
}

.header h1 {
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.header p {
  color: #7f8c8d;
  font-size: 1.1rem;
}

.tabs {
  display: flex;
  margin-bottom: 2rem;
  border-bottom: 1px solid #e0e0e0;
}

.tab-button {
  padding: 1rem 2rem;
  border: none;
  background: none;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.tab-button.active {
  border-bottom-color: #3498db;
  color: #3498db;
  font-weight: bold;
}

.tab-button:hover {
  background-color: #f8f9fa;
}

.form-container {
  background: #f8f9fa;
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.form-section h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

.creation-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: #2c3e50;
}

.form-group input,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group textarea {
  resize: vertical;
}

.btn-primary {
  grid-column: 1 / -1;
  padding: 1rem;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s ease;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-primary:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.search-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  align-items: center;
}

.search-bar {
  position: relative;
  flex: 1;
}

.search-bar i {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #7f8c8d;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.filter-select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.liste-equipe {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.role-section h3 {
  margin-bottom: 1rem;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.member-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.member-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.member-avatar {
  width: 50px;
  height: 50px;
  background: #3498db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.25rem;
}

.member-info h4 {
  margin: 0;
  color: #2c3e50;
}

.member-email {
  margin: 0.25rem 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.role-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: bold;
}

.role-badge.scenariste {
  background: #e8f6f3;
  color: #1abc9c;
}

.role-badge.realisateur {
  background: #ebf5fb;
  color: #3498db;
}

.member-details p {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  line-height: 1.4;
}

.member-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.btn-edit, .btn-delete {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.btn-edit {
  background: #f39c12;
  color: white;
}

.btn-edit:hover {
  background: #e67e22;
}

.btn-delete {
  background: #e74c3c;
  color: white;
}

.btn-delete:hover {
  background: #c0392b;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #7f8c8d;
  font-style: italic;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}

.modal h3 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-secondary {
  padding: 0.75rem 1.5rem;
  border: 1px solid #bdc3c7;
  background: white;
  color: #7f8c8d;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary:hover {
  background: #f8f9fa;
}

@media (max-width: 768px) {
  .gestion-equipe {
    padding: 1rem;
  }
  
  .creation-form {
    grid-template-columns: 1fr;
  }
  
  .search-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .cards-container {
    grid-template-columns: 1fr;
  }
  
  .tabs {
    flex-direction: column;
  }
  
  .tab-button {
    text-align: center;
  }
}
</style>