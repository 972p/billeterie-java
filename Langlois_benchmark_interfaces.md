# 🎟️ Benchmark des Frameworks Java pour un Client Lourd de Billetterie

## 🧩 1. Critères et pondération

| Critère | Description | Poids (%) |
|:--|:--|:--:|
| Intégration à Java | Est-ce un framework natif Java, bien intégré à l’écosystème ? | 10 |
| Installation et configuration | Facilité à installer, créer un projet et afficher une première interface. | 10 |
| Documentation & communauté | Qualité de la doc, disponibilité d’exemples et de forums. | 10 |
| Apprentissage | Temps d’apprentissage et accessibilité pour développeurs Java moyens. | 10 |
| Architecture MVC | Encourage-t-il la structuration du code (MVC, MVVM, etc.) ? | 10 |
| Rendu graphique / modernité | Interface moderne, support CSS, thèmes, etc. | 10 |
| Outils de conception visuelle | Existe-t-il des outils WYSIWYG (type SceneBuilder) ? | 10 |
| Performance | Temps de lancement, fluidité, empreinte mémoire. | 10 |
| Mises à jour / vitalité | Le framework est-il activement maintenu ? | 10 |
| POC & projet concret | Permet-il de démarrer vite mais aussi d’aller loin ? | 10 |

**Total : 100 points**

---

## ⚙️ 2. Frameworks comparés

| Framework | Description |
|:--|:--|
| **JavaFX** | Framework moderne officiel pour les applications graphiques Java (support CSS, FXML, SceneBuilder). |
| **Swing** | Ancien framework standard, stable mais visuellement daté. |
| **SWT / JFace (Eclipse)** | Framework graphique utilisant les widgets natifs du système. |
| **Griffon** | Framework MVC pour applications desktop Java. |
| **Vaadin (mode desktop)** | Framework web-to-desktop, exécutable localement avec un moteur intégré. |

---

## 📊 3. Matrice comparative

| Critère | Poids | JavaFX | Swing | SWT / JFace | Griffon | Vaadin |
|:--|:--:|:--:|:--:|:--:|:--:|:--:|
| Intégration Java | 10 | **10** | **10** | 9 | 8 | 7 |
| Installation & configuration | 10 | 9 | 9 | 8 | 7 | 8 |
| Documentation & communauté | 10 | **10** | 9 | 8 | 7 | 9 |
| Apprentissage | 10 | **9** | 9 | 8 | 7 | 8 |
| Architecture MVC | 10 | **9** | 7 | 8 | **9** | 8 |
| Rendu graphique | 10 | **10** | 6 | 8 | 8 | **9** |
| Outils visuels | 10 | **10 (SceneBuilder)** | 5 | 6 | 5 | 8 |
| Performance | 10 | 9 | 8 | **9** | 7 | 7 |
| Mises à jour / vitalité | 10 | **9** | 6 | 7 | 6 | **9** |
| POC + projet concret | 10 | **9** | 8 | 8 | 8 | 8 |
| **Total /100** |  | **94** | **77** | **80** | **72** | **81** |

---

## 🏁 4. Recommandations

### 🥇 JavaFX — Le standard moderne
**Pourquoi le choisir :**
- Framework officiel soutenu par Oracle / OpenJFX.
- Excellent rendu graphique (effets, CSS, thèmes modernes).
- Outils puissants : **SceneBuilder**, FXML, intégration Maven/Gradle.
- Parfait pour une **application de billetterie professionnelle**.

**Inconvénients :**
- Moins adapté aux machines très anciennes.

**Recommandé pour :** Applications de billetterie modernes, pérennes et ergonomiques.

---

### 🥈 SWT / JFace — Pour la performance native
**Pourquoi le choisir :**
- Utilise les widgets natifs du système.
- Très fluide et stable, idéal pour gros projets internes.

**Inconvénients :**
- Moins d’outils visuels.
- Syntaxe un peu plus lourde.

**Recommandé pour :** Applications performantes sur postes fixes (Windows/Linux).

---

### 🥉 Autres options
| Framework | Cas d’usage |
|:--|:--|
| **Swing** | Projets éducatifs ou existants (legacy). |
| **Griffon** | Prototypage rapide, petits outils MVC. |
| **Vaadin** | App hybride web/desktop via navigateur intégré. |

---

## 📌 Synthèse finale

| Type de besoin | Framework conseillé | Pourquoi |
|:--|:--|:--|
| Application de billetterie moderne et ergonomique | **JavaFX** | Moderne, stable, riche graphiquement |
| Application locale performante sur machine Windows/Linux | **SWT / JFace** | Widgets natifs, fluide et réactif |
| Prototype ou petit projet MVC | **Griffon** | Simple et rapide à mettre en place |
| Application à maintenir (legacy) | **Swing** | Fiable et documenté |
