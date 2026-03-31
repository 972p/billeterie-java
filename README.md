# 🎟️ Billeterie Java

Une application de bureau de billetterie événementielle développée en **Java** structurée autour de l'architecture **MVC**. Elle intègre une interface graphique réalisée avec **JavaFX** et utilise une base de données **MySQL** via JDBC.

## 🌟 Fonctionnalités Principales

- **Authentification & Inscription :** Système de connexion et de création de compte sécurisé.
- **Gestion des Billets :** Visualisation, réservation et gestion des billets d'événements.
- **Base de Données Automatisée :** 
  - Scripts intégrés pour mettre à jour le schéma (`UpdateSchema.java`).
  - Peuplement de la base avec des données de test (`SeedDatabase.java`).
- **Tests Fonctionnels et Unitaires :** Vérification de la fiabilité du code (notamment les AuthFunctionalTest).

## 🛠️ Technologies Utilisées

- **Langage** : Java
- **Interface Graphique** : JavaFX (avec fichiers `*.fxml` et CSS)
- **Base de Données** : MySQL (JDBC)
- **Architecture** : Modèle-Vue-Contrôleur (MVC) & Data Access Object (DAO)

## 🗂️ Structure du Projet

```text
billeterie-java/
├── lib/               # Bibliothèques externes (pilotes JDBC, JavaFX, etc.)
├── ressource/         # Fichiers ressources (Vues FXML, CSS, images)
│   └── views/
├── src/               # Code source principal
│   ├── App.java       # Point d'entrée de l'application (Démarrage JavaFX)
│   ├── controllers/   # Contrôleurs pour la logique de l'interface graphique
│   ├── models/        # Entités et modèles métiers
│   ├── DAO/           # Objets d'accès aux données (Requêtes SQL)
│   ├── database/      # Utilitaires de connexion et gestion DB
│   └── utils/         # Classes utilitaires transversales
├── test/              # Tests unitaires et fonctionnels (ex: AuthFunctionalTest)
```

## ⚙️ Prérequis

- **Java Development Kit (JDK)** : Version 11 ou supérieure (supportant JavaFX).
- **Base de données MySQL** : Serveur MySQL local fonctionnel (ex: MAMP, WAMP, XAMPP). Par défaut, l'application pointe sur le port `8889`.

## 🚀 Installation & Lancement

1. **Cloner le projet** (ou télécharger l'archive).
2. **Configurer la Base de données** :
   - Assurez-vous que votre serveur MySQL est démarré.
   - Par défaut, l'application tente de se connecter à la base nommée `bdd_evenementielle`.
3. **Paramétrer la configuration DB** :
   Ouvrez le fichier `src/config.properties` et modifiez les identifiants si besoin :
   ```properties
   db.url=jdbc:mysql://localhost:8889/bdd_evenementielle
   db.username=root
   db.password=root
   ```
4. **Initialiser la Base de données** :
   - À l'exécution, l'application appellera `UpdateSchema.main(new String[0]);` qui crée ou met à jour les tables nécessaires.
   - Si vous avez besoin de données par défaut, vous pouvez exécuter le fichier `src/SeedDatabase.java`.
5. **Lancer l'application** :
   - Exécutez le fichier `src/App.java` pour démarrer l'interface graphique (sur la page de connexion).

## 📝 Auteurs & Licence

Développé dans le cadre d'un projet d'étude complet (Base de données, Conception, POO et Interface graphique).
