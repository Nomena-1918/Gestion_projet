
<template>
  <div class="connexion-container">
    <!-- Animation de fond minimaliste -->
    <div class="background-animation">
      <div class="animated-bg">
        <div class="bg-shape shape-1"></div>
        <div class="bg-shape shape-2"></div>
        <div class="bg-shape shape-3"></div>
        <div class="bg-shape shape-4"></div>
      </div>
    </div>

    <!-- Carte de connexion -->
    <div class="connexion-card">
      <div class="connexion-header">
        <div class="logo-container">
          <i class="fas fa-user-circle logo-icon"></i>
        </div>
        <h1>Connexion</h1>
        <p>Accédez à votre espace personnel</p>
      </div>
      
      <form @submit.prevent="seConnecter" class="connexion-form">
        <div class="form-group">
          <div class="input-container">
            <i class="fas fa-envelope input-icon"></i>
            <input 
              type="email" 
              id="email"
              v-model="email" 
              required 
              placeholder=" "
              class="form-input"
            />
            <label for="email" class="input-label">Email</label>
          </div>
        </div>
        
        <div class="form-group">
          <div class="input-container">
            <i class="fas fa-lock input-icon"></i>
            <input 
              type="password" 
              id="password"
              v-model="password" 
              required 
              placeholder=" "
              class="form-input"
            />
            <label for="password" class="input-label">Mot de passe</label>
          </div>
        </div>
        
        <div class="form-options">
          <label class="checkbox-container">
            Se souvenir de moi
            <input type="checkbox" v-model="rememberMe">
            <span class="checkmark"></span>
          </label>
          <a href="#" class="forgot-link">Mot de passe oublié?</a>
        </div>
        
        <div v-if="error" class="error-message">
          <i class="fas fa-exclamation-circle error-icon"></i>
          {{ error }}
        </div>
        
        <button type="submit" class="connexion-btn" :disabled="loading">
          <span v-if="loading" class="btn-loading">
            <span class="loading-dots">
              <span></span>
              <span></span>
              <span></span>
            </span>
          </span>
          <span v-else class="btn-text">
            <span>Se connecter</span>
            <i class="fas fa-arrow-right btn-icon"></i>
          </span>
        </button>
        
        <div class="signup-link">
          Pas encore inscrit? <a href="#">Créer un compte</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import '../assets/css/connexion.css';

export default {
  name: 'ConnexionView',
  data() {
    return {
      email: '',
      password: '',
      loading: false,
      error: ''
    };
  },
  methods: {
    async seConnecter() {
      this.loading = true;
      this.error = '';
      
      try {
        const response = await axios.post('/api/auth/login', {
          email: this.email,
          password: this.password
        });
        
        // Stocker les informations de l'utilisateur
        localStorage.setItem('user', JSON.stringify(response.data.user));
        localStorage.setItem('token', response.data.token);
        
        // Rediriger en fonction du rôle
        let route = '/accueil';
        if (response.data.user.role === 'ADMIN') {
          route = '/admin';
        } else if (response.data.user.role === 'SCENARISTE') {
          route = '/scenariste';
        }
        this.$router.push(route);
        
      } catch (error) {
        console.error('Erreur de connexion:', error);
        this.error = error.response?.data?.message || 'Erreur de connexion. Vérifiez vos identifiants.';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
