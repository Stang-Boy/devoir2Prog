/*Version Finale, 25 avril 2025, par Émile Larocque, Hugo Thivierge, Vincent Lafleur, Félix Ladouceur, Sébastien Roussel, Olivier Rossignol*/
package SimulateurGPS;
import java.awt.*;
import java.util.List;

/*Représente un véhicule qui se déplace sur l'itinéraire défini.
 * Cette classe gère la position du véhicule et son rendement graphique
 */
public class Vehicule {
    private List<Intersection> itineraire; //Chemin à suivre
    private int currentSegment; //Segment occupé en direct de l'itinéraire
    private Intersection position; // Position du véhicule en direct
    
    /*Crée un véhicule avec itinéraire donné */
    public Vehicule(List<Intersection> itineraire) {
        this.itineraire = itineraire;
        this.currentSegment = 0;
        this.position = itineraire.get(0);
    }
    
    /*Fait avancer le véhicule */
    public boolean avancer() {
        if (currentSegment >= itineraire.size() - 1) {
            return false; //Arivée à destination
        }
        
        currentSegment++;
        position = itineraire.get(currentSegment);
        
        return currentSegment < itineraire.size() - 1;
    }
    
    /*Dessine le véhicule et son itinéraire */
    public void draw(Graphics2D g2) {
        //Points de départ en vert
        g2.setColor(Color.GREEN);
        g2.fillOval(itineraire.get(0).x - 8, itineraire.get(0).y - 8, 16, 16);
        
        //Position du véhicule en rouge
        g2.setColor(Color.RED);
        g2.fillOval(position.x - 6, position.y - 6, 12, 12);
        
        //Si véhicule arrivé à destination, change en rose
        if (position == itineraire.get(itineraire.size()-1)) {
            g2.setColor(Color.MAGENTA);
            g2.fillOval(position.x - 8, position.y - 8, 16, 16);
        }
    }

    //Accesseurs
    public List<Intersection> getItineraire() {
        return itineraire;
    }
    public int getCurrentSegment() {
        return currentSegment;
    }
}