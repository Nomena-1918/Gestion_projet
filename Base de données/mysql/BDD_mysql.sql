CREATE DATABASE gestion_projet_film;

USE gestion_projet_film;

CREATE TABLE utilisateurs (
    id_utilisateur BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO utilisateurs (nom, email, mot_de_passe, role, cree_le, modifie_le) VALUES
('Admin Principal', 'admin@gmail.com', 'MotdepasseAdmin123', 'ADMIN', '2025-01-01 09:00:00', '2025-01-01 09:00:00'),
('Jean Dupont', 'jean.dupont@gmail.com', 'mdpRealisateur1', 'REALISATEUR', '2025-01-02 10:15:00', '2025-01-02 10:15:00'),
('Sophie Martin', 'sophie.martin@gmail.com', 'mdpRealisateur2', 'REALISATEUR', '2025-01-03 14:30:00', '2025-01-03 14:30:00'),
('Marc Leroy', 'marc.leroy@gmail.com', 'mdpScenariste1', 'SCENARISTE', '2025-01-04 11:45:00', '2025-01-04 11:45:00'),
('Alice Dubois', 'alice.dubois@gmail.com', 'mdpScenariste2', 'SCENARISTE', '2025-01-05 16:20:00', '2025-01-05 16:20:00');


CREATE TABLE scenaristes (
    id_scenariste BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE,
    specialite VARCHAR(100),
    biographie TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO scenaristes (id_utilisateur, specialite, biographie)
SELECT 
    id_utilisateur,
    CASE 
        WHEN nom LIKE '%Leroy%' THEN 'Documentaire'
        WHEN nom LIKE '%Dubois%' THEN 'Animation et Jeunesse'
        ELSE 'Général'
    END as specialite,
    'Scenariste experimente' as biographie
FROM utilisateurs 
WHERE role = 'SCENARISTE';


CREATE TABLE realisateurs (
    id_realisateur BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE,
    specialite VARCHAR(100),
    biographie TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO realisateurs (id_utilisateur, specialite, biographie)
SELECT 
    id_utilisateur,
    CASE 
        WHEN nom LIKE '%Dupont%' THEN 'Thriller et Drame'
        WHEN nom LIKE '%Martin%' THEN 'Romance et Comédie'
        ELSE 'Général'
    END as specialite,
    'Realisateur experimente' as biographie
FROM utilisateurs 
WHERE role = 'REALISATEUR';


CREATE TABLE genres (
    id_genre BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_genre VARCHAR(50) NOT NULL,
    description TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO genres (nom_genre, description, cree_le) VALUES
('Long metrage', 'Film de plus de 60 minutes', '2025-01-01 09:05:00'),
('Court metrage', 'Film de moins de 60 minutes', '2025-01-01 09:10:00'),
('Documentaire', 'Film à vocation informative ou educative', '2025-01-01 09:15:00'),
('Animation', 'Film utilisant des techniques danimation', '2025-01-01 09:20:00'),
('Serie', 'Production audiovisuelle en plusieurs episodes', '2025-01-01 09:25:00'),
('Clip musical', 'Vidéo accompagnant une chanson', '2025-01-01 09:30:00'),
('Film expérimental', 'Œuvre cinématographique explorant de nouvelles formes narratives ou visuelles', '2025-01-01 09:35:00');


CREATE TABLE statuts_projet (
    id_statut_projet BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    nom_statuts_projet VARCHAR(100) NOT NULL,
    description TEXT,
    ordre_affichage INTEGER NOT NULL,
    est_actif TINYINT(1) DEFAULT 1
);


UPDATE statuts_projet SET 
    nom_statuts_projet = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(nom_statuts_projet, 'é', 'e'), 'è', 'e'), 'ê', 'e'), 'à', 'a'), 'À', 'A'),
    description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(description, 'é', 'e'), 'è', 'e'), 'ê', 'e'), 'à', 'a'), 'À', 'A');


INSERT INTO statuts_projet (code, nom_statuts_projet, description, ordre_affichage) VALUES
('en_cours', 'En cours', 'Projet en cours', 1),
('fini', 'Fini', 'Projet terminé', 2),
('annule', 'Annulé', 'Projet abandonné', 3),
('en_pause', 'En pause', 'Projet en pause', 4);


CREATE TABLE projets (
    id_projet BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    synopsis TEXT,
    id_genre BIGINT REFERENCES genres(id_genre),
    date_debut DATE,
    date_fin DATE,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Association scénaristes et projets
CREATE TABLE projet_scenaristes (
    id_projet_scenariste BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_projet BIGINT REFERENCES projets(id_projet) ON DELETE CASCADE,
    id_scenariste BIGINT REFERENCES scenaristes(id_scenariste) ON DELETE CASCADE,
    role_scenariste VARCHAR(100) NOT NULL, 
    pourcentage_contribution INTEGER DEFAULT 100,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(id_projet, id_scenariste)
);


CREATE INDEX idx_scenaristes_utilisateur ON scenaristes(id_utilisateur);
CREATE INDEX idx_projet_scenaristes_projet ON projet_scenaristes(id_projet);
CREATE INDEX idx_projet_scenaristes_scenariste ON projet_scenaristes(id_scenariste);

-- Association Projet et Statut
CREATE TABLE projet_statuts (
    id_projet_statut BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_projet BIGINT REFERENCES projets(id_projet) ON DELETE CASCADE,
    id_statut BIGINT REFERENCES statuts_projet(id_statut_projet) ON DELETE RESTRICT,
    date_debut TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP

);


CREATE TABLE statuts_episode (
    id_statut_episode BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    nom_statuts_episode VARCHAR(100) NOT NULL,
    description TEXT,
    ordre_affichage INTEGER NOT NULL,
    est_actif TINYINT(1) DEFAULT 1
);

INSERT INTO statuts_episode (code, nom_statuts_episode, description, ordre_affichage) VALUES
('planifie', 'Planifié', 'Episode planifié', 1),
('preparation', 'En préparation', 'Préparation technique', 2),
('tournage', 'En tournage', 'Tournage en cours', 3),
('tourne', 'Tourné', 'Tournage terminé', 4),
('monte', 'Monté', 'Montage terminé', 5),
('valide', 'Validé', 'Episode validé', 6);

UPDATE statuts_episode SET 
    nom_statuts_episode = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(nom_statuts_episode, 'é', 'e'), 'è', 'e'), 'ê', 'e'), 'à', 'a'), 'À', 'A'),
    description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(description, 'é', 'e'), 'è', 'e'), 'ê', 'e'), 'à', 'a'), 'À', 'A');



CREATE TABLE episodes (
    id_episode BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_projet BIGINT REFERENCES projets(id_projet) ON DELETE CASCADE,
    titre VARCHAR(255) NOT NULL,
    ordre INTEGER NOT NULL,
    synopsis TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    date_fin TIMESTAMP
);

ALTER TABLE episodes ADD CONSTRAINT unique_ordre_per_projet UNIQUE (id_projet, ordre);


CREATE TABLE episode_statuts (
    id_episode_statut BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_episode BIGINT REFERENCES episodes(id_episode) ON DELETE CASCADE,
    id_statut BIGINT REFERENCES statuts_episode(id_statut_episode) ON DELETE RESTRICT,
    date_debut TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP
);


-- Table pour lier les réalisateurs aux épisodes
CREATE TABLE episode_realisateurs (
    id_episode_realisateur BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_episode BIGINT REFERENCES episodes(id_episode) ON DELETE CASCADE,
    id_realisateur BIGINT REFERENCES realisateurs(id_realisateur) ON DELETE CASCADE,
    role_realisateur VARCHAR(100) NOT NULL DEFAULT 'Realisateur principal',
    pourcentage_contribution INTEGER DEFAULT 100,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(id_episode, id_realisateur)
);

-- Table pour lier les scénaristes aux épisodes (au lieu des projets)
CREATE TABLE episode_scenaristes (
    id_episode_scenariste BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_episode BIGINT REFERENCES episodes(id_episode) ON DELETE CASCADE,
    id_scenariste BIGINT REFERENCES scenaristes(id_scenariste) ON DELETE CASCADE,
    role_scenariste VARCHAR(100) NOT NULL,
    pourcentage_contribution INTEGER DEFAULT 100,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(id_episode, id_scenariste)
);


CREATE INDEX idx_episode_realisateurs_episode ON episode_realisateurs(id_episode);
CREATE INDEX idx_episode_realisateurs_realisateur ON episode_realisateurs(id_realisateur);
CREATE INDEX idx_episode_scenaristes_episode ON episode_scenaristes(id_episode);
CREATE INDEX idx_episode_scenaristes_scenariste ON episode_scenaristes(id_scenariste);


-- Vue pour voir tous les épisodes avec leurs réalisateurs et scénaristes
CREATE VIEW v_episode_equipe AS
SELECT 
    e.id_episode,
    e.titre as titre_episode,
    p.titre as titre_projet,
    r.id_realisateur,
    u_r.nom as nom_realisateur,
    u_r.email as email_realisateur,
    s.id_scenariste,
    u_s.nom as nom_scenariste,
    u_s.email as email_scenariste
FROM episodes e
LEFT JOIN projets p ON e.id_projet = p.id_projet
LEFT JOIN episode_realisateurs er ON e.id_episode = er.id_episode
LEFT JOIN realisateurs r ON er.id_realisateur = r.id_realisateur
LEFT JOIN utilisateurs u_r ON r.id_utilisateur = u_r.id_utilisateur
LEFT JOIN episode_scenaristes es ON e.id_episode = es.id_episode
LEFT JOIN scenaristes s ON es.id_scenariste = s.id_scenariste
LEFT JOIN utilisateurs u_s ON s.id_utilisateur = u_s.id_utilisateur;


CREATE OR REPLACE VIEW v_episodes_par_utilisateur AS
SELECT 
    u.id_utilisateur,
    u.nom,
    u.email,
    u.role,
    e.id_episode,
    e.titre as titre_episode,
    p.titre as titre_projet
FROM utilisateurs u
LEFT JOIN realisateurs r ON u.id_utilisateur = r.id_utilisateur
LEFT JOIN episode_realisateurs er ON r.id_realisateur = er.id_realisateur  -- CORRECTION ici
LEFT JOIN episodes e ON er.id_episode = e.id_episode
LEFT JOIN projets p ON e.id_projet = p.id_projet
WHERE u.role = 'REALISATEUR'

UNION

SELECT 
    u.id_utilisateur,
    u.nom,
    u.email,
    u.role,
    e.id_episode,
    e.titre as titre_episode,
    p.titre as titre_projet
FROM utilisateurs u
LEFT JOIN scenaristes s ON u.id_utilisateur = s.id_utilisateur
LEFT JOIN episode_scenaristes es ON s.id_scenariste = es.id_scenariste
LEFT JOIN episodes e ON es.id_episode = e.id_episode
LEFT JOIN projets p ON e.id_projet = p.id_projet
WHERE u.role = 'SCENARISTE';

CREATE TABLE statuts_sequence (
    id_statut_sequence BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    nom_statuts_sequence VARCHAR(100) NOT NULL,
    description TEXT,
    ordre_affichage INTEGER NOT NULL,
    est_actif TINYINT(1) DEFAULT 1
);

INSERT INTO statuts_sequence (code, nom_statuts_sequence, description, ordre_affichage) VALUES
('a_planifier', 'À planifier', 'Séquence à organiser', 1),
('planifiee', 'Planifiée', 'Séquence planifiée', 2),
('prete', 'Prête', 'Préparée pour tournage', 3),
('tournage', 'En tournage', 'Tournage en cours', 4),
('tournee', 'Tournée', 'Tournage terminé', 5),
('montee', 'Montée', 'Montage terminé', 6);

UPDATE statuts_sequence SET 
    nom_statuts_sequence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(nom_statuts_sequence, 'é', 'e'), 'è', 'e'), 'ê', 'e'), 'à', 'a'), 'À', 'A'),
    description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(description, 'é', 'e'), 'è', 'e'), 'ê', 'e'), 'à', 'a'), 'À', 'A');



CREATE TABLE sequences (
    id_sequence BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_episode BIGINT REFERENCES episodes(id_episode) ON DELETE CASCADE,
    titre VARCHAR(255) NOT NULL,
    ordre INTEGER NOT NULL,
    synopsis TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE sequences ADD CONSTRAINT unique_ordre_per_episode UNIQUE (id_episode, ordre);



CREATE TABLE sequence_statuts (
    id_sequence_statut BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_sequence BIGINT REFERENCES sequences(id_sequence) ON DELETE CASCADE,
    id_statut BIGINT REFERENCES statuts_sequence(id_statut_sequence) ON DELETE RESTRICT,
    date_debut TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP
);


CREATE TABLE statuts_scene (
    id_statut_scene BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    nom_statuts_scene VARCHAR(100) NOT NULL,
    description TEXT,
    ordre_affichage INTEGER NOT NULL,
    est_actif TINYINT(1) DEFAULT 1
);

INSERT INTO statuts_scene (code, nom_statuts_scene, description, ordre_affichage) VALUES
('ecrite', 'Ecrite', 'Scène écrite', 1),
('preparee', 'Préparée', 'Decors et accessoires prêts', 2),
('planifiee', 'Planifiée', 'Plan de tournage établi', 3),
('tournage', 'En tournage', 'Tournage en cours', 4),
('tournee', 'Tournee', 'Tournage terminé', 5),
('a_retourner', 'A retourner', 'Nécessite des reshoots', 6),
('validee', 'Validée', 'Scène approuvée', 7);

UPDATE statuts_scene SET 
    nom_statuts_scene = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(nom_statuts_scene, 'é', 'e'), 'è', 'e'), 'ê', 'e'), 'à', 'a'), 'À', 'A'),
    description = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(description, 'é', 'e'), 'è', 'e'), 'ê', 'e'), 'à', 'a'), 'À', 'A');



CREATE TABLE scenes (
    id_scene BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_sequence BIGINT REFERENCES sequences(id_sequence) ON DELETE CASCADE,
    titre VARCHAR(255) NOT NULL,
    ordre INTEGER NOT NULL,
    synopsis TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE scenes ADD CONSTRAINT unique_ordre_per_sequence UNIQUE (id_sequence, ordre);


CREATE TABLE scene_statuts (
    id_scene_statut BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_scene BIGINT REFERENCES scenes(id_scene) ON DELETE CASCADE,
    id_statut BIGINT REFERENCES statuts_scene(id_statut_scene) ON DELETE RESTRICT,
    date_debut TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP
);

CREATE TABLE comediens (
    id_comedien BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_comedien VARCHAR(255) NOT NULL,
    id_projet BIGINT REFERENCES projets(id_projet) ON DELETE CASCADE,
    age INTEGER,
    email VARCHAR(255),
    photo VARCHAR(255),
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE disponibilites_comediens (
    id_disponibilite_comedien BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_comedien BIGINT REFERENCES comediens(id_comedien) ON DELETE CASCADE,
    date DATE NOT NULL,
    statut VARCHAR(50) NOT NULL
);

CREATE TABLE comedien_scene (
    id_comedien_scene BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_scene BIGINT REFERENCES scenes(id_scene) ON DELETE CASCADE,
    id_comedien BIGINT REFERENCES comediens(id_comedien) ON DELETE RESTRICT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE comedien_scene 
ADD UNIQUE (id_scene, id_comedien);


CREATE TABLE personnages (
    id_personnage BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_projet BIGINT REFERENCES projets(id_projet) ON DELETE CASCADE,
    id_comedien BIGINT REFERENCES comediens(id_comedien) ON DELETE SET NULL,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE dialogues (
    id_dialogue BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_scene BIGINT REFERENCES scenes(id_scene) ON DELETE CASCADE,
    id_personnage BIGINT REFERENCES personnages(id_personnage) ON DELETE SET NULL,
    texte TEXT NOT NULL,
    ordre INTEGER NOT NULL,
    observation TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE dialogues ADD CONSTRAINT unique_ordre_per_scene UNIQUE (id_scene, ordre);


/*ALTER TABLE dialogues 
ADD COLUMN couleur_texte VARCHAR(7) DEFAULT '#000000',
ADD COLUMN couleur_observation VARCHAR(7) DEFAULT '#666666'; */

CREATE TABLE couleurs (
    id_couleur BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    valeur_hex VARCHAR(7) NOT NULL,
    est_defaut TINYINT(1) DEFAULT 0,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO couleurs (nom, valeur_hex, est_defaut) VALUES
('Noir', '#000000', 1),
('Gris', '#666666', 1),
('Golden Yellow', '#FFDD00', 0),
('Key Blue', '#C8F6FF', 0),
('Celery', '#E0F79C', 0),
('Tomato', '#FF6344', 0),
('Dark Blue', '#2A3056', 0),
('Teal', '#43B0AF', 0),
('Light Cyan', '#67FFF2', 0),
('Dark Green', '#294933', 0),
('Medium Green', '#446B5C', 0),
('Light Green', '#81EC86', 0);

CREATE INDEX idx_couleurs_valeur_hex ON couleurs(valeur_hex);


CREATE TABLE lieux (
    id_lieu BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_projet BIGINT REFERENCES projets(id_projet) ON DELETE CASCADE,
    nom_lieu VARCHAR(255) NOT NULL,
    type_lieu VARCHAR(50) NOT NULL, 
    adresse TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);



CREATE TABLE plateaux (
    id_plateau BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_lieu BIGINT REFERENCES lieux(id_lieu) ON DELETE CASCADE,
    nom VARCHAR(255) NOT NULL,
    type_plateau VARCHAR(50) NOT NULL,
    description TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE scene_plateau (
    id_scene_plateau BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_scene BIGINT REFERENCES scenes(id_scene) ON DELETE CASCADE,
    id_plateau BIGINT REFERENCES plateaux(id_plateau) ON DELETE CASCADE,
    description_utilisation TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_scene, id_plateau)
);

CREATE TABLE scene_lieu (
    id_scene_lieu BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_scene BIGINT REFERENCES scenes(id_scene) ON DELETE CASCADE,
    id_lieu BIGINT REFERENCES lieux(id_lieu) ON DELETE CASCADE,
    id_plateau BIGINT REFERENCES plateaux(id_plateau) ON DELETE SET NULL,
    description_utilisation TEXT, 
    UNIQUE (id_scene, id_lieu)
);

CREATE TABLE commentaires (
    id_commentaire BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE,
    contenu TEXT NOT NULL,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE episode_commentaires (
    id_episode_commentaire BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_episode BIGINT REFERENCES episodes(id_episode) ON DELETE CASCADE,
    id_commentaire BIGINT REFERENCES commentaires(id_commentaire) ON DELETE CASCADE,
    UNIQUE(id_episode, id_commentaire)
);

CREATE TABLE sequence_commentaires (
    id_sequence_commentaire BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_sequence BIGINT REFERENCES sequences(id_sequence) ON DELETE CASCADE,
    id_commentaire BIGINT REFERENCES commentaires(id_commentaire) ON DELETE CASCADE,
    UNIQUE(id_sequence, id_commentaire)
);


CREATE TABLE scene_commentaires (
    id_scene_commentaire BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_scene BIGINT REFERENCES scenes(id_scene) ON DELETE CASCADE,
    id_commentaire BIGINT REFERENCES commentaires(id_commentaire) ON DELETE CASCADE,
    UNIQUE(id_scene, id_commentaire)
);


CREATE TABLE dialogue_commentaires (
    id_dialogue_commentaire BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_dialogue BIGINT REFERENCES dialogues(id_dialogue) ON DELETE CASCADE,
    id_commentaire BIGINT REFERENCES commentaires(id_commentaire) ON DELETE CASCADE,
    UNIQUE(id_dialogue, id_commentaire)
);

CREATE INDEX idx_commentaires_utilisateur ON commentaires(id_utilisateur);
CREATE INDEX idx_episode_commentaires_episode ON episode_commentaires(id_episode);
CREATE INDEX idx_sequence_commentaires_sequence ON sequence_commentaires(id_sequence);
CREATE INDEX idx_scene_commentaires_scene ON scene_commentaires(id_scene);
CREATE INDEX idx_dialogue_commentaires_dialogue ON dialogue_commentaires(id_dialogue);


CREATE TABLE dialogue_surlignages (
    id_dialogue_surlignages BIGINT AUTO_INCREMENT PRIMARY KEY,
    dialogue_id BIGINT NOT NULL,
    couleur_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    texte_surligne TEXT NOT NULL,
    debut_index INTEGER NOT NULL,
    fin_index INTEGER NOT NULL,
    cree_le TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dialogue FOREIGN KEY (dialogue_id) REFERENCES dialogues(id_dialogue),
    CONSTRAINT fk_couleur FOREIGN KEY (couleur_id) REFERENCES couleurs(id_couleur),
    CONSTRAINT fk_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id_utilisateur)
);

CREATE INDEX idx_dialogue_surlignage_dialogue_id ON dialogue_surlignages(dialogue_id);
CREATE INDEX idx_dialogue_surlignage_couleur_id ON dialogue_surlignages(couleur_id);
CREATE INDEX idx_dialogue_surlignage_utilisateur_id ON dialogue_surlignages(utilisateur_id);



CREATE VIEW v_episode_permissions AS
SELECT 
    e.id_episode,
    u.id_utilisateur,
    u.role,
    CASE 
        WHEN u.role = 'ADMIN' THEN 1
        WHEN u.role = 'REALISATEUR' AND er.id_episode_realisateur IS NOT NULL THEN 1
        WHEN u.role = 'SCENARISTE' AND es.id_episode_scenariste IS NOT NULL THEN 1
        ELSE 0
    END as has_access
FROM episodes e
CROSS JOIN utilisateurs u
LEFT JOIN episode_realisateurs er ON e.id_episode = er.id_episode 
    AND er.id_realisateur IN (SELECT id_realisateur FROM realisateurs WHERE id_utilisateur = u.id_utilisateur)
LEFT JOIN episode_scenaristes es ON e.id_episode = es.id_episode 
    AND es.id_scenariste IN (SELECT id_scenariste FROM scenaristes WHERE id_utilisateur = u.id_utilisateur)
WHERE u.role IN ('ADMIN', 'REALISATEUR', 'SCENARISTE');

-- Vue pour les permissions détaillées
CREATE VIEW v_user_episode_permissions AS
SELECT 
    ep.id_episode,
    u.id_utilisateur,
    u.nom,
    u.role,
    -- Permissions de base
    CASE WHEN u.role = 'ADMIN' THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_realisateurs er 
                     JOIN realisateurs r ON er.id_realisateur = r.id_realisateur 
                     WHERE er.id_episode = ep.id_episode AND r.id_utilisateur = u.id_utilisateur) THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_scenaristes es 
                     JOIN scenaristes s ON es.id_scenariste = s.id_scenariste 
                     WHERE es.id_episode = ep.id_episode AND s.id_utilisateur = u.id_utilisateur) THEN 1
         ELSE 0
    END as can_access_episode,
    
    -- Permissions d'édition
    CASE WHEN u.role = 'ADMIN' THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_realisateurs er 
                     JOIN realisateurs r ON er.id_realisateur = r.id_realisateur 
                     WHERE er.id_episode = ep.id_episode AND r.id_utilisateur = u.id_utilisateur) THEN 1
         ELSE 0
    END as can_edit_episode,
    
    -- Permissions de création de séquences
    CASE WHEN u.role = 'ADMIN' THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_realisateurs er 
                     JOIN realisateurs r ON er.id_realisateur = r.id_realisateur 
                     WHERE er.id_episode = ep.id_episode AND r.id_utilisateur = u.id_utilisateur) THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_scenaristes es 
                     JOIN scenaristes s ON es.id_scenariste = s.id_scenariste 
                     WHERE es.id_episode = ep.id_episode AND s.id_utilisateur = u.id_utilisateur) THEN 1
         ELSE 0
    END as can_create_sequence,

    -- Permissions de création de scènes
    CASE WHEN u.role = 'ADMIN' THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_realisateurs er 
                     JOIN realisateurs r ON er.id_realisateur = r.id_realisateur 
                     WHERE er.id_episode = ep.id_episode AND r.id_utilisateur = u.id_utilisateur) THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_scenaristes es 
                     JOIN scenaristes s ON es.id_scenariste = s.id_scenariste 
                     WHERE es.id_episode = ep.id_episode AND s.id_utilisateur = u.id_utilisateur) THEN 1
         ELSE 0
    END as can_create_scene,

    -- Permissions de création de dialogues
    CASE WHEN u.role = 'ADMIN' THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_realisateurs er 
                     JOIN realisateurs r ON er.id_realisateur = r.id_realisateur 
                     WHERE er.id_episode = ep.id_episode AND r.id_utilisateur = u.id_utilisateur) THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_scenaristes es 
                     JOIN scenaristes s ON es.id_scenariste = s.id_scenariste 
                     WHERE es.id_episode = ep.id_episode AND s.id_utilisateur = u.id_utilisateur) THEN 1
         ELSE 0
    END as can_create_dialogue,

    -- Permissions de création de lieux/plateaux
    CASE WHEN u.role = 'ADMIN' THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_realisateurs er 
                     JOIN realisateurs r ON er.id_realisateur = r.id_realisateur 
                     WHERE er.id_episode = ep.id_episode AND r.id_utilisateur = u.id_utilisateur) THEN 1
         WHEN EXISTS (SELECT 1 FROM episode_scenaristes es 
                     JOIN scenaristes s ON es.id_scenariste = s.id_scenariste 
                     WHERE es.id_episode = ep.id_episode AND s.id_utilisateur = u.id_utilisateur) THEN 1
         ELSE 0
    END as can_create_lieu

FROM episodes ep
CROSS JOIN utilisateurs u
WHERE u.role IN ('ADMIN', 'REALISATEUR', 'SCENARISTE');