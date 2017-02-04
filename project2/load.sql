LOAD DATA LOCAL INFILE './User.dat' INTO TABLE User
	FIELDS TERMINATED BY ' |*| '
	LINES TERMINATED BY "\n"
	(UserID, Rating, @location, @country) 
	SET Location = nullif(@location, ''), Country = nullif(@country, '');

LOAD DATA LOCAL INFILE './Item.dat' INTO TABLE Item
	FIELDS TERMINATED BY ' |*| '
	LINES TERMINATED BY "\n"
	(ItemID, Name, Currently, @buy_Price, First_Bid, Number_Of_Bids, @start, @end, Seller, Description)
	SET Buy_Price = nullif(@buy_Price, ''), Start = STR_TO_DATE(@start, "%Y-%m-%d %H:%i:%s"), 
		End = STR_TO_DATE(@end, "%Y-%m-%d %H:%i:%s");

LOAD DATA LOCAL INFILE './Category.dat' INTO TABLE Category
	FIELDS TERMINATED BY ' |*| '
	LINES TERMINATED BY "\n";

LOAD DATA LOCAL INFILE './Item2Category.dat' INTO TABLE Categorize
	FIELDS TERMINATED BY ' |*| '
	LINES TERMINATED BY "\n";

LOAD DATA LOCAL INFILE './Bid.dat' INTO TABLE Bid
	FIELDS TERMINATED BY ' |*| '
	LINES TERMINATED BY "\n"
	(Bidder, ItemID, @time, Amount) SET Time = STR_TO_DATE(@time, "%Y-%m-%d %H:%i:%s");