package SimulateurGPS;

public class intersection {
    public int id, x, y;

    public intersection(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public double distanceVers(intersection o) {
        return Math.hypot(x - o.x, y - o.y);
    }
}
