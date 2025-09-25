import { createRouter, createWebHistory } from 'vue-router'
import ConnexionView from '../views/Connexion.vue'
import AccueilView from '../views/Accueil.vue'
import HomeView from '../views/HomeView.vue'
import UtilisateursView from '../views/UtilisateursView.vue'
import Admin_acceuil from '../views/Admin_acceuil.vue'
import Scenariste_acceuil from '../views/Scenariste_acceuil.vue'
import Statistiques from '../views/Statistiques.vue'
import AddProject from '../views/AddProject.vue'
import ProjetScenaristeView from '../views/ProjetScenariste.vue'
import AddEpisode from '../views/AddEpisode.vue'
import EcranTravail from '../components/EcranTravail.vue';
import Ecran_travail_viewers from '../components/Ecran_travail_viewers.vue';
import Add_scene_ecran_travail from '../components/Add_scene_ecran_travail.vue';
import Add_sequence_ecran_travail from '../components/Add_sequence_ecran_travail.vue';
import Add_dialogue_scene from '../components/Add_dialogue_scene.vue';
import Add_lieu_scene_ecran_travail from '../components/Add_lieu_scene_ecran_travail.vue';
import Add_plateau_scene from '../components/Add_plateau_scene.vue';
import Add_episode_ecran_travail from '../components/Add_episode_ecran_travail.vue';


import Add_comedien_ecran_travail from '../components/Add_comedien_ecran_travail.vue';

import Add_personnage_ecran_travail from '../components/Add_personnage_ecran_travail.vue';

const routes = [
  {
    path: '/',
    name: 'connexion',
    component: ConnexionView
  },
  {
    path: '/accueil',
    name: 'accueil',
    component: AccueilView
  },
  {
    path: '/home',
    name: 'home',
    component: HomeView
  },
  {
    path: '/utilisateurs',
    name: 'utilisateurs',
    component: UtilisateursView
  },
  {
    path: '/admin',
    name: 'admin',
    component: Admin_acceuil
  },
  {
    path: '/scenariste',
    name: 'scenariste',
    component: Scenariste_acceuil
  },
  {
    path: '/statistiques',
    name: 'statistiques',
    component: Statistiques
  },
  {
    path: '/add-project', 
    name: 'add-project',
    component: AddProject
  },
  {
    path: '/projet/:id',
    name: 'projet-scenariste',
    component: ProjetScenaristeView
  },
  {
    path: '/projet/:id/add-episode',
    name: 'add-episode',
    component: AddEpisode
  },
    {
    path: '/episode/:id/detail-episode',
    name: 'episode-details',
    component: () => import('../views/EpisodeScenaristeDetails.vue')
  },
  {
    path: '/episode/:id/add-sequence',
    name: 'add-sequence',
    component: () => import('../views/AddSequence.vue')
  },
  {
    path: '/sequence/:sequenceId/add-scene',
    name: 'add-scene',
    component: () => import('../views/AddScene.vue')
  },
  {
    path: '/sequence/:id/detail-sequence',
    name: 'sequence-details',
    component: () => import('../views/SequenceScenaristeDetails.vue')
  },
  {
    path: '/creation-comedien',
    name: 'creation-comedien',
    component: () => import('../views/CreationComedien.vue')
  },
  {
  path: '/scene-comedien',
  name: 'scene-comedien',
  component: () => import('../views/SceneComedien.vue')
  },
  {
    path: '/creation-personnage',
    name: 'CreationPersonnage',
    component: () => import('../views/CreationPersonnage.vue')
  },
  {
    path: '/creation-dialogue',
    name: 'creation-dialogue',
    component: () => import('../views/CreationDialogue.vue')
  },
 {
    path: '/creation-lieu',
    name: 'CreationLieu',
    component: () => import('../views/CreationLieu.vue')
  },
  {
    path: '/creation-plateau',
    name: 'CreationPlateau',
    component: () => import('../views/CreationPlateau.vue')
  },
  {
    path: '/projet/:idProjet/ecran-travail',
    name: 'EcranTravail',
    component: EcranTravail,
    props: true,
  },
  {
    path: '/projet/:idProjet/ecran-travail-viewers',
    name: 'Ecran_travail_viewers',
    component: Ecran_travail_viewers,
    props: true,
  },
  {
    path: '/sequence/:sequenceId/add-scene-ecran-travail',
    name: 'Add_scene_ecran_travail',
    component: Add_scene_ecran_travail,
    props: true,
  },
  {
    path: '/episode/:episodeId/add-sequence-ecran-travail',
    name: 'Add_sequence_ecran_travail',
    component: Add_sequence_ecran_travail,
    props: true,
  },
    {
    path: '/scene/:sceneId/add-dialogue-scene-ecran-travail',
    name: 'Add_dialogue_scene',
    component: Add_dialogue_scene,
    props: true,
  },
    {
    path: '/projet/:projetId/add-lieu-scene-ecran-travail',
    name: 'Add_lieu_scene_ecran_travail',
    component: Add_lieu_scene_ecran_travail,
    props: true,
  },
  {
    path: '/projet/:projetId/add-plateau-scene',
    name: 'Add_plateau_scene',
    component: Add_plateau_scene,
    props: true,
  },
  {
    path: '/projet/:projetId/add-episode-ecran-travail',
    name: 'Add_episode_ecran_travail',
    component: Add_episode_ecran_travail,
    props: true,
  },
  {
    path: '/projet/:projetId/add-comedien-ecran-travail',
    name: 'Add_comedien_ecran_travail',
    component: Add_comedien_ecran_travail,
    props: true,
  },
   {
    path: '/projet/:projetId/add-personnage-ecran-travail',
    name: 'Add_personnage_ecran_travail',
    component: Add_personnage_ecran_travail,
    props: true,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router