package io.github.banjokazooie7.simcore;

public final class SimClock{
	private long nowMillis;

	public SimClock(long startMillis){
		this.nowMillis = startMillis;
	}

	public long now(){
		return nowMillis;
	}

	public void advance(long deltaMillis){
		if(deltaMillis < 0) throw new IllegalArgumentException("deltaMillis must be >= 0");
		nowMillis += deltaMillis;
	}
}
