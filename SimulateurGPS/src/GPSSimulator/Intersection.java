package GPSSimulator;

public class Intersection {
    public int id, x, y;
    public Intersection(int id, int x, int y) { this.id = id; this.x = x; this.y = y; }
    public double distanceVers(Intersection o) { return Math.hypot(x - o.x, y - o.y); }
}