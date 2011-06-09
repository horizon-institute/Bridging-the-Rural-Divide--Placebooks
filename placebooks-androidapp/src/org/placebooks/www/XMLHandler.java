package org.placebooks.www;

//import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//import org.xml.sax.Attributes;
//import java.lang.StringBuilder; 

import android.util.Log;


/**
 * SAX document handler to create instances of our custom object models from the information stored
 * in the XML document. This �document handler� is a listener for the various events that are fired
 * by the SAX parser based on the contents of your XML document.
 */

public class XMLHandler extends DefaultHandler {
	Book myBook;
//	Item item;
	TextItem titem;
	ImageItem imitem;
//	GPSItem gpsitem;	
	VideoItem vitem;
	AudioItem aitem;
	MapImageItem mimitem;
	StringBuilder url,text,filename, panel, order, geometry;

	/*
	 * fields
	 */

	 private boolean in_placebooksText = false;
	 private boolean in_textUrl = false;
	 private boolean in_textText = false;
	 private boolean in_textPanel = false;
	 private boolean in_textOrder = false;

	 private boolean in_placebooksImage = false;
	 private boolean in_imageUrl = false;
	 private boolean in_imageFilename = false;
	 private boolean in_imagePanel = false;
	 private boolean in_imageOrder = false;
	 
	 private boolean in_placebooksVideo = false;
	 private boolean in_videoFilename = false;
	 private boolean in_videoPanel = false;
	 private boolean in_videoOrder = false;
	 
	 private boolean in_placebooksAudio = false;
	 private boolean in_audioFilename = false;
	 private boolean in_audioPanel = false;
	 private boolean in_audioOrder = false;
	 
	 private boolean in_placebooksMapImage = false;
	 private boolean in_mapImageGeometry = false;
	 private boolean in_mapImageFilename = false;
	 
	 
	 private boolean in_key = false;

	 public Book getParsedData() {
		 //return the book;
		 return myBook;
	 }

	 /* 
	  * methods
	  */
	 @Override
	 public void startDocument() throws SAXException {
		//start of reading the xml document so we want to create a new book for our items
		myBook = new Book();
	 }

	 @Override
	 public void endDocument() throws SAXException {
		 // Nothing to do;
		 Log.d("xml", "here");
	 }



	 /** Gets called on opening tags like:
	  * <tag>
	  * Can provide attribute(s), when xml was like:
	  * <tag attribute="attributeValue">*/
	 @Override
	 public void startElement(String namespaceURI, String localName,
			 String qName, Attributes atts) throws SAXException {
		 
		 if(localName.equalsIgnoreCase("placebooks.model.PlaceBook")){
			 this.in_key = true;	
			 String attr = atts.getValue("key");
             myBook.setKey(attr);
		 }

		 else if (localName.equalsIgnoreCase("placebooks.model.TextItem")) {
			 this.in_placebooksText = true;
			 titem = new TextItem();
			 titem.setType("Text");	// NEWLY ADDED

		 }
		 else if (localName.equalsIgnoreCase("placebooks.model.ImageItem")) {
			 this.in_placebooksImage = true;
			 imitem = new ImageItem();
			 imitem.setType("Image");	// NEWLY ADDED

		 }
		 else if (localName.equalsIgnoreCase("placebooks.model.VideoItem")) {
			 this.in_placebooksVideo = true;
			 vitem = new VideoItem();
			 vitem.setType("Video");	// NEWLY ADDED

		 }
		 else if (localName.equalsIgnoreCase("placebooks.model.AudioItem")) {
			 this.in_placebooksAudio = true;
			 aitem = new AudioItem();
			 aitem.setType("Audio");	// NEWLY ADDED

		 }
		 else if (localName.equalsIgnoreCase("placebooks.model.MapImageItem")){
			 this.in_placebooksMapImage = true;
			 mimitem = new MapImageItem();
			 mimitem.setType("MapImage");	// NEWLY ADDED

		 }
		 
		 else if (localName.equalsIgnoreCase("text")) {
			 if(this.in_placebooksText){
				 this.in_textText = true;
				 text = new StringBuilder();
			 }
		 }
		 else if (localName.equalsIgnoreCase("url")) {
			 if(this.in_placebooksText){
				this.in_textUrl = true;
			 	url = new StringBuilder();
			 }else if(this.in_placebooksImage){
				this.in_imageUrl = true;
				url = new StringBuilder();
			 }
		 }
		 else if (localName.equalsIgnoreCase("filename")) {
			 if(this.in_placebooksImage){
				 this.in_imageFilename = true;
			 	filename = new StringBuilder();
			 } else if (this.in_placebooksVideo){
				 this.in_videoFilename = true;
				 filename = new StringBuilder();
			 }
			 else if (this.in_placebooksAudio){
				 this.in_audioFilename = true;
				 filename = new StringBuilder();
			 }
			 else if (this.in_placebooksMapImage){
				 this.in_mapImageFilename = true;
				 filename = new StringBuilder();
			 }
		  }
		 else if(localName.equalsIgnoreCase("panel")){
			 
			 if(this.in_placebooksText){
				 this.in_textPanel = true;
				 panel = new StringBuilder();
			 }
			 else if (this.in_placebooksImage){
				 this.in_imagePanel = true;
				 panel = new StringBuilder();
			 }
			 else if (this.in_placebooksVideo){
				 this.in_videoPanel = true;
				 panel = new StringBuilder();
			 }
			 else if (this.in_placebooksAudio){
				 this.in_audioPanel = true;
				 panel = new StringBuilder();
			 }
			 
		 } //end of else if panel
		 else if(localName.equalsIgnoreCase("order")){
			 
			  if(this.in_placebooksText){
				  this.in_textOrder = true;
				  order = new StringBuilder();
			  }
			  else if (this.in_placebooksImage){
				  this.in_imageOrder = true;
				  order = new StringBuilder();
			  }
			  else if (this.in_placebooksVideo){
				  this.in_videoOrder = true;
				  order = new StringBuilder();
			  }
			  else if (this.in_placebooksAudio){
				  this.in_audioOrder = true;
				  order = new StringBuilder();
			  }
			  
		   }//end of else if order
		 
		 else if(localName.equalsIgnoreCase("geometry")){
			 
			 if(this.in_placebooksMapImage){
				 this.in_mapImageGeometry = true;
				 geometry = new StringBuilder();
			 }
		 } //end of else if geometry
		 
			  	
	 }

	 /** Gets called on closing tags like:
	  * </tag> */
	 @Override
	 public void endElement(String namespaceURI, String localName, String qName)
	 throws SAXException {
		 
		 if (localName.equalsIgnoreCase("placebooks.model.PlaceBook")) {
			 this.in_key = false;
		 
		 }
		 else if (localName.equalsIgnoreCase("placebooks.model.TextItem")) {
			 this.in_placebooksText = false;
			 //this.CurrentBook.items.add(titem);
			 this.myBook.textItems.add(titem);
			 titem = null;
		 }
		 else if (localName.equalsIgnoreCase("placebooks.model.ImageItem")) {
			 this.in_placebooksImage = false;
			 this.myBook.imageItems.add(imitem);
			 imitem = null;
		 }  
		 else if (localName.equalsIgnoreCase("placebooks.model.VideoItem")) {
			 this.in_placebooksVideo = false;
			 this.myBook.videoItems.add(vitem);
			 vitem = null;
		 }  
		 else if (localName.equalsIgnoreCase("placebooks.model.AudioItem")) {
			 this.in_placebooksAudio = false;
			 this.myBook.audioItems.add(aitem);
			 aitem = null;
		 } 
		 else if (localName.equalsIgnoreCase("placebooks.model.MapImageItem")){
			 this.in_placebooksMapImage = false;
			 this.myBook.mapImageItems.add(mimitem);	//add a new map image item to my items to my book
			 mimitem = null;
			 
		 }
		 
		 
		 else if (localName.equalsIgnoreCase("url")) {  
			 if(this.in_placebooksText){
				 this.in_textUrl = false; 
				 titem.setURL(url.toString());
				 url = null;
			 }else if(this.in_placebooksImage){
				 this.in_imageUrl = false;
				 imitem.setURL(url.toString());		
				 url = null;
			 }
		 }
		 else if (localName.equalsIgnoreCase("text")) {
			 if(this.in_placebooksText){
				 this.in_textText = false;
				 titem.setText(text.toString());
				 text = null;
			 }else if(this.in_placebooksImage){
				 this.in_imageUrl = false;
				// imitem.setText(text.toString());
				// text = null;
			 }              	
		 }
		 else if (localName.equalsIgnoreCase("filename")) {
			 
			 if(this.in_placebooksImage){
				 this.in_imageFilename = false; 
				 imitem.setFilename(filename.toString());
				 filename = null;
			 }else if(this.in_placebooksVideo){
				 this.in_videoFilename = false;
				 vitem.setFilename(filename.toString());
				 filename = null;
			 }else if(this.in_placebooksAudio){
				 this.in_audioFilename = false;
				 aitem.setFilename(filename.toString());
				 filename = null;
			 }
			 else if(this.in_placebooksMapImage){
				 this.in_mapImageFilename = false;
				 mimitem.setFilename(filename.toString());
				 filename = null;
			 }
			 
		 }
		 
		 else if(localName.equalsIgnoreCase("panel")){
			 
			 if(this.in_placebooksText){
				 this.in_textPanel = false;
				 titem.setPanel(Integer.parseInt(panel.toString()));
				 panel = null;
			 }
			 else if(this.in_placebooksImage){
				 this.in_imagePanel = false;
				 imitem.setPanel(Integer.parseInt(panel.toString()));
				 panel = null;
			 }
			 else if(this.in_placebooksVideo){
				 this.in_videoPanel = false;
				 vitem.setPanel(Integer.parseInt(panel.toString()));
				 panel = null;
			 }
			 else if(this.in_placebooksAudio){
				 this.in_audioPanel = false;
				 aitem.setPanel(Integer.parseInt(panel.toString()));
				 panel = null;
				
			 }
			 
		 }
		 
		 else if (localName.equalsIgnoreCase("order")){
			 
			 if(this.in_placebooksText){
				this.in_textOrder = false;
				titem.setOrder(Integer.parseInt(order.toString()));
				order = null;
			 }
			 else if(this.in_placebooksImage){
				this.in_imageOrder = false;
				imitem.setOrder(Integer.parseInt(order.toString()));
				order = null;
			 }
			 else if(this.in_placebooksVideo){
				 this.in_videoOrder = false;
				 vitem.setOrder(Integer.parseInt(order.toString()));
				 order = null;
			 }
			 else if(this.in_placebooksAudio){
				 this.in_audioOrder = false;
				 aitem.setOrder(Integer.parseInt(order.toString()));
				 order = null;
				 
			 }
		 }//end of else if order
		 
		 else if (localName.equalsIgnoreCase("geometry")){
			 
			 if(this.in_placebooksMapImage){
				 this.in_mapImageGeometry = false;
				 mimitem.setGeometry(geometry.toString());
			 }
		 }
		 
	 }

	 /** Gets called on the following structure:
	  * <tag>characters</tag> */
	 @Override
	 public void characters(char ch[], int start, int length) {
		 //text item
		 
		 if(this.in_textUrl){
			 url.append(ch, start, length).toString();
			 
		 }
		 else if(this.in_textText){
			 text.append(ch, start, length).toString();
			 
		 }
		 else if(this.in_textPanel){
			 panel.append(ch, start, length).toString();
		 }
		 else if (this.in_textOrder){
			 order.append(ch, start, length).toString();
		 }
		 
		 
		 //image item
		 
		 else if(this.in_imageUrl){
			 url.append(ch, start, length).toString();
			 
		 }
		 else if(this.in_imageFilename){
			 filename.append(ch, start, length).toString();
		 } 
		 else if(this.in_imagePanel){
			 panel.append(ch, start, length).toString();
		 }
		 else if (this.in_imageOrder){
			 order.append(ch, start, length).toString();
		 }
		 
		 //video item
		 
		 else if (this.in_videoFilename){
			 filename.append(ch, start, length).toString();
		 }
		 else if(this.in_videoPanel){
			 panel.append(ch, start, length).toString();
		 }
		 else if (this.in_videoOrder){
			 order.append(ch, start, length).toString();
		 }
		 
		 //audio item
		 
		 else if (this.in_audioFilename){
			 filename.append(ch, start, length).toString();
		 }
		 else if(this.in_audioPanel){
			 panel.append(ch, start, length).toString();
		 }
		 else if (this.in_audioOrder){
			 order.append(ch, start, length).toString();
		 }
		
		 //map image item
		 
		 else if (this.in_mapImageGeometry){
			 geometry.append(ch, start, length).toString();
		 }
		 else if (this.in_mapImageFilename){
			 filename.append(ch, start, length).toString();
		 }
		 
		 
	 } //end of void characters

}
