/*Version Finale, 25 avril 2025, par Émile Larocque, Hugo Thivierge, Vincent Lafleur, Félix Ladouceur, Sébastien Roussel, Olivier Rossignol*/
package GPSSimulator;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une carte routière d'une ville composée d'intersections et de tronçons.
 * Cette classe sert de modèle principal pour le simulateur GPS, stockant l'ensemble
 * des éléments du réseau routier.
 */

public class CarteVille {
    private List<Intersection> intersections = new ArrayList<>();
    private List<Troncon> troncons = new ArrayList<>();
    
/**
     * Ajoute respectivement une intersection, un troncon, retourne la liste d'intersection et retourne la liste de troncon à la carte.
     */

    public void ajouterIntersection(Intersection i) { intersections.add(i); }
    public void ajouterTroncon(Troncon t) { troncons.add(t); }
    public List<Intersection> getIntersections() { return intersections; }
    public List<Troncon> getTroncons() { return troncons; }
}