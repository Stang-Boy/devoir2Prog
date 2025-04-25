/*Version Finale, 25 avril 2025, par Émile Larocque, Hugo Thivierge, Vincent Lafleur, Félix Ladouceur, Sébastien Roussel, Olivier Rossignol*/
package SimulateurGPS;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

/** Classe principale du simulateur GPS qui gère l'interface graphique et la logique d'interaction.
 * Cette classe étend JFrame et combine la visualisation de la carte avec les fonctionnalités de calcul d'itinéraire.
 */
public class SimulateurGPS extends JFrame {

    /*Déclaration des composants principaux */
    private CarteVille carte;
    private GPS gps;
    private JPanel mapPanel;
    private JComboBox<String> startCombo, destCombo;
    private JButton calcButton, accidentButton, trafficButton, resetButton, itineraireButton;

    /*Boutons de contrôle */
    private JFrame itineraireFrame;
    private JTextArea itineraireArea;
    private Vehicule vehicule;
    private javax.swing.Timer timer;

    /**
     * Constructeur principal qui initialise l'interface et le modèle.
     * Configure la fenêtre principale, initialise la carte et l'interface utilisateur.
     */
    public SimulateurGPS() {
        super("Simulateur de GPS");
        setResizable(true);
        initModel();
        initUI();
        initItineraireWindow();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

     /**
     * Calcule les limites de la carte pour l'affichage.
     * Parcourt toutes les intersections pour déterminer la zone à afficher.
     * Ajoute une marge de 50 pixels autour de la zone utile.
     */
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

    /**
     * Initialise le modèle de données avec une carte prédéfinie.
     * Crée 11 intersections et les connecte avec des tronçons.
     * Tous les tronçons sont initialisés avec un état FLUIDE.
     */
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

    /**
     * Initialise l'interface utilisateur principale.
     * Configure :
     * - Le panel de carte avec son rendu personnalisé
     * - Les contrôles de sélection (combobox)
     * - Les boutons d'action et leurs gestionnaires d'événements
     * - La disposition des composants avec GridBagLayout
     */
    private void initUI() {
        mapPanel = new JPanel() {

            /**
             * Méthode de rendu principal qui dessine :
             * 1. Les tronçons routiers (couleur selon l'état)
             * 2. L'itinéraire calculé (ligne bleue)
             * 3. Les intersections (cercles noirs)
             * 4. Le véhicule (position actuelle)
             * 5. Les étiquettes de tronçons et intersections
             * Applique une transformation pour adapter l'affichage à la taille du panel.
             */
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

                // Étape 1 : Dessiner toutes les lignes (tronçons et itinéraire)
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
                }

                if (vehicule != null) {
                    g2.setColor(new Color(0, 0, 255, 200));
                    g2.setStroke(new BasicStroke(4));
                    for (int i = 0; i < vehicule.getCurrentSegment(); i++) {
                        Intersection a = vehicule.getItineraire().get(i);
                        Intersection b = vehicule.getItineraire().get(i+1);
                        g2.drawLine(a.x, a.y, b.x, b.y);
                    }
                }

                // Étape 2 : Dessiner les cercles des intersections
                for (Intersection i : carte.getIntersections()) {
                    g2.setColor(Color.BLACK);
                    g2.fillOval(i.x - 5, i.y - 5, 10, 10);
                }

                // Étape 3 : Dessiner le véhicule (s'il existe)
                if (vehicule != null) {
                    vehicule.draw(g2);
                }

                // Étape 4 : Dessiner tous les textes avec fond semi-transparent
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                for (Troncon t : carte.getTroncons()) {
                    int mx = (t.getA().x + t.getB().x) / 2;
                    int my = (t.getA().y + t.getB().y) / 2 - 10; // Décalage vers le haut
                    String text = t.getNomRue();
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();

                    // Dessiner un fond semi-transparent
                    g2.setColor(new Color(255, 255, 255, 180)); // Blanc avec opacité
                    g2.fillRect(mx - 2, my - textHeight + 3, textWidth + 4, textHeight);
                    // Dessiner le texte
                    g2.setColor(Color.BLACK);
                    g2.drawString(text, mx, my);
                }

                for (Intersection i : carte.getIntersections()) {
                    String text = String.valueOf(i.id);
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();
                    int textX = i.x + 12; // Décalage plus important vers la droite
                    int textY = i.y - 8;  // Légère ajuste vers le haut

                    // Dessiner un fond semi-transparent
                    g2.setColor(new Color(255, 255, 255, 180)); // Blanc avec opacité
                    g2.fillRect(textX - 2, textY - textHeight + 3, textWidth + 4, textHeight);
                    // Dessiner le texte
                    g2.setColor(Color.BLACK);
                    g2.drawString(text, textX, textY);
                }
            }
        };
        mapPanel.setPreferredSize(new Dimension(700, 500)); // Taille par défaut de l'interface
        mapPanel.setMinimumSize(new Dimension(300, 300));   // Taille minimale de l'interface

        calcButton = new JButton("Calculer Itinéraire");
        accidentButton = new JButton("Ajouter Accident");
        trafficButton = new JButton("Ajouter Trafic");
        resetButton = new JButton("Réinitialiser");
        itineraireButton = new JButton("Journal");
        startCombo = new JComboBox<>();
        destCombo = new JComboBox<>();
        for (Intersection i : carte.getIntersections()) {
            startCombo.addItem("Départ " + i.id);
            destCombo.addItem("Destination " + i.id);
        }
        startCombo.setSelectedIndex(0);
        destCombo.setSelectedIndex(10);
        // Limiter la taille des JComboBox
        startCombo.setPreferredSize(new Dimension(150, 25));
        destCombo.setPreferredSize(new Dimension(150, 25));
        // Limiter la taille du bouton Instructions
        itineraireButton.setPreferredSize(new Dimension(150, 25));

        calcButton.addActionListener(e -> {
            recalcItineraire();
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
            vehicule = null; // Supprimer l'itinéraire affiché
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }
            itineraireArea.setText(""); // Réinitialiser les logs
            log("Réinitialisation complète");
            mapPanel.repaint();
        });
        itineraireButton.addActionListener(e -> itineraireFrame.setVisible(true));

        JPanel control = new JPanel();
        control.setLayout(new GridBagLayout());
        control.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Ligne 1 : Départ et Destination (50-50)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        control.add(new JLabel("Départ :"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        control.add(new JLabel("Destination :"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        control.add(startCombo, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        control.add(destCombo, gbc);

        // Ligne 2 : Calculer Itinéraire et Réinitialiser (50-50)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        control.add(calcButton, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        control.add(resetButton, gbc);

        // Ligne 3 : Ajouter Trafic et Ajouter Accident (50-50)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        control.add(trafficButton, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        control.add(accidentButton, gbc);

        // Ligne 4 : Instructions (centré, taille réduite)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        control.add(itineraireButton, gbc);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mapPanel, BorderLayout.CENTER);
        getContentPane().add(control, BorderLayout.SOUTH);
    }

    /**
     * Initialise la fenêtre secondaire pour afficher le journal de navigation.
     * Configure une JTextArea dans une JScrollPane avec les dimensions appropriées.
     */
    private void initItineraireWindow() {
        itineraireFrame = new JFrame("Journal");
        itineraireArea = new JTextArea(20, 40);
        itineraireArea.setEditable(false);
        itineraireFrame.add(new JScrollPane(itineraireArea));
        itineraireFrame.pack();
        itineraireFrame.setLocationRelativeTo(this);
        itineraireFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        itineraireFrame.setVisible(true); // Ouvre la fenêtre au démarrage
    }

    private void log(String message) {
        itineraireArea.append(message + "\n");
    }

    /*Ajoute des accidents */
    private void addAccident() {
        Troncon t = chooseTroncon("Choisissez un tronçon (accident) :");
        if (t != null) {
            t.setEtat(EtatTroncon.ACCIDENT);
            mapPanel.repaint(); // Mettre à jour l'affichage sans recalculer
        }
    }

    /*Ajoute les états de conduite */
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
                mapPanel.repaint(); // Mettre à jour l'affichage sans recalculer
            }
        }
    }

    private void clearAccidents() {
        for (Troncon t : carte.getTroncons()) {
            if (t.getEtat() == EtatTroncon.ACCIDENT) t.setEtat(EtatTroncon.FLUIDE);
        }
    }

    private void clearTrafic() {
        for (Troncon t : carte.getTroncons()) {
            if (t.getEtat() == EtatTroncon.FAIBLE || t.getEtat() == EtatTroncon.MODERE || t.getEtat() == EtatTroncon.INTENSE) {
                t.setEtat(EtatTroncon.FLUIDE);
            }
        }
    }

    /*Méthode pour choisir les bons Tronçons */
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

    /**
     * Recalcule l'itinéraire en fonction des sélections actuelles.
     * Affiche les instructions dans la fenêtre de journal.
     * Anime le véhicule le long du nouvel itinéraire.
     */
    private void recalcItineraire() {
        itineraireArea.setText(""); // Réinitialiser les logs
        Intersection start = carte.getIntersections().get(startCombo.getSelectedIndex());
        Intersection end = carte.getIntersections().get(destCombo.getSelectedIndex());
        gps.setDestination(end);
        List<Intersection> chemin = gps.calculerItineraire(start);
        
        if (chemin != null && chemin.size() > 1) {
            // Generate and log navigation instructions
            List<String> instructions = generateNavigationInstructions(chemin);
            log("Voici les instructions à suivre");
            for (String instruction : instructions) {
                log(instruction);
            }

            vehicule = new Vehicule(chemin);
            if (timer != null && timer.isRunning()) timer.stop();
            
            timer = new javax.swing.Timer(300, e -> {
                if (!vehicule.avancer()) {
                    timer.stop();
                    log("Vous êtes arrivé à destination");
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

        // Generate instruction for each intersection except the first
        for (int i = 1; i < chemin.size(); i++) {
            instructions.add("Aller vers le sommet " + chemin.get(i).id);
        }

        return instructions;
    }

    /**
     * Point d'entrée principal de l'application.
     * Lance l'interface graphique dans le thread EDT de Swing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulateurGPS().setVisible(true));
    }
}