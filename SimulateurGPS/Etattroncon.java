package SimulateurGPS;

public enum Etattroncon {
    FLUIDE(1.0), FAIBLE(1.3), MODERE(1.7), INTENSE(2.5), ACCIDENT(Double.POSITIVE_INFINITY);
    private double weight;

    Etattroncon(double w) {
        weight = w;
    }

    public double getWeight() {
        return weight;
    }
}
