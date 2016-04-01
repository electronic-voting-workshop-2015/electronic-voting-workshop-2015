package workshop;

public class BeneshNetworkLayer {
	private static boolean[][] LAYERS;
	private static BeneshNetworkLayer ROOT;
	private static int TOTAL_DEPTH;
	private int start, length, depth;
	
	
	
	private BeneshNetworkLayer(int start, int length, int depth)
	{
		this.start = start;
		this.length = length;
		this.depth = depth;
	}
	
	public static void INITIATE(int length)
	{
		length /= 2;
		int depth = 0;
		for (int i = 1; i < length; i *= 2)
			depth++;
		BeneshNetworkLayer.ROOT = new BeneshNetworkLayer(0, length, depth);
		BeneshNetworkLayer.TOTAL_DEPTH = depth;
		BeneshNetworkLayer.LAYERS = new boolean[depth * 2 + 1][];
		for (int i = 0; i < BeneshNetworkLayer.LAYERS.length; i++)
			LAYERS[i] = new boolean[length];
	}
	public static void UPDATE(Permutation permutation)
	{
		ROOT.update(permutation);
	}
	
	public boolean getState(boolean first, int index)
	{
		index += this.start;
		if (first)
			return LAYERS[TOTAL_DEPTH - this.depth][index];
		return LAYERS[TOTAL_DEPTH + this.depth][index];
	}
	public boolean getState(int index)
	{
		if (index < this.length())
			return this.getState(true, index);
		return this.getState(false, index - this.length);
	}
	public void setState(boolean first, int index, boolean newState)
	{
		index += this.start;
		if (first)
			LAYERS[TOTAL_DEPTH - this.depth][index] = newState;
		else
			LAYERS[TOTAL_DEPTH + this.depth][index] = newState;
	}
	public void setState(int index, boolean newState)
	{
		if (index < this.length())
			this.setState(true, index, newState);
		else
			this.setState(false, index - this.length, newState);
	}
	public static boolean getState(int depth, int index)
	{
		return LAYERS[depth][index];
	}
	
	public int length()
	{
		return this.length;
	}
	public BeneshNetworkLayer slice(int start, int length)
	{
		return new BeneshNetworkLayer(this.start + start, length, this.depth - 1);
	}
	
	public void applyOnto(Permutation permutation)
	{
		int length = this.length();
		for (int i = 0; i < length; i++)
			if (this.getState(true, i))
				permutation.switchIndex(i, i + length);
		Permutation inverse = permutation.inverse();
		for (int i = 0; i < length; i++)
			if (this.getState(false, i))
				permutation.switchIndex(inverse.getValue(i), inverse.getValue(i + length));
	}
	private void update(Permutation permutation)
	{
		if (this.depth == 0)
		{
			//permutation.print();
			this.setState(0, permutation.getValue(0) != 0);
			return;
		}
		BeneshGraphUtils.switchesOn(this, permutation);
		int length = this.length();
		int half = length / 2;
		this.applyOnto(permutation);
		this.slice(0, half).update(permutation.slice(0, length));
		this.slice(half, half).update(permutation.slice(length, length));		
	}
}
