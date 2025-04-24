import java.util.*;
import java.awt.*;

import javax.swing.*;
import java.util.List;

public class SystemeGuidageRoutier {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CarteVille carte = creerCarteTest();
            Vehicule vehicule = new Vehicule(carte.getIntersections().get(0));
            vehicule.setGPS(new GPS());
            new InterfaceGraphique(carte, vehicule).afficher();
        });
    }

    private static CarteVille creerCarteTest() {
        CarteVille carte = new CarteVilleImpl();
        
        // Création des intersections
        Intersection i1 = new IntersectionImpl(1, new Coordonnee(100, 100));
        Intersection i2 = new IntersectionImpl(2, new Coordonnee(300, 100));
        Intersection i3 = new IntersectionImpl(3, new Coordonnee(300, 300));
        Intersection i4 = new IntersectionImpl(4, new Coordonnee(100, 300));
        Intersection i5 = new IntersectionImpl(5, new Coordonnee(200, 200));
        
        // Ajout des intersections à la carte
        carte.ajouterIntersection(i1);
        carte.ajouterIntersection(i2);
        carte.ajouterIntersection(i3);
        carte.ajouterIntersection(i4);
        carte.ajouterIntersection(i5);
        
        // Création des tronçons
        Troncon t1 = new TronconImpl("Rue A", 50, i1, i2);
        Troncon t2 = new TronconImpl("Rue B", 50, i2, i3);
        Troncon t3 = new TronconImpl("Rue C", 30, i3, i4);
        Troncon t4 = new TronconImpl("Rue D", 30, i4, i1);
        Troncon t5 = new TronconImpl("Avenue E", 70, i1, i5);
        Troncon t6 = new TronconImpl("Avenue F", 70, i5, i3);
        Troncon t7 = new TronconImpl("Boulevard G", 60, i2, i5);
        Troncon t8 = new TronconImpl("Boulevard H", 60, i5, i4);
        
        // Ajout des tronçons à la carte
        carte.ajouterTroncon(t1);
        carte.ajouterTroncon(t2);
        carte.ajouterTroncon(t3);
        carte.ajouterTroncon(t4);
        carte.ajouterTroncon(t5);
        carte.ajouterTroncon(t6);
        carte.ajouterTroncon(t7);
        carte.ajouterTroncon(t8);
        
        return carte;
    }
}

interface Route {
    double getLongueur();
    String getNom();
    String getEtat();
    void setEtat(String etat);
    boolean estAccessible();
}

abstract class RouteAbstraite implements Route {
    protected double longueur;
    protected String nom;
    protected String etat = "fluide";
    
    public RouteAbstraite(String nom, double longueur) {
        this.nom = nom;
        this.longueur = longueur;
    }
    
    @Override
    public double getLongueur() { return longueur; }
    
    @Override
    public String getNom() { return nom; }
    
    @Override
    public String getEtat() { return etat; }
    
    @Override
    public void setEtat(String etat) { this.etat = etat; }
    
    @Override
    public boolean estAccessible() {
        return !("fermé".equals(etat) || "accident".equals(etat));
    }
}

interface Troncon extends Route {
    int getVitesseMax();
    Intersection getIntersectionDepart();
    Intersection getIntersectionArrivee();
    double calculerTempsDeParcours();
    boolean estAffecteParEvenement();
}

class TronconImpl extends RouteAbstraite implements Troncon {
    private int vitesseMax;
    private Intersection intersectionDepart;
    private Intersection intersectionArrivee;
    
    public TronconImpl(String nom, int vitesseMax, Intersection depart, Intersection arrivee) {
        super(nom, calculerLongueur(depart.getPosition(), arrivee.getPosition()));
        this.vitesseMax = vitesseMax;
        this.intersectionDepart = depart;
        this.intersectionArrivee = arrivee;
        depart.ajouterTroncon(this);
    }
    
    private static double calculerLongueur(Coordonnee c1, Coordonnee c2) {
        return Math.sqrt(Math.pow(c2.getX() - c1.getX(), 2) + Math.pow(c2.getY() - c1.getY(), 2));
    }
    
    @Override
    public int getVitesseMax() { return vitesseMax; }
    
    @Override
    public Intersection getIntersectionDepart() { return intersectionDepart; }
    
    @Override
    public Intersection getIntersectionArrivee() { return intersectionArrivee; }
    
    @Override
    public double calculerTempsDeParcours() {
        if (!estAccessible()) return Double.POSITIVE_INFINITY;
        double vitesseEffective = vitesseMax;
        if ("congestion".equals(etat)) vitesseEffective *= 0.5;
        return (longueur / vitesseEffective) * 3600; // temps en secondes
    }
    
    @Override
    public boolean estAffecteParEvenement() {
        return !"fluide".equals(etat);
    }
    
    @Override
    public String toString() {
        return nom + " (" + longueur + " m, " + vitesseMax + " km/h, " + etat + ")";
    }
}

interface Intersection {
    int getId();
    Coordonnee getPosition();
    List<Troncon> getTronconsAdjacents();
    void ajouterTroncon(Troncon t);
    List<Troncon> getTronconsDisponibles();
    Troncon choisirDirection(Vehicule v);
}

class IntersectionImpl implements Intersection {
    private int id;
    private Coordonnee position;
    private List<Troncon> tronconsAdjacents = new ArrayList<>();
    
    public IntersectionImpl(int id, Coordonnee position) {
        this.id = id;
        this.position = position;
    }
    
    @Override
    public int getId() { return id; }
    
    @Override
    public Coordonnee getPosition() { return position; }
    
    @Override
    public List<Troncon> getTronconsAdjacents() { return new ArrayList<>(tronconsAdjacents); }
    
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
        return "Intersection " + id + " (" + position + ")";
    }
}

interface CarteVille {
    List<Intersection> getIntersections();
    List<Troncon> getTroncons();
    void ajouterIntersection(Intersection i);
    void ajouterTroncon(Troncon t);
    List<Troncon> trouverItineraire(Intersection depart, Intersection arrivee);
}

class CarteVilleImpl implements CarteVille {
    private List<Intersection> intersections = new ArrayList<>();
    private List<Troncon> troncons = new ArrayList<>();
    
    @Override
    public List<Intersection> getIntersections() { return new ArrayList<>(intersections); }
    
    @Override
    public List<Troncon> getTroncons() { return new ArrayList<>(troncons); }
    
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
        }
    }
    
    @Override
    public List<Troncon> trouverItineraire(Intersection depart, Intersection arrivee) {
        // Implémentation de l'algorithme de Dijkstra
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
                double alt = distances.get(current) + t.calculerTempsDeParcours();
                
                if (alt < distances.get(neighbor)) {
                    distances.put(neighbor, alt);
                    predecesseurs.put(neighbor, current);
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
        return "(" + x + ", " + y + ")";
    }
}

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
        return type + " sur " + troncon.getNom() + " (gravité: " + gravite + ")";
    }
}

class GPS {
    private List<Troncon> itineraire = new ArrayList<>();
    private Intersection positionActuelle;
    private Intersection destination;
    private int etapeActuelle = 0;
    
    public List<Troncon> getItineraire() { return new ArrayList<>(itineraire); }
    public Intersection getPositionActuelle() { return positionActuelle; }
    public Intersection getDestination() { return destination; }
    
    public void calculerItineraire(CarteVille carte, Intersection depart, Intersection arrivee) {
        this.positionActuelle = depart;
        this.destination = arrivee;
        this.itineraire = carte.trouverItineraire(depart, arrivee);
        this.etapeActuelle = 0;
    }
    
    public void mettreAJourItineraireEnCasEvenement(CarteVille carte) {
        if (positionActuelle != null && destination != null) {
            this.itineraire = carte.trouverItineraire(positionActuelle, destination);
            this.etapeActuelle = 0;
        }
    }
    
    public String donnerInstructionSuivante() {
        if (itineraire.isEmpty() || etapeActuelle >= itineraire.size()) {
            return "Vous êtes arrivé à destination";
        }
        
        Troncon prochainTroncon = itineraire.get(etapeActuelle);
        etapeActuelle++;
        return "Prenez " + prochainTroncon.getNom() + " vers " + 
               prochainTroncon.getIntersectionArrivee().getId();
    }
    
    public void avancerPosition() {
        if (!itineraire.isEmpty() && etapeActuelle > 0 && etapeActuelle <= itineraire.size()) {
            positionActuelle = itineraire.get(etapeActuelle-1).getIntersectionArrivee();
        }
    }
}

class Vehicule {
    private Intersection positionActuelle;
    private Intersection destination;
    private GPS gps;
    
    public Vehicule(Intersection positionInitiale) {
        this.positionActuelle = positionInitiale;
    }
    
    public Intersection getPositionActuelle() { return positionActuelle; }
    public Intersection getDestination() { return destination; }
    public GPS getGPS() { return gps; }
    
    public Vehicule setGPS(GPS gps) {
        this.gps = gps;
        return this;
    }
    
    public void setDestination(Intersection destination, CarteVille carte) {
        this.destination = destination;
        if (gps != null) {
            gps.calculerItineraire(carte, positionActuelle, destination);
        }
    }
    
    public void seDeplacer() {
        if (gps != null) {
            gps.avancerPosition();
            positionActuelle = gps.getPositionActuelle();
        }
    }
    
    public String obtenirInstruction() {
        return gps != null ? gps.donnerInstructionSuivante() : "Pas de GPS configuré";
    }
}

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
        frame = new JFrame("Système de Guidage Routier - Version Améliorée");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
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
        
        // Panel de contrôle à droite
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        
        // Panel des instructions
        instructionsArea = new JTextArea(20, 30);
        instructionsArea.setEditable(false);
        instructionsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        instructionsArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(instructionsArea);
        controlPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel des informations
        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        distanceLabel = new JLabel("Distance totale: 0 m");
        distanceLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        infoPanel.add(distanceLabel);
        
        // Sélection de destination
        JPanel destinationPanel = new JPanel();
        destinationPanel.add(new JLabel("Destination: "));
        destinationComboBox = new JComboBox<>();
        for (Intersection i : carte.getIntersections()) {
            destinationComboBox.addItem(i);
        }
        destinationPanel.add(destinationComboBox);
        
        JButton setDestinationButton = new JButton("Définir destination");
        setDestinationButton.addActionListener(e -> definirDestination());
        destinationPanel.add(setDestinationButton);
        infoPanel.add(destinationPanel);
        
        controlPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        
        JButton simulerAccidentButton = new JButton("Simuler accident");
        simulerAccidentButton.addActionListener(e -> simulerAccident());
        buttonPanel.add(simulerAccidentButton);
        
        JButton recalculerButton = new JButton("Recalculer itinéraire");
        recalculerButton.addActionListener(e -> recalculerItineraire());
        buttonPanel.add(recalculerButton);
        
        JButton deplacerButton = new JButton("Déplacer véhicule");
        deplacerButton.addActionListener(e -> deplacerVehicule());
        buttonPanel.add(deplacerButton);
        
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.add(controlPanel, BorderLayout.EAST);
        
        // Mettre à jour l'affichage initial
        mettreAJourAffichage();
        frame.setVisible(true);
    }
    
    private void definirDestination() {
        Intersection destination = (Intersection) destinationComboBox.getSelectedItem();
        vehicule.setDestination(destination, carte);
        instructionsArea.append(">>> Destination définie: Intersection " + destination.getId() + "\n");
        mettreAJourDistance();
        mettreAJourAffichage();
    }
    
    private void mettreAJourDistance() {
        if (vehicule.getGPS() != null && !vehicule.getGPS().getItineraire().isEmpty()) {
            double distance = 0;
            for (Troncon t : vehicule.getGPS().getItineraire()) {
                distance += t.getLongueur();
            }
            distanceLabel.setText(String.format("Distance totale: %.0f m", distance));
        } else {
            distanceLabel.setText("Distance totale: 0 m");
        }
    }
    
    private void afficherCarte(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Afficher les tronçons normaux
        g2d.setStroke(new BasicStroke(2));
        for (Troncon t : carte.getTroncons()) {
            Coordonnee depart = t.getIntersectionDepart().getPosition();
            Coordonnee arrivee = t.getIntersectionArrivee().getPosition();
            
            // Couleur selon l'état
            switch (t.getEtat()) {
                case "fluide": g2d.setColor(Color.GREEN); break;
                case "congestion": g2d.setColor(Color.ORANGE); break;
                case "accident": g2d.setColor(Color.RED); break;
                case "fermé": g2d.setColor(Color.GRAY); break;
                default: g2d.setColor(Color.BLACK);
            }
            
            g2d.drawLine(depart.getX(), depart.getY(), arrivee.getX(), arrivee.getY());
            
            // Afficher le nom
            g2d.setColor(Color.BLACK);
            g2d.drawString(t.getNom(), (depart.getX() + arrivee.getX()) / 2, (depart.getY() + arrivee.getY()) / 2);
        }
        
        // Afficher l'itinéraire en bleu épais
        if (vehicule.getGPS() != null && !vehicule.getGPS().getItineraire().isEmpty()) {
            g2d.setColor(new Color(0, 0, 255, 150));
            g2d.setStroke(new BasicStroke(4));
            for (Troncon t : vehicule.getGPS().getItineraire()) {
                Coordonnee depart = t.getIntersectionDepart().getPosition();
                Coordonnee arrivee = t.getIntersectionArrivee().getPosition();
                g2d.drawLine(depart.getX(), depart.getY(), arrivee.getX(), arrivee.getY());
            }
        }
        
        // Afficher les intersections
        g2d.setColor(Color.BLUE);
        for (Intersection i : carte.getIntersections()) {
            Coordonnee pos = i.getPosition();
            g2d.fillOval(pos.getX() - 5, pos.getY() - 5, 10, 10);
            g2d.drawString("" + i.getId(), pos.getX() + 10, pos.getY() + 5);
        }
        
        // Afficher la destination
        if (vehicule.getDestination() != null) {
            Coordonnee pos = vehicule.getDestination().getPosition();
            g2d.setColor(Color.MAGENTA);
            g2d.fillOval(pos.getX() - 8, pos.getY() - 8, 16, 16);
            g2d.drawString("DEST", pos.getX() + 15, pos.getY() + 5);
        }
        
        // Afficher le véhicule
        if (vehicule.getPositionActuelle() != null) {
            Coordonnee pos = vehicule.getPositionActuelle().getPosition();
            g2d.setColor(Color.RED);
            g2d.fillOval(pos.getX() - 8, pos.getY() - 8, 16, 16);
        }
    }
    
    private void simulerAccident() {
        if (carte.getTroncons().isEmpty()) return;
        
        Random rand = new Random();
        Troncon tronconAffecte = carte.getTroncons().get(rand.nextInt(carte.getTroncons().size()));
        EvenementRoutier accident = new EvenementRoutier("accident", tronconAffecte, 3);
        accident.affecterTroncon();
        evenements.add(accident);
        
        instructionsArea.append(">>> Nouvel accident: " + accident + "\n");
        
        // Recalculer l'itinéraire si nécessaire
        if (vehicule.getGPS() != null && vehicule.getGPS().getItineraire().contains(tronconAffecte)) {
            vehicule.getGPS().mettreAJourItineraireEnCasEvenement(carte);
            instructionsArea.append(">>> Itinéraire recalculé à cause de l'accident\n");
            mettreAJourDistance();
        }
        
        mettreAJourAffichage();
    }
    
    private void recalculerItineraire() {
        if (vehicule.getGPS() != null && vehicule.getDestination() != null) {
            vehicule.getGPS().calculerItineraire(carte, vehicule.getPositionActuelle(), vehicule.getDestination());
            instructionsArea.append(">>> Itinéraire recalculé\n");
            mettreAJourDistance();
            mettreAJourAffichage();
        } else {
            instructionsArea.append(">>> Veuillez d'abord définir une destination\n");
        }
    }
    
    private void deplacerVehicule() {
        if (vehicule.getGPS() == null || vehicule.getGPS().getItineraire().isEmpty()) {
            instructionsArea.append(">>> Aucun itinéraire calculé\n");
            return;
        }
        
        if (vehicule.getPositionActuelle().equals(vehicule.getDestination())) {
            instructionsArea.append(">>> Vous êtes déjà arrivé à destination!\n");
            return;
        }
        
        // Avancer d'un tronçon à la fois
        String instruction = vehicule.obtenirInstruction();
        vehicule.seDeplacer();
        instructionsArea.append(">>> " + instruction + "\n");
        
        if (vehicule.getPositionActuelle().equals(vehicule.getDestination())) {
            instructionsArea.append(">>> Vous êtes arrivé à destination!\n");
        }
        
        mettreAJourAffichage();
    }
    
    private void mettreAJourAffichage() {
        // Mettre à jour les événements
        Iterator<EvenementRoutier> it = evenements.iterator();
        while (it.hasNext()) {
            EvenementRoutier e = it.next();
            e.mettreAJourEtat();
            if (e.getGravite() == 0) {
                instructionsArea.append(">>> Événement résolu: " + e + "\n");
                it.remove();
            }
        }
        
        // Rafraîchir l'affichage
        cartePanel.repaint();
    }
}