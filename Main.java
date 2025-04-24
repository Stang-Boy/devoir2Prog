import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {
    private JButton bouton;
    private JLabel texte;

    public Main() {
        setTitle("Surprise");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        bouton = new JButton("Click for a suprise");
        bouton.setBounds(70, 30, 150, 30);
        texte = new JLabel("");
        texte.setBounds(100, 80, 200, 30);

        bouton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                texte.setText("E1000 The minor eater");
            }
        });

        add(bouton);
        add(texte);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }
}
