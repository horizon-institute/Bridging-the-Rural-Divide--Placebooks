package placebooks.client.ui.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import placebooks.client.model.PlaceBook;
import placebooks.client.ui.items.PlaceBookItemWidget;
import placebooks.client.ui.items.frames.PlaceBookItemFrame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class PlaceBookColumn extends FlowPanel
{
	interface Style extends CssResource
	{
		String panel();
	    String innerPanel();
	    String panelEdge();
	}

	interface Bundle extends ClientBundle
	{
		@Source("PlaceBookPanel.css")
		Style style();
	}
	
	private static final Bundle STYLES = GWT.create(Bundle.class);
	
	protected static final double HEIGHT_PRECISION = 10000;

	private static final Comparator<PlaceBookItemFrame> orderComparator = new Comparator<PlaceBookItemFrame>()
	{
		@Override
		public int compare(final PlaceBookItemFrame o1, final PlaceBookItemFrame o2)
		{
			return o1.getItem().getParameter("order", 0) - o2.getItem().getParameter("order", 0);
		}
	};

	private final int column;

	private final FlowPanel innerPanel = new FlowPanel();

	private final List<PlaceBookItemFrame> items = new ArrayList<PlaceBookItemFrame>();

	private final int panelIndex;

	private final PlaceBook placebook;
	
	public PlaceBookColumn(final PlaceBook placebook, final int index, final int columns, final double left, final double width,
			final boolean visible)
	{
		STYLES.style().ensureInjected();
		this.placebook = placebook;
		this.panelIndex = index;
		column = index % columns;
		setStyleName(STYLES.style().panel());
		if (visible && column != 0)
		{
			addStyleName(STYLES.style().panelEdge());
		}
		getElement().getStyle().setLeft(left, Unit.PCT);
		getElement().getStyle().setWidth(width, Unit.PCT);

		innerPanel.setStyleName(STYLES.style().innerPanel());

		add(innerPanel);
	}
	
	PlaceBook getPlaceBook()
	{
		return placebook;
	}	

	public void add(final PlaceBookItemFrame item)
	{
		final int order = item.getItem().getParameter("order", items.size());
		if (order >= items.size())
		{
			items.add(item);
		}
		else
		{
			items.add(order, item);
		}
	}

	public int getIndex()
	{
		return panelIndex;
	}

	public Panel getInnerPanel()
	{
		return innerPanel;
	}

	boolean isIn(final int x, final int y)
	{
		final int left = getElement().getAbsoluteLeft();
		final int right = getElement().getAbsoluteRight();
		final int top = getElement().getAbsoluteTop() - 20;
		final int bottom = getElement().getAbsoluteBottom();
		return left < x && x < right && top < y && y < bottom;
	}

	private int layoutItem(final PlaceBookItemFrame item, final int order, final boolean move)
	{
		if (move && innerPanel.getWidgetIndex(item.getRootPanel()) != order)
		{
			innerPanel.insert(item.getRootPanel(), order);
		}
		item.getItem().setParameter("order", order);

		String heightString;

		if (item.getItem().hasParameter("height") && item.getPanel() != null)
		{
			final int height = item.getItem().getParameter("height");
			final double heightPCT = height * 100 / HEIGHT_PRECISION;
			heightString = heightPCT + "%";
			// final int heightPX = (int) (item.getPanel().getOffsetHeight() * heightPCT);

			// heightString = heightPX + "px";
		}
		else
		{
			heightString = "";
		}

		item.resize(heightString);

		return item.getRootPanel().getOffsetHeight();
	}

	public void reflow()
	{
		Collections.sort(items, orderComparator);

		int order = 0;
		for (final PlaceBookItemFrame item : items)
		{
			layoutItem(item, order, true);
			order++;
		}
	}

	void reflow(final PlaceBookItemWidget newItem, final int inserty, final int height)
	{
		Collections.sort(items, orderComparator);

		newItem.getItem().setParameter("panel", panelIndex);

		int top = 0;
		int order = 0;
		boolean inserted = false;
		for (final PlaceBookItemFrame item : items)
		{
			if (!inserted && inserty < top + item.getItemWidget().getOffsetHeight())
			{
				newItem.getItem().setParameter("order", order);
				order++;
				inserted = true;
			}
			top += item.getRootPanel().getOffsetHeight();

			item.getItem().setParameter("order", order);
			order++;
		}

		if (!inserted)
		{
			newItem.getItem().setParameter("order", order);
		}
	}

	void reflow(final Widget insert, final int inserty, final int height)
	{
		Collections.sort(items, orderComparator);

		int top = 0;
		int order = 0;

		insert.setHeight(height + "px");

		for (final PlaceBookItemFrame item : items)
		{
			if (inserty < top + item.getItemWidget().getOffsetHeight())
			{
				innerPanel.insert(insert, order);
				return;
			}
			top += layoutItem(item, order, false);
			order++;
		}

		innerPanel.add(insert);
	}

	public void remove(final PlaceBookItemFrame item)
	{
		items.remove(item);
	}
}