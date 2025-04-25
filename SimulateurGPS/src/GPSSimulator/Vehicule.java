package GPSSimulator;
import java.awt.*;
import java.util.List;

public class Vehicule {
    private List<Intersection> itineraire;
    private int currentSegment;
    private Intersection position;
    
    public Vehicule(List<Intersection> itineraire) {
        this.itineraire = itineraire;
        this.currentSegment = 0;
        this.position = itineraire.get(0);
    }
    
    public boolean avancer() {
        if (currentSegment >= itineraire.size() - 1) {
            return false;
        }
        
        currentSegment++;
        position = itineraire.get(currentSegment);
        
        return currentSegment < itineraire.size() - 1;
    }
    
    public void draw(Graphics2D g2) {
        g2.setColor(Color.GREEN);
        g2.fillOval(itineraire.get(0).x - 8, itineraire.get(0).y - 8, 16, 16);
        
        g2.setColor(Color.RED);
        g2.fillOval(position.x - 6, position.y - 6, 12, 12);
        
        if (position == itineraire.get(itineraire.size()-1)) {
            g2.setColor(Color.MAGENTA);
            g2.fillOval(position.x - 8, position.y - 8, 16, 16);
        }
    }

    public List<Intersection> getItineraire() {
        return itineraire;
    }

    public int getCurrentSegment() {
        return currentSegment;
    }
}