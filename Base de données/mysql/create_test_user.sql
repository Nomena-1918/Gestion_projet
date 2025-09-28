-- create_test_user.sql
-- Adapté pour MySQL 8.x

-- 1) Création de la base si nécessaire
CREATE DATABASE IF NOT EXISTS `gestion_projet_film` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 2) Création (ou récréation) de l'utilisateur de test
-- Mot de passe simple pour tests : change-le si besoin. Exemple : test1234
-- Utilisation de 'localhost' pour limiter l'accès depuis la même machine.
-- Si tu veux autoriser depuis n'importe quelle adresse, remplace 'localhost' par '%'.
DROP USER IF EXISTS 'gestion_projet_film_user'@'localhost';
CREATE USER 'gestion_projet_film_user'@'localhost' IDENTIFIED BY 'test1234Welcome@';
CREATE USER 'gestion_projet_film_user'@'127.0.0.1' IDENTIFIED BY 'test1234Welcome@';

-- 3) Donner tous les privilèges, mais uniquement sur la base gestion_projet_film
GRANT ALL PRIVILEGES ON `gestion_projet_film`.* TO 'gestion_projet_film_user'@'localhost';
GRANT ALL PRIVILEGES ON `gestion_projet_film`.* TO 'gestion_projet_film_user'@'127.0.0.1';

-- 4) Appliquer les changements
FLUSH PRIVILEGES;

-- (Optionnel) Vérification - affiche les privilèges pour l'utilisateur
-- SHOW GRANTS FOR 'gestion_projet_film_user'@'localhost';