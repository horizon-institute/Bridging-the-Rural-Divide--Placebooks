package org.placebooks.model.json;

import org.placebooks.model.PlaceBookItem;

public class PlaceBookItemEntry extends ShelfEntry
{
	private String pbKey;

	public PlaceBookItemEntry()
	{
		super();
	}

	public PlaceBookItemEntry(final PlaceBookItem p)
	{
		super();
		setKey(p.getKey());
		setTitle(p.getMetadataValue("title"));
		setOwner(p.getOwner().getKey());
		setTimestamp(p.getTimestamp());

		pbKey = p.getPlaceBook().getKey();
	}

	public String getPBKey()
	{
		return pbKey;
	}
}
