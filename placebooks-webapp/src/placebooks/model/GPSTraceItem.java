package placebooks.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import placebooks.model.jaxb.GPX10.Gpx;
import placebooks.model.jaxb.GPX11.GpxType;
import placebooks.model.jaxb.GPX11.RteType;
import placebooks.model.jaxb.GPX11.TrkType;
import placebooks.model.jaxb.GPX11.TrksegType;
import placebooks.model.jaxb.GPX11.WptType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

@Entity
public class GPSTraceItem extends PlaceBookItem
{
	@JsonIgnore
	@Lob
	private String trace;
	
	private String hash;

	GPSTraceItem()
	{
	}

	public GPSTraceItem(final GPSTraceItem g)
	{
		super(g);
		setTrace(new String(g.getTrace()));
	}

	public GPSTraceItem(final User owner, final URL sourceURL, final String trace)
	{
		// Geometry is set from calculating the GPX boundaries
		super(owner, null, sourceURL);
		setTrace(trace);
	}
	
	@Override
	public void appendConfiguration(final Document config, final Element root)
	{
		try
		{
			final StringReader reader = new StringReader(trace);
			final InputSource source = new InputSource(reader);
			final DocumentBuilder builder = 
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(source);
			reader.close();

			final Element item = getConfigurationHeader(config);
			final Element data = config.createElement("data");
			final Node traceNode = 
				config.importNode(document.getDocumentElement(), true);
			data.appendChild(traceNode);
			item.appendChild(data);
			root.appendChild(item);
		}
		catch (Exception e)
		{
			log.info(e.getMessage(), e);
		}
	}

	@Override
	public GPSTraceItem deepCopy()
	{
		return new GPSTraceItem(this);
	}
	
	@Override
	public void deleteItemData()
	{
	}

	@Override
	public String getEntityName()
	{
		return GPSTraceItem.class.getName();
	}

	public String getHash()
	{
		return hash;
	}

	// @Persistent
	// @Column(jdbcType = "CLOB")
	public String getTrace()
	{
		return trace;
	}
	
	public void readTrace(final InputStream is) throws Exception
	{
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		final StringWriter writer = new StringWriter();
		int data;
		while((data = reader.read()) != -1)
		{
			writer.write(data);
		}
		reader.close();
		writer.close();
			
		setTrace(writer.toString());
	}

	@SuppressWarnings("unchecked")
	private void setTrace(final String trace)
	{
		if (trace == null)
		{
			this.trace = null;
			return;
		}

		this.trace = trace;

		Geometry bounds = null;
		float minLat = Float.POSITIVE_INFINITY;
		float maxLat = Float.NEGATIVE_INFINITY;
		float minLon = Float.POSITIVE_INFINITY;
		float maxLon = Float.NEGATIVE_INFINITY;

		final WKTReader wktReader = new WKTReader();

		try 
		{
			// GPX 1.1 spec

			GpxType gpx = null;
			Unmarshaller u = JAXBContext.newInstance("placebooks.model.jaxb.GPX11")
										.createUnmarshaller();
			JAXBElement<GpxType> root = 
				(JAXBElement<GpxType>)u.unmarshal(new StreamSource(
												  new StringReader(this.trace))
				);
			gpx = root.getValue();
			for (TrkType track : gpx.getTrk()) 
			{
				for (TrksegType seg : track.getTrkseg())
				{
					for (WptType wpt : seg.getTrkpt())
					{
						minLat = Math.min(minLat, wpt.getLat().floatValue());
						maxLat = Math.max(maxLat, wpt.getLat().floatValue());
						minLon = Math.min(minLon, wpt.getLon().floatValue());
						maxLon = Math.max(maxLon, wpt.getLon().floatValue());
					}
				}
			}
			// Wpt
			for (WptType wpt : gpx.getWpt())
			{
				minLat = Math.min(minLat, wpt.getLat().floatValue());
				maxLat = Math.max(maxLat, wpt.getLat().floatValue());
				minLon = Math.min(minLon, wpt.getLon().floatValue());
				maxLon = Math.max(maxLon, wpt.getLon().floatValue());
			}
			// Rte
			for (RteType rte : gpx.getRte())
			{
				for (WptType wpt : rte.getRtept())
				{
					minLat = Math.min(minLat, wpt.getLat().floatValue());
					maxLat = Math.max(maxLat, wpt.getLat().floatValue());
					minLon = Math.min(minLon, wpt.getLon().floatValue());
					maxLon = Math.max(maxLon, wpt.getLon().floatValue());
				}
			}

		} 
		catch (final Throwable e) 
		{
			// GPX 1.0 spec
			log.info("Failed to read GPX as GPX1.1, trying 1.0");

			try 
			{
				Unmarshaller u = 
					JAXBContext.newInstance("placebooks.model.jaxb.GPX10")
							   .createUnmarshaller();
				// GPX 1.0 is anonymous
				Object root =
					u.unmarshal(new StreamSource(new StringReader(this.trace)));
				log.info(root.getClass());
				Gpx gpx = (Gpx)root;

				// Trk
				for (Gpx.Trk track : gpx.getTrk())
				{
					for (Gpx.Trk.Trkseg seg : track.getTrkseg())
					{
						for (Gpx.Trk.Trkseg.Trkpt pt : seg.getTrkpt())
						{
							minLat = Math.min(minLat, pt.getLat().floatValue());
							maxLat = Math.max(maxLat, pt.getLat().floatValue());
							minLon = Math.min(minLon, pt.getLon().floatValue());
							maxLon = Math.max(maxLon, pt.getLon().floatValue());
						}
					}
				}
				// Wpt
				for (Gpx.Wpt wpt : gpx.getWpt())
				{
					minLat = Math.min(minLat, wpt.getLat().floatValue());
					maxLat = Math.max(maxLat, wpt.getLat().floatValue());
					minLon = Math.min(minLon, wpt.getLon().floatValue());
					maxLon = Math.max(maxLon, wpt.getLon().floatValue());
				}
				// Rte
				for (Gpx.Rte rte : gpx.getRte())
				{
					for (Gpx.Rte.Rtept rpt : rte.getRtept())
					{
						minLat = Math.min(minLat, rpt.getLat().floatValue());
						maxLat = Math.max(maxLat, rpt.getLat().floatValue());
						minLon = Math.min(minLon, rpt.getLon().floatValue());
						maxLon = Math.max(maxLon, rpt.getLon().floatValue());
					}
				}

			} 
			catch (final Exception e_) 
			{
				log.error(e_.toString(), e);
			}

			try
			{
				bounds = wktReader.read("POLYGON ((" + minLat + " " + minLon + ", "
										+ minLat + " " + maxLon + ", "
										+ maxLat + " " + maxLon + ", "
										+ maxLat + " " + minLon + ", "
										+ minLat + " " + minLon + "))");
			}
			catch (final Throwable e_)
			{
				log.error(e_.toString(), e);
			}


		}
		setGeometry(bounds.getBoundary());
		
		try
		{
			final MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(trace.getBytes());
			
			hash = String.format("%032x", new BigInteger(1, md.digest()));			
		}
		catch(Exception e)
		{
			log.error("Failed to hash gpx", e);
		}
	}

	@Override
	/* (non-Javadoc)
	 * @see placebooks.model.PlaceBookItem#udpate(PlaceBookItem)
	 */
	public void updateItem(IUpdateableExternal updateItem)
	{
		GPSTraceItem item = (GPSTraceItem) updateItem;
		super.updateItem(item);
		if(item instanceof GPSTraceItem)
		{
			GPSTraceItem gpsitem = item; 
			if(gpsitem.getTrace() != null && !gpsitem.getTrace().trim().equals(""))
			{
				setTrace((item).getTrace());
			}
		}
	}

}
