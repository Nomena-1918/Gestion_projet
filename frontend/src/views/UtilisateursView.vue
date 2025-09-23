<template>
  <div class="app-wrapper">
    <div class="utilisateurs-container">
      <h1>Liste des Utilisateurs</h1>
      <table class="utilisateurs-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nom</th>
            <th>Email</th>
            <th>Rôle</th>
            <th>Créé le</th>
            <th>Modifié le</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="utilisateur in utilisateurs" :key="utilisateur.id">
            <td>{{ utilisateur.id }}</td>
            <td>{{ utilisateur.nom }}</td>
            <td>{{ utilisateur.email }}</td>
            <td>{{ utilisateur.role }}</td>
            <td>{{ formatDate(utilisateur.creeLe) }}</td>
           <td>{{ formatDate(utilisateur.modifieLe) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
  
</template>

<script>
import axios from 'axios';

export default {
  name: 'UtilisateursView',
  data() {
    return {
      utilisateurs: []
    };
  },
  created() {
    this.fetchUtilisateurs();
  },
  methods: {
    async fetchUtilisateurs() {
  try {
    const response = await axios.get('/api/utilisateurs');
    this.utilisateurs = response.data;
  } catch (error) {
    console.error('Erreur lors de la récupération des utilisateurs:', error);
  }
},
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString();
    }
  }
};
</script>

<style scoped>
.app-wrapper {
 max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Arial', sans-serif;
}

.utilisateurs-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.utilisateurs-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.utilisateurs-table th, .utilisateurs-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.utilisateurs-table th {
  background-color: #f2f2f2;
  font-weight: bold;
}

.utilisateurs-table tr:nth-child(even) {
  background-color: #f9f9f9;
}

.utilisateurs-table tr:hover {
  background-color: #f1f1f1;
}
</style>