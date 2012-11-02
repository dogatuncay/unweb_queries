import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.Queue;

class count_links {
	
	private static int count;
	
	int get_next_level(int url_id, int depth)
	throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
        String userName = "root";
        String password = "root";
        int http_access = 0;
        
        //Assign the values to queue; sort of BFS
        Queue<Integer> queue = new LinkedList<Integer>();
        
        try
        {
           //connecting to the database
           //String url = "jdbc:mysql://localhost/test";
           String url = "jdbc:mysql://localhost:3306/B669";
           Class.forName ("com.mysql.jdbc.Driver").newInstance();
		   Connection conn = DriverManager.getConnection (url, userName, password);
        
		
		   //Search where all the connected indices
		   String get_level_stmt = "SELECT id_1, http_access FROM Link WHERE id_2 = ?";
		   PreparedStatement retrieve_parent = conn.prepareStatement(get_level_stmt);
		   retrieve_parent.setInt(1, url_id);
		   ResultSet rs_level = retrieve_parent.executeQuery();

		   while (rs_level.next())
		   {
			   System.out.println(rs_level.getInt(1));
			   queue.add(rs_level.getInt(1));
			   
			   try
			   {
			   	  http_access = rs_level.getInt(2);
			      if(http_access/100==4)
			   	  {
			   		  System.out.println("Here");
			   		  count++;
			   	  }
			   }
			   catch(Exception e) {System.out.println("No access code");}
		   }
		   
		   //depth >= 1 because one level is already done
		   while(queue.peek() != null && depth >=1)
		   {
			   get_next_level(queue.remove(), (depth - 1));
			  
		   }
			    
		   conn.close();
        }
        catch(SQLException ex)
        {
     	   System.err.println("SQLException: " + ex.getMessage());   
        }
        return count; 
	}
	
}


public class Broken_Links {

	public static void main(String[] args) 
	{
		Connection conn = null;
        try
        {
            String userName = "root";
            String password = "root";
			int output = 0;
            
            
            String url = "jdbc:mysql://localhost/B669";
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
            
            try
            {
       		   BufferedReader reader;
       		   reader = new BufferedReader(new InputStreamReader(System.in));
  
       		   //Enter the enter root URL path
       		   System.out.println("Enter the URL required:- \n");
       		   String url_name;
       		   url_name = reader.readLine(); 

       	 	   if (url_name.endsWith("/")) {
	    		  url_name = url_name.substring(0, url_name.length()-1);
	    	   }
       		   
       		   //Retrieve the index
       		   String get_id_stmt = "SELECT id FROM url_crawler WHERE url = ?";
       		   PreparedStatement retrieve_id = conn.prepareStatement(get_id_stmt);
       		   retrieve_id.setNString(1, url_name);
       		          		   
			   ResultSet rs_id = retrieve_id.executeQuery();
			   rs_id.first();
			   
			   int url_id = rs_id.getInt("id");
       		   System.out.println("Printing the id for current id: "+url_id);
       		   System.out.println();
       		   
       		   count_links c = new count_links(); 
       		   
       		   //n indicates a level of url_crawler = n+1 
       	       output = c.get_next_level(url_id, 2);
       	       System.out.println();
       	       System.out.println("Final Count of the type of 4XX links: " +output);
       		
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
                    System.out.println();
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
