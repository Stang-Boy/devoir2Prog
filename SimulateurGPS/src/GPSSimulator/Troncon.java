package GPSSimulator;

public class Troncon {
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