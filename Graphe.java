import java.util.*;

public class Graphe {
    private Map<String, List<Arete>> adjacence = new HashMap<>();

    public void ajouterNoeud(String nom) {
        adjacence.putIfAbsent(nom, new ArrayList<>());
    }

    public void ajouterArete(String source, String destination, int poids) {
        adjacence.get(source).add(new Arete(destination, poids));
        adjacence.get(destination).add(new Arete(source, poids)); // graphe non orienté
    }

    public List<String> dijkstra(String depart, String arrivee) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> precedents = new HashMap<>();
        PriorityQueue<Noeud> file = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));

        for (String noeud : adjacence.keySet()) {
            distances.put(noeud, Integer.MAX_VALUE);
        }
        distances.put(depart, 0);
        file.add(new Noeud(depart, 0));

        while (!file.isEmpty()) {
            Noeud courant = file.poll();
            for (Arete voisin : adjacence.get(courant.nom)) {
                int nouvelleDistance = distances.get(courant.nom) + voisin.poids;
                if (nouvelleDistance < distances.get(voisin.destination)) {
                    distances.put(voisin.destination, nouvelleDistance);
                    precedents.put(voisin.destination, courant.nom);
                    file.add(new Noeud(voisin.destination, nouvelleDistance));
                }
            }
        }

        // Reconstruction du chemin
        List<String> chemin = new ArrayList<>();
        String courant = arrivee;
        while (courant != null) {
            chemin.add(0, courant);
            courant = precedents.get(courant);
        }

        return chemin;
    }

    // Classes internes
    static class Arete {
        String destination;
        int poids;

        public Arete(String destination, int poids) {
            this.destination = destination;
            this.poids = poids;
        }
    }

    static class Noeud {
        String nom;
        int distance;

        public Noeud(String nom, int distance) {
            this.nom = nom;
            this.distance = distance;
        }
    }
}
