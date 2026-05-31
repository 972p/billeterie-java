#!/usr/bin/env bash
# Script d'exécution portable et d'installation rapide pour Billeterie Java

echo "======================================================="
echo "       🎟️ Lancement de Billeterie Java (Client Lourd)   "
echo "======================================================="

# 1. Vérification de la commande Java
if ! command -v java &> /dev/null; then
    echo "❌ Erreur : Java n'est pas installé ou accessible dans votre PATH."
    echo "Veuillez installer un JDK (version 11 ou supérieure)."
    exit 1
fi

# 2. Vérification et regénération automatique du .jar si manquant
if [ ! -f "billeterie.jar" ]; then
    echo "📦 Archive billeterie.jar introuvable. Génération automatique en cours..."
    
    # Détection du SDK JavaFX pour la compilation
    JFX_LIB=""
    if [ -n "$JAVAFX_HOME" ] && [ -d "$JAVAFX_HOME/lib" ]; then
        JFX_LIB="$JAVAFX_HOME/lib"
    elif [ -d "/Users/favelas/JavaFX/javafx-sdk-21.0.9/lib" ]; then
        JFX_LIB="/Users/favelas/JavaFX/javafx-sdk-21.0.9/lib"
    fi

    if [ -n "$JFX_LIB" ]; then
        find src -name "*.java" > sources.txt
        javac -d bin -cp "lib/*:$JFX_LIB/*" @sources.txt
        cp src/config.properties bin/
        cp -r ressource/* bin/
        jar cvfm billeterie.jar MANIFEST.MF -C bin .
        echo "✅ Archive billeterie.jar générée avec succès !"
    else
        # Tentative de compilation sans JavaFX externe (au cas où le JDK inclut déjà JavaFX)
        echo "⚠️ Chemin JavaFX SDK introuvable. Tentative de compilation avec le JDK par défaut..."
        find src -name "*.java" > sources.txt
        if javac -d bin -cp "lib/*" @sources.txt 2>/dev/null; then
            cp src/config.properties bin/
            cp -r ressource/* bin/
            jar cvfm billeterie.jar MANIFEST.MF -C bin .
            echo "✅ Archive billeterie.jar générée avec succès !"
        else
            echo "❌ Erreur : SDK JavaFX introuvable et impossible de compiler le projet."
            echo "Veuillez définir la variable d'environnement JAVAFX_HOME pointant vers votre dossier JavaFX SDK."
            exit 1
        fi
    fi
fi

# 3. Détection du chemin vers les modules JavaFX pour l'exécution
JFX_PATH=""
if [ -n "$JAVAFX_HOME" ] && [ -d "$JAVAFX_HOME/lib" ]; then
    JFX_PATH="$JAVAFX_HOME/lib"
elif [ -d "/Users/favelas/JavaFX/javafx-sdk-21.0.9/lib" ]; then
    JFX_PATH="/Users/favelas/JavaFX/javafx-sdk-21.0.9/lib"
fi

# 4. Lancement de l'application
echo "🚀 Démarrage de l'interface graphique..."

if [ -n "$JFX_PATH" ]; then
    java --module-path "$JFX_PATH" --add-modules javafx.controls,javafx.fxml -jar billeterie.jar
else
    # Tentative de lancement standard (fonctionne nativement sur les JDK avec JavaFX embarqué, ex: Zulu FX / Liberica Full)
    java -jar billeterie.jar || {
        echo "-------------------------------------------------------"
        echo "⚠️ L'exécution native a échoué. Votre distribution Java (JDK) ne contient pas les modules JavaFX."
        echo ""
        echo "👉 SOLUTIONS POUR TESTER DIRECTEMENT :"
        echo "  1. Installez un JDK incluant JavaFX (recommandé : 'Liberica JDK Full' ou 'Azul Zulu FX')."
        echo "  2. Définissez la variable d'environnement JAVAFX_HOME pointant vers votre SDK JavaFX."
        echo "-------------------------------------------------------"
    }
fi
