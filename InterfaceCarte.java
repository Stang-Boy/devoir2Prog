import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InterfaceCarte extends JPanel {
    Graphe graphe;
    List<String> chemin;

    public InterfaceCarte() {
        // Création du graphe
        graphe = new Graphe();
        graphe.ajouterNoeud("A");
        graphe.ajouterNoeud("B");
        graphe.ajouterNoeud("C");
        graphe.ajouterNoeud("D");

        graphe.ajouterArete("A", "B", 2);
        graphe.ajouterArete("A", "C", 5);
        graphe.ajouterArete("B", "C", 1);
        graphe.ajouterArete("C", "D", 3);

        chemin = graphe.dijkstra("A", "D");
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Coordonnées des points
        int ax = 100, ay = 100;
        int bx = 300, by = 100;
        int cx = 200, cy = 250;
        int dx = 400, dy = 250;

        // Routes
        g.setColor(Color.GRAY);
        g.drawLine(ax, ay, bx, by); // A-B
        g.drawLine(ax, ay, cx, cy); // A-C
        g.drawLine(bx, by, cx, cy); // B-C
        g.drawLine(cx, cy, dx, dy); // C-D

        // Noeuds
        g.setColor(Color.BLACK);
        g.fillOval(ax - 10, ay - 10, 20, 20);
        g.fillOval(bx - 10, by - 10, 20, 20);
        g.fillOval(cx - 10, cy - 10, 20, 20);
        g.fillOval(dx - 10, dy - 10, 20, 20);

        g.drawString("A", ax - 20, ay - 10);
        g.drawString("B", bx + 10, by - 10);
        g.drawString("C", cx - 10, cy + 25);
        g.drawString("D", dx + 10, dy + 10);

        // Affichage du plus court chemin
        g.setColor(Color.RED);
        for (int i = 0; i < chemin.size() - 1; i++) {
            String p1 = chemin.get(i);
            String p2 = chemin.get(i + 1);
            Point pt1 = getCoordonnees(p1, ax, ay, bx, by, cx, cy, dx, dy);
            Point pt2 = getCoordonnees(p2, ax, ay, bx, by, cx, cy, dx, dy);
            g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
        }
    }

    private Point getCoordonnees(String nom, int ax, int ay, int bx, int by, int cx, int cy, int dx, int dy) {
        return switch (nom) {
            case "A" -> new Point(ax, ay);
            case "B" -> new Point(bx, by);
            case "C" -> new Point(cx, cy);
            case "D" -> new Point(dx, dy);
            default -> new Point(0, 0);
        };
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Carte + Dijkstra");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new InterfaceCarte());
        frame.setVisible(true);
    }
}
