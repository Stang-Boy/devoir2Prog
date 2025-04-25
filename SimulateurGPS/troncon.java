package SimulateurGPS;

public class troncon {
    private intersection a, b;
    private double distance;
    private String nomRue;
    private Etattroncon etat;

    public troncon(intersection a, intersection b, double distance, String nomRue) {
        this.a = a;
        this.b = b;
        this.distance = distance;
        this.nomRue = nomRue;
        this.etat = Etattroncon.FLUIDE;
    }

    public intersection getA() {
        return a;
    }

    public intersection getB() {
        return b;
    }

    public double getDistance() {
        return distance;
    }

    public String getNomRue() {
        return nomRue;
    }

    public Etattroncon getEtat() {
        return etat;
    }

    public void setEtat(Etattroncon e) {
        etat = e;
    }
}
