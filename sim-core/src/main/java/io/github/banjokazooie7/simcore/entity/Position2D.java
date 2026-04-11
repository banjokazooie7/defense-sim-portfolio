package io.github.banjokazooie7.simcore.entity;

public record Position2D(double xMeters, double yMeters) {

    public double distanceTo(Position2D other) {
        double dx = this.xMeters - other.xMeters;
        double dy = this.yMeters - other.yMeters;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Position2D translate(double dx, double dy) {
        return new Position2D(xMeters + dx, yMeters + dy);
    }

    @Override
    public String toString() {
        return String.format("(%.1fm, %.1fm)", xMeters, yMeters);
    }
}
