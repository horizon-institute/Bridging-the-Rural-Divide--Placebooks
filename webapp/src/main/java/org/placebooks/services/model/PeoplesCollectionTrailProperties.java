package org.placebooks.services.model;


/**
 * Class to encapsulate response from Peoples Collection API for Trail Properties
 * 
 * @author pszmp
 * 
 */
public class PeoplesCollectionTrailProperties
{

	PeoplesCollectionTrailCentroid centroid;
	int[] items;
	String title;
	String titlecym;

	String description;
	String descriptioncym;

	String difficulty;
	String trailtype;

	double distance;
	int userid;

	String tags;
	String tagscym;

	int trailid;

	public PeoplesCollectionTrailProperties()
	{

	}

	public PeoplesCollectionTrailProperties(final PeoplesCollectionTrailCentroid centroid, final int[] items,
			final String title, final String titlecym, final String description, final String descriptioncym,
			final String difficulty, final String trailtype, final double distance, final int userid,
			final String tags, final String tagscym, final int trailid)
	{
		this.centroid = centroid;
		this.items = items;
		this.title = title;
		this.titlecym = titlecym;

		this.description = description;
		this.descriptioncym = descriptioncym;

		this.difficulty = difficulty;
		this.trailtype = trailtype;

		this.distance = distance;
		this.userid = userid;

		this.tags = tags;
		this.tagscym = tagscym;

		this.trailid = trailid;
	}

	public PeoplesCollectionTrailCentroid GetCentroid()
	{
		return centroid;
	}

	public String GetDecriptionCym()
	{
		return descriptioncym;
	}

	public String GetDescription()
	{
		return description;
	}

	public String GetDifficulty()
	{
		return difficulty;
	}

	public double GetDistance()
	{
		return distance;
	}

	public int[] GetItems()
	{
		return items;
	}

	public String GetTags()
	{
		return tags;
	}

	public String GetTagsCym()
	{
		return tagscym;
	}

	public String GetTitle()
	{
		return title;
	}

	public String GetTitleCym()
	{
		return titlecym;
	}

	public int GetTrailId()
	{
		return trailid;
	}

	public String GetTrailType()
	{
		return trailtype;
	}

	public int GetUserId()
	{
		return userid;
	}
}
