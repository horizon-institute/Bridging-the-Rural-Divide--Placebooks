package org.placebooks.model.json;

import org.placebooks.model.PlaceBookItem;

public class PlaceBookItemDistanceEntry extends PlaceBookItemEntry
{
	private final double distance;

	private final String ownerName;

	public PlaceBookItemDistanceEntry(final PlaceBookItem p, final double distance)
	{
		super(p);
		this.distance = distance;
		ownerName = p.getOwner().getName();
	}

	public double getDistance()
	{
		return distance;
	}

	public String getOwnerName()
	{
		return ownerName;
	}
}
