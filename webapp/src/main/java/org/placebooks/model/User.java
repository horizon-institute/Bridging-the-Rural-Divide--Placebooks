package org.placebooks.model;

import static javax.persistence.CascadeType.ALL;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.placebooks.model.json.JsonIgnore;
import org.placebooks.model.json.JsonDownloadIgnore;

@Entity
public class User
{
	@Column(unique = true)
	private String email;

	@JsonIgnore
	@ManyToMany
	private Collection<User> friends = new HashSet<User>();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;

	@JsonDownloadIgnore
	@OneToMany(mappedBy = "user", cascade = ALL)
	private Collection<LoginDetails> loginDetails = new HashSet<LoginDetails>();

	@JsonDownloadIgnore	
	@OneToMany(mappedBy = "owner", cascade = ALL)
	private Collection<PlaceBookGroup> groups = new HashSet<PlaceBookGroup>();

	private String name;

	@JsonIgnore
	private String passwordHash;

	@JsonIgnore
	@OneToMany(mappedBy = "owner", cascade = ALL)
	private Collection<PlaceBookBinder> placebookBinders = new HashSet<PlaceBookBinder>();

	public User(final String name, final String email, final String passwordHash)
	{
		this.name = name;
		this.email = email.toLowerCase();
		this.passwordHash = passwordHash;
	}

	User()
	{

	}

	public void add(final LoginDetails loginDetail)
	{
		loginDetails.add(loginDetail);
	}
	
	public void add(final PlaceBookGroup group)
	{
		for(PlaceBookGroup altGroup: groups)
		{
			if(altGroup.getId().equals(group.getId()))
			{
				return;
			}
		}
		groups.add(group);
	}

	public void add(final PlaceBookBinder placebookBinder)
	{
		placebookBinders.add(placebookBinder);
	}

	public void add(final User friend)
	{
		friends.add(friend);
	}

	public boolean contains(final LoginDetails details)
	{
		return loginDetails.contains(details);
	}

	public String getEmail()
	{
		return email;
	}

	public Iterable<User> getFriends()
	{
		return friends;
	}

	public String getKey()
	{
		return id;
	}

	public Iterable<LoginDetails> getLoginDetails()
	{
		return loginDetails;
	}

	public LoginDetails getLoginDetails(final String service)
	{
		for (final LoginDetails login : loginDetails)
		{
			if (login.getService().equals(service)) { return login; }
		}
		return null;
	}

	public String getName()
	{
		return name;
	}

	public String getPasswordHash()
	{
		return passwordHash;
	}

	public Iterable<PlaceBookBinder> getPlacebookBinders()
	{
		return placebookBinders;
	}

	public void remove(final LoginDetails loginDetails)
	{
		this.loginDetails.remove(loginDetails);
	}

	public void remove(final PlaceBookBinder placebookBinder)
	{
		placebookBinders.remove(placebookBinder);
	}

	public void remove(final User friend)
	{
		friends.remove(friend);
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public Iterable<PlaceBookGroup> getGroups()
	{
		return groups;
	}
}
