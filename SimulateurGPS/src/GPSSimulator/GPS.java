/*Version Finale, 25 avril 2025, par Émile Larocque, Hugo Thivierge, Vincent Lafleur, Félix Ladouceur, Sébastien Roussel, Olivier Rossignol*/
package GPSSimulator;

import java.util.*;

/**
 * Système de calcul d'itinéraire utilisant l'algorithme Dijkstra.
 * Trouve le chemin optimal entre deux intersections en tenant compte des conditions de circulation.
 */

public class GPS {
    private CarteVille carte;
    private Intersection destination;
    
    /**
     * Initialise le GPS avec une carte routière.
     */
    public GPS(CarteVille carte) { this.carte = carte; }

    /*Définit la destination pour le calcul d'itinéraire */
    public void setDestination(Intersection d) { destination = d; }
    
    /*Calcule l'itinéraire le plus court entre le départ et la destination */
    public List<Intersection> calculerItineraire(Intersection depart) {
        Map<Intersection, Double> dist = new HashMap<>();
        Map<Intersection, Intersection> prev = new HashMap<>();
        PriorityQueue<Intersection> pq = new PriorityQueue<>(Comparator.comparing(dist::get));
        
        /*Les distances sont à l'infini au départ */
        for (Intersection i : carte.getIntersections()) {
            dist.put(i, Double.POSITIVE_INFINITY);
        }
        dist.put(depart, 0.0);
        pq.add(depart);
        
        /*Scan le graphe */
        while (!pq.isEmpty()) {
            Intersection u = pq.poll();
            if (u == destination) break;
            
            /*Examine les tronçons adjacents */
            for (Troncon t : carte.getTroncons()) {
                if (t.getA() == u || t.getB() == u) {
                    Intersection v = (t.getA() == u) ? t.getB() : t.getA();
                    double weight = t.getDistance() * t.getEtat().getWeight();
                    double alt = dist.get(u) + weight;

                    /*Mise à jour si un meilleur chemin est trouvé */
                    if (alt < dist.get(v)) {
                        dist.put(v, alt);
                        prev.put(v, u);
                        pq.remove(v);
                        pq.add(v);
                    }
                }
            }
        }
        /*Reconstruction du chemin à partir des informations recueillies */
        List<Intersection> chemin = new ArrayList<>();
        Intersection step = destination;
        if (!dist.get(step).isInfinite()) {
            while (prev.containsKey(step)) {
                chemin.add(0, step);
                step = prev.get(step);
            }
            chemin.add(0, depart);
        }
        
        return chemin.isEmpty() ? null : chemin;
    }
}