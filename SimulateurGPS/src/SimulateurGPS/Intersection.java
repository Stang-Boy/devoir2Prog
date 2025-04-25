/*Version Finale, 25 avril 2025, par Émile Larocque, Hugo Thivierge, Vincent Lafleur, Félix Ladouceur, Sébastien Roussel, Olivier Rossignol*/
package SimulateurGPS;

/**
 * Représente une intersection dans le réseau routier.
 * Une intersection est un point nodal de la carte caractérisé par :
 * - Un identifiant unique
 * - Des coordonnées cartésiennes (x,y) pour le positionnement
 * 
 * Cette classe fournit également des méthodes utilitaires pour calculer
 * les distances entre intersections.
 */

public class Intersection {
    public int id, x, y;
    public Intersection(int id, int x, int y) { this.id = id; this.x = x; this.y = y; }
    public double distanceVers(Intersection o) { return Math.hypot(x - o.x, y - o.y); }
}