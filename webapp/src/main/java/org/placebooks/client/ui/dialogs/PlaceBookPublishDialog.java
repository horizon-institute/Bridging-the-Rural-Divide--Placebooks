package org.placebooks.client.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.placebooks.client.PlaceBooks;
import org.placebooks.client.controllers.ItemController;
import org.placebooks.client.model.Item.Type;
import org.placebooks.client.model.PlaceBook;
import org.placebooks.client.ui.UIMessages;
import org.placebooks.client.ui.items.frames.PlaceBookItemFrame;
import org.placebooks.client.ui.pages.PlaceBookPage;
import org.placebooks.client.ui.views.PageView;
import org.placebooks.client.ui.views.PagesView;
import org.wornchaos.client.server.AsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PlaceBookPublishDialog extends PlaceBookDialog
{
	interface PlaceBookPublishUiBinder extends UiBinder<Widget, PlaceBookPublishDialog>
	{
	}

	private static final UIMessages uiMessages = GWT.create(UIMessages.class);

	private static final PlaceBookPublishUiBinder uiBinder = GWT.create(PlaceBookPublishUiBinder.class);

	private static final String[] activities = new String[] { "Walking", "Running", "Driving", "Road biking",
																"Mountain biking", "Hiking", "Motorcycling",
																"Sightseeing", "Trail running", "Alpine skiing",
																"Kayaking / Canoeing", "Geocaching",
																"Cross-country skiing", "Flying", "Mountaineering",
																"Sailing", "Backpacking", "Train",
																"Back-country skiing", "Offroading", "Roller skating",
																"Snowshoeing", "ATV/Offroading", "Boating",
																"Relaxation", "Horseback Riding", "Photography",
																"Snowboarding", "Ice skating", "Snowmobiling",
																"Hang Gliding/Paragliding", "Fly-fishing",
																"Romantic Getaway", "Skateboarding", "Bird Watching",
																"Rock Climbing", "Paddleboarding", "Fishing", "Other:" };

	@UiField
	TextBox activity;

	@UiField
	TextArea description;

	@UiField
	Panel leftButton;

	@UiField
	ListBox activityList;

	@UiField
	TextBox location;

	@UiField
	Image placebookImage;

	@UiField
	Button publish;

	@UiField
	Panel rightButton;

	@UiField
	TextBox title;

	private final List<PlaceBookItemFrame> imageItems = new ArrayList<PlaceBookItemFrame>();

	private int index = 0;

	private final PlaceBook placebook;

	private boolean allowPublish = true;

	public PlaceBookPublishDialog(final PagesView canvas)
	{
		setWidget(uiBinder.createAndBindUi(this));
		setTitle(uiMessages.publishPlaceBook());
		title.setMaxLength(64);
		title.setText(canvas.getPlaceBook().getMetadata("title", uiMessages.noTitle()));
		description.setText(canvas.getPlaceBook().getMetadata("description", ""));
		location.setText(canvas.getPlaceBook().getMetadata("location", ""));

		activity.setVisible(false);

		boolean found = false;
		for (final String item : activities)
		{
			activityList.addItem(item);
			if (!found && item.equals(canvas.getPlaceBook().getMetadata("activity", "")))
			{
				activityList.setSelectedIndex(activityList.getItemCount() - 1);
				found = true;
			}
		}

		if (!found && !"".equals(canvas.getPlaceBook().getMetadata("activity", "")))
		{
			activity.setText(canvas.getPlaceBook().getMetadata("activity", ""));
			activityList.setSelectedIndex(activityList.getItemCount() - 1);
			activity.setVisible(true);
		}

		placebook = canvas.getPlaceBook();

		for (final PageView page : canvas.getPages())
		{
			for (final PlaceBookItemFrame frame : page.getItems())
			{
				if (frame.getItem().is(Type.ImageItem) && frame.getItem().getHash() != null)
				{
					imageItems.add(frame);
				}

				if ((frame.getItem().is(Type.ImageItem, Type.VideoItem, Type.AudioItem)) && frame.getItem().getHash() == null)
				{
					allowPublish = false;
					setError(uiMessages.uploadRequired());
				}
			}
		}

		refresh();
	}

	public void addClickHandler(final ClickHandler clickHandler)
	{
		publish.addClickHandler(clickHandler);
	}

	@UiHandler("activityList")
	void activitySelect(final ChangeEvent event)
	{
		activity.setVisible(activityList.getItemText(activityList.getSelectedIndex()).equals("Other:"));
		refresh();
	}

	@UiHandler(value = { "location", "activity", "title" })
	void handleChangeValue(final KeyUpEvent event)
	{
		refresh();
	}

	@UiHandler("leftButton")
	void handleLeftClick(final ClickEvent event)
	{
		if (index > 0)
		{
			index--;
			refresh();
		}
	}

	@UiHandler("publish")
	void handlePublish(final ClickEvent event)
	{
		if (index >= 0 && index < imageItems.size())
		{
			final PlaceBookItemFrame frame = imageItems.get(index);
			placebook.getMetadata().put("placebookImage", frame.getItem().getHash());
		}

		placebook.getMetadata().put("title", title.getText());
		placebook.getMetadata().put("location", location.getText());
		if (activityList.getItemText(activityList.getSelectedIndex()).equals("Other:"))
		{
			placebook.getMetadata().put("activity", activity.getText());
		}
		else
		{
			placebook.getMetadata().put("activity", activityList.getItemText(activityList.getSelectedIndex()));
		}
		placebook.getMetadata().put("description", description.getText());

		PlaceBooks.getServer().publishPlaceBook(placebook, new AsyncCallback<PlaceBook>()
		{			
			@Override
			public void onSuccess(PlaceBook binder)
			{
				PlaceBooks.goTo(new PlaceBookPage(binder));
			}
		});
	}

	@UiHandler("rightButton")
	void handleRightClick(final ClickEvent event)
	{
		if (index < imageItems.size())
		{
			index++;
			refresh();
		}
	}

	private void refresh()
	{
		if (index >= 0 && index < imageItems.size())
		{
			final PlaceBookItemFrame frame = imageItems.get(index);
			placebookImage.setUrl(ItemController.getURL(frame.getItem(), null));
		}

		publish.setEnabled(allowPublish
				&& !title.getText().trim().isEmpty()
				&& (!activityList.getItemText(activityList.getSelectedIndex()).equals("Other:") || !activity.getText()
						.trim().isEmpty()) && !location.getText().trim().isEmpty());

		rightButton.setVisible(index < imageItems.size());
		leftButton.setVisible(index > 0);
	}
}
