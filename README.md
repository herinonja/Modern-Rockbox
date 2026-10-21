# Edition moderne d'un Rockbox simulé dans un iPod

Ce projet Android Studio contient l'application Rockbox iPod complète avec :
- **Rendu visuel 100% IDENTIQUE** à la WebApp AI Studio (OLED Pure Dark, châssis iPod, molette ClickWheel, égaliseur 10 bandes flat, jeu Snake UX).
- **Architecture moderne** : Conteneur WebKit officiel Google (`androidx.webkit:webkit:1.11.0`) avec accélération matérielle GPU native.
- **Retour haptique physique** : Molette tactile connectée au vibreur Android via `AndroidBridge`.
- **Lecture audio continue** : `RockboxPlaybackService` avec support MediaPlayback Android 14.

---

## Méthode 1 : Compilation Locale avec Android Studio
1. Décompressez cette archive dans un dossier.
2. Ouvrez **Android Studio** (Flamingo, Giraffe, Hedgehog ou supérieur).
3. Cliquez sur **File > Open** et sélectionnez ce dossier.
4. Laissez Gradle synchroniser les dépendances (1-2 minutes).
5. Branchez votre smartphone Android en mode débogage USB (ou lancez un émulateur).
6. Cliquez sur le bouton vert **Run ▶** (ou appuyez sur Shift+F10).

Pour générer directement le fichier `.apk` sans installer l'application :
Dans le terminal du projet :
```bash
./gradlew assembleDebug
```
L'APK se trouvera directement dans :
`app/build/outputs/apk/debug/app-debug.apk`

---

## Méthode 2 : Compilation Automatique Gratuite dans le Cloud (GitHub Actions)
1. Créez un nouveau dépôt sur **GitHub** (public ou privé).
2. Poussez tous les fichiers de cette archive sur votre dépôt :
   ```bash
   git init
   git add .
   git commit -m "Initial commit Rockbox Android"
   git branch -M main
   git remote add origin https://github.com/VOTRE_PSEUDO/rockbox-touch.git
   git push -u origin main
   ```
3. Rendez-vous dans l'onglet **Actions** de votre dépôt GitHub : le workflow `Build Rockbox Android APK` compile automatiquement l'APK.
4. Une fois terminé (environ 2 minutes), téléchargez l'artefact **Rockbox-Touch-Debug-APK** et installez l'APK directement sur votre téléphone !
