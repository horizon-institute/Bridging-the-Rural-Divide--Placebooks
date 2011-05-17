package placebooks.client.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class PlaceBook extends JavaScriptObject
{
	public static final native PlaceBook parse(final String json) /*-{ return eval('(' + json + ')'); }-*/;

	protected PlaceBook()
	{
	}

	public final native String getGeometry() /*-{ return this.geom; }-*/;

	public final native JsArray<PlaceBookItem> getItems() /*-{ return this.items; }-*/;

	public final native String getKey() /*-{ return this.id; }-*/;

	public final native String getMetadata(String name) /*-{ return this.metadata[name]; }-*/;

	public final native User getOwner() /*-{ return this.owner; }-*/;

	public final native void setMetadata(String name, String value) /*-{ this.metadata[name] = value; }-*/;
	
	public final native boolean hasMetadata(String name) /*-{ return name in this.metadata; }-*/;
	
	public final native void removeItem(PlaceBookItem item) /*-{
		var idx = this.items.indexOf(item);
		if (idx != -1)
		{
			this.items.splice(idx, 1);
		}
	}-*/;

	// @Persistent
	// private Date timestamp;

	// @Persistent(dependent="true")
	// private PlaceBookIndex index;
}
