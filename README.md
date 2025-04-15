# Devoir 2 - Système de Guidage Routier

Ce projet simule un système de guidage routier, où une voiture est guidée à travers un réseau routier. Le système prend en compte les événements en temps réel, comme les accidents ou les congestions, et ajuste automatiquement l'itinéraire de la voiture. L'interface graphique permet à l'utilisateur d'interagir avec le système pour provoquer des événements et observer les changements d'état du réseau.

## Structure du projet

### Arborescence du code

```
/src
  /model       -> Gestion du modèle de données (Voiture, Route, GPS, etc.)
  /algorithm   -> Implémentation de l'algorithme de calcul de chemin
  /ui          -> Interface graphique et gestion des interactions utilisateur
  /main        -> Classe de lancement du système
```

### Branches Git

- **`main`** : Branche stable du projet, contient la version finale et validée du système.
- **`dev`** : Branche de développement où les fonctionnalités sont intégrées avant d'être fusionnées dans `main`.

Chaque membre de l’équipe doit créer une branche fonctionnelle basée sur `dev` pour travailler sur une partie spécifique du projet. Une fois une tâche terminée, il est nécessaire de soumettre une **pull request** pour fusionner la branche avec `dev`.

### Répartition des tâches

| **Membre**        || **Branche Git**         || **Tâche**  
|-------------------||-------------------------||
| **Personne 1**    || `model-classes`         || Modélisation du système : Création des classes `GPS`, `Voiture`, `Route`, `Intersection`, `CarteVille` et Gestion des états des routes. 
| **Personne 2**    || `pathfinding-algorithm` || Implémentation de l'algorithme de calcul de chemin : Choix et implémentation de l'algorithme de recherche de chemin (Dijkstra, A*). Gestion de l’impact des états des routes.
| **Personne 3**    || `ui-map-display`        || Interface graphique de la carte : Création de la fenêtre principale.<br>Dessin du graphe, de la voiture, des routes et des événements. Mise à jour dynamique de l’affichage.
| **Personne 4**    || `user-interaction`      || Interactions utilisateur : Gestion des boutons pour l'ajout d'événements (accident, recalcul). Affichage des instructions de navigation. Coordination avec le modèle. 

### Comment démarrer

1. Clonez le projet :

   git clone https://github.com/Stang-Boy/devoir2Prog
   ```

2. Accédez au répertoire du projet :
   ```
   cd devoir2Prog
   ```


### Contribuer

1. Créez une nouvelle branche pour votre fonctionnalité :
   ```
   git checkout -b ma-nouvelle-branche
   ```

2. Effectuez vos changements, puis ajoutez-les et commitez-les :
   ```
   git add .
   git commit -m "Description de la fonctionnalité"
   ```

3. Poussez votre branche :
   ```
   git push origin ma-nouvelle-branche
   ```

4. Lorsqu'une fonctionnalité est terminée :

   Ouvrez une pull request vers la branche `dev` pour révision.


### Licences

## don't know c'est quoi ca mais si jamais
Ce projet est sous la licence [MIT](https://opensource.org/licenses/MIT).
⠀⠀