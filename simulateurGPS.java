package SimulateurGPS;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

public class simulateurGPS extends JFrame {
    private carteVille carte;
    private GPS gps;
    private JPanel mapPanel;
    private JComboBox<String> startCombo, destCombo;
    private JButton calcButton, accidentButton, trafficButton, resetButton, itineraireButton;
    private JFrame itineraireFrame;
    private JTextArea itineraireArea;
    private vehicule vehicule;
    private javax.swing.Timer timer;

    public simulateurGPS() {
        super("GPS Simulator");
        setResizable(true);
        initModel();
        initUI();
        initItineraireWindow();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private Rectangle calculateMapBounds() {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (intersection i : carte.getIntersections()) {
            minX = Math.min(minX, i.x);
            minY = Math.min(minY, i.y);
            maxX = Math.max(maxX, i.x);
            maxY = Math.max(maxY, i.y);
        }
        int margin = 50;
        return new Rectangle(minX - margin, minY - margin,
                maxX - minX + 2 * margin,
                maxY - minY + 2 * margin);
    }

    private void initModel() {
        carte = new carteVille();
        int[][] coords = {{100,400},{250,200},{250,400},{250,600},
                {400,200},{400,400},{400,600},
                {550,200},{550,400},{550,600},
                {700,400}};
        for (int i = 0; i < coords.length; i++) {
            carte.ajouterintersection(new intersection(i, coords[i][0], coords[i][1]));
        }
        int[][] edges = {{1,2},{2,3},{4,5},{5,6},{7,8},{8,9},
                {2,5},{5,8},{1,4},{4,7},{3,6},{6,9},
                {0,1},{0,3},{10,9},{10,7}};
        for (int[] e : edges) {
            intersection a = carte.getIntersections().get(e[0]);
            intersection b = carte.getIntersections().get(e[1]);
            double dist = a.distanceVers(b) / 100.0;
            String name = String.format("r%d-%d", Math.min(e[0], e[1]), Math.max(e[0], e[1]));
            carte.ajoutertroncon(new troncon(a, b, dist, name));
        }
        for (troncon t : carte.gettroncons()) t.setEtat(Etattroncon.FLUIDE);
        gps = new GPS(carte);
    }

    private void initUI() {
        mapPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                Rectangle bounds = calculateMapBounds();
                int panelWidth = getWidth(), panelHeight = getHeight();
                double scale = Math.min((double)panelWidth / bounds.width,
                        (double)panelHeight / bounds.height);
                int offsetX = (int)((panelWidth - bounds.width * scale) / 2);
                int offsetY = (int)((panelHeight - bounds.height * scale) / 2);
                g2.translate(offsetX, offsetY);
                g2.scale(scale, scale);
                g2.translate(-bounds.x, -bounds.y);
                // dessiner troncons
                for (troncon t : carte.gettroncons()) {
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
                // dessiner itineraire
                if (vehicule != null) {
                    g2.setColor(new Color(0, 0, 255, 200));
                    g2.setStroke(new BasicStroke(4));
                    for (int i = 0; i < vehicule.currentSegment; i++) {
                        intersection a = vehicule.itineraire.get(i);
                        intersection b = vehicule.itineraire.get(i+1);
                        g2.drawLine(a.x, a.y, b.x, b.y);
                    }
                }
                // intersections
                for (intersection i : carte.getIntersections()) {
                    g2.setColor(Color.BLACK);
                    g2.fillOval(i.x - 5, i.y - 5, 10, 10);
                }
                // vehicule
                if (vehicule != null) {
                    vehicule.draw(g2);
                }
                // textes
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                for (troncon t : carte.gettroncons()) {
                    int mx = (t.getA().x + t.getB().x)/2,
                            my = (t.getA().y + t.getB().y)/2 -10;
                    String text = t.getNomRue();
                    FontMetrics fm = g2.getFontMetrics();
                    int w = fm.stringWidth(text), h = fm.getHeight();
                    g2.setColor(new Color(255,255,255,180));
                    g2.fillRect(mx-2, my-h+3, w+4, h);
                    g2.setColor(Color.BLACK);
                    g2.drawString(text, mx, my);
                }
                for (intersection i : carte.getIntersections()) {
                    String text = String.valueOf(i.id);
                    FontMetrics fm = g2.getFontMetrics();
                    int w = fm.stringWidth(text), h = fm.getHeight();
                    int tx = i.x+12, ty = i.y-8;
                    g2.setColor(new Color(255,255,255,180));
                    g2.fillRect(tx-2, ty-h+3, w+4, h);
                    g2.setColor(Color.BLACK);
                    g2.drawString(text, tx, ty);
                }
            }
        };
        mapPanel.setPreferredSize(new Dimension(700,500));
        mapPanel.setMinimumSize(new Dimension(300,300));
        calcButton = new JButton("Calculer Itinéraire");
        accidentButton = new JButton("Ajouter Accident");
        trafficButton = new JButton("Ajouter Trafic");
        resetButton = new JButton("Réinitialiser");
        itineraireButton = new JButton("Itinéraire");
        startCombo = new JComboBox<>(); destCombo = new JComboBox<>();
        for (intersection i : carte.getIntersections()){
            startCombo.addItem("Départ "+i.id);
            destCombo.addItem("Destination "+i.id);
        }
        startCombo.setSelectedIndex(0);
        destCombo.setSelectedIndex(10);
        calcButton.addActionListener(e -> recalcItineraire());
        accidentButton.addActionListener(e -> { addAccident(); log("Accident ajouté"); });
        trafficButton.addActionListener(e -> { addTraffic(); log("Trafic ajouté"); });
        resetButton.addActionListener(e -> { clearAccidents(); clearTrafic(); vehicule=null;
            if(timer!=null&&timer.isRunning())timer.stop();
            itineraireArea.setText(""); log("Réinitialisation complète"); mapPanel.repaint();
        });
        itineraireButton.addActionListener(e->itineraireFrame.setVisible(true));
        JPanel control=new JPanel(new GridLayout(4,2,5,5));
        control.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        control.add(startCombo);control.add(destCombo);
        control.add(calcButton);control.add(accidentButton);
        control.add(trafficButton);control.add(resetButton);
        control.add(itineraireButton);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mapPanel,BorderLayout.CENTER);
        getContentPane().add(control,BorderLayout.SOUTH);
    }

    private void initItineraireWindow() {
        itineraireFrame=new JFrame("Itinéraire");
        itineraireArea=new JTextArea(20,40);
        itineraireArea.setEditable(false);
        itineraireFrame.add(new JScrollPane(itineraireArea));
        itineraireFrame.pack();
        itineraireFrame.setLocationRelativeTo(this);
        itineraireFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void log(String message) {
        itineraireArea.append(message+"\n");
    }

    private void addAccident() {
        troncon t=choosetroncon("Choisissez un tronçon (accident) :");
        if(t!=null){t.setEtat(Etattroncon.ACCIDENT);recalcItineraire();mapPanel.repaint();}
    }

    private void addTraffic() {
        troncon t=choosetroncon("Choisissez un tronçon (trafic) :");
        if(t!=null&&t.getEtat()!=Etattroncon.ACCIDENT){
            String[] levels={"Faible","Modéré","Intense"};
            String lvl=(String)JOptionPane.showInputDialog(this,"Niveau de trafic :","Selection",JOptionPane.PLAIN_MESSAGE,null,levels,levels[0]);
            if(lvl!=null){
                switch(lvl){case "Faible":t.setEtat(Etattroncon.FAIBLE);break;
                    case "Modéré":t.setEtat(Etattroncon.MODERE);break;
                    case "Intense":t.setEtat(Etattroncon.INTENSE);break;}
                recalcItineraire();mapPanel.repaint();
            }
        }
    }

    private void clearAccidents(){
        for(troncon t:carte.gettroncons())if(t.getEtat()==Etattroncon.ACCIDENT)t.setEtat(Etattroncon.FLUIDE);
    }

    private void clearTrafic(){
        for(troncon t:carte.gettroncons())if(t.getEtat()!=Etattroncon.FLUIDE)t.setEtat(Etattroncon.FLUIDE);
    }

    private troncon choosetroncon(String msg){
        List<troncon> lst=carte.gettroncons();
        String[] names=lst.stream().map(troncon::getNomRue).toArray(String[]::new);
        String sel=(String)JOptionPane.showInputDialog(this,msg,"Selection",JOptionPane.PLAIN_MESSAGE,null,names,names[0]);
        if(sel==null) return null;
        for(troncon t:lst)if(t.getNomRue().equals(sel))return t;
        return null;
    }

    private void recalcItineraire(){
        itineraireArea.setText("");
        intersection start=carte.getIntersections().get(startCombo.getSelectedIndex());
        intersection end=carte.getIntersections().get(destCombo.getSelectedIndex());
        gps.setDestination(end);
        List<intersection> chemin=gps.calculerItineraire(start);
        if(chemin!=null&&chemin.size()>1){
            List<String> instr=generateNavigationInstructions(chemin);
            log("Voici les instructions à suivre");for(String s:instr)log(s);
            vehicule=new vehicule(chemin);
            if(timer!=null&&timer.isRunning())timer.stop();
            timer=new javax.swing.Timer(300,e->{if(!vehicule.avancer()){timer.stop();log("Vous êtes arrivé à destination");}mapPanel.repaint();});
            timer.start();
        } else log("Aucun itinéraire possible");
        mapPanel.repaint();
    }

    private List<String> generateNavigationInstructions(List<intersection> ch){
        List<String> ins=new ArrayList<>();if(ch.size()<2)return ins;
        for(int i=1;i<ch.size();i++)ins.add("Aller vers le sommet "+ch.get(i).id);
        return ins;
    }

    public static void main(String[] args){SwingUtilities.invokeLater(() -> new simulateurGPS().setVisible(true));}
}



