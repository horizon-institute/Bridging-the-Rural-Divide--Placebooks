package org.placebooks.client.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.placebooks.client.ui.items.PlaceBookItemView;
import org.placebooks.client.ui.items.frames.PlaceBookItemFrame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class ColumnView extends FlowPanel
{
	interface Bundle extends ClientBundle
	{
		@Source("PlaceBookPanel.css")
		Style style();
	}

	interface Style extends CssResource
	{
		String innerPanel();

		String panel();

		String panelEdge();
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

	static void setHeight(final PlaceBookItemFrame item, final int heightPX)
	{
		//Log.info("Set Height: " + heightPX);
		final int canvasHeight = item.getColumn().getOffsetHeight();
		final int heightPCT = (int) ((heightPX * PlaceBookItemView.HEIGHT_PRECISION) / canvasHeight);

		item.getItem().getParameters().put("height", heightPCT);
		item.getItemWidget().refresh();
	}

	private final int column;

	private final FlowPanel innerPanel = new FlowPanel();

	private final List<PlaceBookItemFrame> items = new ArrayList<PlaceBookItemFrame>();

	private final int panelIndex;

	private PageView page;

	public ColumnView(final PageView page, final int index, final int columns, final double left,
			final double width, final boolean visible)
	{
		STYLES.style().ensureInjected();
		this.page = page;
		panelIndex = index;
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

	public PageView getPage()
	{
		return page;
	}

	public void reflow()
	{
		Collections.sort(items, orderComparator);

		resizeUploadItems();

		int order = 0;
		for (final PlaceBookItemFrame item : items)
		{
			layoutItem(item, order, true);
			order++;
		}
	}

	public void remove(final PlaceBookItemFrame item)
	{
		items.remove(item);
	}

	public void setPage(final PageView page)
	{
		this.page = page;
	}

	int getRemainingHeight()
	{
		int height = 0;
		for (final PlaceBookItemFrame item : items)
		{
			height += item.getRootPanel().getOffsetHeight();
		}

		return getOffsetHeight() - height;
	}

	boolean isIn(final int x, final int y)
	{
		final int left = getElement().getAbsoluteLeft();
		final int right = getElement().getAbsoluteRight();
		final int top = getElement().getAbsoluteTop() - 20;
		final int bottom = getElement().getAbsoluteBottom();
		return left < x && x < right && top < y && y < bottom;
	}

	void reflow(final PlaceBookItemView newItem, final int inserty)
	{
		Collections.sort(items, orderComparator);

		newItem.getItem().getParameters().put("column", panelIndex);

		int top = 0;
		int order = 0;
		boolean inserted = false;
		for (final PlaceBookItemFrame item : items)
		{
			if (!inserted && inserty < top + item.getItemWidget().getOffsetHeight())
			{
				newItem.getItem().getParameters().put("order", order);
				order++;
				inserted = true;
			}

			top += item.getRootPanel().getOffsetHeight();

			item.getItem().getParameters().put("order", order);
			order++;
		}

		if (!inserted)
		{
			newItem.getItem().getParameters().put("order", order);
		}
	}

	void reflow(final Widget insert, final int inserty, final int height)
	{
		Collections.sort(items, orderComparator);

		int top = 0;
		int order = 0;

		insert.setHeight(Math.min(height, getRemainingHeight()) + "px");

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

	private int layoutItem(final PlaceBookItemFrame item, final int order, final boolean move)
	{
		if (move && innerPanel.getWidgetIndex(item.getRootPanel()) != order)
		{
			innerPanel.insert(item.getRootPanel(), order);
		}
		item.getItem().getParameters().put("order", order);

		String heightString;

		if (item.getItem().getParameters().containsKey("height") && item.getColumn() != null)
		{
			final int height = item.getItem().getParameters().get("height");
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

	private void resizeUploadItems()
	{
		final List<PlaceBookItemFrame> resizable = new ArrayList<PlaceBookItemFrame>();
		int height = 0;
		for (final PlaceBookItemFrame item : items)
		{
			final int itemHeight = item.getRootPanel().getOffsetHeight();
			if (itemHeight > 0)
			{
				height += itemHeight;
				if (item.getItem().getParameter("uploadResize", 0) == 1)
				{
					resizable.add(item);
				}
			}
		}

		for (final PlaceBookItemFrame item : resizable)
		{
			item.getItem().getParameters().remove("uploadResize");
		}

		final int remaining = getOffsetHeight() - height;
		if (remaining < 0 && !resizable.isEmpty())
		{
			final int offset = remaining / resizable.size();
			for (final PlaceBookItemFrame item : resizable)
			{
				setHeight(item, item.getRootPanel().getOffsetHeight() + offset);
			}
		}
	}
}