-- Find the number of users in the database
SELECT COUNT(*) FROM User;

-- Find the number of items in "New York"
SELECT COUNT(*) FROM Item WHERE Seller IN 
	(SELECT UserID FROM User WHERE Location = "New York");

-- Find the number of auctions belonging to exactly four categories
SELECT COUNT(*) FROM 
	(SELECT ItemID, COUNT(CategoryID) AS cateCount FROM Categorize GROUP BY ItemID HAVING cateCount = 4) AS cate4items;

-- Find the ID of current autions with the highest bid. 
SELECT ItemID FROM Item WHERE End > '2001-12-20 00:00:01' AND Currently = 
	(SELECT MAX(Currently) FROM Item WHERE End > '2001-12-20 00:00:01');

-- Find the number of sellers whose rating is higher than 1000
SELECT COUNT(*) FROM User INNER JOIN Item ON User.UserID = Item.Seller WHERE User.Rating > 1000;

-- Find the number of users who are both seller and bidders
SELECT COUNT(*) FROM User WHERE UserID IN
	(SELECT Item.Seller FROM Item INNER JOIN Bid ON Item.Seller = Bid.Bidder);

-- Find the number of categories that include at least one item with a bid of more than $100
SELECT COUNT(*) FROM Category WHERE CategoryID IN
	(SELECT CategoryID FROM Categorize INNER JOIN Bid ON Categorize.ItemID = Bid.ItemID 
	WHERE Bid.Amount > 100);