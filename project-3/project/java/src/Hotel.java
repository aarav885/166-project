/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Hotel {
   private static int current_user;
   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Hotel 
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Hotel(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Hotel

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement ();

      ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   public int getNewUserID(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement ();
      ResultSet rs = stmt.executeQuery (sql);
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }
   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Hotel.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Hotel esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Hotel object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Hotel (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
				  String userType = esql.executeQueryAndReturnResult("SELECT userType FROM Users WHERE userID = " + authorisedUser + ";").get(0).get(0).trim();
				  System.out.println("\"" + userType + "\""); 
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Hotels within 30 units");
                System.out.println("2. View Rooms");
                System.out.println("3. Book a Room");
                System.out.println("4. View recent booking history");

                //the following functionalities basically used by managers
                System.out.println("5. Update Room Information");
                System.out.println("6. View 5 recent Room Updates Info");
                System.out.println("7. View booking history of the hotel");
                System.out.println("8. View 5 regular Customers");
                System.out.println("9. Place room repair Request to a company");
                System.out.println("10. View room repair Requests history");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewHotels(esql); break;
                   case 2: viewRooms(esql); break;
                   case 3: bookRooms(esql); break;
                   case 4: viewRecentBookingsfromCustomer(esql,authorisedUser); break;
                   case 5: if(!userType.equals("manager")){System.out.println("Only managers have access to this function");}else {updateRoomInfo(esql, authorisedUser);} break;
                   case 6: if (!userType.equals("manager")) {System.out.println("Only managers have access to this function");} else {viewRecentUpdates(esql, authorisedUser);} break;
                   case 7: if (!userType.equals("manager")) {System.out.println("Only managers have access to this function");} else {viewBookingHistoryofHotel(esql, authorisedUser);} break;
                   case 8: if (!userType.equals("manager")) {System.out.println("Only managers have access to this function");} else {viewRegularCustomers(esql, authorisedUser);} break;
                   case 9: if (!userType.equals("manager")) {System.out.println("Only managers have access to this function");} else {placeRoomRepairRequests(esql, authorisedUser);} break;
                   case 10: if (!userType.equals("manager")) {System.out.println("Only managers have access to this function");} else {viewRoomRepairHistory(esql, authorisedUser);} break;
                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Hotel esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine(); 
         String type="Customer";
			String query = String.format("INSERT INTO USERS (name, password, userType) VALUES ('%s','%s', '%s')", name, password, type);
         esql.executeUpdate(query);
         System.out.println ("User successfully created with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
         
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Hotel esql){
      try{
         System.out.print("\tEnter userID: ");
         String userID = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE userID = '%s' AND password = '%s'", userID, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            current_user = userNum;
            return userID;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here
	public static int getIntInput(String prompt) {
		int input;

		do {
			System.out.print(prompt);
			try {
				input = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		} while (true);
		return input;
	}
   public static void viewHotels(Hotel esql) {
      //returns all hotels that are located within 30 units of the users inputted coordinates
         try{
      System.out.print("\tPlease enter your latitude: ");
      double userLat = Double.parseDouble(in.readLine());
      System.out.print("\tPlease enter your longtitude: ");
      double userLong = Double.parseDouble(in.readLine());
      String query = String.format("SELECT h.hotelName FROM Hotel h WHERE calculate_Distance(%s,%s,h.latitude,h.longitude)<=30",userLat,userLong);
      int rowCount = esql.executeQueryAndPrintResult(query);
      System.out.println("Total row(s): "+rowCount);
      }
      catch(Exception e){
         System.out.println(e.getMessage());
      }
   }

      public static void viewRooms(Hotel esql) {
         //checks to see if a room is booked on a given date, and then returns all the rooms that are not booked on that date.
      try{
         System.out.print("\tPlease enter a Hotel ID: ");
         int hotel_id = Integer.parseInt(in.readLine());
         System.out.print("\tPlease enter a date(YYYY-MM-DD): ");
         String user_date = in.readLine();
         String query = String.format("SELECT DISTINCT r.roomNumber, r.price FROM Rooms r WHERE  r.hotelID = '%s' AND r.roomNumber NOT IN(SELECT rb.roomNumber FROM RoomBookings rb WHERE rb.bookingDate = '%s')",hotel_id,user_date);
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Total row(s): "+rowCount);
      }
      catch(Exception e){
         System.out.println(e.getMessage());
      }
   }

   public static void bookRooms(Hotel esql){
      try{
         System.out.print("\tPlease enter the Hotel's ID: ");
         int hotel_id = Integer.parseInt(in.readLine());
         System.out.print("\tPlease enter a room number: ");
         int room_num = Integer.parseInt(in.readLine());
         System.out.print("\tPlease enter the date that you wish to book the room(YYYY-MM-DD): ");
         String user_date = in.readLine();

         //String check_avail = String.format("SELECT  r.roomNumber FROM Hotel h, Rooms r, RoomBookings b WHERE h.hotelID = '%s' AND r.roomNumber = '%s' AND b.bookingDate ='%s'AND r.roomNumber IN(SELECT rb.roomNumber FROM RoomBookings rb WHERE rb.bookingDate ='%s')",hotel_id,room_num,user_date,user_date);
         String check_avail = String.format("SELECT b.roomNumber FROM RoomBookings b WHERE b.hotelID = '%s' AND b.roomNumber = '%s' AND b.bookingDate = '%s'",hotel_id,room_num,user_date);
         if(esql.executeQuery(check_avail)!=0){
             System.out.println("Room "+room_num+" at Hotel "+hotel_id+" is not available on "+user_date);
             }
         else{
            System.out.println("Room is available, do you wish to book the room? (Yes/No)");
            String doesBook = in.readLine();
            String yes = "Yes";
            if(doesBook.equals(yes)){
               System.out.print("Please enter your Customer ID. Please note that this is sensitive, otherwise your booking will be under another's ID: ");
               int custID = Integer.parseInt(in.readLine());
               String insertBooked =String.format("INSERT INTO RoomBookings(bookingID,customerID,hotelID,roomNumber,bookingDate) VALUES(DEFAULT,'%d','%d','%d','%s');",custID,hotel_id,room_num,user_date);
               esql.executeUpdate(insertBooked);
               System.out.println("Room booked, price is: ");
               String getPrice = String.format("SELECT DISTINCT r.price FROM Rooms r, RoomBookings b WHERE r.hotelID = '%s' AND r.roomNumber ='%s' AND r.roomNumber = b.roomNumber",hotel_id,room_num);
               int rowCount = esql.executeQueryAndPrintResult(getPrice);
               System.out.println("Total Row(s): "+rowCount);
               }

            }
         }
        catch(Exception e){
        System.out.println(e.getMessage());
        }
     }


   public static void viewRecentBookingsfromCustomer(Hotel esql, String userID) {
      try{
         String recentBookings = String.format("SELECT b.hotelID, b.roomNumber, r.price, b.bookingDate FROM RoomBookings b, Rooms r WHERE b.customerID = '%s' AND b.roomNumber = r.roomNumber AND b.hotelID = r.hotelID ORDER BY b.roomNumber DESC LIMIT 5",userID);
         int rowCount = esql.executeQueryAndPrintResult(recentBookings);
         System.out.println("Total Row(s): "+rowCount);
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   public static void updateRoomInfo(Hotel esql, String userID) {
      try{
         boolean doesManage = false;
         System.out.print("Please enter the Hotel's ID: ");
         int hotel_id = Integer.parseInt(in.readLine());
         String hotelsTheyManage = "SELECT h.hotelID FROM Hotel h WHERE h.managerUserID= "+userID;
         if(esql.executeQuery(hotelsTheyManage) == 0){
            System.out.println("You don't manage this hotel!");
         }
         else{
            doesManage = true;
         }
         while(doesManage){
            System.out.print("Please enter the room number: ");
            int room_num = Integer.parseInt(in.readLine());
            String yes = "Yes";
            System.out.print("Do you want to update the price of this room?(Yes/No): ");
            String priceUpdate = in.readLine();
            if(priceUpdate.equals(yes)){
               System.out.print("Please enter the new price of the room: ");
               int newPrice = Integer.parseInt(in.readLine());
               String changedPrice = String.format("UPDATE Rooms  SET price = '%s' WHERE hotelID = '%s' AND roomNumber = '%s'",newPrice,hotel_id,room_num);
               String updatePriceLog = String.format("INSERT INTO RoomUpdatesLog(managerID,hotelID,roomNumber,updatedOn) VALUES('%s','%s','%s',CURRENT_TIMESTAMP)",userID,hotel_id,room_num);
               esql.executeQuery(changedPrice);
            }
            System.out.print("Do you want to update the image of this room?(Yes/No): ");
            String imageUpdate = in.readLine();
            if(imageUpdate.equals(yes)){
               System.out.print("Please enter the new imageURL: ");
               String newImage = in.readLine();
               String changedImage = String.format("UPDATE Rooms SET imageURL = '%s' WHERE hotelID ='%s' AND roomNumber = '%s'",newImage,hotel_id,room_num);
               esql.executeQuery(changedImage);
            }
            System.out.print("Do you want to change more rooms?(Yes/No): ");
            String cont = in.readLine();
            if(!(cont.equals(yes))){
               doesManage = false;
            }
         }
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   
   public static void viewRecentUpdates(Hotel esql, String userID) {
      try {
			String hotelsTheyManage = "SELECT hotelID FROM Hotel WHERE managerUserID = " + userID;
         String query = "SELECT H.hotelName, R.hotelID, price, imageURL, U.updatedOn FROM RoomUpdatesLog U INNER JOIN Rooms R ON U.hotelID = R.hotelID AND U.roomNumber = U.roomNumber INNER JOIN Hotel H ON R.hotelID = H.hotelID WHERE U.hotelID IN (" + hotelsTheyManage + ") ORDER BY U.updatedOn DESC LIMIT 5;";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewBookingHistoryofHotel(Hotel esql, String userID) {
      try {
			int hotelID = getIntInput("Enter hotelID: ");
			String manageQuery = String.format("SELECT hotelID FROM Hotel WHERE hotelID = %s AND managerUserID = '%s'", hotelID, userID);
		   boolean manageHotel = !esql.executeQueryAndReturnResult(manageQuery).isEmpty(); 	
			
			if (!manageHotel) {
				System.out.println("You do not manage this hotel");
			} else {
         	System.out.println("1. See all booking information");
         	System.out.println("2. See booking information in a date range");

         	String query = "SELECT B.bookingID, U.name, B.hotelID, B.roomNumber, B.bookingDate FROM RoomBookings B INNER JOIN Users U ON B.customerID = U.userID WHERE B.hotelID = " + hotelID;
         	switch(readChoice()) { 
            	case 1: 
               	break;

            	case 2:
               	System.out.print("\tEnter start date (format: YYYY-MM-DD): ");
               	String startDate = in.readLine() + " 00:00:00";

               
               	System.out.print("\tEnter end date (format: YYYY-MM-DD): ");
               	String endDate = in.readLine() + " 23:59:59";

               	query += String.format(" AND B.bookingDate BETWEEN '%s' AND '%s'", startDate, endDate);
               	break;
            	default:
               	throw new Exception("Unrecognized choice.");
         	}
         	query += ";";
         
         	int rowCount = esql.executeQueryAndPrintResult(query);
         	System.out.println("total row(s): " + rowCount);
			}
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void viewRegularCustomers(Hotel esql, String userID) {
      try {
		         
         String query = "SELECT DISTINCT B.customerID, U.name FROM RoomBookings B INNER JOIN Users U ON B.customerID = U.userID WHERE B.customerID IN (SELECT B2.customerID FROM RoomBookings B2 GROUP BY B2.customerID ORDER BY COUNT(B2.customerID) DESC LIMIT 5);";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void placeRoomRepairRequests(Hotel esql, String userID) {
      try {
			int hotelID = getIntInput("Enter hotel ID: ");
			String manageQuery = String.format("SELECT hotelID FROM Hotel WHERE hotelID = %s AND managerUserID = '%s'", hotelID, userID);
		   boolean manageHotel = !esql.executeQueryAndReturnResult(manageQuery).isEmpty(); 	
        	while (!manageHotel && hotelID != -1) {	
				//hotelID = getIntInput("Enter hotel ID (-1 to exit): ");
				if (hotelID != -1) {
					System.out.println("You do not manage this hotel");
					hotelID = getIntInput("Enter hotel ID (-1 to exit): ");
					manageQuery = String.format("SELECT hotelID FROM Hotel WHERE hotelID = %s AND managerUserID = '%s'", hotelID, userID);
		   		manageHotel = !esql.executeQueryAndReturnResult(manageQuery).isEmpty(); 	
				} else {
					hotelID = getIntInput("Enter hotel ID (-1 to exit): ");
					return;
				}
			}
			int roomNumber = getIntInput("Enter room number: ");
			int companyID = getIntInput("Enter maintenance company ID: ");
			String updateRoomRepairs = String.format("INSERT INTO RoomRepairs(companyID, hotelID, roomNumber, repairDate) VALUES (%s, %s, %s, CURRENT_TIMESTAMP);", companyID, hotelID, roomNumber);
			String findLatestRepairQuery = "SELECT MAX(repairID) FROM RoomRepairs;";
         
			esql.executeUpdate(updateRoomRepairs);
			int repairID = Integer.parseInt(esql.executeQueryAndReturnResult(findLatestRepairQuery).get(0).get(0));
			
			String updateRoomRepairRequests = String.format("INSERT INTO RoomRepairRequests(managerID, repairID) VALUES (%s, %s);", userID, repairID);
			esql.executeUpdate(updateRoomRepairRequests);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void viewRoomRepairHistory(Hotel esql, String userID) {
      try {
         String query = String.format("SELECT companyID, hotelID, roomNumber, repairDate FROM RoomRepairs A INNER JOIN RoomRepairRequests B ON A.repairID = B.repairID WHERE managerID = '%s';", userID);
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

}//end Hotel

