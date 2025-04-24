import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// Main class to run the navigation system
public class NavigationSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfaceGraphique gui = new InterfaceGraphique();
            gui.setVisible(true);
        });
    }
}

// Coordonnee class for 2D positions
class Coordonnee {
    private int x, y;

    public Coordonnee(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}

// Abstract Route class
abstract class Route {
    protected double longueur;
    protected String nom;
    protected String etat;

    public Route(double longueur, String nom) {
        this.longueur = longueur;
        this.nom = nom;
        this.etat = "fluide";
    }

    public double getLongueur() { return longueur; }
    public String getNom() { return nom; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public boolean estAccessible() { return !etat.equals("fermé"); }
}

// Troncon class (inherits from Route)
class Troncon extends Route {
    private int vitesseMax;
    private Intersection intersectionDepart;
    private Intersection intersectionArrivee;

    public Troncon(double longueur, String nom, int vitesseMax, Intersection depart, Intersection arrivee) {
        super(longueur, nom);
        this.vitesseMax = vitesseMax;
        this.intersectionDepart = depart;
        this.intersectionArrivee = arrivee;
    }

    public Intersection getIntersectionDepart() { return intersectionDepart; }
    public Intersection getIntersectionArrivee() { return intersectionArrivee; }
    public double calculerTempsDeParcours() {
        double temps = longueur / vitesseMax;
        if (etat.equals("congestion")) temps *= 2;
        return temps;
    }
    public boolean estAffecteParEvenement() { return !etat.equals("fluide"); }
}

// Intersection class
class Intersection {
    private int id;
    private Coordonnee position;
    private List<Troncon> tronconsAdjacents;

    public Intersection(int id, Coordonnee position) {
        this.id = id;
        this.position = position;
        this.tronconsAdjacents = new ArrayList<>();
    }

    public int getId() { return id; }
    public Coordonnee getPosition() { return position; }
    public void ajouterTroncon(Troncon t) { tronconsAdjacents.add(t); }
    public List<Troncon> getTronconsDisponibles() {
        List<Troncon> disponibles = new ArrayList<>();
        for (Troncon t : tronconsAdjacents) {
            if (t.estAccessible()) disponibles.add(t);
        }
        return disponibles;
    }
}

// CarteVille class
class CarteVille {
    private List<Intersection> intersections;
    private List<Troncon> troncons;

    public CarteVille() {
        intersections = new ArrayList<>();
        troncons = new ArrayList<>();
    }

    public void ajouterIntersection(Intersection i) { intersections.add(i); }
    public void ajouterTroncon(Troncon t) { troncons.add(t); }

    public List<Troncon> trouverItineraire(Intersection depart, Intersection arrivee) {
        // Dijkstra's algorithm
        Map<Intersection, Double> distances = new HashMap<>();
        Map<Intersection, Troncon> predecesseurs = new HashMap<>();
        PriorityQueue<Intersection> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        Set<Intersection> visite = new HashSet<>();

        for (Intersection i : intersections) {
            distances.put(i, Double.POSITIVE_INFINITY);
        }
        distances.put(depart, 0.0);
        queue.add(depart);

        while (!queue.isEmpty()) {
            Intersection courant = queue.poll();
            if (visite.contains(courant)) continue;
            visite.add(courant);

            if (courant == arrivee) break;

            for (Troncon t : courant.getTronconsDisponibles()) {
                Intersection voisin = (t.getIntersectionDepart() == courant) ?
                        t.getIntersectionArrivee() : t.getIntersectionDepart();
                double poids = t.calculerTempsDeParcours();
                double nouvelleDistance = distances.get(courant) + poids;

                if (nouvelleDistance < distances.get(voisin)) {
                    distances.put(voisin, nouvelleDistance);
                    predecesseurs.put(voisin, t);
                    queue.add(voisin);
                }
            }
        }

        // Reconstruct path
        List<Troncon> itineraire = new ArrayList<>();
        Intersection courant = arrivee;
        while (predecesseurs.containsKey(courant)) {
            Troncon t = predecesseurs.get(courant);
            itineraire.add(t);
            courant = (t.getIntersectionDepart() == courant) ?
                    t.getIntersectionArrivee() : t.getIntersectionDepart();
        }
        Collections.reverse(itineraire);
        return itineraire.isEmpty() ? null : itineraire;
    }

    public List<Intersection> getIntersections() { return intersections; }
    public List<Troncon> getTroncons() { return troncons; }
}

// EvenementRoutier class
class EvenementRoutier {
    private String type;
    private Troncon troncon;
    private int gravite;

    public EvenementRoutier(String type, Troncon troncon, int gravite) {
        this.type = type;
        this.troncon = troncon;
        this.gravite = gravite;
    }

    public void affecterTroncon() {
        if (type.equals("accident")) {
            troncon.setEtat("fermé");
        } else if (type.equals("congestion")) {
            troncon.setEtat("congestion");
        }
    }
}

// GPS class
class GPS {
    private List<Troncon> itineraire;
    private Intersection positionActuelle;
    private Intersection destination;

    public void calculerItineraire(CarteVille carte, Intersection depart, Intersection arrivee) {
        positionActuelle = depart;
        destination = arrivee;
        itineraire = carte.trouverItineraire(depart, arrivee);
    }

    public void mettreAJourItineraireEnCasEvenement(CarteVille carte) {
        if (itineraire == null) return;
        for (Troncon t : itineraire) {
            if (t.estAffecteParEvenement()) {
                itineraire = carte.trouverItineraire(positionActuelle, destination);
                break;
            }
        }
    }

    public String donnerInstructionSuivante() {
        if (itineraire == null || itineraire.isEmpty()) return "Aucun itinéraire.";
        Troncon prochain = itineraire.get(0);
        return "Suivez " + prochain.getNom() + " (" + prochain.getEtat() + ")";
    }

    public List<Troncon> getItineraire() { return itineraire; }
    public void avancer() {
        if (itineraire != null && !itineraire.isEmpty()) {
            Troncon courant = itineraire.remove(0);
            positionActuelle = (courant.getIntersectionDepart() == positionActuelle) ?
                    courant.getIntersectionArrivee() : courant.getIntersectionDepart();
        }
    }

    public Intersection getPositionActuelle() { return positionActuelle; }
    public Intersection getDestination() { return destination; }
}

// Vehicule class
class Vehicule {
    private Coordonnee positionActuelle;
    private Coordonnee destination;
    private GPS gps;

    public Vehicule(Coordonnee position, Coordonnee destination) {
        this.positionActuelle = position;
        this.destination = destination;
        this.gps = new GPS();
    }

    public void seDeplacer() {
        gps.avancer();
        // Update position to current intersection
        if (gps.getItineraire() != null && !gps.getItineraire().isEmpty()) {
            Troncon t = gps.getItineraire().get(0);
            positionActuelle = t.getIntersectionDepart().getPosition();
        } else if (gps.getDestination() != null) {
            positionActuelle = gps.getDestination().getPosition();
        }
    }

    public Coordonnee getPositionActuelle() { return positionActuelle; }
    public GPS getGps() { return gps; }
}

// InterfaceGraphique class
class InterfaceGraphique extends JFrame {
    private CarteVille carte;
    private Vehicule vehicule;
    private GPS gps;
    private JLabel instructionLabel;
    private JPanel cartePanel;
    private JComboBox<String> destinationComboBox;

    public InterfaceGraphique() {
        setTitle("Système de Guidage Routier");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize model
        carte = new CarteVille();
        initialiserCarte();
        vehicule = new Vehicule(new Coordonnee(100, 100), new Coordonnee(300, 500));
        gps = vehicule.getGps();
        gps.calculerItineraire(carte, carte.getIntersections().get(0), carte.getIntersections().get(7));

        // Initialize GUI components
        cartePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw troncons
                for (Troncon t : carte.getTroncons()) {
                    Coordonnee start = t.getIntersectionDepart().getPosition();
                    Coordonnee end = t.getIntersectionArrivee().getPosition();
                    if (t.getEtat().equals("fluide")) g2d.setColor(Color.GREEN);
                    else if (t.getEtat().equals("congestion")) g2d.setColor(Color.ORANGE);
                    else g2d.setColor(Color.RED);
                    g2d.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
                    // Draw troncon name
                    g2d.drawString(t.getNom(), (start.getX() + end.getX()) / 2, (start.getY() + end.getY()) / 2);
                }

                // Draw intersections
                for (Intersection i : carte.getIntersections()) {
                    g2d.setColor(Color.BLUE);
                    g2d.fillOval(i.getPosition().getX() - 5, i.getPosition().getY() - 5, 10, 10);
                    g2d.drawString("I" + i.getId(), i.getPosition().getX() + 10, i.getPosition().getY());
                }

                // Draw vehicle
                g2d.setColor(Color.BLACK);
                Coordonnee pos = vehicule.getPositionActuelle();
                g2d.fillOval(pos.getX() - 7, pos.getY() - 7, 14, 14);

                // Draw itinerary
                if (gps.getItineraire() != null) {
                    g2d.setColor(Color.MAGENTA);
                    for (Troncon t : gps.getItineraire()) {
                        Coordonnee start = t.getIntersectionDepart().getPosition();
                        Coordonnee end = t.getIntersectionArrivee().getPosition();
                        g2d.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
                    }
                }
            }
        };
        add(cartePanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel();
        JButton avancerButton = new JButton("Avancer");
        JButton accidentButton = new JButton("Simuler Accident");
        instructionLabel = new JLabel(gps.donnerInstructionSuivante());

        // Initialize JComboBox for destination selection
        destinationComboBox = new JComboBox<>();
        for (Intersection i : carte.getIntersections()) {
            destinationComboBox.addItem("I" + i.getId());
        }
        destinationComboBox.setSelectedIndex(7); // Default to I8
        destinationComboBox.addActionListener(e -> {
            int selectedIndex = destinationComboBox.getSelectedIndex();
            Intersection newDestination = carte.getIntersections().get(selectedIndex);
            gps.calculerItineraire(carte, gps.getPositionActuelle(), newDestination);
            instructionLabel.setText(gps.donnerInstructionSuivante());
            cartePanel.repaint();
        });

        controlPanel.add(new JLabel("Destination:"));
        controlPanel.add(destinationComboBox);
        controlPanel.add(avancerButton);
        controlPanel.add(accidentButton);
        controlPanel.add(instructionLabel);
        add(controlPanel, BorderLayout.SOUTH);

        // Action listeners
        avancerButton.addActionListener(e -> {
            vehicule.seDeplacer();
            gps.mettreAJourItineraireEnCasEvenement(carte);
            instructionLabel.setText(gps.donnerInstructionSuivante());
            cartePanel.repaint();
        });

        accidentButton.addActionListener(e -> {
            // Simulate accident on first troncon
            if (!carte.getTroncons().isEmpty()) {
                EvenementRoutier evt = new EvenementRoutier("accident", carte.getTroncons().get(0), 5);
                evt.affecterTroncon();
                gps.mettreAJourItineraireEnCasEvenement(carte);
                instructionLabel.setText(gps.donnerInstructionSuivante());
                cartePanel.repaint();
            }
        });
    }

    private void initialiserCarte() {
        // Create a map with 8 intersections (4x2 grid)
        Intersection i1 = new Intersection(1, new Coordonnee(100, 100));
        Intersection i2 = new Intersection(2, new Coordonnee(300, 100));
        Intersection i3 = new Intersection(3, new Coordonnee(500, 100));
        Intersection i4 = new Intersection(4, new Coordonnee(700, 100));
        Intersection i5 = new Intersection(5, new Coordonnee(100, 300));
        Intersection i6 = new Intersection(6, new Coordonnee(300, 300));
        Intersection i7 = new Intersection(7, new Coordonnee(500, 300));
        Intersection i8 = new Intersection(8, new Coordonnee(300, 500));

        // Create troncons to connect intersections
        Troncon t1 = new Troncon(200, "Rue A", 50, i1, i2);
        Troncon t2 = new Troncon(200, "Rue B", 50, i2, i3);
        Troncon t3 = new Troncon(200, "Rue C", 50, i3, i4);
        Troncon t4 = new Troncon(200, "Rue D", 50, i1, i5);
        Troncon t5 = new Troncon(200, "Rue E", 50, i5, i6);
        Troncon t6 = new Troncon(200, "Rue F", 50, i6, i7);
        Troncon t7 = new Troncon(200, "Rue G", 50, i2, i6);
        Troncon t8 = new Troncon(200, "Rue H", 50, i3, i7);
        Troncon t9 = new Troncon(200, "Rue I", 50, i6, i8);
        Troncon t10 = new Troncon(200, "Rue J", 50, i5, i8);

        // Add troncons to intersections
        i1.ajouterTroncon(t1);
        i1.ajouterTroncon(t4);
        i2.ajouterTroncon(t1);
        i2.ajouterTroncon(t2);
        i2.ajouterTroncon(t7);
        i3.ajouterTroncon(t2);
        i3.ajouterTroncon(t3);
        i3.ajouterTroncon(t8);
        i4.ajouterTroncon(t3);
        i5.ajouterTroncon(t4);
        i5.ajouterTroncon(t5);
        i5.ajouterTroncon(t10);
        i6.ajouterTroncon(t5);
        i6.ajouterTroncon(t6);
        i6.ajouterTroncon(t7);
        i6.ajouterTroncon(t9);
        i7.ajouterTroncon(t6);
        i7.ajouterTroncon(t8);
        i8.ajouterTroncon(t9);
        i8.ajouterTroncon(t10);

        // Add intersections and troncons to carte
        carte.ajouterIntersection(i1);
        carte.ajouterIntersection(i2);
        carte.ajouterIntersection(i3);
        carte.ajouterIntersection(i4);
        carte.ajouterIntersection(i5);
        carte.ajouterIntersection(i6);
        carte.ajouterIntersection(i7);
        carte.ajouterIntersection(i8);
        carte.ajouterTroncon(t1);
        carte.ajouterTroncon(t2);
        carte.ajouterTroncon(t3);
        carte.ajouterTroncon(t4);
        carte.ajouterTroncon(t5);
        carte.ajouterTroncon(t6);
        carte.ajouterTroncon(t7);
        carte.ajouterTroncon(t8);
        carte.ajouterTroncon(t9);
        carte.ajouterTroncon(t10);
    }
}