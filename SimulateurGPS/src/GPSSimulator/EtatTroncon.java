package GPSSimulator;

public enum EtatTroncon {
    FLUIDE(1.0), FAIBLE(1.3), MODERE(1.7), INTENSE(2.5), ACCIDENT(Double.POSITIVE_INFINITY);
    
    private final double weight;
    EtatTroncon(double weight) { this.weight = weight; }
    public double getWeight() { return weight; }
}