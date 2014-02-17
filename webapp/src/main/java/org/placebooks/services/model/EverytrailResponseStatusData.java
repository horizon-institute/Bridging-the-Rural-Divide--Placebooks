/**
 * 
 */
package org.placebooks.services.model;

/**
 * @author pszmp Used within EverytrailHelper to pass the response data back to calling methods when
 *         parsing a response
 */
public class EverytrailResponseStatusData
{
	private String status;
	private String value;

	public EverytrailResponseStatusData(final String status, final String value)
	{
		this.status = status;
		this.value = value;
	}

	public String getStatus()
	{
		return status;
	}

	public String getValue()
	{
		return value;
	}
}