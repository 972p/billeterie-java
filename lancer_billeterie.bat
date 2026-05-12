@echo off
chcp 65001 > nul
:: Script d'exécution portable pour Billeterie Java (Windows)

echo =======================================================
echo        🎟️ Lancement de Billeterie Java (Client Lourd)   
echo =======================================================

:: Vérification de la présence du livrable
if not exist "billeterie.jar" (
    echo ❌ Fichier billeterie.jar introuvable. Veuillez d'abord compiler le projet.
    pause
    exit /b 1
)

echo 🚀 Démarrage de l'interface graphique...

:: Tentative de lancement avec la variable JAVAFX_HOME si configurée
if defined JAVAFX_HOME (
    java --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.fxml -jar billeterie.jar
    exit /b 0
)

:: Lancement standard (pour les JDK intégrant JavaFX comme Liberica JDK Full ou Zulu FX)
java -jar billeterie.jar
if errorlevel 1 (
    echo.
    echo -------------------------------------------------------
    echo ⚠️ L'exécution a echoue. Votre JDK actuel n'intègre pas les modules JavaFX.
    echo.
    echo 👉 SOLUTIONS RAPIDES :
    echo   1. Installez un JDK incluant JavaFX (ex: Liberica JDK Full ou Azul Zulu FX).
    echo   2. Configurez la variable système JAVAFX_HOME pointant vers votre dossier JavaFX SDK.
    echo -------------------------------------------------------
    pause
)
