package placebooks.model;

import java.util.*;
import javax.jdo.annotations.*;

import org.apache.log4j.*;

import com.vividsolutions.jts.geom.Geometry;


@PersistenceCapable
@Extension(vendorName="datanucleus", key="mysql-engine-type", value="MyISAM")
public class PlaceBook
{

  	private static final Logger log = 
		Logger.getLogger(PlaceBook.class.getName());

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.UUIDHEX)
	private String key;

	@Persistent
	private String owner;
	
	@Persistent
	private Date timestamp;

	@Persistent
	private Geometry geom; // Pertaining to the PlaceBook

	@Persistent
	private List<PlaceBookItem> items = new ArrayList<PlaceBookItem>();

	// The PlaceBook's configuration data
	@Persistent
	private HashMap<String, String> parameters;

	// Make a new PlaceBook
	public PlaceBook(String owner, Geometry geom)
	{
		this.owner = owner;
		this.geom = geom;
		parameters = new HashMap<String, String>();
		parameters.put("test", "testing");

		this.timestamp = new Date();
	}
	
	public PlaceBook(String owner, Geometry geom, List<PlaceBookItem> items)
	{
		this(owner, geom);
		setItems(items);
	}

	public void setItems(List<PlaceBookItem> items)
	{
		this.items.clear();
		this.items.addAll(items);
	}

	public List<PlaceBookItem> getItems()
	{
		return Collections.unmodifiableList(items);
	}

	public void addItem(PlaceBookItem item) 
	{
  		items.add(item);
	}

	public boolean removeItem(PlaceBookItem item)
	{
		return items.remove(item);
	}

	public void setItemKeys()
	{
		for (PlaceBookItem pbi : items) 
			pbi.setPBKey(key);
	}

	public String getKey() { return key; }

	public void setOwner(String owner) { this.owner = owner; }
	public String getOwner() { return owner; }

	public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
	public Date getTimestamp() { return timestamp; }

	public void setGeometry(Geometry geom) { this.geom = geom; }
	public Geometry getGeometry() { return geom; }


}
