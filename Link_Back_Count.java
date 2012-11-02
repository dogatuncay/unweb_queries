import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.Queue;

class count_broken {
	
	private static int count;
	
	int redirect_get_next_level(int url_id, int depth, int url_id_constant)
	throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
        String userName = "root";
        String password = "root";
        
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
		   String get_level_stmt = "SELECT id_1 FROM Link WHERE id_2 = ?";
		   PreparedStatement retrieve_parent = conn.prepareStatement(get_level_stmt);
		   retrieve_parent.setInt(1, url_id);
		   ResultSet rs_level = retrieve_parent.executeQuery();

		   while (rs_level.next())
		   {
			   //System.out.println(rs_level.getInt(1));
			   try
			   {
			      if(rs_level.getInt(1) == url_id_constant)
			   	  {
			    	  //System.out.println("-->"+ rs_level.getInt(1));
			   		  //System.out.println("Redirect Here");
			   		  count++;
			   	  }
			   }
			   catch(Exception e) {System.out.println("Invalid input");}
			   queue.add(rs_level.getInt(1));
		   }
		   
		   //depth >= 1 because one level is already done
		   while(queue.peek() != null && depth >=1)
		   {
			   redirect_get_next_level(queue.remove(), (depth - 1), url_id_constant);
			  
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


public class Link_Back_Count {

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
       		   
       		   count_broken c = new count_broken(); 
       		   
       		   //n indicates a level of url_crawler = n+1 
       	       output = c.redirect_get_next_level(url_id, 2, url_id);
       	       System.out.println();
       	       if (output >= 0)
       	    	   System.out.println("There are atleast " +output+" redirect/s back to the initial URL.");
       	       else
       	    	   System.out.println("No redirects.");
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
