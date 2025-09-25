<template>
  <nav class="sidebar" :class="{ 'collapsed': isCollapsed }">
    <div class="sidebar-brand">
      <router-link to="/" class="sidebar-logo">
        <i class="fas fa-film icon"></i>
        <span v-if="!isCollapsed" class="link-text">Cinema</span>
      </router-link>
      <button @click="toggleSidebar" class="menu-toggle">
        <span v-if="!isCollapsed" class="menu-icon"><i class="fas fa-bars"></i></span>
        <span v-else class="menu-icon"><i class="fas fa-arrow-left"></i></span>
      </button>
    </div>

    
    <div class="sidebar-menu">
      <div class="sidebar-links">
        <router-link to="/accueil" class="sidebar-link" @click="toggleSidebarIfMobile">
          <i class="fas fa-home icon"></i>
          <span v-if="!isCollapsed" class="link-text">Accueil</span>
        </router-link>

        <router-link to="/admin" class="sidebar-link" v-if="user?.role === 'ADMIN'" @click="toggleSidebarIfMobile">
          <i class="fas fa-user-shield icon"></i>
          <span v-if="!isCollapsed" class="link-text">Admin</span>
        </router-link>

        <router-link to="/scenariste" class="sidebar-link" v-if="user?.role === 'SCENARISTE'" @click="toggleSidebarIfMobile">
          <i class="fas fa-pen-fancy icon"></i>
          <span v-if="!isCollapsed" class="link-text">Scénariste</span>
        </router-link>

        <router-link to="/creation-comedien" class="sidebar-link" @click="toggleSidebarIfMobile">
          <i class="fas fa-users icon"></i>
          <span v-if="!isCollapsed" class="link-text">Gestion Comédiens</span>
        </router-link>

        <router-link to="/utilisateurs" class="sidebar-link" v-if="user?.role === 'ADMIN'" @click="toggleSidebarIfMobile">
          <i class="fas fa-users icon"></i>
          <span v-if="!isCollapsed" class="link-text">Utilisateurs</span>
        </router-link>

        <router-link to="/statistiques" class="sidebar-link" v-if="user?.role !== 'ADMIN' && 'SCENARISTE' " @click="toggleSidebarIfMobile">
          <i class="fas fa-chart-bar icon"></i>
          <span v-if="!isCollapsed" class="link-text">Ecran travail</span>
        </router-link>

        <router-link to="/creation-personnage" class="sidebar-link" @click="toggleSidebarIfMobile">
          <i class="fas fa-user-astronaut icon"></i>
          <span v-if="!isCollapsed" class="link-text">Gestion Personnages</span>
        </router-link>

        <router-link to="/creation-dialogue" class="sidebar-link" @click="toggleSidebarIfMobile">
          <i class="fas fa-comment-dots icon"></i>
          <span v-if="!isCollapsed" class="link-text">Gestion Dialogues</span>
        </router-link>

        <router-link to="/creation-lieu" class="sidebar-link" @click="toggleSidebarIfMobile">
          <i class="fas fa-map-marker-alt icon"></i>
          <span v-if="!isCollapsed" class="link-text">Gestion Lieux</span>
        </router-link>

        <router-link to="/creation-plateau" class="sidebar-link" @click="toggleSidebarIfMobile">
          <i class="fas fa-camera icon"></i>
          <span v-if="!isCollapsed" class="link-text">Gestion Plateaux</span>
        </router-link>

        <router-link to="/gestion-equipe" class="sidebar-link" v-if="user?.role === 'ADMIN'" @click="toggleSidebarIfMobile">
          <i class="fas fa-users-cog icon"></i>
          <span v-if="!isCollapsed" class="link-text">Gestion Équipe</span>
        </router-link>

      </div>
      
      <div class="sidebar-footer">
        <div class="user-info" v-if="user">
          <span class="user-name"><i class="fas fa-user-circle icon"></i> <span v-if="!isCollapsed">{{ user.nom }}</span></span>
          <span v-if="!isCollapsed" class="user-role">({{ user.role }})</span>
          
          <button @click="logout" class="logout-btn">
            <i class="fas fa-sign-out-alt icon"></i> 
            <span v-if="!isCollapsed">Déconnexion</span>
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
import '../assets/css/sidebar.css';
export default {
  name: 'Sidebar',
  data() {
    return {
      user: null,
      isCollapsed: false,
      isMobile: false
    };
  },
  mounted() {
    this.loadUser();
    this.checkIfMobile();
    window.addEventListener('resize', this.checkIfMobile);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkIfMobile);
  },
  methods: {
    loadUser() {
      const userData = localStorage.getItem('user');
      if (userData) {
        this.user = JSON.parse(userData);
      }
    },
    logout() {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      this.$router.push('/');
    },
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed;
    },
    toggleSidebarIfMobile() {
      if (this.isMobile) {
        this.isCollapsed = true;
      }
    },
    checkIfMobile() {
      this.isMobile = window.innerWidth <= 768;
      if (this.isMobile) {
        this.isCollapsed = true;
      }
    }
  }
};
</script>

