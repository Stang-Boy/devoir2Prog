package GPSSimulator;

import java.util.ArrayList;
import java.util.List;

public class CarteVille {
    private List<Intersection> intersections = new ArrayList<>();
    private List<Troncon> troncons = new ArrayList<>();
    
    public void ajouterIntersection(Intersection i) { intersections.add(i); }
    public void ajouterTroncon(Troncon t) { troncons.add(t); }
    public List<Intersection> getIntersections() { return intersections; }
    public List<Troncon> getTroncons() { return troncons; }
}