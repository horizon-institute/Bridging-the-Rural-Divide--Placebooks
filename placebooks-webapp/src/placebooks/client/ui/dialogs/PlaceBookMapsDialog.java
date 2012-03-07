package placebooks.client.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import placebooks.client.model.LoginDetails;
import placebooks.client.ui.elements.PlaceBookController;
import placebooks.client.ui.images.markers.Markers;
import placebooks.client.ui.items.MapItem;
import placebooks.client.ui.items.PlaceBookItemWidget;
import placebooks.client.ui.items.frames.PlaceBookItemFrame;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

public class PlaceBookMapsDialog extends PlaceBookDialog
{
	interface PlaceBookMapsDialogUiBinder extends UiBinder<Widget, PlaceBookMapsDialog>
	{
	}

	private static PlaceBookMapsDialogUiBinder uiBinder = GWT.create(PlaceBookMapsDialogUiBinder.class);

	@UiField
	Panel mapPanel;

	@UiField
	ListBox mapSelect;
	
	@UiField
	Label mapLabel;

	@UiField
	CheckBox markerShow;
	
	@UiField
	Panel markerListPanel;
	
	private MapItem map;

	private final PlaceBookItemWidget item;
	private final List<PlaceBookItemFrame> mapItems;

	private final PlaceBookController controller;
	
	private final CellList<ImageResource> markers;

	public PlaceBookMapsDialog(final PlaceBookItemWidget item, final List<PlaceBookItemFrame> mapItems,
			final PlaceBookController controller)
	{
		setWidget(uiBinder.createAndBindUi(this));
		this.controller = controller;
		this.item = item;
		this.mapItems = mapItems;
		setTitle("Locate " + item.getItem().getMetadata("title", "Item") + " on Map");
		onInitialize();

		final int mapPage = item.getItem().getParameter("mapPage", -1);

		if (mapPage != -1)
		{
			final String mapPageString = Integer.toString(mapPage);
			for (int index = 0; index < mapSelect.getItemCount(); index++)
			{
				if (mapPageString.equals(mapSelect.getValue(index)))
				{
					mapSelect.setSelectedIndex(index);
				}
			}
		}

		markers = new CellList<ImageResource>(new ImageResourceCell());
		
		List<ImageResource> markerList = new ArrayList<ImageResource>();
		for(ResourcePrototype resource: Markers.IMAGES.getResources())
		{
			if(resource instanceof ImageResource)
			{
				markerList.add((ImageResource) resource);
			}
		}
		
		markers.setRowData(markerList);
		
		final SingleSelectionModel<ImageResource> selectionModel = new SingleSelectionModel<ImageResource>();
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler()
		{
			@Override
			public void onSelectionChange(SelectionChangeEvent event)
			{
				final ImageResource marker = selectionModel.getSelectedObject();
				try
				{	
					int markerIndex = (int)marker.getName().charAt(6);
					item.getItem().setParameter("marker", markerIndex);
				}
				catch(Exception e)
				{
					item.getItem().removeParameter("marker");
				}					
				markerShow(null);
				if(map != null)
				{
					map.refreshMarkers();
				}
				controller.markChanged();
				item.refresh();
			}
		});
		markers.setSelectionModel(selectionModel);
		
		selectionModel.setSelected(item.getItem().getMarkerImage(), true);
		
		markerListPanel.add(markers);
		
		//markerSelect.setSelectedIndex(item.getItem().getParameter("marker", 0));
		markerShow.setValue(item.getItem().getParameter("markerShow", 0) == 1);
		
		selectMap(mapPage);		
	}

	@UiHandler("markerShow")
	void markerShow(final ValueChangeEvent<Boolean> event)
	{
		if(markerShow.getValue())
		{
			item.getItem().setParameter("markerShow", 1);
		}
		else
		{
			item.getItem().removeParameter("markerShow");
		}
		controller.markChanged();
		item.refresh();
	}
	
	@UiHandler("mapSelect")
	void mapSelected(final ChangeEvent event)
	{
		final int index = mapSelect.getSelectedIndex();
		final String value = mapSelect.getValue(index);
		if ("".equals(value))
		{
			selectMap(-1);
		}
		else
		{
			selectMap(Integer.parseInt(value));
		}

		controller.markChanged();
		// item.getItem().setMetadata("mapItemID", mapItems.iterator().next().getKey());
		// item.getItemWidget().refresh();
		// controller.getContext().markChanged();
	}

	private void onInitialize()
	{
		mapSelect.clear();
		mapSelect.addItem("Not On Any Map", "");

		mapPanel.clear();

		for (final PlaceBookItemFrame item : mapItems)
		{
			mapSelect.addItem("On " + item.getItem().getMetadata("title", "Untitled") + " Map (page "
					+ (item.getColumn().getPage().getIndex() + 1) + ")", Integer.toString(item.getColumn().getPage().getIndex()));
		}
	}

	private void selectMap(final int page)
	{
		if (page == -1)
		{
			item.getItem().removeParameter("mapPage");
		}
		else
		{
			item.getItem().setParameter("mapPage", page);
		}

		mapPanel.clear();

		mapPanel.setVisible(page != -1);
		mapLabel.setVisible(page != -1);
		markers.setVisible(page != -1);
		markerShow.setVisible(page != -1);

		if (item.getItem().getGeometry() == null)
		{
			mapLabel.setText("Click to Place " + item.getItem().getMetadata("title", "Untitled") + " on Map:");
		}
		else
		{
			mapLabel.setText("Click to Move " + item.getItem().getMetadata("title", "Untitled") + " on Map:");
		}

		if (page != -1)
		{
			for (final PlaceBookItemFrame mapItem : mapItems)
			{
				if (page == mapItem.getColumn().getPage().getIndex())
				{
					map = new MapItem(mapItem.getItem(), controller);
					map.refreshMarkers();

					mapPanel.add(map);

					map.moveMarker(item.getItem(), new ChangeHandler()
					{
						@Override
						public void onChange(final ChangeEvent event)
						{
							mapItem.getItemWidget().refresh();
						}
					});
					break;
				}
			}
		}

		center();
	}
}
