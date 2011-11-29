package placebooks.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.Date;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;

import org.apache.log4j.Logger;

@Entity
@JsonAutoDetect(fieldVisibility = Visibility.ANY, 
				getterVisibility = Visibility.NONE)
public class PlaceBookBinder extends BoundaryGenerator
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;

	@ManyToOne
	private User owner;

	@Temporal(TIMESTAMP)
	private Date timestamp;

	@JsonIgnore
	protected static final Logger log = 
		Logger.getLogger(PlaceBookBinder.class.getName());

	private Geometry geom; // Super-geometry of all PlaceBooks in this binder

	// We are relying on ArrayList to preserve PlaceBook ordering
	@OneToMany(mappedBy = "placebookbinder", cascade = ALL)
	private List<PlaceBook> pages = new ArrayList<PlaceBook>();

	@ElementCollection
	private Map<String, String> metadata = new HashMap<String, String>();

	@ElementCollection
	private Map<User, Permission> perms = new HashMap<User, Permission>();

	@JsonIgnore
	private String permsUsers;

	public enum Permission
	{
		R("r"), W("w"), R_W("r+w"); 
		
		private String perms;
		
		private Permission(final String perms)
		{
			this.perms = perms;
		}

		public final String toString()
		{
			return perms;
		}
	}

	public enum State
	{
		UNPUBLISHED(0),
		PUBLISHED(1);
		
		private int value;
		
		private static final Map<Integer, State> lu = 
			new HashMap<Integer, State>();

		static
		{
			for (State s : EnumSet.allOf(State.class))
				lu.put(s.getValue(), s);
		}

		private State(int value)
		{
			this.value = value;
		}

		public int getValue()
		{
			return value;
		}

		public static State get(int value)
		{
			return lu.get(value);
		}

		public final toString()
		{
			String.valueOf(value);
		}

	}

	public PlaceBookBinder()
	{
		this.state = State.UNPUBLISHED;
		this.timestamp = new Date();
		geom = null;
	}

	public PlaceBookBinder(final User owner)
	{
		this();
		this.owner = owner;
		if (owner != null)
		{
			this.owner.add(this);
			perms.put(owner, Permission.R_W);
			permsUsers = getPermissionsAsString();			
		}

		log.info("Created new PlaceBookBinder: timestamp=" 
				 + this.timestamp.toString());

	}

	public PlaceBookBinder(final PlaceBookBinder p)
	{
		this.owner = p.getOwner();
		if (this.owner != null)
			this.owner.add(this);

		perms.putAll(p.getPermissions());
		permsUsers = getPermissionsAsString();

		if (p.getGeometry() != null)
			this.geom = (Geometry)p.getGeometry().clone();
		else
			this.geom = null;
		
		this.timestamp = (Date)p.getTimestamp().clone();

		this.metadata = new HashMap<String, String>(p.getMetadata());

        for (final PlaceBook page : p.getPlaceBooks())
			this.addPlaceBook(page.deepCopy());

        log.info("Created copy of PlaceBookBinder; old key = " + p.getKey());

	}


	public void calcBoundary()
	{
		final Set<Geometry> geoms = new HashSet<Geometry>();
		for (final PlaceBook p : getPlaceBooks())
		{
			p.calcBoundary();
			geoms.add(p.getGeometry());
		}

		final Geometry geom = calcBoundary(geoms);
		this.geom = geom;
		log.info("calcBoundary()= " + this.geom);
	}

	public Element createConfigurationRoot(final Document config)
	{
		log.info("PlaceBookBinder.createConfigurationRoot(), key=" 
				 + this.getKey());
		final Element root = config.createElement(PlaceBook.class.getName());
		root.setAttribute("key", this.getKey());
		root.setAttribute("owner", this.getOwner().getKey());
		root.setAttribute("state", this.getState().toString());

		if (!perms.isEmpty())
		{
			log.info("Setting perms=" + this.getPermissionsAsString());
			final Element permissions = config.createElement("permissions");
			permissions.appendChild(
				config.createTextNode(this.getPermissionsAsString());
			);
			root.appendChild(permissions);
		}

		if (getTimestamp() != null)
		{
			log.info("Setting timestamp=" + this.getTimestamp().toString());
			final Element timestamp = config.createElement("timestamp");
			timestamp.appendChild(
				config.createTextNode(this.getTimestamp().toString())
			);
			root.appendChild(timestamp);
		}

		if (getGeometry() != null)
		{
			log.info("Setting geometry=" + this.getGeometry().toText());
			final Element geometry = config.createElement("geometry");
			geometry.appendChild(
				config.createTextNode(this.getGeometry().toText())
			);
			root.appendChild(geometry);
		}

		if (!metadata.isEmpty())
		{
			log.info("Writing metadata to config");
			final Element sElem = config.createElement("metadata");
			log.info("metadata set size = " + metadata.size());
			for (final Map.Entry<String, String> e : metadata.entrySet())
			{
				log.info("Metadata element key, value=" + e.getKey().toString()
						 + ", " + e.getValue().toString());
				final Element elem = 	
					config.createElement(e.getKey().toString());
				elem.appendChild(config.createTextNode(
					e.getValue().toString())
				);
				sElem.appendChild(elem);
			}

			root.appendChild(sElem);
		}

		return root;
	}

	public String getPackagePath() 
	{
		return PropertiesSingleton
					.get(this.getClass().getClassLoader())
					.getProperty(PropertiesSingleton.IDEN_PKG, "") + "/" 
																   + getKey();
	}

	private final String getPermissionsAsString()
	{
		final StringBuffer l = new StringBuffer();
		final Iterator<User> i = getPermissions().keySet().iterator();
		while (i.hasNext())
		{
			final User u = i.next();
			l.append("'" + u.getEmail() + "'=" + getPermission(u).toString());
			if (i.hasNext())
				l.append(",");
		}
		return l.toString();
	}

	public void setPermission(final User user, final Permission p)
	{
		if (perms.get(user) != null)
			perms.remove(user);

		perms.put(user, p);
		permsUsers = getPermissionsAsString();		
	}

	public void removePermission(final User user)
	{
		perms.remove(user);
		permsUsers = getPermissionsAsString();		
	}

	public final Permission getPermission(final User user)
	{
		return perms.get(user);
	}

	public final Map<User, Permission> getPermissions()
	{
		return Collections.unmodifiableMap(perms);
	}

	public void addPlaceBook(final PlaceBook page)
	{
		pages.add(page);
		p.setPlaceBookBinder(this);
	}

	public List<PlaceBook> getPlaceBooks()
	{
		return Collections.unmodifiableList(pages);
	}

	public boolean removePlaceBook(final PlaceBook page)
	{
		page.setPlaceBookBinder(null);
		return pages.remove(page);
	}

	public void setPlaceBooks(final List<PlaceBook> pages)
	{
		this.pages.clear();
		this.pages.addAll(pages);
	}

	public Map<String, String> getMetadata()
	{
		return Collections.unmodifiableMap(metadata);
	}

	public String getMetadataValue(final String key)
	{
		return metadata.get(key);
	}

	public State getState()
	{
		return state;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public boolean hasMetadata()
	{
		return (!metadata.isEmpty());
	}
	
	public void setGeometry(final Geometry geom)
	{
		this.geom = geom;
	}


	public void setOwner(final User owner)
	{
		this.owner = owner;
	}

	public void setState(State state)
	{
		this.state = state;
	}

	public void setTimestamp(final Date timestamp)
	{
		this.timestamp = timestamp;
	}

	public Geometry getGeometry()
	{
		return geom;
	}

	public String getKey()
	{
		return id;
	}

	public User getOwner()
	{
		return owner;
	}

}
