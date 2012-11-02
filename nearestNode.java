import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.DriverManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
import org.gephi.preview.api.ColorizerFactory;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.ColorTransformer;
import org.gephi.ranking.api.EdgeRanking;
import org.gephi.ranking.api.NodeRanking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.SizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.netbeans.modules.masterfs.providers.Attributes;
import org.openide.util.Lookup;

public class nearestNode {

	public static void main(String[] argv) {

		   BufferedReader reader;
		   reader = new BufferedReader(new InputStreamReader(System.in));
		   System.out.println("Enter the URL required: ");
		   String url_name = null;
		   try {
			url_name = reader.readLine();
			//Throw out any extra backslashes
			if(url_name.endsWith("/"))
				url_name = url_name.substring(0, url_name.length()-1);
			}
		   catch (IOException e1) {
			e1.printStackTrace();
			} 
		   
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        PreviewModel pModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        
        //Import database
        EdgeListDatabaseImpl db = new EdgeListDatabaseImpl();
        db.setDBName("test");
        db.setHost("localhost");
        db.setUsername("root");
        db.setPasswd("root");
        db.setSQLDriver(new MySQLDriver());
        db.setPort(3306);

        //multiple queries
        Connection conn = null;
		 try {
			 //Connecting
			 String userName = "root";
             String password = "root";
             Statement stmt;
             String url = "jdbc:mysql://localhost/test";
             Class.forName ("com.mysql.jdbc.Driver").newInstance();
             conn = DriverManager.getConnection (url, userName, password);
             System.out.println ("Database connection established");
            
             //executing query
             stmt = conn.createStatement();
			 String getID_NameQ = "select id from url_crawler where url = \"" + url_name+"\"";
			 ResultSet rs2 = stmt.executeQuery(getID_NameQ);
			 rs2.next();
			 int temp_id = rs2.getInt(1);
			 System.out.println(temp_id);
			 //incoming edges and outgoing edges
			 String tempQ = "SELECT link.id_2 AS source," +
				 		" link.id_1 AS target," +
				 		" link.http_access AS label" +
				 		" FROM link Where id_2 = "+temp_id+" or id_1 = "+ temp_id;
			 
			 //prepare for the second level
			 /*
			 ResultSet rs3 = stmt.executeQuery("select id_1 from link where id_2 = " + temp_id);
			 
			 //prepare query for the second level links
			 int temp_child = 0;
			 while(rs3.next()) {
				 temp_child = rs3.getInt(1);
				 tempQ = tempQ.concat(" or id_2 = "+temp_child);
			 }
			 System.out.println(tempQ);
			 */
			 //get all the links for Gephi
			 
			 db.setEdgeQuery(tempQ);
			  
            conn.close();
		 }
		 catch (Exception e) {
             System.err.println (e.getMessage());
         }

        ImporterEdgeList edgeListImporter = new ImporterEdgeList();
        Container container = importController.importDatabase(db, edgeListImporter);
        //container.setAllowAutoNode(false);      //Don't create missing nodes
        container.setAllowAutoNode(true);
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
        //AttributeColumn a = attributeModel.getNodeTable().getColumn("indexed");
        //NodeRanking degreeRanking = rankingController.getRankingModel().getNodeAttributeRanking(a);
        NodeRanking degreeRanking = rankingController.getRankingModel().getInDegreeRanking();
        ColorTransformer colorTransformer = rankingController.getObjectColorTransformer(degreeRanking);
        colorTransformer.setColors(new Color[]{new Color(0xA4FFF9), new Color(0xFFFF00), new Color(0xF72500)});
        rankingController.transform(colorTransformer);
        
        //Get Centrality
        /*
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel, attributeModel);
        
        //Rank size by centrality
        AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        NodeRanking centralityRanking = rankingController.getRankingModel().getNodeAttributeRanking(centralityColumn);
        SizeTransformer sizeTransformer = rankingController.getObjectSizeTransformer(centralityRanking);
        sizeTransformer.setMinSize(3);
        sizeTransformer.setMaxSize(10);
        rankingController.transform(sizeTransformer);
        
        //Preview
        pModel.getNodeSupervisor().setShowNodeLabels(Boolean.TRUE);
        ColorizerFactory colorizerFactory = Lookup.getDefault().lookup(ColorizerFactory.class);
        pModel.getSelfLoopSupervisor().setColorizer((EdgeColorizer) colorizerFactory.createCustomColorMode(Color.RED));
        pModel.getUniEdgeSupervisor().setEdgeScale(0.1f);
        pModel.getBiEdgeSupervisor().setEdgeScale(0.1f);
        pModel.getNodeSupervisor().setBaseNodeLabelFont(pModel.getNodeSupervisor().getBaseNodeLabelFont().deriveFont(8));
        */
        
        //Export full graph
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("C://Users//Doa//Desktop//io_gexf.gexf"));
            //ec.exportFile(new File("C://Users//Doa//Desktop//io_gexf.pdf"));
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        
       
    }
}
