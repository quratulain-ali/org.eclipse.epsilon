package org.eclipse.epsilon.eol.staticanalyser;

import org.jgrapht.graph.DefaultDirectedGraph;

public class TestGraph {

	static DefaultDirectedGraph<String, RelationshipEdge> callGraph;
			
	public static void main(String[] args) {
		// constructs a directed graph with the specified vertices and edges
        
        callGraph = new DefaultDirectedGraph<String, RelationshipEdge>(RelationshipEdge.class);
        callGraph.addVertex("main");
        callGraph.addVertex("bar");
        callGraph.addVertex("foo");
        callGraph.addVertex("for");
        callGraph.addVertex("test");
        callGraph.addVertex("hello");

        callGraph.addEdge("main", "bar");
		callGraph.addEdge("bar", "hello");
		callGraph.addEdge("main", "foo");
		callGraph.addEdge("main", "for");
		callGraph.addEdge("for", "test");
		callGraph.addEdge("test", "foo");
		
		System.err.println(callGraph.toString());

//        // computes all the strongly connected components of the directed graph
//        StrongConnectivityAlgorithm<String, DefaultEdge> scAlg =
//            new KosarajuStrongConnectivityInspector<>(callGraph);
//        List<Graph<String, DefaultEdge>> stronglyConnectedSubgraphs =
//            scAlg.getStronglyConnectedComponents();
//
//        // prints the strongly connected components
//        System.out.println("Strongly connected components:");
//        for (int i = 0; i < stronglyConnectedSubgraphs.size(); i++) {
//            System.out.println(stronglyConnectedSubgraphs.get(i));
//        }
//        System.out.println(callGraph instanceof Graph);
//        
//        // Prints the shortest path from vertex i to vertex c. This certainly
//        // exists for our particular directed graph.
//        System.out.println("Shortest path from i to c:");
//        AllDirectedPaths<String, DefaultEdge> dijkstraAlg =
//            new AllDirectedPaths<>(callGraph);
//        System.out.println(dijkstraAlg.getAllPaths("main", "foo", true, null) + "\n");
////
////        // Prints the shortest path from vertex c to vertex i. This path does
////        // NOT exist for our particular directed graph. Hence the path is
////        // empty and the variable "path"; must be null.
////        System.out.println("Shortest path from main to test:");
////        SingleSourcePaths<String, DefaultEdge> cPaths = dijkstraAlg.getPaths("main");
//        System.out.println("Path contains loop : "+pathContainsLoop("main", "hello"));
//        
//      //Create the exporter (without ID provider)
//        DOTExporter<String, DefaultEdge> exporter=new DOTExporter<>(v -> v.toString());
//        Writer writer = new StringWriter();
//        exporter.exportGraph(callGraph, writer);
//        System.out.println(writer.toString());
        
        
    }
//	
//	public static boolean pathContainsLoop(String source, String destination){
//		boolean pathContainsLoop = false;
//		if(callGraph.containsVertex(destination) && callGraph.containsVertex(source)) {
//		List<GraphPath<String, DefaultEdge>> possiblePaths = 
//			new AllDirectedPaths<>(callGraph).getAllPaths(source, destination, true, null);
//		
//		for(GraphPath<String, DefaultEdge> path : possiblePaths)
//		if(path.getVertexList().contains("for"))
//			pathContainsLoop = true;
//		else
//			pathContainsLoop = false;
//		}
//		return pathContainsLoop;
//	}
}
