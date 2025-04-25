package SimulateurGPS;

import java.util.*;

public class carteVille {
    private List<intersection> intersections = new ArrayList<>();
    private List<troncon> troncons = new ArrayList<>();

    public void ajouterintersection(intersection i) {
        intersections.add(i);
    }

    public void ajoutertroncon(troncon t) {
        troncons.add(t);
    }

    public List<intersection> getIntersections() {
        return intersections;
    }

    public List<troncon> gettroncons() {
        return troncons;
    }
}
