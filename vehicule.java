package SimulateurGPS;

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.List;

public class vehicule {
    List<intersection> itineraire;
    int currentSegment;
    intersection position;

    public vehicule(List<intersection> itineraire) {
        this.itineraire = itineraire;
        currentSegment = 0;
        position = itineraire.get(0);
    }

    public boolean avancer() {
        if (currentSegment >= itineraire.size() - 1) return false;
        currentSegment++;
        position = itineraire.get(currentSegment);
        return currentSegment < itineraire.size() - 1;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.GREEN);
        intersection s = itineraire.get(0);
        g2.fillOval(s.x - 8, s.y - 8, 16, 16);
        g2.setColor(Color.RED);
        g2.fillOval(position.x - 6, position.y - 6, 12, 12);
        if (position.equals(itineraire.get(itineraire.size() - 1))) {
            g2.setColor(Color.MAGENTA);
            g2.fillOval(position.x - 8, position.y - 8, 16, 16);
        }
    }
}
