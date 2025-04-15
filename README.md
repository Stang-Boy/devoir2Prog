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

<table border="1">
  <tr>
    <th><h3>Membre</h3></th>
    <th><h3>Tâche</h3></th>
    <th><h3>Branche Git</h3></th>
  </tr>
  <tr>
    <td><strong>Membre 1</strong></td>
    <td>
      <strong>Tâche : Modélisation du système</strong><br>
      <ul>
        <li>Création des classes GPS, Voiture, Route, Intersection, CarteVille</li>
        <li>Gestion des états des routes (fluide, congestionnée, accidentée)</li>
      </ul>
    </td>
    <td><strong>Branche Git :</strong> model-classes</td>
  </tr>
  <tr>
    <td><strong>Membre 2</strong></td>
    <td>
      <strong>Tâche : Implémentation de l'algorithme de calcul de chemin</strong><br>
      <ul>
        <li>Choix et implémentation de l'algorithme de recherche de chemin (Dijkstra, A*)</li>
        <li>Gestion de l'impact des états des routes (par exemple, tenir compte des congestions et accidents)</li>
      </ul>
    </td>
    <td><strong>Branche Git :</strong> pathfinding-algorithm</td>
  </tr>
  <tr>
    <td><strong>Membre 3</strong></td>
    <td>
      <strong>Tâche : Interface graphique de la carte</strong><br>
      <ul>
        <li>Création de la fenêtre principale du système</li>
        <li>Dessin du graphe, de la voiture, des routes et des événements</li>
        <li>Mise à jour dynamique de l'affichage en fonction des changements</li>
      </ul>
    </td>
    <td><strong>Branche Git :</strong> ui-map-display</td>
  </tr>
  <tr>
    <td><strong>Membre 4</strong></td>
    <td>
      <strong>Tâche : Interactions utilisateur</strong><br>
      <ul>
        <li>Gestion des boutons pour l'ajout d'événements comme les accidents ou le recalcul du chemin</li>
        <li>Affichage des instructions de navigation</li>
        <li>Coordination entre l'interface graphique et le modèle de données</li>
      </ul>
    </td>
    <td><strong>Branche Git :</strong> user-interaction</td>
  </tr>
  <tr>
    <td><strong>Membre 5</strong></td>
    <td>
      <strong>Tâche : Gestion des données d'état en temps réel</strong><br>
      <ul>
        <li>Récupération et mise à jour des informations de circulation en temps réel</li>
        <li>Gestion des événements (accidents, congestions) sur les routes</li>
      </ul>
    </td>
    <td><strong>Branche Git :</strong> realtime-data</td>
  </tr>
  <tr>
    <td><strong>Membre 6</strong></td>
    <td>
      <strong>Tâche : Tests et validation</strong><br>
      <ul>
        <li>Écriture et exécution des tests unitaires</li>
        <li>Test d'intégration du système complet</li>
        <li>Validation des fonctionnalités selon les spécifications</li>
      </ul>
    </td>
    <td><strong>Branche Git :</strong> testing-validation</td>
  </tr>
</table>



### Comment démarrer

1. Clonez le projet :
   ```
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
Ce projet est sous la licence [MIT](https://opensource.org/licenses/MIT).
