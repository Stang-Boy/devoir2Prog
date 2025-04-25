/*Version Finale, 25 avril 2025, par Émile Larocque, Hugo Thivierge, Vincent Lafleur, Félix Ladouceur, Sébastien Roussel, Olivier Rossignol*/
package SimulateurGPS;

/**
 * Représente un tronçon routier entre deux intersections.
 * Un tronçon possède :
 * - Deux extrémités (intersections a et b)
 * - Une longueur (distance)
 * - Un nom de rue
 * - Un état de circulation
 * 
 * Cette classe est utilisée par le système GPS pour modéliser
 * les routes et calculer les itinéraires optimaux.
 */
public class Troncon {
    private Intersection a, b;
    private double distance;
    private String nomRue;
    private EtatTroncon etat;
    
    /*Constructeur pour tronçon routier */
    public Troncon(Intersection a, Intersection b, double distance, String nomRue) {
        this.a = a;
        this.b = b;
        this.distance = distance;
        this.nomRue = nomRue;
        this.etat = EtatTroncon.FLUIDE;
    }
    
    public Intersection getA() { return a; } //Intersection de départ
    public Intersection getB() { return b; } //Intersection d'arrivée
    public double getDistance() { return distance; } //Longueur du tronçon
    public String getNomRue() { return nomRue; } //Nom de la rue
    public EtatTroncon getEtat() { return etat; } //État en direct du tronçon
    public void setEtat(EtatTroncon etat) { this.etat = etat; } //Modificateur d'état de la circulation
}