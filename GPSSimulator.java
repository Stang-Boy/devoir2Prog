import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GPSSimulator extends JFrame {
    private CarteVille carte;
    private GPS gps;
    private MapPanel mapPanel;
    private JComboBox<String> startCombo, destCombo;
    private JButton calcButton, accidentButton, resetButton, logsButton;
    private JFrame logsFrame;
    private JTextArea logsArea;

    public GPSSimulator() {
        super("GPS Simulator");
        initModel();
        initUI();
        initLogsWindow();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
            double dist = a.distanceVers(b);
            carte.ajouterTroncon(new Troncon(a, b, dist, "R" + e[0] + "-" + e[1]));
        }
        for (Troncon t : carte.getTroncons()) t.setEtat(EtatTroncon.FLUIDE);
        gps = new GPS(carte);
    }

    private void initUI() {
        mapPanel = new MapPanel();
        mapPanel.setPreferredSize(new Dimension(800, 800));
        calcButton = new JButton("Calculer Itinéraire");
        accidentButton = new JButton("Ajouter Accident");
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
        resetButton.addActionListener(e -> {
            clearAccidents();
            clearTrafic();
            log("Réinitialisation complète");
        });
        logsButton.addActionListener(e -> logsFrame.setVisible(true));

        JPanel control = new JPanel();
        control.setLayout(new GridLayout(3, 2, 5, 5));
        control.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        control.add(new JLabel("Départ:"));
        control.add(startCombo);
        control.add(new JLabel("Destination:"));
        control.add(destCombo);
        control.add(calcButton);
        control.add(accidentButton);
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
            mapPanel.repaint();
        }
    }

    private void clearAccidents() {
        for (Troncon t : carte.getTroncons()) {
            if (t.getEtat() == EtatTroncon.ACCIDENT) t.setEtat(EtatTroncon.FLUIDE);
        }
        mapPanel.repaint();
    }

    private void clearTrafic() {
        for (Troncon t : carte.getTroncons()) {
            if (t.getEtat() == EtatTroncon.FAIBLE || t.getEtat() == EtatTroncon.MODERE || t.getEtat() == EtatTroncon.INTENSE) {
                t.setEtat(EtatTroncon.FLUIDE);
            }
        }
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
        gps.setDestination(carte.getIntersections().get(destCombo.getSelectedIndex()));
        gps.calculerItineraire(start);
        mapPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GPSSimulator().setVisible(true));
    }

    /**
     * Panel de dessin principal
     */
    
    private class MapPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            // dessiner tronçons
            for (Troncon t : carte.getTroncons()) {
                int x1 = t.origine.x, y1 = t.origine.y;
                int x2 = t.destination.x, y2 = t.destination.y;
                Color c;
                switch (t.getEtat()) {
                    case FAIBLE: c = Color.YELLOW; break;
                    case MODERE: c = Color.ORANGE; break;
                    case INTENSE: c = Color.RED; break;
                    case ACCIDENT: c = Color.MAGENTA; break;
                    default: c = Color.BLACK;
                }
                g2.setColor(c);
                g2.setStroke(new BasicStroke(4));
                g2.drawLine(x1, y1, x2, y2);
                String lbl = String.format("%.1f", t.distance);
                g2.setColor(Color.BLACK);
                g2.drawString(lbl, (x1 + x2)/2, (y1 + y2)/2 - 5);
            }
            // intersections
            g2.setColor(Color.BLACK);
            for (Intersection i : carte.getIntersections()) {
                g2.fillOval(i.x-5, i.y-5, 10, 10);
                g2.drawString(String.valueOf(i.id), i.x + 10, i.y - 10);
            }
            // itinéraire
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(8));
            for (Troncon t : gps.itineraire) {
                g2.drawLine(t.origine.x, t.origine.y, t.destination.x, t.destination.y);
            }
        }
    }
    

    // -------- Modèle --------

    static abstract class Route {
        protected Intersection origine, destination;
        protected double distance;
        public Route(Intersection o, Intersection d, double dist) { origine = o; destination = d; distance = dist; }
        public abstract double getTempsParcours();
        public abstract String getNomRue();
    }

    static class Troncon extends Route {
        private String nom;
        private EtatTroncon etat;
        public Troncon(Intersection o, Intersection d, double dist, String name) {
            super(o, d, dist);
            nom  = name;
            etat = EtatTroncon.FLUIDE;
        }
        public double getTempsParcours() { return distance * etat.getFacteur(); }
        public String getNomRue()            { return nom; }
        public EtatTroncon getEtat()         { return etat; }
        public void setEtat(EtatTroncon e)   { etat = e; }
    }

    static enum EtatTroncon {
        FLUIDE, FAIBLE, MODERE, INTENSE, ACCIDENT;
        public double getFacteur() {
            switch (this) {
                case FAIBLE:  return 1.2;
                case MODERE:  return 1.5;
                case INTENSE: return 2.0;
                case ACCIDENT: return Double.POSITIVE_INFINITY;
                default:      return 1.0;
            }
        }
    }

    static class Intersection {
        public int id, x, y;
        public Intersection(int id, int x, int y) { this.id = id; this.x = x; this.y = y; }
        public double distanceVers(Intersection o) { return Math.hypot(x - o.x, y - o.y); }
    }

    static class CarteVille {
        private List<Intersection> inters = new ArrayList<>();
        private List<Troncon>    trons  = new ArrayList<>();
        public void ajouterIntersection(Intersection i) { inters.add(i); }
        public void ajouterTroncon(Troncon t)           { trons.add(t); }
        public List<Intersection> getIntersections()    { return inters; }
        public List<Troncon>      getTroncons()         { return trons; }
    }

    class GPS {
        private CarteVille carte;
        private Intersection depart, dest;
        public List<Troncon> itineraire = new ArrayList<>();

        public GPS(CarteVille c) { carte = c; }
        public void setDestination(Intersection d) { dest = d; }
        public void calculerItineraire(Intersection start) {
            depart = start;
            Map<Intersection, Double> dist = new HashMap<>();
            Map<Intersection, Troncon> prev = new HashMap<>();
            Set<Intersection> visited = new HashSet<>();
            PriorityQueue<Intersection> pq = new PriorityQueue<>(Comparator.comparing(dist::get));
            for (Intersection i : carte.getIntersections()) dist.put(i, Double.POSITIVE_INFINITY);
            dist.put(start, 0.0);
            pq.add(start);
            while (!pq.isEmpty()) {
                Intersection u = pq.poll();
                if (!visited.add(u)) continue;
                if (u == dest) break;
                for (Troncon t : carte.getTroncons()) {
                    Intersection v = (t.origine == u ? t.destination : t.destination == u ? t.origine : null);
                    if (v != null && !visited.contains(v)) {
                        double alt = dist.get(u) + t.getTempsParcours();
                        if (alt < dist.get(v)) {
                            dist.put(v, alt);
                            prev.put(v, t);
                            pq.add(v);
                        }
                    }
                }
            }
            itineraire.clear();
            Intersection step = dest;
            while (prev.containsKey(step)) {
                Troncon e = prev.get(step);
                itineraire.add(0, e);
                step = (e.destination == step ? e.origine : e.destination);
            }
        }
        public boolean avancer(Vehicule v) {
            if (itineraire.isEmpty()) return false;
            Troncon curr = v.currentEdge;
            if (curr == null || v.progress >= 1.0) {
                curr = itineraire.remove(0);
                v.currentEdge = curr;
                v.progress = 0.0;
            }
            double speed = 2.0 / curr.getEtat().getFacteur();
            v.progress += speed / curr.distance;
            if (v.progress > 1.0) v.progress = 1.0;
            int x1 = curr.origine.x, y1 = curr.origine.y;
            int x2 = curr.destination.x, y2 = curr.destination.y;
            v.x = (int)(x1 + (x2 - x1) * v.progress);
            v.y = (int)(y1 + (y2 - y1) * v.progress);
            if (v.progress >= 1.0 && itineraire.isEmpty()) {
                v.current = curr.destination;
                return false;
            }
            return true;
        }
    }

    static class Vehicule {
        public int x, y;
        public Intersection current;
        public Troncon      currentEdge;
        public double       progress;
        public Vehicule(int x, int y, Intersection start) {
            this.x = x; this.y = y; this.current = start;
        }
    }
}