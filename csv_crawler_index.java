import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;

import com.csvreader.CsvReader;

public class csv_crawler_index {
	public static void main(String[] args) throws InstantiationException,
	       IllegalAccessException, ClassNotFoundException, SQLException {
		try {
			
		  	System.out.println("* inserting_url_crawler v");
		    String userName = "root";
	        String password = "root";
	        String url = "jdbc:mysql://localhost:3306/B669";
	        Class.forName ("com.mysql.jdbc.Driver").newInstance();
	        Connection conn = DriverManager.getConnection (url, userName, password);
		  
	        CsvReader products = new CsvReader("/home/darkprince/workspace/Index/output.csv");
			products.readHeaders();

			while (products.readRecord())
			{
				String crawler_url = products.get(0);
				String index_value = products.get(1);

				// perform program logic here
				if (crawler_url.startsWith("https://"))
					System.out.println(crawler_url + ":" + index_value);
				else
				{
					crawler_url  = "http://" + crawler_url;
					System.out.println(crawler_url + " :::  " + index_value);
				}
				
				if (index_value.equals("true"))
				{
					System.out.println("Update Index for this...");
					String update_index = "UPDATE url_crawler " +
										  "SET indexed = 1 WHERE url=?";
					PreparedStatement updateindex = conn.prepareStatement(update_index);
					updateindex.setString(1, crawler_url);
					updateindex.executeUpdate();
				}
				
			}
			
			products.close();
			conn.close();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
