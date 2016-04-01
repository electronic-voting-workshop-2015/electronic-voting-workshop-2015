package workshop;

public class Edge 
{
	int left_vertex;
	int right_vertex;
	boolean broken;
	
	public Edge(int left_vertex_index, int right_vertex_index, boolean isBroken)
	{
		left_vertex = left_vertex_index;
		right_vertex = right_vertex_index;
		broken = isBroken;
	}
	
}
