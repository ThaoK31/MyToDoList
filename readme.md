
Liste des fonctionnalités implémentées :
- [x] Utilisation de l'Api Preference (Stockage des préférences utilisateur et du mode sombre)
- [ ] Ecriture/lecture dans un Fichier
- [ ] Utilisation de SQLite
- [ ] Utilisation de Room
- [x] Utilisation de Firebase (Base de données temps réel pour les catégories et tâches, gestion des utilisateurs)
- [x] Nombre d'activités ou fragment supérieur ou égal à 3 (MainActivity, CategoryFragment, AboutActivity, SettingsActivity, LoginActivity, et gestion des fragments dynamiques pour chaque catégorie)
- [x] Gestion du bouton Back (message de confirmation pour quitter l'application)
- [x] L'affichage d'une liste avec son adapter (CategoryAdapter pour les onglets)
- [x] L'affichage d'une liste avec un custom adapter (TaskAdapter avec gestion des événements de cochage de modification et suppression)
- [x] La pertinence d'utilisation des layouts (L'application doit être responsive et supporter: portrait/paysage et tablette)
- [x] L'utilisation d'événement améliorant l'ux (Long press sur les onglets pour supprimer une catégorie, mode sombre, confirmation par boite de dialogue pour des actions importantes)
- [ ] La réalisation de composant graphique custom (Paint 2D, Calendrier,...) Préciser : 
- [ ] Les taches en background (codage du démarrage d'un thread)
- [x] Le codage d'un menu (TabLayout avec menu d'ajout de catégorie)
- [x] L'application de pattern (MVC pour la gestion des tâches, Adapter pattern pour les listes, Singleton pour MyToDoListApp)

# MyToDoList

MyToDoList est une application Android de gestion de tâches intuitive et personnalisable. Elle permet aux utilisateurs de gérer efficacement leurs tâches quotidiennes en les organisant par catégories.

## Fonctionnalités principales

- **Gestion des tâches**
  - Création, modification et suppression de tâches
  - Organisation par catégories
  - Marquage des tâches comme terminées
  - Suppresion et modifications des tâches par des boites de dialogues
  - Supression des catégories par un long press sur l'onglet
  - Ajout de catégories par un bouton en haut à droite de l'écran

- **Gestion des utilisateurs**
  - Connexion avec un nom d'utilisateur unique
  - Données personnalisées pour chaque utilisateur
  - Synchronisation en temps réel

- **Interface utilisateur**
  - Mode sombre personnalisable
  - Design Material 
  - Support du mode portrait et paysage
  - Optimisation pour tablettes
  - Gestion des fragments dynamiques
    
- **Stockage et synchronisation**
  - Synchronisation en temps réel avec Firebase
  - Stockage local des préférences utilisateur

## Technologies utilisées

- Firebase Realtime Database pour le stockage en temps réel
- Material Design Components pour l'interface utilisateur
- SharedPreferences pour les préférences locales
- RecyclerView avec adaptateurs personnalisés
- Fragments pour une navigation fluide

## Installation

1. Clonez le dépôt
2. Ouvrez le projet dans Android Studio
3. Synchronisez avec Gradle
4. Lancez l'application sur un émulateur ou un appareil Android




