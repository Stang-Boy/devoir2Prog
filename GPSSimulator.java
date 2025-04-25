import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

public class GPSSimulator extends JFrame {
    private CarteVille carte;
    private GPS gps;
    private JPanel mapPanel;
    private JComboBox<String> startCombo, destCombo;
    private JButton calcButton, accidentButton, trafficButton, resetButton, logsButton;
    private JFrame logsFrame;
    private JTextArea logsArea;
    private Vehicule vehicule;
    private javax.swing.Timer timer;

    public GPSSimulator() {
        super("GPS Simulator");
        setResizable(true);
        initModel();
        initUI();
        initLogsWindow();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private Rectangle calculateMapBounds() {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        
        for (Intersection i : carte.getIntersections()) {
            minX = Math.min(minX, i.x);
            minY = Math.min(minY, i.y);
            maxX = Math.max(maxX, i.x);
            maxY = Math.max(maxY, i.y);
        }
        
        // Ajouter une marge autour de la carte
        int margin = 50;
        return new Rectangle(minX - margin, minY - margin, 
                            maxX - minX + 2 * margin, 
                            maxY - minY + 2 * margin);
    }

    private void initModel() {
        carte = new CarteVille();
        int[][] coords = {
            {100,400},{250,200},{250,400},{250,600},
            {400,200},{400,400},{400,600},
            {550,200},{550,400},{550,600},
            {700,400}
        };
        for (int i = 0; i < coords.length; i++) {
            carte.ajouterIntersection(new Intersection(i, coords[i][0], coords[i][1]));
        }
        int[][] edges = {
            {1,2},{2,3},{4,5},{5,6},{7,8},{8,9},
            {2,5},{5,8},{1,4},{4,7},{3,6},{6,9},
            {0,1},{0,3},{10,9},{10,7}
        };
        for (int[] e : edges) {
            Intersection a = carte.getIntersections().get(e[0]);
            Intersection b = carte.getIntersections().get(e[1]);
            double dist = a.distanceVers(b) / 100.0;
            String name = String.format("r%d-%d", Math.min(e[0], e[1]), Math.max(e[0], e[1]));
            carte.ajouterTroncon(new Troncon(a, b, dist, name));
        }
        for (Troncon t : carte.getTroncons()) t.setEtat(EtatTroncon.FLUIDE);
        gps = new GPS(carte);
    }

    private void initUI() {
        mapPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                
                // Calculer les dimensions
            Rectangle bounds = calculateMapBounds();
            int panelWidth = getWidth();
            int panelHeight = getHeight();
        
                // Calculer le facteur d'échelle
            double scale = Math.min((double)panelWidth / bounds.width, 
                              (double)panelHeight / bounds.height);
        
                // Calculer le décalage pour centrer
            int offsetX = (int)((panelWidth - bounds.width * scale) / 2);
            int offsetY = (int)((panelHeight - bounds.height * scale) / 2);
        
                // Appliquer la transformation
            g2.translate(offsetX, offsetY);
            g2.scale(scale, scale);
            g2.translate(-bounds.x, -bounds.y);

                for (Troncon t : carte.getTroncons()) {
                    switch (t.getEtat()) {
                        case FLUIDE: g2.setColor(Color.BLACK); break;
                        case FAIBLE: g2.setColor(Color.GREEN); break;
                        case MODERE: g2.setColor(Color.ORANGE); break;
                        case INTENSE: g2.setColor(Color.RED); break;
                        case ACCIDENT: g2.setColor(Color.MAGENTA); break;
                    }
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(t.getA().x, t.getA().y, t.getB().x, t.getB().y);
                    int mx = (t.getA().x + t.getB().x) / 2;
                    int my = (t.getA().y + t.getB().y) / 2;
                    g2.setColor(Color.BLACK);
                    g2.drawString(t.getNomRue(), mx, my);
                }
                
                for (Intersection i : carte.getIntersections()) {
                    g2.setColor(Color.BLACK);
                    g2.fillOval(i.x - 5, i.y - 5, 10, 10);
                    g2.drawString(String.valueOf(i.id), i.x + 6, i.y - 6);
                }
                
                if (vehicule != null) {
                    g2.setColor(new Color(0, 0, 255, 200));
                    g2.setStroke(new BasicStroke(4));
                    for (int i = 0; i < vehicule.currentSegment; i++) {
                        Intersection a = vehicule.itineraire.get(i);
                        Intersection b = vehicule.itineraire.get(i+1);
                        g2.drawLine(a.x, a.y, b.x, b.y);
                    }
                    vehicule.draw(g2);
                }
            }
        };
        mapPanel.setPreferredSize(new Dimension(600, 600)); // Taille par défaut
        mapPanel.setMinimumSize(new Dimension(300, 300));   // Taille minimale

        calcButton = new JButton("Calculer Itinéraire");
        accidentButton = new JButton("Ajouter Accident");
        trafficButton = new JButton("Ajouter Trafic");
        resetButton = new JButton("Réinitialiser");
        logsButton = new JButton("Logs");
        startCombo = new JComboBox<>();
        destCombo = new JComboBox<>();
        for (Intersection i : carte.getIntersections()) {
            startCombo.addItem("Départ " + i.id);
            destCombo.addItem("Destination " + i.id);
        }
        startCombo.setSelectedIndex(0);
        destCombo.setSelectedIndex(10);

        calcButton.addActionListener(e -> {
            recalcItineraire();
            log("Itinéraire calculé");
        });
        accidentButton.addActionListener(e -> {
            addAccident();
            log("Accident ajouté");
        });
        trafficButton.addActionListener(e -> {
            addTraffic();
            log("Trafic ajouté");
        });
        resetButton.addActionListener(e -> {
            clearAccidents();
            clearTrafic();
            log("Réinitialisation complète");
        });
        logsButton.addActionListener(e -> logsFrame.setVisible(true));

        JPanel control = new JPanel();
        control.setLayout(new GridLayout(4, 2, 5, 5));
        control.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        control.add(new JLabel("Départ:"));
        control.add(startCombo);
        control.add(new JLabel("Destination:"));
        control.add(destCombo);
        control.add(calcButton);
        control.add(accidentButton);
        control.add(trafficButton);
        control.add(resetButton);
        control.add(logsButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mapPanel, BorderLayout.CENTER);
        getContentPane().add(control, BorderLayout.SOUTH);
    }

    private void initLogsWindow() {
        logsFrame = new JFrame("Logs");
        logsArea = new JTextArea(20, 40);
        logsArea.setEditable(false);
        logsFrame.add(new JScrollPane(logsArea));
        logsFrame.pack();
        logsFrame.setLocationRelativeTo(this);
        logsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void log(String message) {
        logsArea.append("[" + new Date() + "] " + message + "\n");
    }

    private void addAccident() {
        Troncon t = chooseTroncon("Choisissez un tronçon (accident) :");
        if (t != null) {
            t.setEtat(EtatTroncon.ACCIDENT);
            recalcItineraire();
            mapPanel.repaint();
        }
    }

    private void addTraffic() {
        Troncon t = chooseTroncon("Choisissez un tronçon (trafic) :");
        if (t != null && t.getEtat() != EtatTroncon.ACCIDENT) {
            String[] levels = {"Faible", "Modéré", "Intense"};
            String level = (String) JOptionPane.showInputDialog(
                this, "Niveau de trafic :", "Selection",
                JOptionPane.PLAIN_MESSAGE, null, levels, levels[0]
            );
            if (level != null) {
                switch (level) {
                    case "Faible": t.setEtat(EtatTroncon.FAIBLE); break;
                    case "Modéré": t.setEtat(EtatTroncon.MODERE); break;
                    case "Intense": t.setEtat(EtatTroncon.INTENSE); break;
                }
                recalcItineraire();
                mapPanel.repaint();
            }
        }
    }

    private void clearAccidents() {
        for (Troncon t : carte.getTroncons()) {
            if (t.getEtat() == EtatTroncon.ACCIDENT) t.setEtat(EtatTroncon.FLUIDE);
        }
        recalcItineraire();
        mapPanel.repaint();
    }

    private void clearTrafic() {
        for (Troncon t : carte.getTroncons()) {
            if (t.getEtat() == EtatTroncon.FAIBLE || t.getEtat() == EtatTroncon.MODERE || t.getEtat() == EtatTroncon.INTENSE) {
                t.setEtat(EtatTroncon.FLUIDE);
            }
        }
        recalcItineraire();
        mapPanel.repaint();
    }

    private Troncon chooseTroncon(String msg) {
        List<Troncon> list = carte.getTroncons();
        String[] names = list.stream().map(Troncon::getNomRue).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(
                this, msg, "Selection",
                JOptionPane.PLAIN_MESSAGE, null, names, names[0]
        );
        if (sel == null) return null;
        for (Troncon t : list) if (t.getNomRue().equals(sel)) return t;
        return null;
    }

    private void recalcItineraire() {
        Intersection start = carte.getIntersections().get(startCombo.getSelectedIndex());
        Intersection end = carte.getIntersections().get(destCombo.getSelectedIndex());
        gps.setDestination(end);
        List<Intersection> chemin = gps.calculerItineraire(start);
        
        if (chemin != null && chemin.size() > 1) {
            // Generate and log navigation instructions
            List<String> instructions = generateNavigationInstructions(chemin);
            log("Itinéraire calculé");
            for (String instruction : instructions) {
                log(instruction);
            }

            vehicule = new Vehicule(chemin);
            if (timer != null && timer.isRunning()) timer.stop();
            
            timer = new javax.swing.Timer(300, e -> {
                if (!vehicule.avancer()) {
                    timer.stop();
                    log("Arrivé à destination");
                }
                mapPanel.repaint();
            });
            timer.start();
        } else {
            log("Aucun itinéraire possible");
        }
        mapPanel.repaint();
    }

    private List<String> generateNavigationInstructions(List<Intersection> chemin) {
        List<String> instructions = new ArrayList<>();
        if (chemin.size() < 2) return instructions;

        // Start instruction
        instructions.add("Commencer à l'intersection " + chemin.get(0).id);

        // Generate instructions for each segment
        for (int i = 0; i < chemin.size() - 1; i++) {
            Intersection current = chemin.get(i);
            Intersection next = chemin.get(i + 1);

            // Add "continuer" instruction
            instructions.add("Continuer jusqu'à l'intersection " + next.id);

            // Add turn instruction if not at the destination
            if (i < chemin.size() - 2) {
                Intersection nextNext = chemin.get(i + 2);
                String direction = getTurnDirection(current, next, nextNext);
                if (direction.equals("gauche") || direction.equals("droite")) {
                    instructions.add(String.format("Tourner à %s à l'intersection %d", direction, next.id));
                }
            }
        }

        // Arrival instruction
        instructions.add("Arriver à l'intersection " + chemin.get(chemin.size() - 1).id);
        return instructions;
    }

    private String getTurnDirection(Intersection prev, Intersection current, Intersection next) {
        // Transform to car coordinate system (north up, negate y)
        double v1x = current.x - prev.x;
        double v1y = -(current.y - prev.y); // Negate y to make north up
        double v2x = next.x - current.x;
        double v2y = -(next.y - current.y); // Negate y to make north up

        // Calculate angle between vectors using atan2
        double angle1 = Math.toDegrees(Math.atan2(v1y, v1x));
        double angle2 = Math.toDegrees(Math.atan2(v2y, v2x));

        // Calculate relative angle
        double angle = angle2 - angle1;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;

        // Determine turn direction
        if (Math.abs(angle) < 30) { // Straight path
            return "tout droit";
        } else if (angle > 0) {
            return "droite"; // Positive angle means clockwise (right turn)
        } else {
            return "gauche"; // Negative angle means counterclockwise (left turn)
        }
    }

    private class Vehicule {
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
    }

    static class CarteVille {
        private List<Intersection> intersections = new ArrayList<>();
        private List<Troncon> troncons = new ArrayList<>();
        
        public void ajouterIntersection(Intersection i) { intersections.add(i); }
        public void ajouterTroncon(Troncon t) { troncons.add(t); }
        public List<Intersection> getIntersections() { return intersections; }
        public List<Troncon> getTroncons() { return troncons; }
    }

    static class Intersection {
        public int id, x, y;
        public Intersection(int id, int x, int y) { this.id = id; this.x = x; this.y = y; }
        public double distanceVers(Intersection o) { return Math.hypot(x - o.x, y - o.y); }
    }

    static class Troncon {
        private Intersection a, b;
        private double distance;
        private String nomRue;
        private EtatTroncon etat;
        
        public Troncon(Intersection a, Intersection b, double distance, String nomRue) {
            this.a = a;
            this.b = b;
            this.distance = distance;
            this.nomRue = nomRue;
            this.etat = EtatTroncon.FLUIDE;
        }
        
        public Intersection getA() { return a; }
        public Intersection getB() { return b; }
        public double getDistance() { return distance; }
        public String getNomRue() { return nomRue; }
        public EtatTroncon getEtat() { return etat; }
        public void setEtat(EtatTroncon etat) { this.etat = etat; }
    }

    enum EtatTroncon {
        FLUIDE(1.0), FAIBLE(1.3), MODERE(1.7), INTENSE(2.5), ACCIDENT(Double.POSITIVE_INFINITY);
        
        private final double weight;
        EtatTroncon(double weight) { this.weight = weight; }
        public double getWeight() { return weight; }
    }

    static class GPS {
        private CarteVille carte;
        private Intersection destination;
        
        public GPS(CarteVille carte) { this.carte = carte; }
        public void setDestination(Intersection d) { destination = d; }
        
        public List<Intersection> calculerItineraire(Intersection depart) {
            Map<Intersection, Double> dist = new HashMap<>();
            Map<Intersection, Intersection> prev = new HashMap<>();
            PriorityQueue<Intersection> pq = new PriorityQueue<>(Comparator.comparing(dist::get));
            
            for (Intersection i : carte.getIntersections()) {
                dist.put(i, Double.POSITIVE_INFINITY);
            }
            dist.put(depart, 0.0);
            pq.add(depart);
            
            while (!pq.isEmpty()) {
                Intersection u = pq.poll();
                if (u == destination) break;
                
                for (Troncon t : carte.getTroncons()) {
                    if (t.getA() == u || t.getB() == u) {
                        Intersection v = (t.getA() == u) ? t.getB() : t.getA();
                        double weight = t.getDistance() * t.getEtat().getWeight();
                        double alt = dist.get(u) + weight;
                        if (alt < dist.get(v)) {
                            dist.put(v, alt);
                            prev.put(v, u);
                            pq.remove(v);
                            pq.add(v);
                        }
                    }
                }
            }
            
            List<Intersection> chemin = new ArrayList<>();
            Intersection step = destination;
            if (!dist.get(step).isInfinite()) {
                while (prev.containsKey(step)) {
                    chemin.add(0, step);
                    step = prev.get(step);
                }
                chemin.add(0, depart);
            }
            
            return chemin.isEmpty() ? null : chemin;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GPSSimulator().setVisible(true));
    }
}