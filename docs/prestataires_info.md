# Comptes de Test & Informations Connexion

Suite aux mises à jour de la base de données, voici les comptes utilisables pour tester les différents espaces.

## 🔐 Espace Administrateur
**Interface :** Login habituel
- **Email :** `admin@billeterie.fr` (ou votre compte admin habituel)
- **Mot de passe :** `admin123`

## 🤝 Espace Prestataire (NOUVEAU)
**Interface :** Même page de login (redirection automatique)

| Nom du Prestataire | Email (Identifiant) | Mot de passe | Spécialité |
| :--- | :--- | :--- | :--- |
| **Les Délices de Paris** | `contact@delicesparis.fr` | `password123` | Traiteur |
| **SecurGuard** | `secu@securguard.eu` | `password123` | Sécurité |
| **DJ Sono Pro** | `djsono@pro-events.com` | `password123` | Technique & Son |

> [!TIP]
> Lorsque vous vous connectez avec un compte prestataire, vous arrivez sur un **Tableau de Bord spécifique** affichant uniquement les événements auxquels vous êtes associé.

## 🛠️ Instructions de test
1. **Admin** : Créez un nouvel évènement et sélectionnez "SecurGuard" dans la liste des prestataires. Cochez le service "Gardiennage".
2. **Déconnexion**.
3. **Prestataire** : Connectez-vous avec `secu@securguard.eu`.
4. **Vérification** : L'évènement que vous venez de créer (et ceux pré-remplis par le système) doit apparaître dans votre liste de missions.
