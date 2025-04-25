import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

public class GPSSimulator extends JFrame {
    // Modèle
    private CarteVille carte;
    private GPS gps;
    private List<Troncon> currentRoute = new ArrayList<>();
    private Intersection currentPositionNode;

    // UI
    private JPanel mapPanel;
    private JComboBox<String> startCombo, destCombo;
    private JButton calcRouteBtn, moveBtn, addTrafficBtn, addAccidentBtn, removeObsBtn, resetBtn;
    private JFrame logFrame;
    private JTextArea logArea;

    public GPSSimulator() {
        super("GPS Simulator");
        initModel();
        initUI();
        initLogWindow();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initModel() {
        carte = new CarteVille();
        int[][] coords = {
                {100, 400}, {250, 200}, {250, 400}, {250, 600},
                {400, 200}, {400, 400}, {400, 600},
                {550, 200}, {550, 400}, {550, 600},
                {700, 400}
        };
        for (int i = 0; i < coords.length; i++) {
            carte.ajouterIntersection(new Intersection(i, coords[i][0], coords[i][1]));
        }
        int[][] edges = {
                {1, 2}, {2, 3}, {4, 5}, {5, 6}, {7, 8}, {8, 9},
                {2, 5}, {5, 8}, {1, 4}, {4, 7}, {3, 6}, {6, 9},
                {0, 1}, {0, 3}, {10, 9}, {10, 7}
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
        currentPositionNode = carte.getIntersections().get(0);
    }

    private void initUI() {
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Rectangle bounds = calculateMapBounds();
                int w = getWidth(), h = getHeight();
                double scale = Math.min((double) w / bounds.width, (double) h / bounds.height);
                int offX = (int) ((w - bounds.width * scale) / 2);
                int offY = (int) ((h - bounds.height * scale) / 2);
                g2.translate(offX, offY);
                g2.scale(scale, scale);
                g2.translate(-bounds.x, -bounds.y);
                for (Troncon t : carte.getTroncons()) {
                    g2.setColor(t.getEtat().getColor());
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(t.getA().x, t.getA().y, t.getB().x, t.getB().y);
                }
                if (!currentRoute.isEmpty()) {
                    g2.setColor(Color.BLUE);
                    g2.setStroke(new BasicStroke(3));
                    for (Troncon t : currentRoute) {
                        g2.drawLine(t.getA().x, t.getA().y, t.getB().x, t.getB().y);
                    }
                }
                for (Intersection i : carte.getIntersections()) {
                    g2.setColor(Color.BLACK);
                    g2.fillOval(i.x - 5, i.y - 5, 10, 10);
                }
                if (currentPositionNode != null) {
                    g2.setColor(Color.RED);
                    g2.fillOval(currentPositionNode.x - 6, currentPositionNode.y - 6, 12, 12);
                }
            }
        };
        mapPanel.setPreferredSize(new Dimension(700, 500));

        startCombo = new JComboBox<>();
        destCombo = new JComboBox<>();
        for (Intersection i : carte.getIntersections()) {
            String label = String.format("Sommet %d", i.id);
            startCombo.addItem(label);
            destCombo.addItem(label);
        }
        startCombo.setSelectedIndex(0);
        destCombo.setSelectedIndex(10);

        calcRouteBtn = new JButton("Calculer Itinéraire");
        moveBtn = new JButton("Démarrer Mouvement");
        addTrafficBtn = new JButton("Ajouter Trafic");
        addAccidentBtn = new JButton("Ajouter Accident");
        removeObsBtn = new JButton("Supprimer Trafic/Accident");
        resetBtn = new JButton("Réinitialiser");

        calcRouteBtn.addActionListener(e -> recalcItineraire());
        moveBtn.addActionListener(e -> startVehicleMovement());
        addAccidentBtn.addActionListener(e -> {
            addObstacle(EtatTroncon.ACCIDENT);
            log("Accident ajouté");
        });
        addTrafficBtn.addActionListener(e -> {
            addTraffic();
            log("Trafic ajouté");
        });
        removeObsBtn.addActionListener(e -> openRemoveObstacleDialog());
        resetBtn.addActionListener(e -> {
            for (Troncon t : carte.getTroncons()) t.setEtat(EtatTroncon.FLUIDE);
            currentRoute.clear();
            currentPositionNode = carte.getIntersections().get(0);
            logArea.setText("");
            mapPanel.repaint();
            log("Réinitialisation complète");
        });

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(new JLabel("Départ :"));
        inputPanel.add(startCombo);
        inputPanel.add(Box.createVerticalStrut(5));
        inputPanel.add(new JLabel("Destination :"));
        inputPanel.add(destCombo);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        controlsPanel.add(calcRouteBtn);
        controlsPanel.add(moveBtn);
        controlsPanel.add(addAccidentBtn);
        controlsPanel.add(addTrafficBtn);
        controlsPanel.add(removeObsBtn);
        controlsPanel.add(resetBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(inputPanel, BorderLayout.NORTH);
        getContentPane().add(mapPanel, BorderLayout.CENTER);
        getContentPane().add(controlsPanel, BorderLayout.SOUTH);
    }

    private void initLogWindow() {
        logFrame = new JFrame("Itinéraire & Logs");
        logArea = new JTextArea(20, 40);
        logArea.setEditable(false);
        logFrame.add(new JScrollPane(logArea));
        logFrame.pack();
        logFrame.setLocationRelativeTo(this);
        logFrame.setVisible(true);
    }

    private void log(String message) {
        logArea.append(message + "\n");
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
        int margin = 50;
        return new Rectangle(minX - margin, minY - margin,
                maxX - minX + 2 * margin, maxY - minY + 2 * margin);
    }

    private void recalcItineraire() {
        logArea.setText("");
        int startIdx = startCombo.getSelectedIndex();
        int destIdx = destCombo.getSelectedIndex();
        Intersection start = carte.getIntersections().get(startIdx);
        Intersection dest = carte.getIntersections().get(destIdx);
        gps.setDestination(dest);
        List<Intersection> path = gps.calculerItineraire(start);
        currentRoute.clear();
        if (path != null && path.size() > 1) {
            log("Itinéraire calculé :");
            for (int i = 0; i < path.size() - 1; i++) {
                Intersection a = path.get(i), b = path.get(i + 1);
                Troncon t = carte.findTroncon(a, b);
                if (t != null) {
                    currentRoute.add(t);
                    log(String.format(" -> %s", t.getNomRue()));
                }
            }
            currentPositionNode = start;
            mapPanel.repaint();
        } else {
            log("Aucun itinéraire possible.");
        }
    }

    private void startVehicleMovement() {
        if (currentRoute.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Calculez d'abord un itinéraire.");
            return;
        }
        calcRouteBtn.setEnabled(false);
        new Thread(() -> {
            for (Troncon t : currentRoute) {
                currentPositionNode = t.getB();
                SwingUtilities.invokeLater(() -> mapPanel.repaint());
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }
            }
            SwingUtilities.invokeLater(() -> {
                log("Arrivé à destination !");
                calcRouteBtn.setEnabled(true);
                // Ne pas mettre à jour startCombo ici
            });
        }).start();
    }

    private void addObstacle(EtatTroncon etat) {
        Troncon t = chooseTroncon("Choisissez un tronçon :");
        if (t != null) {
            t.setEtat(etat);
            mapPanel.repaint();
        }
    }

    private void addTraffic() {
        Troncon t = chooseTroncon("Choisissez un tronçon (trafic) :");
        if (t != null && t.getEtat() != EtatTroncon.ACCIDENT) {
            String[] levels = {"Faible", "Modéré", "Intense"};
            String sel = (String) JOptionPane.showInputDialog(
                    this, "Niveau de trafic :", "Trafic",
                    JOptionPane.PLAIN_MESSAGE, null, levels, levels[0]
            );
            if (sel != null) {
                switch (sel) {
                    case "Faible":
                        t.setEtat(EtatTroncon.FAIBLE);
                        break;
                    case "Modéré":
                        t.setEtat(EtatTroncon.MODERE);
                        break;
                    case "Intense":
                        t.setEtat(EtatTroncon.INTENSE);
                        break;
                }
                mapPanel.repaint();
            }
        }
    }

    private void openRemoveObstacleDialog() {
        JDialog dlg = new JDialog(this, "Supprimer obstacle", true);
        dlg.setLayout(new BorderLayout());
        String[] names = carte.getTroncons().stream()
                .map(Troncon::getNomRue).toArray(String[]::new);
        JList<String> list = new JList<>(names);
        dlg.add(new JScrollPane(list), BorderLayout.CENTER);
        JRadioButton optT = new JRadioButton("Retirer Trafic", true);
        JRadioButton optA = new JRadioButton("Retirer Accident");
        ButtonGroup grp = new ButtonGroup();
        grp.add(optT);
        grp.add(optA);
        JButton apply = new JButton("Appliquer");
        apply.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                Troncon t = carte.getTroncons().get(idx);
                t.setEtat(EtatTroncon.FLUIDE);
                mapPanel.repaint();
                log("Obstacle retiré sur " + t.getNomRue());
            }
            dlg.dispose();
        });
        JPanel bottom = new JPanel(new FlowLayout());
        bottom.add(optT);
        bottom.add(optA);
        bottom.add(apply);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private Troncon chooseTroncon(String msg) {
        List<Troncon> list = carte.getTroncons();
        String[] names = list.stream().map(Troncon::getNomRue).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(
                this, msg, "Choix Tronçon",
                JOptionPane.PLAIN_MESSAGE, null, names, names[0]
        );
        if (sel == null) return null;
        for (Troncon t : list) if (t.getNomRue().equals(sel)) return t;
        return null;
    }

    static class Intersection {
        public final int id, x, y;

        public Intersection(int id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        public double distanceVers(Intersection o) {
            return Math.hypot(x - o.x, y - o.y);
        }
    }

    static class Troncon {
        private final Intersection a, b;
        private final String nomRue;
        private EtatTroncon etat;

        public Troncon(Intersection a, Intersection b, double d, String nom) {
            this.a = a;
            this.b = b;
            this.nomRue = nom;
            this.etat = EtatTroncon.FLUIDE;
        }

        public Intersection getA() {
            return a;
        }

        public Intersection getB() {
            return b;
        }

        public String getNomRue() {
            return nomRue;
        }

        public EtatTroncon getEtat() {
            return etat;
        }

        public void setEtat(EtatTroncon e) {
            etat = e;
        }
    }

    enum EtatTroncon {
        FLUIDE(1.0, Color.BLACK), FAIBLE(1.3, Color.GREEN), MODERE(1.7, Color.ORANGE), INTENSE(2.5, Color.RED), ACCIDENT(Double.POSITIVE_INFINITY, Color.MAGENTA);
        private final double weight;
        private final Color color;

        EtatTroncon(double w, Color c) {
            weight = w;
            color = c;
        }

        public double getWeight() {
            return weight;
        }

        public Color getColor() {
            return color;
        }
    }

    static class CarteVille {
        private final List<Intersection> intersections = new ArrayList<>();
        private final List<Troncon> troncons = new ArrayList<>();

        public void ajouterIntersection(Intersection i) {
            intersections.add(i);
        }

        public void ajouterTroncon(Troncon t) {
            troncons.add(t);
        }

        public List<Intersection> getIntersections() {
            return intersections;
        }

        public List<Troncon> getTroncons() {
            return troncons;
        }

        public Troncon findTroncon(Intersection a, Intersection b) {
            for (Troncon t : troncons) {
                if ((t.a == a && t.b == b) || (t.a == b && t.b == a)) return t;
            }
            return null;
        }
    }

    static class GPS {
        private final CarteVille carte;
        private Intersection destination;

        public GPS(CarteVille c) {
            carte = c;
        }

        public void setDestination(Intersection d) {
            destination = d;
        }

        public List<Intersection> calculerItineraire(Intersection depart) {
            Map<Intersection, Double> dist = new HashMap<>();
            Map<Intersection, Intersection> prev = new HashMap<>();
            Comparator<Intersection> cmp = Comparator.comparing(dist::get);
            PriorityQueue<Intersection> pq = new PriorityQueue<>(cmp);
            for (Intersection i : carte.getIntersections()) dist.put(i, Double.POSITIVE_INFINITY);
            dist.put(depart, 0.0);
            pq.add(depart);
            while (!pq.isEmpty()) {
                Intersection u = pq.poll();
                if (u == destination) break;
                for (Troncon t : carte.getTroncons()) {
                    Intersection v = null;
                    if (t.getA() == u) v = t.getB();
                    else if (t.getB() == u) v = t.getA();
                    if (v != null) {
                        double alt = dist.get(u) + t.getEtat().getWeight();
                        if (alt < dist.get(v)) {
                            dist.put(v, alt);
                            prev.put(v, u);
                            pq.remove(v);
                            pq.add(v);
                        }
                    }
                }
            }
            LinkedList<Intersection> path = new LinkedList<>();
            if (prev.containsKey(destination) || depart == destination) {
                Intersection step = destination;
                while (step != null) {
                    path.addFirst(step);
                    step = prev.get(step);
                }
            }
            return path.size() < 2 ? null : path;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GPSSimulator().setVisible(true));
    }
}
