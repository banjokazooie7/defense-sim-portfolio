package io.github.banjokazooie7.simcore.entity;

public final class Target implements Entity{
    private final String id;
    private Position2D position;
    private double vxMetersPerSec;
    private double vyMetersPerSec;

    public Target(String id, Position2D startPosition, double vxMetersPerSec, double vyMetersPerSec){
        if (id == null || id.isBlank()){
            throw new IllegalArgumentException("Target id must be null or blank");
        }
        if(startPosition == null){
            throw new IllegalArgumentException("Start position must not be null");
        }
        this.id = id;
        this.position = startPosition;
        this.vxMetersPerSec = vxMetersPerSec;
        this.vyMetersPerSec = vyMetersPerSec;
    }

    @Override
    public String getId(){return id;}

    @Override
    public Position2D getPosition(){return position;}
    public double getVx(){return vxMetersPerSec;}
    public double getVy(){return vyMetersPerSec;}

    public double getSpeed(){return Math.sqrt(vxMetersPerSec * vxMetersPerSec 
        + vyMetersPerSec * vyMetersPerSec);
    }
    public double getHeadingDegrees(){
	if(vxMetersPerSec == 0.0 && vyMetersPerSec == 0.0){
		return 0.0;
	}
	double radians = Math.atan2(vxMetersPerSec, vyMetersPerSec);
	double degrees = Math.toDegrees(radians);
	if(degrees < 0){
		degrees += 360.0;
	}
	return degrees;
    }

    public void setVelocity(double vx, double vy){
        this.vxMetersPerSec = vx;
        this.vyMetersPerSec = vy;
    }

    public void update(long deltaMillis){
        if(deltaMillis < 0){
            throw new IllegalArgumentException("deltaMillis must be non-negative");//reject negative time
        }
        if(deltaMillis == 0) return; //if zero time, do nothing

        double dtSec = deltaMillis / 1000.0;//convert millisecond to seconds
        double dx = vxMetersPerSec * dtSec;//displacement = velocity * time
        double dy = vyMetersPerSec * dtSec;
        this.position = position.translate(dx, dy);
    }

	@Override
    public String toString() {
        return String.format("Target{id=%s, pos=%s, vel=(%.1f, %.1f) m/s, hdg=%.1f°, spd=%.1f m/s}",
                id, position, vxMetersPerSec, vyMetersPerSec, getHeadingDegrees(), getSpeed());
    }
}
