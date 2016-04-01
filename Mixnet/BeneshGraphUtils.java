package workshop;

public class BeneshGraphUtils {
	public static bipartiteGraph generateBeneshGraph(Permutation permutation)
	{
		int middle = permutation.length() / 2;
		int p_i;
		bipartiteGraph benesGraph = new MyBiprtiteGraph(middle, middle);
		for (int i = 0; i < middle; i++)
		{
			p_i = permutation.getValue(i);
			benesGraph.addEdge(i, p_i % middle, p_i >= middle);
			p_i = permutation.getValue(i + middle);
			benesGraph.addEdge(i, p_i % middle, p_i < middle);
		}
		return benesGraph;
	}
	
	public static void switchesOn(BeneshNetworkLayer BNL, Permutation permutation)
	{
		bipartiteGraph graph = generateBeneshGraph(permutation);
		int length = graph.numberOfVertices();
		int[][] DFS = graph.DFS();
		int update, source;
		for (int i = 0; i < length; i++)
			if (DFS[i][0] == -1)
				BNL.setState(DFS[i][1], false);
			else
			{
				update = DFS[i][1];
				source = DFS[i][0];
				BNL.setState(update, xor(graph.getEdgeBrokenValue(update, source), BNL.getState(source)));
			}
	}
	
 	private static boolean xor(boolean a, boolean b)
	{
		return (a && (!b)) || ((!a) && b);
	}
}
