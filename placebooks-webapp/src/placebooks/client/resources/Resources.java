package placebooks.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the entire application.
 */
public interface Resources extends ClientBundle
{
	public static final Resources INSTANCE = GWT.create(Resources.class);

	ImageResource dropMenuIcon();

	ImageResource folder();

	ImageResource image();

	ImageResource map();
	
	ImageResource placebook();	
	
	ImageResource arrow_left();
	
	ImageResource arrow_right();	
	
	ImageResource add();
	
	ImageResource book();	
	
	ImageResource chrome();
	
	ImageResource television();
	
	ImageResource video();
	
	ImageResource save();
	
	ImageResource audio();	
	
	ImageResource everytrail();
	
	ImageResource youtube();	
	
	ImageResource phone_Android();
	
	ImageResource book_open();	
	
	ImageResource movies();

	ImageResource music();

	ImageResource picture();

	ImageResource progress();

	ImageResource progress2();

	ImageResource progress3();

	@Source("PlaceBook.css")
	PlaceBookCSS style();

	ImageResource text();

	ImageResource web_page();

	ImageResource zoom_in();

	ImageResource zoom_out();
}
