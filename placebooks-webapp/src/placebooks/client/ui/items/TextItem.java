package placebooks.client.ui.items;

import placebooks.client.model.PlaceBookItem;
import placebooks.client.ui.elements.PlaceBookController;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class TextItem extends PlaceBookItemWidget
{
	private final FlowPanel rootPanel = new FlowPanel();
	private final SimplePanel textPanel = new SimplePanel();
	private final Image markerImage = new Image();

	TextItem(final PlaceBookItem item, final PlaceBookController handler)
	{
		super(item, handler);

		rootPanel.add(markerImage);
		rootPanel.add(textPanel);
		
		markerImage.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				controller.goToPage(getItem().getParameter("mapPage"));				
			}
		});
		
		initWidget(rootPanel);
	}

	@Override
	public void refresh()
	{
		if(getItem().showMarker())
		{
			markerImage.setResource(getItem().getMarkerImage());
			markerImage.getElement().getStyle().setFloat(com.google.gwt.dom.client.Style.Float.LEFT);
			markerImage.getElement().getStyle().setProperty("margin", "0 8px 0 0");					
		}
		markerImage.setVisible(getItem().showMarker());
		textPanel.getElement().setInnerHTML(item.getText());
	}

	@Override
	public String resize()
	{
		super.resize();
		if (getParent() != null)
		{
			double panelWidth = 300;
			if (getParent().getParent() != null && getParent().getParent().getParent() != null)
			{
				final String panelWidthString = getParent().getParent().getParent().getElement().getStyle().getWidth();
				if (panelWidthString != null && panelWidthString.endsWith("%"))
				{
					final double percent = Double
							.parseDouble(panelWidthString.substring(0, panelWidthString.length() - 1));
					panelWidth = (900d * percent) / 100d;
				}
			}
			final double scale = getParent().getOffsetWidth() / panelWidth;
			textPanel.getElement()
					.setAttribute(	"style",
									"width: " + panelWidth
											+ "px; -webkit-transform-origin: 0% 0%; -webkit-transform: scale(" + scale
											+ ")");
			return (getOffsetHeight() * scale) + "px";
		}
		return null;
	}
}
