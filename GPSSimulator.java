import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

public class GPSSimulator extends JFrame {
    // Modèle
    private CityMap cityMap;
    private GPS gps;
    private List<RoadSegment> currentRoute;
    private Intersection currentPosition;

    // UI Components
    private JPanel mapPanel;
    private JComboBox<String> startCombo, destCombo;
    private JButton calcRouteBtn, moveBtn, addTrafficBtn, addAccidentBtn, removeObsBtn, resetBtn;
    private JTextArea logArea;
    private JFrame logFrame;

    /**
     * Constructeur principal
     */
    public GPSSimulator() {
        super("GPS Simulator");
        initModel();
        initUI();
        initLogWindow();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Initialisation du modèle de données
     */
    private void initModel() {
        cityMap = new CityMap();

        // Création des intersections (noeuds du graphe)
        int[][] coords = {
                {100, 400}, {250, 200}, {250, 400}, {250, 600},
                {400, 200}, {400, 400}, {400, 600},
                {550, 200}, {550, 400}, {550, 600},
                {700, 400}
        };

        for (int i = 0; i < coords.length; i++) {
            cityMap.addIntersection(new Intersection(i, coords[i][0], coords[i][1]));
        }

        // Création des routes (arêtes du graphe)
        int[][] edges = {
                {1, 2}, {2, 3}, {4, 5}, {5, 6}, {7, 8}, {8, 9},
                {2, 5}, {5, 8}, {1, 4}, {4, 7}, {3, 6}, {6, 9},
                {0, 1}, {0, 3}, {10, 9}, {10, 7}
        };

        for (int[] e : edges) {
            Intersection a = cityMap.getIntersections().get(e[0]);
            Intersection b = cityMap.getIntersections().get(e[1]);
            double distance = a.distanceTo(b) / 100.0;
            String name = String.format("r%d-%d", Math.min(e[0], e[1]), Math.max(e[0], e[1]));
            cityMap.addRoadSegment(new RoadSegment(a, b, distance, name));
        }

        // Configuration initiale des états de route
        for (RoadSegment road : cityMap.getRoadSegments()) {
            road.setState(RoadState.FLUID);
        }

        gps = new GPS(cityMap);
        currentRoute = new ArrayList<>();
        currentPosition = cityMap.getIntersections().get(0);
    }

    /**
     * Initialisation de l'interface utilisateur
     */
    private void initUI() {
        mapPanel = createMapPanel();
        mapPanel.setPreferredSize(new Dimension(700, 500));

        // Sélecteurs de départ et destination
        startCombo = new JComboBox<>();
        destCombo = new JComboBox<>();

        for (Intersection intersection : cityMap.getIntersections()) {
            startCombo.addItem("Node " + intersection.getId());
            destCombo.addItem("Node " + intersection.getId());
        }

        startCombo.setSelectedIndex(0);
        destCombo.setSelectedIndex(cityMap.getIntersections().size() - 1);

        // Création des boutons
        calcRouteBtn = new JButton("Calculate Route");
        moveBtn = new JButton("Start Movement");
        addAccidentBtn = new JButton("Add Accident");
        addTrafficBtn = new JButton("Add Traffic");
        removeObsBtn = new JButton("Remove Obstacle");
        resetBtn = new JButton("Reset");

        // Configuration des listeners
        calcRouteBtn.addActionListener(e -> recalculateRoute());
        moveBtn.addActionListener(e -> startVehicleMovement());
        addAccidentBtn.addActionListener(e -> addObstacle(RoadState.ACCIDENT));
        addTrafficBtn.addActionListener(e -> addTraffic());
        removeObsBtn.addActionListener(e -> openRemoveObstacleDialog());
        resetBtn.addActionListener(e -> resetSimulation());

        // Layout
        JPanel inputPanel = createInputPanel();
        JPanel controlPanel = createControlPanel();

        // Assemblage final
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(inputPanel, BorderLayout.NORTH);
        getContentPane().add(mapPanel, BorderLayout.CENTER);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Création du panneau d'affichage de la carte
     */
    private JPanel createMapPanel() {
        return new MapPanel();
    }

    // Classe interne pour le panneau de carte au lieu d'une classe anonyme
    private class MapPanel extends JPanel {
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

            // Dessiner tous les tronçons
            drawRoadSegments(g2);

            // Dessiner itinéraire
            drawCurrentRoute(g2);

            // Dessiner noms de tronçons
            drawRoadNames(g2);

            // Dessiner intersections et IDs
            drawIntersections(g2);

            // Dessiner véhicule
            drawVehicle(g2);
        }
    }

    /**
     * Dessine les segments routiers sur la carte
     */
    private void drawRoadSegments(Graphics2D g2) {
        for (RoadSegment road : cityMap.getRoadSegments()) {
            g2.setColor(road.getState().getColor());
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(
                    road.getStart().getX(),
                    road.getStart().getY(),
                    road.getEnd().getX(),
                    road.getEnd().getY()
            );
        }
    }

    /**
     * Dessine l'itinéraire calculé sur la carte
     */
    private void drawCurrentRoute(Graphics2D g2) {
        if (!currentRoute.isEmpty()) {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(3));
            for (RoadSegment road : currentRoute) {
                g2.drawLine(
                        road.getStart().getX(),
                        road.getStart().getY(),
                        road.getEnd().getX(),
                        road.getEnd().getY()
                );
            }
        }
    }

    /**
     * Dessine les noms des routes sur la carte
     */
    private void drawRoadNames(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        for (RoadSegment road : cityMap.getRoadSegments()) {
            int mx = (road.getStart().getX() + road.getEnd().getX()) / 2;
            int my = (road.getStart().getY() + road.getEnd().getY()) / 2 - 5;
            String txt = road.getName();

            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(txt);
            int th = fm.getHeight();

            g2.setColor(new Color(255, 255, 255, 180));
            g2.fillRect(mx - tw / 2 - 2, my - th + 2, tw + 4, th);
            g2.setColor(Color.BLACK);
            g2.drawString(txt, mx - tw / 2, my);
        }
    }

    /**
     * Dessine les intersections (noeuds) sur la carte
     */
    private void drawIntersections(Graphics2D g2) {
        for (Intersection intersection : cityMap.getIntersections()) {
            g2.setColor(Color.BLACK);
            g2.fillOval(intersection.getX() - 5, intersection.getY() - 5, 10, 10);

            String id = String.valueOf(intersection.getId());
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(id);

            g2.setColor(new Color(255, 255, 255, 180));
            g2.fillRect(
                    intersection.getX() - tw / 2 - 2,
                    intersection.getY() + 6 - fm.getAscent(),
                    tw + 4,
                    fm.getHeight()
            );

            g2.setColor(Color.BLACK);
            g2.drawString(id, intersection.getX() - tw / 2, intersection.getY() + 6);
        }
    }

    /**
     * Dessine le véhicule sur la carte
     */
    private void drawVehicle(Graphics2D g2) {
        if (currentPosition != null) {
            g2.setColor(Color.RED);
            g2.fillOval(currentPosition.getX() - 6, currentPosition.getY() - 6, 12, 12);
        }
    }

    /**
     * Création du panneau d'entrées utilisateur
     */
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        inputPanel.add(new JLabel("Start:"));
        inputPanel.add(startCombo);
        inputPanel.add(Box.createVerticalStrut(5));
        inputPanel.add(new JLabel("Destination:"));
        inputPanel.add(destCombo);

        return inputPanel;
    }

    /**
     * Création du panneau de contrôle
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));

        controlPanel.add(calcRouteBtn);
        controlPanel.add(moveBtn);
        controlPanel.add(addAccidentBtn);
        controlPanel.add(addTrafficBtn);
        controlPanel.add(removeObsBtn);
        controlPanel.add(resetBtn);

        return controlPanel;
    }

    /**
     * Initialisation de la fenêtre de logs
     */
    private void initLogWindow() {
        logFrame = new JFrame("Route & Logs");
        logArea = new JTextArea(15, 40);
        logArea.setEditable(false);

        logFrame.add(new JScrollPane(logArea));
        logFrame.pack();
        logFrame.setLocationRelativeTo(this);
        logFrame.setVisible(true);
    }

    /**
     * Ajoute un message au log
     */
    private void log(String message) {
        logArea.append(message + "\n");
    }

    /**
     * Réinitialise la simulation
     */
    private void resetSimulation() {
        for (RoadSegment road : cityMap.getRoadSegments()) {
            road.setState(RoadState.FLUID);
        }

        currentRoute.clear();
        currentPosition = cityMap.getIntersections().get(0);
        logArea.setText("");
        mapPanel.repaint();
        log("Simulation reset");
    }

    /**
     * Calcule les limites de la carte pour l'affichage
     */
    private Rectangle calculateMapBounds() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Intersection intersection : cityMap.getIntersections()) {
            minX = Math.min(minX, intersection.getX());
            minY = Math.min(minY, intersection.getY());
            maxX = Math.max(maxX, intersection.getX());
            maxY = Math.max(maxY, intersection.getY());
        }

        int margin = 50;
        return new Rectangle(
                minX - margin,
                minY - margin,
                (maxX - minX) + 2 * margin,
                (maxY - minY) + 2 * margin
        );
    }

    /**
     * Animation de déplacement du véhicule corrigée
     * Cette méthode assure que le véhicule parcourt bien tout l'itinéraire
     */
    private void startVehicleMovement() {
        if (currentRoute.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Calculate route first");
            return;
        }

        calcRouteBtn.setEnabled(false);

        new Thread(() -> {
            try {
                // On s'assure que la position de départ est correcte
                List<Intersection> routeNodes = new ArrayList<>();
                routeNodes.add(currentRoute.get(0).getStart());

                // Construire une liste ordonnée de toutes les intersections du trajet
                for (RoadSegment road : currentRoute) {
                    routeNodes.add(road.getEnd());
                }

                // Animation à travers chaque intersection
                for (Intersection node : routeNodes) {
                    currentPosition = node;
                    SwingUtilities.invokeLater(() -> mapPanel.repaint());
                    Thread.sleep(300);  // Délai plus long pour voir le mouvement
                }

                SwingUtilities.invokeLater(() -> {
                    log("Arrived at destination");
                    calcRouteBtn.setEnabled(true);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                SwingUtilities.invokeLater(() -> {
                    log("Movement interrupted");
                    calcRouteBtn.setEnabled(true);
                });
            }
        }).start();
    }

    /**
     * Recalcule l'itinéraire entre le départ et la destination
     * Corrigé pour assurer que l'itinéraire est correctement construit
     */
    private void recalculateRoute() {
        logArea.setText("");

        int startIndex = startCombo.getSelectedIndex();
        int destIndex = destCombo.getSelectedIndex();

        Intersection start = cityMap.getIntersections().get(startIndex);
        Intersection destination = cityMap.getIntersections().get(destIndex);

        gps.setDestination(destination);
        List<Intersection> path = gps.calculateRoute(start);

        currentRoute.clear();

        if (path != null && path.size() > 1) {
            log("Route:");
            // Vérification du premier nœud
            currentPosition = start;

            for (int i = 0; i < path.size() - 1; i++) {
                RoadSegment road = cityMap.findRoadSegment(path.get(i), path.get(i + 1));
                if (road != null) {
                    // S'assurer que le segment est orienté dans le bon sens
                    // Si start != road.getStart(), on crée un nouveau segment orienté correctement
                    if (path.get(i) != road.getStart()) {
                        road = new RoadSegment(path.get(i), path.get(i + 1),
                                path.get(i).distanceTo(path.get(i + 1)) / 100.0,
                                road.getName());
                        road.setState(cityMap.findRoadSegment(path.get(i), path.get(i + 1)).getState());
                    }
                    currentRoute.add(road);
                    log(" -> " + road.getName());
                }
            }
            mapPanel.repaint();
        } else {
            log("No route found.");
        }
    }

    /**
     * Ajoute un obstacle sur une route
     */
    private void addObstacle(RoadState type) {
        RoadSegment road = chooseRoadSegment("Choose road segment");
        if (road != null) {
            road.setState(type);
            mapPanel.repaint();
            log(type + " applied");
        }
    }

    /**
     * Ajoute du trafic sur une route avec niveau configurable
     */
    private void addTraffic() {
        RoadSegment road = chooseRoadSegment("Choose road segment (traffic)");
        if (road != null && road.getState() != RoadState.ACCIDENT) {
            String[] levels = {"Light", "Moderate", "Heavy"};
            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "Traffic Level:",
                    "Traffic",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    levels,
                    levels[0]
            );

            if (selected != null) {
                switch (selected) {
                    case "Light":
                        road.setState(RoadState.LIGHT);
                        break;
                    case "Moderate":
                        road.setState(RoadState.MODERATE);
                        break;
                    case "Heavy":
                        road.setState(RoadState.HEAVY);
                        break;
                }

                mapPanel.repaint();
                log("Traffic " + selected);
            }
        }
    }

    /**
     * Ouvre une boîte de dialogue pour supprimer un obstacle
     */
    private void openRemoveObstacleDialog() {
        JDialog dialog = new JDialog(this, "Remove Obstacle", true);
        dialog.setLayout(new BorderLayout());

        String[] names = cityMap.getRoadSegments().stream()
                .map(RoadSegment::getName)
                .toArray(String[]::new);

        JList<String> list = new JList<>(names);
        dialog.add(new JScrollPane(list), BorderLayout.CENTER);

        JRadioButton removeTraffic = new JRadioButton("Remove Traffic", true);
        JRadioButton removeAccident = new JRadioButton("Remove Accident");

        ButtonGroup group = new ButtonGroup();
        group.add(removeTraffic);
        group.add(removeAccident);

        JButton applyButton = new JButton("OK");
        applyButton.addActionListener(e -> {
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex >= 0) {
                RoadSegment road = cityMap.getRoadSegments().get(selectedIndex);
                road.setState(RoadState.FLUID);
                mapPanel.repaint();
                log("Obstacle removed");
            }
            dialog.dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(removeTraffic);
        buttonPanel.add(removeAccident);
        buttonPanel.add(applyButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Permet à l'utilisateur de choisir un segment routier
     */
    private RoadSegment chooseRoadSegment(String message) {
        List<RoadSegment> roadSegments = cityMap.getRoadSegments();
        String[] names = roadSegments.stream()
                .map(RoadSegment::getName)
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                message,
                "Selection",
                JOptionPane.PLAIN_MESSAGE,
                null,
                names,
                names[0]
        );

        if (selected == null) {
            return null;
        }

        for (RoadSegment road : roadSegments) {
            if (road.getName().equals(selected)) {
                return road;
            }
        }

        return null;
    }

    /**
     * Point d'entrée de l'application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GPSSimulator().setVisible(true));
    }
}

/**
 * Représente une intersection dans la ville (nœud du graphe)
 */
class Intersection {
    private final int id;
    private final int x;
    private final int y;

    /**
     * Crée une nouvelle intersection
     *
     * @param id identifiant unique
     * @param x  coordonnée X
     * @param y  coordonnée Y
     */
    public Intersection(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    /**
     * Calcule la distance vers une autre intersection
     *
     * @param other l'autre intersection
     * @return distance euclidienne
     */
    public double distanceTo(Intersection other) {
        return Math.hypot(x - other.x, y - other.y);
    }

    // Getters
    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
}

/**
 * Représente un segment routier entre deux intersections (arête du graphe)
 */
class RoadSegment {
    private final Intersection start;
    private final Intersection end;
    private final String name;
    private final double distance;
    private RoadState state;

    /**
     * Crée un nouveau segment routier
     *
     * @param start    intersection de départ
     * @param end      intersection d'arrivée
     * @param distance distance réelle
     * @param name     nom de la route
     */
    public RoadSegment(Intersection start, Intersection end, double distance, String name) {
        this.start = start;
        this.end = end;
        this.distance = distance;
        this.name = name;
        this.state = RoadState.FLUID;
    }

    /**
     * Vérifie si cette route contient l'intersection donnée
     */
    public boolean contains(Intersection intersection) {
        return start == intersection || end == intersection;
    }

    /**
     * Retourne l'autre extrémité de la route à partir d'une intersection
     */
    public Intersection getOtherEnd(Intersection from) {
        if (from == start) return end;
        if (from == end) return start;
        return null;
    }

    // Getters et setters
    public Intersection getStart() { return start; }
    public Intersection getEnd() { return end; }
    public String getName() { return name; }
    public RoadState getState() { return state; }
    public void setState(RoadState state) { this.state = state; }
}

/**
 * États possibles pour un segment routier
 */
enum RoadState {
    FLUID(1.0, Color.BLACK),
    LIGHT(1.3, Color.GREEN),
    MODERATE(1.7, Color.ORANGE),
    HEAVY(2.5, Color.RED),
    ACCIDENT(Double.POSITIVE_INFINITY, Color.MAGENTA);

    private final double weight;
    private final Color color;

    /**
     * Constructeur
     *
     * @param weight facteur de pondération pour le calcul d'itinéraire
     * @param color  couleur d'affichage
     */
    RoadState(double weight, Color color) {
        this.weight = weight;
        this.color = color;
    }

    public double getWeight() { return weight; }
    public Color getColor() { return color; }
}

/**
 * Représente la carte de la ville avec ses intersections et segments routiers
 */
class CityMap {
    private List<Intersection> intersections = new ArrayList<>();
    private List<RoadSegment> roadSegments = new ArrayList<>();

    /**
     * Ajoute une intersection à la carte
     */
    public void addIntersection(Intersection intersection) {
        intersections.add(intersection);
    }

    /**
     * Ajoute un segment routier à la carte
     */
    public void addRoadSegment(RoadSegment roadSegment) {
        roadSegments.add(roadSegment);
    }

    /**
     * Trouve un segment routier entre deux intersections (ordre indifférent)
     */
    public RoadSegment findRoadSegment(Intersection a, Intersection b) {
        for (RoadSegment road : roadSegments) {
            if ((road.getStart() == a && road.getEnd() == b) ||
                    (road.getStart() == b && road.getEnd() == a)) {
                return road;
            }
        }
        return null;
    }

    /**
     * Trouve tous les segments routiers connectés à une intersection
     */
    public List<RoadSegment> getConnectedRoads(Intersection intersection) {
        List<RoadSegment> connected = new ArrayList<>();
        for (RoadSegment road : roadSegments) {
            if (road.contains(intersection)) {
                connected.add(road);
            }
        }
        return connected;
    }

    // Getters
    public List<Intersection> getIntersections() { return intersections; }
    public List<RoadSegment> getRoadSegments() { return roadSegments; }
}

/**
 * Simulateur de GPS qui calcule les itinéraires optimaux
 */
class GPS {
    private CityMap cityMap;
    private Intersection destination;

    /**
     * Crée un nouveau GPS
     *
     * @param cityMap carte de la ville
     */
    public GPS(CityMap cityMap) {
        this.cityMap = cityMap;
    }

    /**
     * Définit la destination
     */
    public void setDestination(Intersection destination) {
        this.destination = destination;
    }

    /**
     * Calcule l'itinéraire optimal entre deux points
     * Utilise l'algorithme de Dijkstra
     *
     * @param start point de départ
     * @return liste des intersections formant l'itinéraire
     */
    public List<Intersection> calculateRoute(Intersection start) {
        // Cartes des distances et précédences
        Map<Intersection, Double> distances = new HashMap<>();
        Map<Intersection, Intersection> predecessors = new HashMap<>();

        // Initialisation
        for (Intersection intersection : cityMap.getIntersections()) {
            distances.put(intersection, Double.POSITIVE_INFINITY);
        }

        distances.put(start, 0.0);

        // Comparateur pour la file de priorité
        Comparator<Intersection> comparator = new Comparator<Intersection>() {
            @Override
            public int compare(Intersection i1, Intersection i2) {
                return Double.compare(distances.get(i1), distances.get(i2));
            }
        };

        PriorityQueue<Intersection> queue = new PriorityQueue<>(comparator);
        queue.add(start);

        // Algorithme de Dijkstra
        while (!queue.isEmpty()) {
            Intersection current = queue.poll();

            // Si on a atteint la destination, on peut arrêter
            if (current == destination) {
                break;
            }

            // Distance actuelle
            double currentDistance = distances.get(current);

            // Si la distance est infinie, ce sommet n'est pas accessible
            if (currentDistance == Double.POSITIVE_INFINITY) {
                continue;
            }

            // Explorer les voisins
            for (RoadSegment road : cityMap.getConnectedRoads(current)) {
                Intersection neighbor = road.getOtherEnd(current);

                // Calculer la nouvelle distance
                double weight = road.getState().getWeight();
                double newDistance = currentDistance + weight;

                // Si c'est un meilleur chemin, mettre à jour
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    predecessors.put(neighbor, current);

                    // Ajouter à la file de priorité (ou mettre à jour)
                    queue.remove(neighbor);  // O(n) mais simplifie l'implémentation
                    queue.add(neighbor);
                }
            }
        }

        // Reconstruction du chemin
        LinkedList<Intersection> path = new LinkedList<>();

        if (predecessors.containsKey(destination) || start == destination) {
            for (Intersection step = destination; step != null; step = predecessors.get(step)) {
                path.addFirst(step);
            }
        }

        return path.size() < 2 ? null : path;
    }
}
