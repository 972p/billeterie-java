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

## 📦 Mise en Production & Test Rapide

L'application est empaquetée sous forme d'archive exécutable **`billeterie.jar`** prête pour la production.

### ⚡ Scripts de Lancement Automatique (Prêt à tester)

Pour permettre à n'importe qui de tester l'application instantanément sans se soucier des commandes de terminal complexes, deux scripts portables sont fournis à la racine du projet :

- **Sur macOS / Linux** : Exécutez simplement la commande suivante dans votre terminal :
  ```bash
  chmod +x lancer_billeterie.sh
  ./lancer_billeterie.sh
  ```
  *(Ce script est intelligent : si le fichier `billeterie.jar` n'a pas encore été généré, il le compile et l'assemble automatiquement pour vous !)*

- **Sur Windows** : Double-cliquez directement sur le fichier **`lancer_billeterie.bat`**.

---

### Instructions d'utilisation manuelle du `.jar`

1. **Dépendances requises :**
   L'archive `billeterie.jar` fait référence aux bibliothèques externes situées dans le dossier `lib/`. Si vous déplacez le `.jar`, assurez-vous que le dossier `lib/` reste accessible dans le même répertoire.

2. **Lancement standard (JDK incluant JavaFX, ex: Liberica JDK Full ou Zulu FX) :**
   ```bash
   java -jar billeterie.jar
   ```

3. **Lancement avec un JDK standard (nécessitant le SDK JavaFX externe) :**
   Si votre environnement de production utilise un JDK classique sans les modules JavaFX intégrés, spécifiez le chemin vers la bibliothèque JavaFX via l'option `--module-path` :
   ```bash
   java --module-path /chemin/vers/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar billeterie.jar
   ```

> [!TIP]
> **Base de données en production :** Lors de l'exécution du `.jar`, le schéma de la base de données configurée dans `config.properties` est automatiquement vérifié et mis à jour si nécessaire.

## 🔑 Comptes de Démonstration & Test

Pour évaluer rapidement les différents espaces de l'application, voici les identifiants préconfigurés (générés via `SetupDatabase.java` et `SeedDatabase.java`) :

### 🔐 Espace Administrateur
- **Email** : `admin@favelas.eu` (ou `admin@billeterie.fr`)
- **Mot de passe** : `admin` (ou `admin123`)

### 👤 Espace Client standard
- **Email** : `client@favelas.eu`
- **Mot de passe** : `client`

### 🤝 Espace Prestataire
- **Identifiant** : `secu@securguard.eu`
- **Mot de passe** : `password123`
*(Accès direct au tableau de bord des missions et services associés à vos événements).*

## 📝 Auteurs & Licence

Développé dans le cadre d'un projet d'étude complet (Base de données, Conception, POO et Interface graphique).
