package org.placebooks.client.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.placebooks.client.model.Page;
import org.placebooks.client.model.PlaceBook;
import org.placebooks.client.ui.UIMessages;
import org.placebooks.client.ui.widgets.Canvas;
import org.wornchaos.client.ui.CompositeView;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class PagesView extends CompositeView<PlaceBook>
{
	interface PageStyle extends CssResource
	{
		String page();

		String pageDisabled();

		String pageEnabled();
	}

	interface PlaceBookPanelUiBinder extends UiBinder<Widget, PagesView>
	{
	}

	private class Flip extends Timer
	{
		private FlipState state = FlipState.none;

		private double target;
		private double progress;
		private PageView left;
		private PageView right;

		@Override
		public void run()
		{
			if (state != FlipState.flipping)
			{
				cancel();
			}
			else
			{
				if (target == 1)
				{
					setPage(left);
					right.setVisible(true);
				}
				else if (target == -1)
				{
					setPage(right);
				}

				if (Math.abs(target - progress) > 0.01)
				{
					drawFlip(left, progress + (target - progress) * 0.2);
				}
				else
				{
					drawFlip(left, target);

					if (target == 1)
					{
						left.clearFlip();
					}
					else if (target == -1)
					{
						right.clearFlip();
					}

					state = FlipState.none;
					cancel();
				}
			}
		}

		public void setup(final PageView left, final PageView right)
		{
			flip.cancel();
			if (!left.equals(this.left))
			{
				if (this.left != null)
				{
					this.left.setVisible(false);
				}
				this.left = left;
				left.reflow();
			}

			if (this.right != right)
			{
				if (this.right != null)
				{
					this.right.setVisible(false);
				}
				this.right = right;
				right.reflow();
			}

			left.setVisible(true);
			left.getElement().getStyle().setZIndex(1);

			right.setVisible(true);
			right.getElement().getStyle().setZIndex(0);
		}
	}

	private enum FlipState
	{
		none, dragging, edgeHighlight, flipping
	}

	private static final UIMessages uiMessages = GWT.create(UIMessages.class);

	private static PlaceBookPanelUiBinder uiBinder = GWT.create(PlaceBookPanelUiBinder.class);

	private PageView currentPage;

	@UiField
	Panel rootPanel;

	@UiField
	Panel pagesPanel;

	@UiField
	Panel leftPanel;

	@UiField
	Canvas canvas;

	@UiField
	PageStyle style;

	@UiField
	Panel nextPage;

	@UiField
	Panel prevPage;

	private double bookWidth;
	private double pageWidth;
	private double pageHeight;

	private int startX;
	private int startY;
	private boolean dragged = false;

	private double margin = 30;

	private Flip flip = new Flip();

	protected final List<PageView> pages = new ArrayList<PageView>();

	private PlaceBook placebook;

	protected DragController controller;

	public PagesView()
	{
		Window.addResizeHandler(new ResizeHandler()
		{
			@Override
			public void onResize(final ResizeEvent event)
			{
				resized();
			}
		});
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void createPage()
	{
		int index = 0;
		if (currentPage != null)
		{
			index = currentPage.getIndex() + 1;
		}

		final Page page = new Page();		
		page.getMetadata().put("tempID", "" + System.currentTimeMillis());
		getPlaceBook().getPages().add(index, page);
		final PageView pageUI = new PageView(page, controller, index, getDefaultColumnCount());

		pages.add(index, pageUI);
		getPagePanel().add(pageUI);
		pageUI.setStyleName(style.page());

		if (currentPage != null)
		{
			flip.setup(currentPage, pageUI);
			flip.target = -1;
			flip.progress = 1;
			flip.state = FlipState.flipping;
			flip.scheduleRepeating(1000 / 60);
		}
		else
		{
			currentPage = pageUI;
		}

		updateIndices();
	}

	public boolean deleteCurrentPage()
	{
		if (pages.size() <= 1) { return false; }
		final int index = currentPage.getIndex();
		getPlaceBook().getPages().remove(currentPage.getPage());
		remove(currentPage);
		setPage(pages.get(Math.min(index, pages.size() - 1)));
		currentPage.clearFlip();
		updateIndices();
		return true;
	}

	public Page getCurrentPage()
	{
		if (currentPage == null) { return null; }
		return currentPage.getPage();
	}

	public Iterable<PageView> getPages()
	{
		return pages;
	}

	public PlaceBook getPlaceBook()
	{
		return placebook;
	}

	public void goToPage(final int page)
	{
		goToPage(pages.get(page));
	}

	public void goToPage(final Page page)
	{
		goToPage(getPage(page));
	}

	public void goToPage(final PageView page)
	{
		if (page != null)
		{
			if (page.getIndex() < currentPage.getIndex())
			{
				flip.setup(page, currentPage);
				flip.target = 1;
				flip.progress = -1;
				flip.state = FlipState.flipping;
				flip.scheduleRepeating(1000 / 60);
			}
			else if (page.getIndex() > currentPage.getIndex())
			{
				flip.setup(currentPage, page);
				flip.target = -1;
				flip.progress = 1;
				flip.state = FlipState.flipping;
				flip.scheduleRepeating(1000 / 60);
			}
		}
	}

	@Override
	public void itemChanged(final PlaceBook newPlaceBook)
	{	
		PlaceBook oldPlacebook = placebook;
		
		placebook = newPlaceBook;

		int pageIndex = 0;
		for (final Page page : newPlaceBook.getPages())
		{
			final PageView pageUI = getPage(page);
			if (pageUI != null)
			{
				pageUI.itemChanged(page);
			}
			else
			{
				//Log.info("Page not matched!");

				add(new PageView(page, controller, pageIndex, getDefaultColumnCount()));
			}

			pageIndex++;
		}

		if (pageIndex == 0)
		{
			createPage();
		}

		if(oldPlacebook == null)
		{
			if (pages.size() > 0)
			{
				setPage(pages.get(0));
			}
		}
	}

	public void resized()
	{
		final double height = getOffsetHeight();
		if (height == 0) { return; }
		final double width = getOffsetWidth();
		final double bookWidth = height * 3 / 2;
		if (bookWidth < width)
		{
			// Height is constraint
			final double left = (width - bookWidth) / 2;

			setPosition(left, 0, bookWidth, height);
		}
		else
		{
			// Width is constraint
			final double bookHeight = (width * 2 / 3);
			final double top = (height - bookHeight) / 2;

			setPosition(0, top, width, bookHeight);
		}
	}

	public void setDragController(final DragController dragController)
	{
		controller = dragController;
	}

	public void setPage(final PageView page)
	{
		if (currentPage == page) { return; }

		if (currentPage != null)
		{
			currentPage.setVisible(false);
		}

		currentPage = page;
		currentPage.setVisible(true);
		currentPage.getElement().getStyle().setZIndex(1);

		currentPage.reflow();

		if (currentPage.getIndex() == 0)
		{
			prevPage.removeStyleName(style.pageEnabled());
			prevPage.addStyleName(style.pageDisabled());
			prevPage.setTitle(null);
		}
		else
		{
			prevPage.removeStyleName(style.pageDisabled());
			prevPage.addStyleName(style.pageEnabled());
			prevPage.setTitle(uiMessages.page(currentPage.getIndex(), pages.size()));
		}

		if (currentPage.getIndex() >= pages.size() - 1)
		{
			nextPage.removeStyleName(style.pageEnabled());
			nextPage.addStyleName(style.pageDisabled());
			nextPage.setTitle(null);
		}
		else
		{
			nextPage.removeStyleName(style.pageDisabled());
			nextPage.addStyleName(style.pageEnabled());
			nextPage.setTitle(uiMessages.page(currentPage.getIndex() + 2, pages.size()));
		}
	}

	@UiHandler("rootPanel")
	void clickFlip(final ClickEvent event)
	{
		if (pages.size() <= 1) { return; }
		if (!dragged)
		{
			final int mouseX = event.getRelativeX(pagesPanel.getElement());
			final int mouseY = event.getRelativeY(pagesPanel.getElement());
			// Make sure the mouse pointer is inside of the book
			if (currentPage.getIndex() > 0 && mouseIsOverLeftEdge(mouseX, mouseY))
			{
				prevPage(null);
			}
			else if (currentPage.getIndex() + 1 < pages.size() && mouseIsOverRightEdge(mouseX, mouseY))
			{
				nextPage(null);
			}
		}
	}

	@UiHandler("rootPanel")
	void flip(final MouseMoveEvent event)
	{
		if (pages.size() <= 1) { return; }
		final int mouseX = event.getRelativeX(pagesPanel.getElement());
		final int mouseY = event.getRelativeY(pagesPanel.getElement());
		if (flip.state == FlipState.dragging)
		{
			drawFlip(flip.left, Math.max(Math.min(mouseX / pageWidth, 1), -1));

			final int distanceX = Math.abs(startX - mouseX);
			final int distanceY = Math.abs(startY - mouseY);

			if (!dragged && distanceX + distanceY > 10)
			{
				dragged = true;
			}

			event.preventDefault();
		}
		else if (flip.state != FlipState.flipping)
		{
			if (mouseIsOverRightEdge(mouseX, mouseY) && currentPage.getIndex() + 1 < pages.size())
			{
				if (flip.state != FlipState.edgeHighlight)
				{
					flip.cancel();
					flip.state = FlipState.edgeHighlight;
					flip.setup(currentPage, pages.get(currentPage.getIndex() + 1));
				}
				drawFlip(currentPage, Math.max(Math.min(mouseX / pageWidth, 1), -1));
			}
			else if (flip.state == FlipState.edgeHighlight)
			{
				flip.target = 1;
				flip.state = FlipState.flipping;
				flip.scheduleRepeating(1000 / 60);
			}
		}
	}

	@UiHandler("rootPanel")
	void flipEnd(final MouseUpEvent event)
	{
		endFlip(event);
	}

	@UiHandler("rootPanel")
	void flipExit(final MouseOutEvent event)
	{
		endFlip(event);
		if (flip.state == FlipState.edgeHighlight)
		{
			flip.target = 1;
			flip.state = FlipState.flipping;
			flip.scheduleRepeating(1000 / 60);
		}
	}

	@UiHandler("rootPanel")
	void flipStart(final MouseDownEvent event)
	{
		if (pages.size() <= 1) { return; }
		if (flip.state != FlipState.flipping)
		{
			final int mouseX = event.getRelativeX(pagesPanel.getElement());
			final int mouseY = event.getRelativeY(pagesPanel.getElement());
			// Make sure the mouse pointer is inside of the book
			if (mouseIsOverLeftEdge(mouseX, mouseY) && currentPage.getIndex() - 1 >= 0)
			{
				// We are on the left side, drag the previous page
				flip.state = FlipState.dragging;
				flip.setup(pages.get(currentPage.getIndex() - 1), currentPage);
				startX = mouseX;
				startY = mouseY;
				dragged = false;
				event.preventDefault();
			}
			else if (mouseIsOverRightEdge(mouseX, mouseY) && currentPage.getIndex() + 1 < pages.size())
			{
				// We are on the right side, drag the current page
				flip.state = FlipState.dragging;
				flip.setup(currentPage, pages.get(currentPage.getIndex() + 1));
				startX = mouseX;
				startY = mouseY;
				dragged = false;
				event.preventDefault();
			}
		}
	}

	@UiHandler("nextPage")
	void nextPage(final ClickEvent event)
	{
		if (currentPage.getIndex() + 1 >= pages.size()) { return; }
		flip.setup(currentPage, pages.get(currentPage.getIndex() + 1));
		flip.target = -1;
		flip.progress = 1;
		flip.state = FlipState.flipping;
		flip.scheduleRepeating(1000 / 60);
	}

	@UiHandler("prevPage")
	void prevPage(final ClickEvent event)
	{
		if (currentPage.getIndex() == 0) { return; }
		flip.setup(pages.get(currentPage.getIndex() - 1), currentPage);
		flip.target = 1;
		flip.progress = -1;
		flip.state = FlipState.flipping;
		flip.scheduleRepeating(1000 / 60);
	}

	protected void add(final PageView page)
	{
		pages.add(page);
		getPagePanel().add(page);
		page.setStyleName(style.page());
		page.setVisible(false);
	}

	protected int getDefaultColumnCount()
	{
		return 2;
	}

	protected Panel getPagePanel()
	{
		return pagesPanel;
	}

	protected void remove(final PageView page)
	{
		pages.remove(page);
		getPagePanel().remove(page);
	}

	private void drawFlip(final PageView page, final double progress)
	{
		flip.progress = progress;

		// Determines the strength of the fold/bend on a 0-1 range
		final double strength = 1 - Math.abs(progress);

		// Width of the folded paper
		final double foldWidth = (pageWidth * 0.5) * (1 - progress);

		// X position of the folded paper
		final double foldX = pageWidth * progress + foldWidth;

		// How far outside of the book the paper is bent due to perspective
		final double verticalOutdent = margin * strength;

		// The maximum widths of the three shadows used
		final double rightShadowWidth = (pageWidth * 0.5) * Math.max(Math.min(strength, 0.5), 0);
		final double leftShadowWidth = (pageWidth * 0.5) * Math.max(Math.min(strength, 0.5), 0);

		// Mask the page by setting its width to match the foldX
		page.setFlip(Math.max(foldX, 0), pageWidth);

		final Context2d context = canvas.getContext2d();
		context.clearRect(0, 0, canvas.getOffsetWidth(), canvas.getOffsetHeight());
		context.save();
		context.translate((bookWidth / 2) + margin, margin);

		// Draw a sharp shadow on the left side of the page
		final String strokeAlpha = NumberFormat.getFormat("#.################").format(strength * 0.05);
		context.setStrokeStyle("rgba(0,0,0," + strokeAlpha + ")");
		context.setLineWidth(30 * strength);
		context.beginPath();
		context.moveTo(foldX - foldWidth, -verticalOutdent * 0.5);
		context.lineTo(foldX - foldWidth, pageHeight + (verticalOutdent * 0.5));
		context.stroke();

		// Right side drop shadow
		final CanvasGradient rightShadowGradient = context.createLinearGradient(foldX, 0, foldX + rightShadowWidth, 0);
		final String rightShadowAlpha = NumberFormat.getFormat("#.################").format(strength * 0.2);
		rightShadowGradient.addColorStop(0, "rgba(0,0,0," + rightShadowAlpha + ")");
		rightShadowGradient.addColorStop(0.8, "rgba(0,0,0,0.0)");

		context.setFillStyle(rightShadowGradient);
		context.beginPath();
		context.moveTo(foldX, 0);
		context.lineTo(foldX + rightShadowWidth, 0);
		context.lineTo(foldX + rightShadowWidth, pageHeight);
		context.lineTo(foldX, pageHeight);
		context.closePath();
		context.fill();

		// Left side drop shadow
		final CanvasGradient leftShadowGradient = context.createLinearGradient(	foldX - foldWidth - leftShadowWidth, 0,
																				foldX - foldWidth, 0);
		leftShadowGradient.addColorStop(0, "rgba(0,0,0,0.0)");
		final String leftShadowAlpha = NumberFormat.getFormat("#.################").format(strength * 0.15);
		leftShadowGradient.addColorStop(1, "rgba(0,0,0," + leftShadowAlpha + ")");

		context.setFillStyle(leftShadowGradient);
		context.beginPath();
		context.moveTo(foldX - foldWidth - leftShadowWidth, 0);
		context.lineTo(foldX - foldWidth, 0);
		context.lineTo(foldX - foldWidth, pageHeight);
		context.lineTo(foldX - foldWidth - leftShadowWidth, pageHeight);
		context.fill();

		// Gradient applied to the folded paper (highlights & shadows)
		final CanvasGradient foldGradient = context.createLinearGradient(foldX - foldWidth, 0, foldX, 0);
		foldGradient.addColorStop(0.35, "#fafafa");
		foldGradient.addColorStop(0.73, "#eeeeee");
		foldGradient.addColorStop(0.9, "#fafafa");
		foldGradient.addColorStop(1.0, "#d7d7d7");

		context.setFillStyle(foldGradient);
		context.setStrokeStyle("rgba(0,0,0,0.06)");
		context.setLineWidth(0.5);

		// Draw the folded piece of paper
		context.beginPath();
		context.moveTo(foldX, 0);
		context.lineTo(foldX, pageHeight);
		context.quadraticCurveTo(foldX, pageHeight + (verticalOutdent * 2), foldX - foldWidth, pageHeight
				+ verticalOutdent);
		context.lineTo(foldX - foldWidth, -verticalOutdent);
		context.quadraticCurveTo(foldX, -verticalOutdent * 2, foldX, 0);

		context.fill();
		context.stroke();

		context.restore();
	}

	private void endFlip(final MouseEvent<?> event)
	{
		if (flip.state == FlipState.dragging)
		{
			flip.state = FlipState.flipping;
			final int mouseX = event.getRelativeX(pagesPanel.getElement());
			final int mouseY = event.getRelativeY(pagesPanel.getElement());

			if (mouseIsOverRightEdge(mouseX, mouseY))
			{
				flip.state = FlipState.edgeHighlight;
			}
			else
			{
				final double progress = Math.max(Math.min(mouseX / pageWidth, 1), -1);
				if (progress < 0.5)
				{
					flip.target = -1;
					flip.state = FlipState.flipping;
					flip.scheduleRepeating(1000 / 60);
				}
				else
				{
					flip.target = 1;
					flip.state = FlipState.flipping;
					flip.scheduleRepeating(1000 / 60);
				}
			}
		}
	}

	private PageView getPage(final Page page)
	{
		for (final PageView pbPage : pages)
		{
			if (page.getId() != null && page.getId().equals(pbPage.getPage().getId()))
			{
				return pbPage;
			}
			else if (page.getMetadata().containsKey("tempID")
					&& page.getMetadata().get("tempID").equals(pbPage.getPage().getMetadata().get("tempID"))) { return pbPage; }
		}
		return null;
	}

	private boolean mouseIsOverLeftEdge(final int mouseX, final int mouseY)
	{
		if (mouseY > 0 && mouseY < pagesPanel.getOffsetHeight() && mouseX < margin)
		{
			//Log.info("Left : " + mouseX + ", " + mouseY);
		}
		return mouseY > 0 && mouseY < pagesPanel.getOffsetHeight() && mouseX < margin;
	}

	private boolean mouseIsOverRightEdge(final int mouseX, final int mouseY)
	{
		if (mouseY > 0 && mouseY < pagesPanel.getOffsetHeight() && mouseX > (pageWidth - margin) && mouseX < pageWidth)
		{
			//Log.info("Right : " + mouseX + ", " + mouseY);
		}
		return mouseY > 0 && mouseY < pagesPanel.getOffsetHeight() && mouseX > (pageWidth - margin)
				&& mouseX < pageWidth;
	}

	private void setPosition(final double left, final double top, final double width, final double height)
	{
		pageHeight = height - (margin * 2);
		pageWidth = width - (margin * 2);
		bookWidth = width * 2;

		canvas.getElement().getStyle().setTop(top, Unit.PX);
		canvas.getElement().getStyle().setLeft(left - width, Unit.PX);
		canvas.getCanvasElement().setWidth((int) bookWidth);
		canvas.getCanvasElement().setHeight((int) height);

		pagesPanel.getElement().getStyle().setTop(top + margin, Unit.PX);
		pagesPanel.getElement().getStyle().setLeft(left + margin, Unit.PX);
		pagesPanel.setWidth(pageWidth + "px");
		pagesPanel.setHeight(pageHeight + "px");

		leftPanel.getElement().getStyle().setTop(top + margin, Unit.PX);
		leftPanel.getElement().getStyle().setLeft(left + margin - pageWidth, Unit.PX);
		leftPanel.setWidth(pageWidth + "px");
		leftPanel.setHeight(pageHeight + "px");

		for (final PageView page : pages)
		{
			page.setSize(width, height);
		}
	}

	private void updateIndices()
	{
		for (int index = 0; index < pages.size(); index++)
		{
			final PageView page = pages.get(index);
			page.setIndex(index);
		}
	}
}