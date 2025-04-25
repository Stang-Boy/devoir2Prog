package SimulateurGPS;

import java.util.*;

public class GPS {
    private carteVille carte;
    private intersection destination;

    public GPS(carteVille carte) {
        this.carte = carte;
    }

    public void setDestination(intersection d) {
        destination = d;
    }

    public List<intersection> calculerItineraire(intersection depart) {
        Map<intersection, Double> dist = new HashMap<>();
        Map<intersection, intersection> prev = new HashMap<>();
        PriorityQueue<intersection> pq = new PriorityQueue<>(Comparator.comparing(dist::get));
        for (intersection i : carte.getIntersections()) dist.put(i, Double.POSITIVE_INFINITY);
        dist.put(depart, 0.0);
        pq.add(depart);
        while (!pq.isEmpty()) {
            intersection u = pq.poll();
            if (u.equals(destination)) break;
            for (troncon t : carte.gettroncons()) {
                if (t.getA().equals(u) || t.getB().equals(u)) {
                    intersection v = t.getA().equals(u) ? t.getB() : t.getA();
                    double alt = dist.get(u) + t.getDistance() * t.getEtat().getWeight();
                    if (alt < dist.get(v)) {
                        dist.put(v, alt);
                        prev.put(v, u);
                        pq.remove(v);
                        pq.add(v);
                    }
                }
            }
        }
        List<intersection> chemin = new ArrayList<>();
        if (dist.get(destination).isInfinite()) return null;
        for (intersection step = destination; step != null; step = prev.get(step)) chemin.add(0, step);
        return chemin;
    }
}