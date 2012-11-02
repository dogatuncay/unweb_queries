import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

class read_for_link
{
	   @SuppressWarnings({ "deprecation", "null" })
	   void readlinkdata()
	   {
		   try
		   {
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
					   String child_url = null, parent_url = null, http_access_code = null;
			   
					   //Read File Line By Line
					   while (in.available() != 0)
					   {
						   count = 0;
						   StringTokenizer st = new StringTokenizer(in.readLine());
						   while (st.hasMoreTokens()) {
							   count = count + 1;
							   String token = st.nextToken();
				            
							   switch(count) {
				            		case 2://child URL
				            			child_url = token;
				            			System.out.println("Accessed URL = " + token);
				            			break;
				            		case 6://Access code
				            			http_access_code = token;
				            			System.out.println("HTTP Access Code = " + token);
				            			break;
				            		case 7://parent URL
				            			parent_url = token;
				            			System.out.println("Parent URL = " + token);
				            			break;
							   }
						   }
						   //call insert
						   if(!parent_url.equals("NA")) {
							   //remove the last "/" from url
							   if(child_url.endsWith("/")) {
								   child_url = child_url.substring(0, child_url.length()-1);
							   }
							   if(parent_url.endsWith("/")) {
								   parent_url = parent_url.substring(0, parent_url.length()-1);
							   }
				    	 
							   insert_link_table ins = new insert_link_table();
							   ins.inserting_url_link(child_url, parent_url, http_access_code);
						   }
						   //Re-initialize before looping again
						   child_url = null;
						   parent_url = null;
						   http_access_code = null;
						   System.out.println("-------------------------------------------------");
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

//For inserting data into link table table;
class insert_link_table
{
	   void inserting_url_link(String child_url_name, String parent_url_name,  String http_access)
	   {
		   try
		   {
               String userName = "root";
               String password = "root";
               
               int id_child = 0;
               int id_parent = 0;
               //connecting to the database
               //String url = "jdbc:mysql://localhost/test";
               String url = "jdbc:mysql://localhost:3306/B669";
               Class.forName ("com.mysql.jdbc.Driver").newInstance();
			   Connection conn = DriverManager.getConnection (url, userName, password);
			   
			   //Getting the child id from the url_crawler-----------------
			   //preparing query
			   Statement stmt = null;
			   stmt = conn.createStatement();
			   String query = "select id from url_crawler where url = \""+child_url_name+"\"";
			   //executing query and get the id_1
			   ResultSet rs1 = stmt.executeQuery(query);
			   rs1.next();
			   id_child = rs1.getInt("id");
			   
			   //Getting the parent id from the url_crawler-----------------
			   //preparing query
			   String get_id_parent_stmt = "SELECT id FROM url_crawler WHERE url = ?";
			   PreparedStatement retrieve_parent = conn.prepareStatement(get_id_parent_stmt);
			   retrieve_parent.setString(1, parent_url_name);
			   //executing query and get the id_2
			   ResultSet rs2 = retrieve_parent.executeQuery();
			   rs2.next();
			   id_parent = rs2.getInt("id");
			   System.out.println(id_parent +" -> "+ id_child);
			   
			   //Inserting into link table-----------------------------------
			   //preparing query
			   String insert_query = "INSERT INTO Link(id_1, id_2, http_access) values (?,?,?)";
			   PreparedStatement pstmt = conn.prepareStatement(insert_query);
			   pstmt.setInt(1, id_child);
			   pstmt.setInt(2, id_parent);
			   pstmt.setString(3, http_access);
			   //executing query and close the connection
			   pstmt.executeUpdate();
			   pstmt.close();
			   conn.close();
		   }
		   catch(Exception url_err)
		   {
			   System.err.println(url_err.getMessage());
		   }
	   }
}

public class link_table 
{
    public static void main (String[] args)
    {
        Connection conn = null;
        try
        {
            String userName = "root";
            String password = "root";
            Statement stmt;
            
            //connect to database
            String url = "jdbc:mysql://localhost/B669";
            //String url = "jdbc:mysql://localhost/test";
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
            System.out.println ("Creating the Link table");
            
            try
            {
         	   stmt = conn.createStatement();
         	   String query = "CREATE TABLE IF NOT EXISTS Link (" +
         	   "id_1 INT UNSIGNED not null, " +
               "id_2 INT UNSIGNED not null, "+
               "http_access VARCHAR(5), "+
               "FOREIGN KEY(id_1) REFERENCES url_crawler(id)," +
               "FOREIGN KEY(id_2) REFERENCES url_crawler(id)," +
               "PRIMARY KEY(id_1, id_2))";
         	   
                stmt.executeUpdate(query);
                System.out.println("Printing all the data first before inserting.....");
                System.out.println(".........");
                System.out.println("-------------------------------------------------");
                
                read_for_link read_links = new read_for_link();
                read_links.readlinkdata();
            }
            catch(SQLException ex)
            {
         	   System.err.println("SQLException: " + ex.getMessage());   
            }	   
        }
        catch (Exception e)
        {
            System.err.println (e.getMessage());
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close ();
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