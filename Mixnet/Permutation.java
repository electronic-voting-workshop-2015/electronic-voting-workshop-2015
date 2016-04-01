package workshop;

import java.util.Random;

public class Permutation {
	private int[] permutation; // value at index i moves to permutaion[i]
	private int start;	// only some of the array is observed: starts at start_index
	private int length;		// ends at end_index (exclude, meaning that for empty permutation, end = start 
	
	public Permutation(int[] permutation, int start, int length)
	{
		this.permutation = permutation;
		this.start = start;
		this.length = length;
	}
	
	public int getValue(int index)
	{
		index += this.start;
		index = this.permutation[index];
		index -= this.start;
		return index;
	}
	
	public void setValue(int index, int value)
	{
		index += this.start;
		value += this.start;
		this.permutation[index] = value;
	}
	
	public void switchIndex(int index1, int index2)
	{
		int t = this.getValue(index1);
		this.setValue(index1, this.getValue(index2));
		this.setValue(index2, t);
	}
	
	public int length()
	{
		return this.length;
	}
	
	public Permutation copy()
	{
		int length = this.length();
		int[] newPermutation = new int[length];
		for (int i = 0; i < length; i++)
			newPermutation[i] = this.getValue(i);
		return new Permutation(newPermutation, 0, length);
	}
	
	public Permutation slice(int start, int length)
	{
		return new Permutation(this.permutation, start + this.start, length);
	}
	
	public static Permutation generateRandomPermutation(int length)
	{
		int[] permutation = new int[length];
		Random r = new Random();
		for (int i = 0; i < length; ++i)
		{
			int t = r.nextInt(i + 1); // might not be random enough
			permutation[i] = permutation[t];
			permutation[t] = i;
		}
		return new Permutation(permutation, 0, length);
	}
	
	public boolean isEqual(Permutation other)
	{
		int length = this.length();
		if (length != other.length())
			return false;
		for (int i = 0; i < length; i++)
			if (this.getValue(i) != other.getValue(i))
				return false;
		return true;
	}
	
	public Permutation inverse()
	{
		int[] inverse = new int[this.length()];
		int length = this.length();
		for (int i = 0; i < length; i++)
			inverse[this.getValue(i)] = i;
		return new Permutation(inverse, 0, length);
	}
	
	public boolean isValid()
	{
		int length = this.length();
		boolean[] entered = new boolean[length];
		for (int i = 0; i < length; i++)
			entered[i] = false;
		for (int i = 0; i < length; i++)
			entered[this.getValue(i)] = true;
		for (int i = 0; i < length; i++)
			if (!entered[i])
				return false;
		return true;
	}
	
	public void print()
	{
		int length = this.length();
		System.out.printf("%d: { ", length);
		for (int i = 0; i < length; i++)
			System.out.printf("%d , ", this.getValue(i));
		System.out.println("end }");
	}

	public Permutation multiply(Permutation other)
	{
		if (this.length() != other.length())
			return null;
		int[] mult = new int[this.length()];
		for (int i = 0; i < mult.length; i++)
			mult[i] = this.getValue(other.getValue(i));
		return new Permutation(mult, 0, mult.length);
	}
}
