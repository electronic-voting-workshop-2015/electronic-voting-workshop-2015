package workshop;

public abstract class bipartiteGraph 
{
	public bipartiteGraph(int numberOfVerticesLeft, int numberOfVerticesRight) {	}
	public abstract void addEdge(int leftIndex, int rightIndex, boolean broken);
	public abstract int[][] DFS(); // 2 arrays: the first one is the dfs sorted from root to leaves; the second is the vertex at each index
	public abstract boolean getEdgeBrokenValue(int leftVertex, int rightVertex);
	public abstract int numberOfVertices();
}
