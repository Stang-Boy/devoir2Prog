package GPSSimulator;

import java.util.*;

public class GPS {
    private CarteVille carte;
    private Intersection destination;
    
    public GPS(CarteVille carte) { this.carte = carte; }
    public void setDestination(Intersection d) { destination = d; }
    
    public List<Intersection> calculerItineraire(Intersection depart) {
        Map<Intersection, Double> dist = new HashMap<>();
        Map<Intersection, Intersection> prev = new HashMap<>();
        PriorityQueue<Intersection> pq = new PriorityQueue<>(Comparator.comparing(dist::get));
        
        for (Intersection i : carte.getIntersections()) {
            dist.put(i, Double.POSITIVE_INFINITY);
        }
        dist.put(depart, 0.0);
        pq.add(depart);
        
        while (!pq.isEmpty()) {
            Intersection u = pq.poll();
            if (u == destination) break;
            
            for (Troncon t : carte.getTroncons()) {
                if (t.getA() == u || t.getB() == u) {
                    Intersection v = (t.getA() == u) ? t.getB() : t.getA();
                    double weight = t.getDistance() * t.getEtat().getWeight();
                    double alt = dist.get(u) + weight;
                    if (alt < dist.get(v)) {
                        dist.put(v, alt);
                        prev.put(v, u);
                        pq.remove(v);
                        pq.add(v);
                    }
                }
            }
        }
        
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