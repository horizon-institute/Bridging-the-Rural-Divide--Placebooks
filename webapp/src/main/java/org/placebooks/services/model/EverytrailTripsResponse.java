/**
 * 
 */
package org.placebooks.services.model;

import java.util.Vector;

import org.w3c.dom.Node;

/**
 * @author pszmp
 * 
 */
public class EverytrailTripsResponse
{
	private String status;
	private Vector<Node> trips;

	/**
	 * 
	 */
	public EverytrailTripsResponse(final String status, final Vector<Node> trips)
	{
		this.status = status;
		this.trips = trips;
	}

	public String getStatus()
	{
		return status;
	}

	public Vector<Node> getTrips()
	{
		return trips;
	}
}
