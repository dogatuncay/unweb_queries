import java.awt.Color;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.database.drivers.MySQLDriver;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.gephi.io.importer.plugin.database.ImporterEdgeList;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.Colorizer;
import org.gephi.preview.api.ColorizerClient;
import org.gephi.preview.api.ColorizerFactory;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.NodeChildColorizer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.ColorTransformer;
import org.gephi.ranking.api.NodeRanking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.SizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import com.mysql.jdbc.Statement;

class eval {
	void gephiGraph(String query, String nQuery, String innerQuery) {
		//Gephi
		//Init a project - and therefore a workspace
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		//Get controllers and models
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		PreviewModel pModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
		
		//Import database for Gephi
        EdgeListDatabaseImpl db = new EdgeListDatabaseImpl();
        db.setDBName("test");
        db.setHost("localhost");
        db.setUsername("root");
        db.setPasswd("root");
        db.setSQLDriver(new MySQLDriver());
        db.setPort(3306);
        
        //query
        db.setEdgeQuery(query);
        db.setNodeQuery(innerQuery+" union "+nQuery);
        
        ImporterEdgeList edgeListImporter = new ImporterEdgeList();
        Container container = importController.importDatabase(db, edgeListImporter);
        container.setAllowAutoNode(true);//create missing nodes
        container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force UNDIRECTED
        
        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //See if graph is well imported
        DirectedGraph graph = graphModel.getDirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        //Layout - 100 Yifan Hu passes
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        
        //Rank color by Degree
        RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
        AttributeColumn a = attributeModel.getNodeTable().getColumn("indexed");
        NodeRanking degreeRanking = rankingController.getRankingModel().getNodeAttributeRanking(a);
        //NodeRanking degreeRanking = rankingController.getRankingModel().getDegreeRanking();
        ColorTransformer colorTransformer = rankingController.getObjectColorTransformer(degreeRanking);
        colorTransformer.setColors(new Color[]{new Color(0xB30000), new Color(0x0000B3)});
        rankingController.transform(colorTransformer);
        
        //Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel, attributeModel);
        
        //Rank size by centrality
        /*
        //AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        //NodeRanking centralityRanking = rankingController.getRankingModel().getNodeAttributeRanking(centralityColumn);
        NodeRanking centralityRanking = rankingController.getRankingModel().getOutDegreeRanking();
        SizeTransformer sizeTransformer = rankingController.getObjectSizeTransformer(centralityRanking);
        sizeTransformer.setMinSize(10);
        sizeTransformer.setMaxSize(12);
        rankingController.transform(sizeTransformer);
        */
        //Preview
        /*
        pModel.getNodeSupervisor().setShowNodeLabels(Boolean.TRUE);
        ColorizerFactory colorizerFactory = Lookup.getDefault().lookup(ColorizerFactory.class);
        //Set edges gray
        pModel.getUniEdgeSupervisor().setColorizer((EdgeColorizer) colorizerFactory.createCustomColorMode(Color.LIGHT_GRAY));
        //Set mutual edges and self loop red
        pModel.getBiEdgeSupervisor().setColorizer((EdgeColorizer) colorizerFactory.createCustomColorMode(Color.RED));
        pModel.getSelfLoopSupervisor().setColorizer((EdgeColorizer) colorizerFactory.createCustomColorMode(Color.RED));
        pModel.getUniEdgeSupervisor().setEdgeScale(0.1f);
        pModel.getBiEdgeSupervisor().setEdgeScale(0.1f);
        pModel.getNodeSupervisor().setBaseNodeLabelFont(pModel.getNodeSupervisor().getBaseNodeLabelFont().deriveFont(8));
        //Set nodes labels white
        pModel.getNodeSupervisor().setNodeLabelColorizer((NodeChildColorizer) colorizerFactory.createCustomColorMode(Color.BLUE));
        //background color
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        previewController.setBackgroundColor(Color.BLACK);
        */
        
        //Export full graph
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("C://Users//Eric//Desktop//io_gexf.gexf"));
            //ec.exportFile(new File("C://Users//Eric//Desktop//io_gexf.pdf"));
        }
        catch (IOException ex) {
            ex.printStackTrace();
            //return;
        }
	}
	
	void dest_get_next_level(int url_id, int dest_id, int depth)
	throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String userName = "root";
        String password = "root";
        String myQuery = "SELECT link.id_2 AS source, " +
        		"link.id_1 AS target, " +
        		"link.http_access AS label " +
        		"FROM link Where id_2 in (" + url_id;
        String nodeQuery = "SELECT url_crawler.id AS id, " +
        		"url_crawler.url AS label, " +
        		"url_crawler.indexed " +
        		"FROM url_crawler where id in (" + url_id;
        String innerQuery = "select url_crawler.id AS id, " +
        		"url_crawler.url AS label, " +
        		"url_crawler.indexed " +
        		"FROM url_crawler where id in (" +
        		"select distinct id_1 from link where id_2 in ("+ url_id;
        Queue<Integer> queue = new LinkedList<Integer>();
        LinkedList<Integer> tempQueue = new LinkedList<Integer>();//use for keeping track of the path
        LinkedList<Integer> copyQueue = new LinkedList<Integer>();//use for copy the element
        LinkedList<Integer> pathQueue = new LinkedList<Integer>();//path
        
        try {
           //connecting to the database
           String url = "jdbc:mysql://localhost:3306/test";
           Class.forName ("com.mysql.jdbc.Driver").newInstance();
		   Connection conn = DriverManager.getConnection (url, userName, password);
        
		   //Search where all the connected indices
		   String get_level_stmt = "SELECT id_1 FROM link WHERE id_2 = ?";
		   PreparedStatement retrieve_parent = conn.prepareStatement(get_level_stmt);
		   retrieve_parent.setInt(1, url_id);
		   ResultSet rs_level = retrieve_parent.executeQuery();

		   //insert into a queue
		   while (rs_level.next()) {
			   queue.add(rs_level.getInt(1));
			   tempQueue.add(url_id);
		   }
		   
		   //http://www.computer.org/portal/web/security/home
		   //http://www.cl.cam.ac.uk/users/sjm217
		   //https://www.easychair.org/account/signin.cgi?conf=fcsprivmod2010
		   //http://www.nytimes.com/2010/07/05/nyregion/05cricket.html?_r=2
		   //http://www.cl.cam.ac.uk/~srl32/
		   //
		   //http://www.mdcr.cz/en/HomePage.htm
		   int first_item;
		   int count = 0;
		   int ptr;
		   int level_size = queue.size();
		   while(!queue.isEmpty() && depth > 0) {
			   //get the first item in the queue and remove it
			   first_item = queue.remove();
			   copyQueue.add(first_item);//keep a copy of them so we can restore the path later
			   if(first_item == dest_id) {
				   //found it!
				   System.out.println("found it! ");
				   System.out.println(count);//print the queue size
				   pathQueue.add(dest_id);
				   ptr = tempQueue.get(copyQueue.indexOf(dest_id));
				   pathQueue.add(ptr);
				   while(ptr != url_id) {
					   ptr = tempQueue.get(copyQueue.indexOf(ptr));
					   pathQueue.add(ptr);
				   }
				   System.out.println("----- path ---------");
				   while(!pathQueue.isEmpty()) {
					   ptr = pathQueue.removeLast();
					   myQuery = myQuery.concat(", "+ptr);
					   nodeQuery = nodeQuery.concat(", "+ptr);
					   innerQuery = innerQuery.concat(", "+ptr);
					   System.out.println(ptr);
				   }
				   myQuery = myQuery.concat(")");
				   nodeQuery = nodeQuery.concat(")");
				   innerQuery = innerQuery.concat("))");
				   conn.close();       
			       //call gephi
			       gephiGraph(myQuery, nodeQuery, innerQuery);
				   System.exit(0);
			   }
			   else {
				   get_level_stmt = "SELECT id_1 FROM link WHERE id_2 = ?";
				   retrieve_parent = conn.prepareStatement(get_level_stmt);
				   retrieve_parent.setInt(1, first_item);
				   rs_level = retrieve_parent.executeQuery();
				   level_size = level_size - 1;
				   
				   //add its children to the end of the queue
				   while (rs_level.next()) {
					   //if(!queue.contains(rs_level.getInt(1))) {//this is too expensive!
					   		queue.add(rs_level.getInt(1));        //but could save some space
					   		tempQueue.add(first_item);
					   //}
				   }
				   //decrease the depth by one since we finished searching this level
				   if(level_size == 0) {
					   depth = depth - 1;
					   level_size = queue.size(); //reset the level size
				   }
			   }
			   count++;
			   if(count>160000) {
				   conn.close();
				   System.out.println("unable to find the item");
				   System.exit(0);
			   }
		   }
		   System.out.println("level reached!");
		   System.out.println("unable to find the item");
		   conn.close();
        }
        catch(SQLException ex) {
     	   System.err.println("SQLException: " + ex.getMessage());   
        }
	}
}

public class isConnect {
	public static void main(String[] args) {
		//eval e1 = new eval();
		Connection conn = null;
        try {
        	//e1.dest_get_next_level(1, 707, 5);
            String userName = "root";
            String password = "root";
            String url = "jdbc:mysql://localhost/test";
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
            
            try {
       		   BufferedReader reader;
       		   reader = new BufferedReader(new InputStreamReader(System.in));
       		   String line = null;
       		   String source = null;
       		   String target = null;
       		   int limit = 0;
       		   
       		   //Enter URL path
       		   System.out.println("Enter the URL required: ");
       		   source = reader.readLine(); 
       	 	   if (source.endsWith("/")) {
       	 		   source = source.substring(0, source.length()-1);
	    	   }
       		   
       	 	   //Enter the destination URL
       		   System.out.println("Enter the destination URL: ");
       		   target = reader.readLine(); 
       	 	   if (target.endsWith("/")) {
       	 		target = target.substring(0, target.length()-1);
	    	   }
       	 	   
       	 	   //set the limit
       	 	   System.out.println("Enter the level: ");
    		   line = reader.readLine();
    		   try {
    			   limit = Integer.parseInt(line);
    		   }
    		   catch(NumberFormatException e) {
    			   System.err.println("Not a valid number: "+line);
    		   }
    		   
       		   //Retrieve the index for source
       		   String get_id_stmt = "SELECT id FROM url_crawler WHERE url = ?";
       		   PreparedStatement retrieve_id = conn.prepareStatement(get_id_stmt);
       		   retrieve_id.setString(1, source);
			   ResultSet rs_id = retrieve_id.executeQuery();
			   rs_id.first();
			   int url_id = rs_id.getInt("id");
       		   System.out.println("Source id is "+url_id);
       		   
       		   //Retrieve the index for target
       		   String get_id_stmt_dest = "SELECT id FROM url_crawler WHERE url = ?";
       		   PreparedStatement dest_retrieve_id = conn.prepareStatement(get_id_stmt_dest);
       		   dest_retrieve_id.setString(1, target);
 		       ResultSet dest_rs_id = dest_retrieve_id.executeQuery();
		       dest_rs_id.first();
 		       int dest_url_id = dest_rs_id.getInt("id");
       		   System.out.println("Target id is "+dest_url_id);
       		   
       		   //call BFS
       		   eval e = new eval(); 
       		   if(limit >4) {
       			   System.out.println("Sorry, the maximum level is 4");
       			   System.exit(0);
       		   }
       	       e.dest_get_next_level(url_id, dest_url_id, limit);
       		}
            catch(SQLException ex) {
         	   System.err.println("SQLException: " + ex.getMessage());
            }
            
        }
        catch (Exception e) {
            System.err.println ("Error is "+e.getMessage());
        }
        finally {
            if (conn != null) {
                try {
                    conn.close ();
                    System.out.println();
                }
                catch (Exception e)  {
             	   System.err.println("SQLException: " + e.getMessage());
                }
            }

        }
        
	}  
}
