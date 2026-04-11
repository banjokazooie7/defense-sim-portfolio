package io.github.banjokazooie7.simcore.entity;

public interface Entity{
	String getId();
	Position2D getPosition();

	void update(long deltaMillis);
}
