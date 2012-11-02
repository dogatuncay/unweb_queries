   import java.sql.*;
   import java.io.*;
   import java.net.*;
   import java.util.StringTokenizer;
   import java.sql.PreparedStatement;
   import java.sql.DriverManager;
   import java.sql.Connection;
   
   class readcrawler 
   {
	   @SuppressWarnings("deprecation")
	   void readcrawldata()
	   {
		   try
		   {
			   System.out.println("* read crawl data v");
			   File folder = new File("/home/darkprince/workspace/Data");
			   File[] listOfFiles = folder.listFiles();
			   
			   for (int i = 0; i < listOfFiles.length; i++)
			   {
				   if (listOfFiles[i].isFile()) 
				   { 
			   
					   FileInputStream fstream = new FileInputStream(listOfFiles[i].getPath());
					   //FileInputStream fstream = new FileInputStream("C:/Users/Eric/Desktop/b669/1.txt");
					   DataInputStream in = new DataInputStream(fstream);
					   int count = 0;
			   
					   //Read File Line By Line
					   while (in.available() != 0)
					   {
						   count = 0;
						   StringTokenizer st = new StringTokenizer(in.readLine());
						   while (st.hasMoreTokens())
						   {
							   count = count + 1;
							   String token = st.nextToken();
							   if (count == 2)
							   {   //once we see the URL, we insert it into the database
								   System.out.println("Accessed URL = " + token);
								   insert_url_crawler_table ins = new insert_url_crawler_table();
								   ins.inserting_url_crawler(token, 0);
					               //After we inserted the URL, we don't need to read the rest of data from this line.
								   //Therefore, break the while loop and read the next line of input.
								   break;
							   }				            
						   }
						   System.out.println("* read crawl data ^");
					   }
				   }
			   }
		   }
		   catch (Exception e)
		   { 
			   System.err.println("Error: " + e.getMessage());
		   }
	   }
   }
   
   //For inserting data into url_crawler table; this takes the input of the crawled data
   class insert_url_crawler_table
   {
	   void inserting_url_crawler(String url_name, int index)
	   {
		   try
		   {
               System.out.println("* inserting_url_crawler v");
			   String userName = "root";
               String password = "root";
               int dot = 0;
               //connect to the database
               //String url = "jdbc:mysql://localhost:3306/test";
               String url = "jdbc:mysql://localhost:3306/B669";
               Class.forName ("com.mysql.jdbc.Driver").newInstance();
			   Connection conn = DriverManager.getConnection (url, userName, password);
			   String insert_query = "INSERT INTO url_crawler(url, secured, tld, indexed) values (?,?,?,?)";
			   URL a_URL = new URL(url_name);

			   StringTokenizer tld = new StringTokenizer(a_URL.getHost(),".");
			   //Inserting the values one by one
			   PreparedStatement pstmt = conn.prepareStatement(insert_query);

			   //remove the last "/" from url since for example, "www.google.com/" === "www.google.com"
			   if(url_name.endsWith("/"))
				   url_name = url_name.substring(0, url_name.length()-1);

			   //set url_name and protocol
			   pstmt.setString(1, url_name);
			   pstmt.setString(2, a_URL.getProtocol());
			   
			   //set the tld value
			   while(tld.hasMoreTokens())
			   {
				   dot = dot + 1;
				   String tld_value = tld.nextToken();
				   if (!tld.hasMoreTokens())
                      pstmt.setString(3, tld_value);
			   }
			   //set the indexed? value
			   pstmt.setInt(4,0);
			   //execute the query and close the connection
			   pstmt.executeUpdate();
			   pstmt.close();
			   conn.close();
			   System.out.println("** inserted! **");
			   System.out.println("* inserting_url_crawler ^\n");
		   }
		   catch(Exception url_err)
		   {
			   System.err.println(url_err.getMessage());
		   }
	   }
   }

   public class url_crawler
   {
       public static void main (String[] args)
       {
           Connection conn = null;

           try
           {
               String userName = "root";
               String password = "root";
               
               Statement stmt;
               //connect to the database
               //String url = "jdbc:mysql://localhost:3306/test";
               String url = "jdbc:mysql://localhost:3306/B669";
               Class.forName ("com.mysql.jdbc.Driver").newInstance ();
               conn = DriverManager.getConnection (url, userName, password);
               System.out.println ("Database connection established!");
               
               try
               {
            	   stmt = conn.createStatement();
            	   String query = "CREATE TABLE IF NOT EXISTS url_crawler (" +
					                                    "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
					                                    "url VARCHAR(700), "+ //TEXT
					                                    "secured VARCHAR(10), "+
					                                    "tld VARCHAR(10), "+
					                                    "indexed INT(1), " +
					                                    "unique (url));";
            	   
                   stmt.executeUpdate(query);
                   //close connection
                   conn.close();
                   System.out.println ("url_crawler table created");
                   System.out.println("Printing all the data first before inserting.....");
                   System.out.println(".........");
                   System.out.println("-------------------------------------------------");
                   
                   readcrawler readc = new readcrawler();
                   readc.readcrawldata();
               }
               catch(SQLException ex) {
            	   System.err.println("SQLException: " + ex.getMessage());   
               }	   
           }
           catch (Exception e) {
               System.err.println (e.getMessage());
           }
           finally
           {
               if (conn != null)
               {
                   try
                   {
                       conn.close();
                       System.out.println ("Database connection terminated");
                   }
                   catch (Exception e) 
                   {
                	   System.err.println("SQLException: " + e.getMessage());
                   }
               }
           }
       }
   }