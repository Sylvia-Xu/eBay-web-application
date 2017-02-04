/* CS144
 *
 * Parser skeleton for processing item-???.xml files. Must be compiled in
 * JDK 1.5 or above.
 *
 * Instructions:
 *
 * This program processes all files passed on the command line (to parse
 * an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 *
 * At the point noted below, an individual XML file has been parsed into a
 * DOM Document node. You should fill in code to process the node. Java's
 * interface for the Document Object Model (DOM) is in package
 * org.w3c.dom. The documentation is available online at
 *
 * http://java.sun.com/j2se/1.5.0/docs/api/index.html
 *
 * A tutorial of Java's XML Parsing can be found at:
 *
 * http://java.sun.com/webservices/jaxp/
 *
 * Some auxiliary methods have been written for you. You may find them
 * useful.
 */

package edu.ucla.cs.cs144;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;


class MyParser {
    
	// Helper Classes corresponding to the SQL table schema 
	// represent the entries in the csv files
	public static class User {
        String userID;
		String rating;
		String location;
		String country;
		
		public User(String id, String rating, String location, String country) {
            this.userID = id;
			this.rating = rating;
			this.location = location;
			this.country = country;
		}
	}
	
	public static class Item {
		int itemID;
        String name;
		String currentPrice;
		String buyPrice;
		String firstBid;
		String numOfBids;
        String start;
        String end;
		String seller;
		String description;
		
		public Item(int id, String name, String cPrice, String bPrice, String firstBid, String numOfBids, String start, String end, String seller, String description) {
			this.itemID = id;
            this.name = name;
			this.currentPrice = cPrice;
            this.buyPrice = bPrice;
            this.firstBid = firstBid;
            this.numOfBids = numOfBids;
            this.start = start;
            this.end = end;
            this.seller = seller;
            
            if(description.length() >= 4000) {
                this.description = description.substring(0, 4000);
            }
            else {
                this.description = description;
            }
		}
	}

    public static class Category {
        int categoryID;
        String name;

        public Category(int id, String name) {
            this.categoryID = id;
            this.name = name;
        }
    }

    public static class Bid {
        String userID;
        int itemID;
        String time;
        String amount;

        public Bid(String userID, int itemID, String time, String amount) {
            this.userID = userID;
            this.itemID = itemID;
            this.time = time;
            this.amount = amount;
        }
    }

    public static class Categorize {
        int itemID;
        int categoryID;

        public Categorize(int itemID, int categoryID) {
            this.itemID = itemID;
            this.categoryID = categoryID;
        }
    }
	
    // Hash maps that hold the entries of the tables
    static Map<String, User> userMap = new HashMap<>();
    static Map<String, Item> itemMap = new HashMap<>();
    static Map<String, Category> categoryMap = new HashMap<>();
    static Map<String, Bid> bidMap = new HashMap<>();
    static Map<String, Categorize> categorizeMap = new HashMap<>();

    static final String columnSeparator = "|*|";
    static DocumentBuilder builder;
    
    static final String[] typeName = {
	"none",
	"Element",
	"Attr",
	"Text",
	"CDATA",
	"EntityRef",
	"Entity",
	"ProcInstr",
	"Comment",
	"Document",
	"DocType",
	"DocFragment",
	"Notation",
    };
    
    static class MyErrorHandler implements ErrorHandler {
        
        public void warning(SAXParseException exception)
        throws SAXException {
            fatalError(exception);
        }
        
        public void error(SAXParseException exception)
        throws SAXException {
            fatalError(exception);
        }
        
        public void fatalError(SAXParseException exception)
        throws SAXException {
            exception.printStackTrace();
            System.out.println("There should be no errors " +
                               "in the supplied XML files.");
            System.exit(3);
        }
        
    }
    
    /* Non-recursive (NR) version of Node.getElementsByTagName(...)
     */
    static Element[] getElementsByTagNameNR(Element e, String tagName) {
        Vector< Element > elements = new Vector< Element >();
        Node child = e.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
            {
                elements.add( (Element)child );
            }
            child = child.getNextSibling();
        }
        Element[] result = new Element[elements.size()];
        elements.copyInto(result);
        return result;
    }
    
    /* Returns the first subelement of e matching the given tagName, or
     * null if one does not exist. NR means Non-Recursive.
     */
    static Element getElementByTagNameNR(Element e, String tagName) {
        Node child = e.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
                return (Element) child;
            child = child.getNextSibling();
        }
        return null;
    }
    
    /* Returns the text associated with the given element (which must have
     * type #PCDATA) as child, or "" if it contains no text.
     */
    static String getElementText(Element e) {
        if (e.getChildNodes().getLength() == 1) {
            Text elementText = (Text) e.getFirstChild();
            return elementText.getNodeValue();
        }
        else
            return "";
    }
    
    /* Returns the text (#PCDATA) associated with the first subelement X
     * of e with the given tagName. If no such X exists or X contains no
     * text, "" is returned. NR means Non-Recursive.
     */
    static String getElementTextByTagNameNR(Element e, String tagName) {
        Element elem = getElementByTagNameNR(e, tagName);
        if (elem != null)
            return getElementText(elem);
        else
            return "";
    }
    
    /* Returns the amount (in XXXXX.xx format) denoted by a money-string
     * like $3,453.23. Returns the input if the input is an empty string.
     */
    static String strip(String money) {
        if (money.equals(""))
            return money;
        else {
            double am = 0.0;
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            try { am = nf.parse(money).doubleValue(); }
            catch (ParseException e) {
                System.out.println("This method should work for all " +
                                   "money values you find in our data.");
                System.exit(20);
            }
            nf.setGroupingUsed(false);
            return nf.format(am).substring(1);
        }
    }
    
    /* Convert the xml time format to the MySQL timestamp format
     */
    static String convertDate(String xmlDate) {
        String xmlFormat = "MMM-dd-yy HH:mm:ss";
        String sqlFormat = "yyyy-MM-dd HH:mm:ss";

        SimpleDateFormat formatter = new SimpleDateFormat(xmlFormat);
        String result = "";
        try {
            Date d = formatter.parse(xmlDate);
            formatter.applyPattern(sqlFormat);
            result = formatter.format(d);
        } catch (ParseException e) {
            System.out.println("Could not format date");
        }

        return result;
    }

    /* Process one items-???.xml file.
     */
    static void processFile(File xmlFile) {
        Document doc = null;
        try {
            doc = builder.parse(xmlFile);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }
        catch (SAXException e) {
            System.out.println("Parsing error on file " + xmlFile);
            System.out.println("  (not supposed to happen with supplied XML files)");
            e.printStackTrace();
            System.exit(3);
        }
        
        /* At this point 'doc' contains a DOM representation of an 'Items' XML
         * file. Use doc.getDocumentElement() to get the root Element. */
        System.out.println("Successfully parsed - " + xmlFile);
        
        /* Fill in code here (you will probably need to write auxiliary
            methods). */
        Element root = doc.getDocumentElement();
        Element[] itemList = getElementsByTagNameNR(root, "Item");
        
        for(Element item: itemList) {
            String itemID = item.getAttribute("ItemID");
        	String itemName = getElementTextByTagNameNR(item, "Name");
            String country = getElementTextByTagNameNR(item, "Country");
            String startTime = convertDate(getElementTextByTagNameNR(item, "Started"));
            String endTime = convertDate(getElementTextByTagNameNR(item, "Ends"));
            String description = getElementTextByTagNameNR(item, "Description");

            // retrieve the location information
            Element locElement = getElementByTagNameNR(item, "Location");
            String location = getElementText(locElement);
            String latString = locElement.getAttribute("Latitude");
            String lngString = locElement.getAttribute("Longitude");
            double latitude = latString.isEmpty()? 0.0: Double.parseDouble(latString);
            double longitude = lngString.isEmpty()? 0.0: Double.parseDouble(lngString);

            // retrieve the bid price information and format the strings.
            String currentPrice = strip(getElementTextByTagNameNR(item, "Currently"));
            String firstBid = strip(getElementTextByTagNameNR(item, "First_Bid"));
            String buyPrice = strip(getElementTextByTagNameNR(item, "Buy_Price"));
            if(buyPrice.isEmpty()) buyPrice = "";
            String numOfBids = getElementTextByTagNameNR(item, "Number_of_Bids");

            // retrieve the category information and keep track of the mappings between items and categories
            Element[] categories = getElementsByTagNameNR(item, "Category");
            for(Element category: categories) {
                String categoryName = getElementText(category);
                int categoryID;
                if(categoryMap.containsKey(categoryName)) {
                    categoryID = categoryMap.get(categoryName).categoryID;
                }
                else {
                    categoryID = categoryMap.size() + 1;
                    Category categoryObject = new Category(categoryID, categoryName);
                    categoryMap.put(categoryName, categoryObject);
                }
                
                Categorize itemToCategory = new Categorize(Integer.parseInt(itemID), categoryID);
                categorizeMap.put(itemID + " " + categoryID, itemToCategory);
            }

            // retrieve the bidding history
            Element bidRoot = getElementByTagNameNR(item, "Bids");
            Element[] bidRecords = getElementsByTagNameNR(bidRoot, "Bid");
            for(Element bid: bidRecords) {
                String time = convertDate(getElementTextByTagNameNR(bid, "Time"));
                String amount = strip(getElementTextByTagNameNR(bid, "Amount"));
                Element bidder = getElementByTagNameNR(bid, "Bidder");

                String userID = bidder.getAttribute("UserID");
                String rating = bidder.getAttribute("Rating");
                String bidLocation = getElementTextByTagNameNR(bidder, "Location");
                if(bidLocation.isEmpty()) bidLocation = "";
                String bidCountry = getElementTextByTagNameNR(bidder, "Country");
                if(bidCountry.isEmpty()) bidCountry = "";
                if(!userMap.containsKey(userID)) {
                    userMap.put(userID, new User(userID, rating, bidLocation, bidCountry));
                }

                bidMap.put(userID + "\t" + time, 
                            new Bid(userID, Integer.parseInt(itemID), time, amount));
            }
            
            // retrieve the seller information
            Element seller = getElementByTagNameNR(item, "Seller");
            String sellerID = seller.getAttribute("UserID");
            if(!userMap.containsKey(sellerID)) {
                userMap.put(sellerID, new User(sellerID, seller.getAttribute("Rating"), location, country));
            }

            itemMap.put(itemID,
                        new Item(Integer.parseInt(itemID), 
                                itemName, 
                                currentPrice,
                                buyPrice,
                                firstBid,
                                numOfBids, 
                                startTime, 
                                endTime, 
                                sellerID,
                                description));

        }
        
    }
    
    public static void main (String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java MyParser [file] [file] ...");
            System.exit(1);
        }
        
        /* Initialize parser. */
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);      
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new MyErrorHandler());
        }
        catch (FactoryConfigurationError e) {
            System.out.println("unable to get a document builder factory");
            System.exit(2);
        } 
        catch (ParserConfigurationException e) {
            System.out.println("parser was unable to be configured");
            System.exit(2);
        }
        
        /* Process all files listed on command line. */
        for (int i = 0; i < args.length; i++) {
            File currentFile = new File(args[i]);
            processFile(currentFile);
        }
        
        // write the item entries to Item.dat
        try {
            FileWriter itemFile = new FileWriter("Item.dat");
            BufferedWriter itemWriter = new BufferedWriter(itemFile);

            for(Map.Entry<String, Item> entry: itemMap.entrySet()) {
                Item item = entry.getValue();
                String line = String.format("%d |*| %s |*| %s |*| %s |*| %s |*| %s |*| %s |*| %s |*| %s |*| %s\n",
                                item.itemID,
                                item.name,
                                item.currentPrice,
                                item.buyPrice,
                                item.firstBid,
                                item.numOfBids,
                                item.start,
                                item.end,
                                item.seller,
                                item.description);
                itemWriter.write(line);
            }

            itemWriter.close();
            itemFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write the user entries to User.dat
        try {
            FileWriter userFile = new FileWriter("User.dat");
            BufferedWriter userWriter = new BufferedWriter(userFile);

            for(Map.Entry<String, User> entry: userMap.entrySet()) {
                User user = entry.getValue();
                String line = String.format("%s |*| %s |*| %s |*| %s\n",
                                user.userID,
                                user.rating,
                                user.location,
                                user.country);
                userWriter.write(line);
            }

            userWriter.close();
            userFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write the category entries to Category.dat
        try {
            FileWriter categoryFile = new FileWriter("Category.dat");
            BufferedWriter categoryWriter = new BufferedWriter(categoryFile);

            for(Map.Entry<String, Category> entry: categoryMap.entrySet()) {
                Category category = entry.getValue();
                String line = String.format("%d |*| %s\n",
                                category.categoryID,
                                category.name);
                categoryWriter.write(line);
            }

            categoryWriter.close();
            categoryFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write the bid entries to Bid.dat
        try {
            FileWriter bidFile = new FileWriter("Bid.dat");
            BufferedWriter bidWriter = new BufferedWriter(bidFile);

            for(Map.Entry<String, Bid> entry: bidMap.entrySet()) {
                Bid bid = entry.getValue();
                String line = String.format("%s |*| %d |*| %s |*| %s\n",
                                bid.userID,
                                bid.itemID,
                                bid.time,
                                bid.amount);
                bidWriter.write(line);
            }

            bidWriter.close();
            bidFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write the item-to-category mappings to Item2Category.dat
        try {
            FileWriter categorizeFile = new FileWriter("Item2Category.dat");
            BufferedWriter categorizeWriter = new BufferedWriter(categorizeFile);

            for(Map.Entry<String, Categorize> entry: categorizeMap.entrySet()) {
                Categorize mapping = entry.getValue();
                String line = String.format("%d |*| %d\n", mapping.itemID, mapping.categoryID);
                categorizeWriter.write(line);
            }

            categorizeWriter.close();
            categorizeFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
