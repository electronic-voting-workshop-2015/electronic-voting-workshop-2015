package Graph;




public class BipartiteGraph
{
	public static int MAX_DEG = 2;
	public final Edge NO_EDGE = new Edge(-1, -1, false);
	public static final int NO_NEIGHBOR = -17;

	
	int left_side;
	int right_side;
	Edge[][] edges; /* the index i represent the i'th vertex:
	left_side[i] if i < left_side and right_side[i - |left_side|] else.
	the edge edges[i][j] represents an edge adjacent to i. */

	public BipartiteGraph(int numberOfVerticesLeft, int numberOfVerticesRight) 
	{
		left_side = numberOfVerticesLeft;
		right_side = numberOfVerticesRight;
		edges = new Edge[left_side + right_side][MAX_DEG];
		for (int i = 0; i < left_side + right_side; ++i)
		{
			for (int j = 0; j < MAX_DEG; ++j)
			{
				edges[i][j] = NO_EDGE;
			}
		}
	}

	public void addEdge(int leftIndex, int rightIndex, boolean broken) 
	{
		Edge edge = new Edge(leftIndex, rightIndex, broken);
		help_addEdge(leftIndex, edge);
		help_addEdge(rightIndex + left_side, edge);
	}
	
	private void help_addEdge(int index, Edge edge)
	{
		int i = 0;
		while ((i < MAX_DEG) && (edges[index][i] != NO_EDGE))
		{
			++i;
		}
		if (i < MAX_DEG)
		{
			edges[index][i] = edge;
		}
		else
			System.out.printf("shit %d\n", index);
	}
	
	public int[] neighborhood(int vertex)
	{
		int[] res = new int[MAX_DEG];
		for (int i = 0; i < MAX_DEG; ++i)
		{
			res[i] = NO_NEIGHBOR; /* no neighbor */
		}
		if (vertex < left_side) /* is in left side */
		{
			for (int j = 0; j < MAX_DEG; ++j)
			{
				if (edges[vertex][j] != NO_EDGE)
				{
					res[j] = edges[vertex][j].right_vertex + left_side; 
					/* "rights" come after "lefts" */
				}
			}
			return res;
		}
		for (int j = 0; j < MAX_DEG; ++j) /* vertex is in right side */
		{
			if (edges[vertex][j] != NO_EDGE)
			{
				res[j] = edges[vertex][j].left_vertex;
			}
		}
		return res;
	}

	public boolean getEdgeBrokenValue(int vertex1, int vertex2) 
	{
		int leftVertex = Math.min(vertex1, vertex2);
		int rightVertex = Math.max(vertex1, vertex2) - left_side;
		for (int i = 0; i < MAX_DEG; ++i)
		{
			if (edges[leftVertex][i].right_vertex == rightVertex)
			{
				return edges[leftVertex][i].broken;
			}
		}
		System.out.format("no such edge! (got %d, %d)\n", 
				leftVertex, rightVertex);
		return false;
	}
	
	public int numberOfVertices() 
	{
		return left_side + right_side;
	}
	
	public int[][] DFS()
	{
		boolean[] seen = new boolean[right_side + left_side];
		/* initialize seen */
		for (int i = 0; i < right_side + left_side; ++i)
		{
			seen[i] = false;
		}
		/* we initialize a pointer to "index" */
		int[] index = new int[1];
		index[0] = 0;
		int[][] res = new int[left_side + right_side][2];
		/* we run over connectivity components */
		for (int vertex = 0; vertex < left_side + right_side; ++vertex)
			if (!seen[vertex])
			{
				/* for each origin, his parent is -1 (no parent) */
				rec_dfs(vertex, -1, index, seen, res);
			}
		return res;
	}
	
	private void rec_dfs(int vertex, int parent, int[] index, boolean[] seen, int[][] res)
	{
		seen[vertex] = true;
		res[index[0]][1] = vertex;
		res[index[0]][0] = parent;
		++index[0];
		for (int neighbor : neighborhood(vertex))
		{
			if ((neighbor != NO_NEIGHBOR) && (!(seen[neighbor])))
			{
				rec_dfs(neighbor, vertex, index, seen, res);
			}
		}
	}
}
