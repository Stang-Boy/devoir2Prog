import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.List;

public class SystemeGuidageRoutier {
    // Positions fixes des intersections (x, y)
    private static final int[] INTERSECTION_X = {100, 250, 400, 380, 450, 300, 150, 80, 200, 280, 350, 220};
    private static final int[] INTERSECTION_Y = {100, 80, 120, 250, 350, 400, 380, 300, 200, 180, 220, 320};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CarteVille carte = creerCarteTest();
            Vehicule vehicule = new Vehicule(carte.getIntersections().get(0));
            new InterfaceGraphique(carte, vehicule).afficher();
        });
    }

    private static CarteVille creerCarteTest() {
        CarteVille carte = new CarteVilleImpl();
        Random rand = new Random();
        
        // 1. Création des intersections fixes
        List<Intersection> intersections = new ArrayList<>();
        for (int i = 0; i < INTERSECTION_X.length; i++) {
            intersections.add(new IntersectionImpl(i+1, new Coordonnee(INTERSECTION_X[i], INTERSECTION_Y[i])));
            carte.ajouterIntersection(intersections.get(i));
        }

        // 2. Définition des connexions fixes
        int[][] connections = {
            {0,1}, {1,2}, {2,3}, {3,4}, {4,5}, {5,6}, {6,7}, {7,0},
            {0,8}, {8,9}, {9,10}, {10,5}, {1,9}, {2,10}, {3,11}, {11,6}
        };

        String[] noms = {
            "Boulevard Principal", "Avenue Centrale", "Rue de la Gare", "Chemin Vert",
            "Boulevard Nord", "Rue Rivoli", "Avenue Sud", "Rue Courte",
            "Passage Secret", "Boulevard Est", "Rue Longue", "Avenue Ouest",
            "Chemin Diagonal", "Rue Serpentine", "Boulevard Courbe", "Route Forestière"
        };

        // 3. Création des tronçons avec longueurs aléatoires
        for (int i = 0; i < connections.length; i++) {
            int from = connections[i][0];
            int to = connections[i][1];
            
            Troncon t = new TronconImpl(
                noms[i],
                40 + rand.nextInt(31), // Vitesse 40-70 km/h
                intersections.get(from),
                intersections.get(to),
                80 + rand.nextInt(171) // Longueur 80-250m
            );
            carte.ajouterTroncon(t);
        }
        
        return carte;
    }
}

// Interface Route
interface Route {
    double getLongueur();
    String getNom();
    String getEtat();
    void setEtat(String etat);
    boolean estAccessible();
}

// Classe abstraite RouteAbstraite
abstract class RouteAbstraite implements Route {
    protected double longueur;
    protected String nom;
    protected String etat = "fluide";
    
    public RouteAbstraite(String nom, double longueur) {
        this.nom = nom;
        this.longueur = longueur;
    }
    
    @Override public double getLongueur() { return longueur; }
    @Override public String getNom() { return nom; }
    @Override public String getEtat() { return etat; }
    @Override public void setEtat(String etat) { this.etat = etat; }
    
    @Override 
    public boolean estAccessible() {
        return !("fermé".equals(etat) || "accident".equals(etat));
    }
}

// Interface Troncon
interface Troncon extends Route {
    int getVitesseMax();
    Intersection getIntersectionDepart();
    Intersection getIntersectionArrivee();
    double calculerTempsDeParcours();
    boolean estAffecteParEvenement();
}

// Implémentation Troncon
class TronconImpl extends RouteAbstraite implements Troncon {
    private int vitesseMax;
    private Intersection intersectionDepart;
    private Intersection intersectionArrivee;
    
    public TronconImpl(String nom, int vitesseMax, Intersection depart, Intersection arrivee, double longueur) {
        super(nom, longueur);
        this.vitesseMax = vitesseMax;
        this.intersectionDepart = depart;
        this.intersectionArrivee = arrivee;
    }
    
    @Override public int getVitesseMax() { return vitesseMax; }
    @Override public Intersection getIntersectionDepart() { return intersectionDepart; }
    @Override public Intersection getIntersectionArrivee() { return intersectionArrivee; }
    
    @Override
    public double calculerTempsDeParcours() {
        if (!estAccessible()) return Double.POSITIVE_INFINITY;
        double vitesseEffective = vitesseMax;
        if ("congestion".equals(etat)) vitesseEffective *= 0.5;
        return (longueur / vitesseEffective) * 3600;
    }
    
    @Override 
    public boolean estAffecteParEvenement() {
        return !"fluide".equals(etat);
    }
    
    @Override
    public String toString() {
        return String.format("%s (%dm, %d km/h, %s)", nom, (int)longueur, vitesseMax, etat);
    }
}

// Interface Intersection
interface Intersection {
    int getId();
    Coordonnee getPosition();
    List<Troncon> getTronconsAdjacents();
    void ajouterTroncon(Troncon t);
    List<Troncon> getTronconsDisponibles();
    Troncon choisirDirection(Vehicule v);
}

// Implémentation Intersection
class IntersectionImpl implements Intersection {
    private int id;
    private Coordonnee position;
    private List<Troncon> tronconsAdjacents = new ArrayList<>();
    
    public IntersectionImpl(int id, Coordonnee position) {
        this.id = id;
        this.position = position;
    }
    
    @Override public int getId() { return id; }
    @Override public Coordonnee getPosition() { return position; }
    @Override public List<Troncon> getTronconsAdjacents() { return new ArrayList<>(tronconsAdjacents); }
    
    @Override
    public void ajouterTroncon(Troncon t) {
        if (!tronconsAdjacents.contains(t)) {
            tronconsAdjacents.add(t);
        }
    }
    
    @Override
    public List<Troncon> getTronconsDisponibles() {
        List<Troncon> disponibles = new ArrayList<>();
        for (Troncon t : tronconsAdjacents) {
            if (t.estAccessible()) {
                disponibles.add(t);
            }
        }
        return disponibles;
    }
    
    @Override
    public Troncon choisirDirection(Vehicule v) {
        if (v.getGPS() == null || v.getGPS().getItineraire().isEmpty()) {
            return null;
        }
        
        List<Troncon> itineraire = v.getGPS().getItineraire();
        for (Troncon t : tronconsAdjacents) {
            if (itineraire.contains(t)) {
                return t;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("Intersection %d (%s)", id, position);
    }
}

// Classe Coordonnee
class Coordonnee {
    private int x, y;
    
    public Coordonnee(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    
    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }
}

// Interface CarteVille
interface CarteVille {
    List<Intersection> getIntersections();
    List<Troncon> getTroncons();
    void ajouterIntersection(Intersection i);
    void ajouterTroncon(Troncon t);
    List<Troncon> trouverItineraire(Intersection depart, Intersection arrivee);
}

// Implémentation CarteVille
class CarteVilleImpl implements CarteVille {
    private List<Intersection> intersections = new ArrayList<>();
    private List<Troncon> troncons = new ArrayList<>();
    
    @Override public List<Intersection> getIntersections() { return new ArrayList<>(intersections); }
    @Override public List<Troncon> getTroncons() { return new ArrayList<>(troncons); }
    
    @Override
    public void ajouterIntersection(Intersection i) {
        if (!intersections.contains(i)) {
            intersections.add(i);
        }
    }
    
    @Override
    public void ajouterTroncon(Troncon t) {
        if (!troncons.contains(t)) {
            troncons.add(t);
            t.getIntersectionDepart().ajouterTroncon(t);
        }
    }
    
    @Override
    public List<Troncon> trouverItineraire(Intersection depart, Intersection arrivee) {
        // Implémentation de Dijkstra optimisée
        Map<Intersection, Double> distances = new HashMap<>();
        Map<Intersection, Intersection> predecesseurs = new HashMap<>();
        PriorityQueue<Intersection> queue = new PriorityQueue<>(
            Comparator.comparingDouble(distances::get)
        );
        
        // Initialisation
        for (Intersection i : intersections) {
            distances.put(i, Double.POSITIVE_INFINITY);
        }
        distances.put(depart, 0.0);
        queue.add(depart);
        
        while (!queue.isEmpty()) {
            Intersection current = queue.poll();
            
            if (current.equals(arrivee)) break;
            
            for (Troncon t : current.getTronconsDisponibles()) {
                Intersection neighbor = t.getIntersectionArrivee();
                double newDist = distances.get(current) + t.getLongueur();
                
                if (newDist < distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, newDist);
                    predecesseurs.put(neighbor, current);
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        // Reconstruction du chemin
        List<Troncon> chemin = new ArrayList<>();
        Intersection current = arrivee;
        
        while (predecesseurs.containsKey(current)) {
            Intersection pred = predecesseurs.get(current);
            
            for (Troncon t : pred.getTronconsAdjacents()) {
                if (t.getIntersectionArrivee().equals(current) && t.estAccessible()) {
                    chemin.add(0, t);
                    break;
                }
            }
            current = pred;
        }
        
        return chemin.isEmpty() || !current.equals(depart) ? Collections.emptyList() : chemin;
    }
}

// Classe EvenementRoutier
class EvenementRoutier {
    private String type;
    private Troncon troncon;
    private int gravite;
    
    public EvenementRoutier(String type, Troncon troncon, int gravite) {
        this.type = type;
        this.troncon = troncon;
        this.gravite = gravite;
    }
    
    public String getType() { return type; }
    public Troncon getTroncon() { return troncon; }
    public int getGravite() { return gravite; }
    
    public void affecterTroncon() {
        troncon.setEtat(type);
    }
    
    public void mettreAJourEtat() {
        if (gravite > 0) {
            gravite--;
            if (gravite == 0) {
                troncon.setEtat("fluide");
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s sur %s (gravité: %d)", type, troncon.getNom(), gravite);
    }
}

// Classe GPS
class GPS {
    private List<Troncon> itineraire = new ArrayList<>();
    private Intersection positionActuelle;
    private Intersection destination;
    
    public List<Troncon> getItineraire() { return new ArrayList<>(itineraire); }
    public Intersection getPositionActuelle() { return positionActuelle; }
    public Intersection getDestination() { return destination; }
    
    public void calculerItineraire(CarteVille carte, Intersection depart, Intersection arrivee) {
        this.positionActuelle = depart;
        this.destination = arrivee;
        this.itineraire = carte.trouverItineraire(depart, arrivee);
    }
    
    public void mettreAJourItineraireEnCasEvenement(CarteVille carte) {
        if (positionActuelle != null && destination != null) {
            calculerItineraire(carte, positionActuelle, destination);
        }
    }
    
    public String donnerInstructionSuivante() {
        if (itineraire.isEmpty()) {
            return "Vous êtes arrivé à destination";
        }
        
        Troncon prochain = itineraire.get(0);
        itineraire.remove(0);
        return String.format("Prenez %s (%dm) vers Intersection %d", 
               prochain.getNom(), (int)prochain.getLongueur(), 
               prochain.getIntersectionArrivee().getId());
    }
}

// Classe Vehicule
class Vehicule {
    private Intersection positionActuelle;
    private Intersection destination;
    private GPS gps = new GPS();
    
    public Vehicule(Intersection positionInitiale) {
        this.positionActuelle = positionInitiale;
    }
    
    public void setGPS(GPS gps) {
        this.gps = gps;
    }

    public Intersection getPositionActuelle() { return positionActuelle; }
    public Intersection getDestination() { return destination; }
    public GPS getGPS() { return gps; }
    
    public void setDestination(Intersection destination, CarteVille carte) {
        this.destination = destination;
        gps.calculerItineraire(carte, positionActuelle, destination);
    }
    
    public void seDeplacer() {
        if (!gps.getItineraire().isEmpty()) {
            positionActuelle = gps.getItineraire().get(0).getIntersectionArrivee();
        }
    }
}

// Classe InterfaceGraphique
class InterfaceGraphique {
    private CarteVille carte;
    private Vehicule vehicule;
    private JFrame frame;
    private JPanel cartePanel;
    private JTextArea instructionsArea;
    private List<EvenementRoutier> evenements = new ArrayList<>();
    private JComboBox<Intersection> destinationComboBox;
    private JLabel distanceLabel;
    
    public InterfaceGraphique(CarteVille carte, Vehicule vehicule) {
        this.carte = carte;
        this.vehicule = vehicule;
    }
    
    public void afficher() {
        frame = new JFrame("Système de Guidage Routier Complet");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLayout(new BorderLayout());
        
        // Panel de la carte
        cartePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                afficherCarte(g);
            }
        };
        cartePanel.setPreferredSize(new Dimension(700, 700));
        frame.add(cartePanel, BorderLayout.CENTER);
        
        // Panel de contrôle
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setPreferredSize(new Dimension(350, 700));
        
        // Panel de destination
        JPanel destinationPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        destinationComboBox = new JComboBox<>();
        for (Intersection i : carte.getIntersections()) {
            destinationComboBox.addItem(i);
        }
        
        JButton setDestButton = new JButton("Définir destination");
        setDestButton.addActionListener(e -> definirDestination());
        
        destinationPanel.add(new JLabel("Destination:"), gbc);
        gbc.gridx = 1;
        destinationPanel.add(destinationComboBox, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        destinationPanel.add(setDestButton, gbc);
        
        // Panel d'informations
        JPanel infoPanel = new JPanel(new BorderLayout());
        distanceLabel = new JLabel("Distance totale: 0 m");
        distanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(distanceLabel, BorderLayout.NORTH);
        infoPanel.add(destinationPanel, BorderLayout.CENTER);
        
        // Panel des instructions
        JPanel logPanel = new JPanel(new BorderLayout());
        instructionsArea = new JTextArea(15, 30);
        instructionsArea.setEditable(false);
        instructionsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(instructionsArea);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        
        JButton accidentBtn = new JButton("Simuler accident");
        accidentBtn.addActionListener(e -> simulerAccident());
        
        JButton recalcBtn = new JButton("Recalculer");
        recalcBtn.addActionListener(e -> recalculerItineraire());
        
        JButton moveBtn = new JButton("Déplacer");
        moveBtn.addActionListener(e -> deplacerVehicule());
        
        buttonPanel.add(accidentBtn);
        buttonPanel.add(recalcBtn);
        buttonPanel.add(moveBtn);
        
        // Assemblage
        controlPanel.add(infoPanel, BorderLayout.NORTH);
        controlPanel.add(logPanel, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.add(controlPanel, BorderLayout.EAST);
        mettreAJourAffichage();
        frame.setVisible(true);
    }
    
    private void definirDestination() {
        Intersection destination = (Intersection) destinationComboBox.getSelectedItem();
        vehicule.setDestination(destination, carte);
        instructionsArea.append(String.format(">>> Destination définie: Intersection %d\n", destination.getId()));
        mettreAJourDistance();
        mettreAJourAffichage();
    }
    
    private void mettreAJourDistance() {
        if (vehicule.getGPS().getItineraire().isEmpty()) {
            distanceLabel.setText("Distance totale: 0 m");
            return;
        }
        
        double distance = 0;
        for (Troncon t : vehicule.getGPS().getItineraire()) {
            distance += t.getLongueur();
        }
        distanceLabel.setText(String.format("Distance totale: %.0f m", distance));
    }
    
    private void afficherCarte(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Afficher les tronçons
        for (Troncon t : carte.getTroncons()) {
            switch (t.getEtat()) {
                case "fluide": g2d.setColor(Color.GREEN); break;
                case "congestion": g2d.setColor(Color.ORANGE); break;
                case "accident": g2d.setColor(Color.RED); break;
                case "fermé": g2d.setColor(Color.GRAY); break;
                default: g2d.setColor(Color.BLACK);
            }
            
            Coordonnee dep = t.getIntersectionDepart().getPosition();
            Coordonnee arr = t.getIntersectionArrivee().getPosition();
            g2d.drawLine(dep.getX(), dep.getY(), arr.getX(), arr.getY());
            
            // Afficher le nom et la longueur
            g2d.setColor(Color.BLACK);
            String info = String.format("%s (%dm)", t.getNom(), (int)t.getLongueur());
            g2d.drawString(info, (dep.getX()+arr.getX())/2, (dep.getY()+arr.getY())/2);
        }
        
        // Afficher l'itinéraire
        if (!vehicule.getGPS().getItineraire().isEmpty()) {
            g2d.setColor(new Color(0, 0, 255, 150));
            g2d.setStroke(new BasicStroke(3));
            for (Troncon t : vehicule.getGPS().getItineraire()) {
                Coordonnee dep = t.getIntersectionDepart().getPosition();
                Coordonnee arr = t.getIntersectionArrivee().getPosition();
                g2d.drawLine(dep.getX(), dep.getY(), arr.getX(), arr.getY());
            }
        }
        
        // Afficher les intersections
        g2d.setColor(Color.BLUE);
        for (Intersection i : carte.getIntersections()) {
            Coordonnee pos = i.getPosition();
            g2d.fillOval(pos.getX()-5, pos.getY()-5, 10, 10);
            g2d.drawString(String.valueOf(i.getId()), pos.getX()+10, pos.getY()+5);
        }
        
        // Afficher la destination
        if (vehicule.getDestination() != null) {
            Coordonnee pos = vehicule.getDestination().getPosition();
            g2d.setColor(Color.MAGENTA);
            g2d.fillOval(pos.getX()-8, pos.getY()-8, 16, 16);
            g2d.drawString("DEST", pos.getX()+15, pos.getY()+5);
        }
        
        // Afficher le véhicule
        if (vehicule.getPositionActuelle() != null) {
            Coordonnee pos = vehicule.getPositionActuelle().getPosition();
            g2d.setColor(Color.RED);
            g2d.fillOval(pos.getX()-8, pos.getY()-8, 16, 16);
        }
    }
    
    private void simulerAccident() {
        if (carte.getTroncons().isEmpty()) return;
        
        // Filtrer les tronçons sans accident
        List<Troncon> tronconsDisponibles = new ArrayList<>();
        for (Troncon t : carte.getTroncons()) {
            if (!t.getEtat().equals("accident")) {
                tronconsDisponibles.add(t);
            }
        }
        
        if (tronconsDisponibles.isEmpty()) {
            instructionsArea.append(">>> Tous les tronçons ont déjà un accident!\n");
            return;
        }
        
        Troncon tronconAffecte = tronconsDisponibles.get(new Random().nextInt(tronconsDisponibles.size()));
        EvenementRoutier accident = new EvenementRoutier("accident", tronconAffecte, 3 + new Random().nextInt(3));
        accident.affecterTroncon();
        evenements.add(accident);
        
        instructionsArea.append(String.format(">>> Accident sur %s (durée: %d)\n", 
            tronconAffecte.getNom(), accident.getGravite()));
        
        // Recalcul immédiat si nécessaire
        if (vehicule.getGPS().getItineraire().contains(tronconAffecte)) {
            recalculerItineraire();
        }
        
        mettreAJourAffichage();
    }
    
    private void recalculerItineraire() {
        if (vehicule.getDestination() == null) {
            instructionsArea.append(">>> Aucune destination définie\n");
            return;
        }
        
        // Réappliquer tous les accidents avant recalcul
        for (EvenementRoutier e : evenements) {
            e.affecterTroncon();
        }
        
        vehicule.getGPS().calculerItineraire(carte, vehicule.getPositionActuelle(), vehicule.getDestination());
        mettreAJourDistance();
        instructionsArea.append(">>> Itinéraire recalculé en tenant compte des obstacles\n");
        mettreAJourAffichage();
    }
    
    private void deplacerVehicule() {
        if (vehicule.getGPS().getItineraire().isEmpty()) {
            instructionsArea.append(">>> Aucun itinéraire à suivre\n");
            return;
        }
        
        String instruction = vehicule.getGPS().donnerInstructionSuivante();
        vehicule.seDeplacer();
        instructionsArea.append(">>> " + instruction + "\n");
        
        if (vehicule.getPositionActuelle().equals(vehicule.getDestination())) {
            instructionsArea.append(">>> Vous êtes arrivé à destination!\n");
        }
        
        mettreAJourDistance();
        mettreAJourAffichage();
    }
    
    private void mettreAJourAffichage() {
        // Mettre à jour les événements
        Iterator<EvenementRoutier> it = evenements.iterator();
        while (it.hasNext()) {
            EvenementRoutier e = it.next();
            e.mettreAJourEtat();
            if (e.getGravite() == 0) {
                instructionsArea.append(String.format(">>> Accident résolu sur %s\n", e.getTroncon().getNom()));
                it.remove();
            }
        }
        
        cartePanel.repaint();
    }
}