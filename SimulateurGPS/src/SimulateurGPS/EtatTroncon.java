/*Version Finale, 25 avril 2025, par Émile Larocque, Hugo Thivierge, Vincent Lafleur, Félix Ladouceur, Sébastien Roussel, Olivier Rossignol*/
package GPSSimulator;

/**
 * Énumération représentant les différents états de circulation possibles pour un tronçon routier.
 * Chaque état a un coefficient de poids associé utilisé pour le calcul d'itinéraire optimal.
 */

public enum EtatTroncon {
    /** Valeurs pour chaque états de circulation */
    FLUIDE(1.0), FAIBLE(1.3), MODERE(1.7), INTENSE(2.5), ACCIDENT(Double.POSITIVE_INFINITY);
    
    /** Constructeur pour les valeurs et coefficient retourné au poids associé */
    private final double weight;
    EtatTroncon(double weight) { this.weight = weight; }
    public double getWeight() { return weight; }
}