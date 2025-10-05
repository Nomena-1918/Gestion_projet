// Configuration centralisée des URLs d'API
const getApiBaseUrl = () => {
  // En développement, utiliser le proxy configuré dans vite.config.js
  if (import.meta.env.DEV) {
    return '';
  }

  // En production, utiliser l'URL complète du serveur
  // Adapter selon votre configuration serveur réel
  const currentUrl = window.location.origin;

  // Si déployé avec un sous-chemin (context path de Tomcat)
  // Remplacez 'gestion_projet_cinema' par votre context path réel
  return `${currentUrl}/gestion_projet_cinema`;
};

// Export de la constante pour utilisation dans les composants
export const API_BASE_URL = getApiBaseUrl();
