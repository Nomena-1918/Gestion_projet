<template>
  <div class="home">
    <h1>Bienvenue dans la gestion de création d'un film</h1>
    <router-link to="/utilisateurs" class="btn">Voir les utilisateurs</router-link>
  </div>
</template>

<script>
export default {
  name: 'HomeView'
}
</script>

<style scoped>
.home {
  text-align: center;
  margin-top: 50px;
}

.btn {
  display: inline-block;
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #42b983;
  color: white;
  text-decoration: none;
  border-radius: 5px;
}

.btn:hover {
  background-color: #369f6b;
}
</style>