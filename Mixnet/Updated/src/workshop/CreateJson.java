package workshop;

import java.io.File;
import java.io.PrintWriter;

public class CreateJson {
	public File createJsonFile(String filepath, int n, String[][][] codes, String[][][][] proofs) {
		/*
		 * numberOfVotes = 2^n
		 * 
		 * codes[2n][2^n][2]
		 * 
		 * codes = list of 2n layers. in each layer, 2^n votes. in each vote, 2
		 * group-elements.
		 * 
		 * proofs[2n-1][2^(n-1)][4][9]
		 * 
		 * proofs = list of 2n-1 "intervals between layers" in each "interval"
		 * there are 2^(n-1) AND-proofs. in each AND-proof there are 4
		 * OR-proofs. in each OR-proof there are 9 strings of proof.
		 */
		int numberOfLayers = codes.length;
		assert numberOfLayers == 2 * n;
		int numberOfVotes = codes[0].length;
		assert numberOfVotes == Math.pow(2, n);
		int voteSize = codes[0][0].length;
		assert voteSize == 2;
		int numberOfOrProofs = proofs[0][0].length;
		assert numberOfOrProofs == 4;
		
		/* creating empty file */
		File file = null;
		try {
			/* create new file */
			file = new File(filepath);
			file.createNewFile();
			boolean goodFile = file.canWrite();
			if (!goodFile) {
				System.out.println("can't make new file...");
				return file;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return file;
		}

		/* prepare for writing to the file */
		PrintWriter writer;
		try {
			writer = new PrintWriter(filepath, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return file;
		}

		/* writing */
		writer.write("{\ncontent:{\n");
		writer.write("numberOfVotes:" + Integer.toString(numberOfVotes) + ",\n");
		writer.write("voteLayers:[\n");
		for (int i = 0; i < numberOfLayers - 1; ++i)
		/* indexing as humans do */
		/* we run one time less than numberOfLayers */
		{
			writeLayer(writer, voteSize, numberOfVotes, codes, i, ",");
		}
		/* write one last time without the comma */
		writeLayer(writer, voteSize, numberOfVotes, codes, numberOfLayers - 1, "");
		writer.write("],\n"); /* close "voteLayers:[\n" */

		/* CODES PART IS DONE HERE. NOW PROOFS */

		writer.write("layers:[\n");
		for (int i = 0; i < numberOfLayers - 1; ++i) {
			writer.write("{\nlayerNumber:" + Integer.toString(i) + ",\n");
			writer.write("andProofs:[\n");
			for (int j = 0; j < (numberOfVotes/2) - 1; ++j) {
				writeAndProof(writer, numberOfOrProofs, proofs, i, j, n, ",");
			}
			writeAndProof(writer, numberOfOrProofs, proofs, i, (numberOfVotes/2) - 1, n, "");
			writer.write("]\n"); /* close "andProofs:[\n" */
			writer.write("}\n"); /* close "{\nlayerNumber:i,\n" */
		}

		writer.write("]\n"); /* close "layers:[\n" */
		writer.write("}\n}\n"); /* close "{\ncontent:{\n" */
		writer.close();
		return file;
	}

	private void writeAndProof(PrintWriter writer, int numberOfOrProofs, String[][][][] proofs, int i, int j, int n,
			String comma) {
		int[] toSwap = NirsFormula(n, i + 1, j + 1);
		writer.write("{\n");
		writer.write("index1:" + Integer.toString(toSwap[0] - 1) + ",\n");
		writer.write("index2:" + Integer.toString(toSwap[1] - 1) + ",\n");
		for (int k = 0; k < numberOfOrProofs - 1; ++k)
		/* there are 4 OR proofs at each AND proof */
		/* the first 3 are with commas */
		{
			writeOrProof(writer, k, proofs[i][j], ",");
		}
		writeOrProof(writer, numberOfOrProofs - 1, proofs[i][j], "");
		writer.write("}" + comma + "\n"); /* close "{\n" */
	}

	private void writeOrProof(PrintWriter writer, int k, String[][] proofs_ij, String comma) {
		writer.write("orProof" + Integer.toString(k) + ":{\n");
		writer.write("a1:" + proofs_ij[k][0] + ",\n");
		writer.write("b1:" + proofs_ij[k][1] + ",\n");
		writer.write("a2:" + proofs_ij[k][2] + ",\n");
		writer.write("b2:" + proofs_ij[k][3] + ",\n");
		writer.write("c:" + proofs_ij[k][4] + ",\n");
		writer.write("r1:" + proofs_ij[k][5] + ",\n");
		writer.write("d1:" + proofs_ij[k][6] + ",\n");
		writer.write("r2:" + proofs_ij[k][7] + ",\n");
		writer.write("d2:" + proofs_ij[k][8] + "\n");
		writer.write("}" + comma + "\n"); /* close ("orProofk:{\n") */
	}

	private void writeVote(PrintWriter writer, int voteSize, String[][][] codes, int i, int j, String comma) {
		writer.write("{\n");
		for (int k = 0; k < voteSize - 1; ++k)
		/*
		 * one time less than voteSize (probably 'for' of one iteration...)
		 */
		{
			writer.write("code" + Integer.toString(k) + ":" + codes[i][j][k] + ",\n");
		}
		/* one last (second) time without the comma */
		writer.write("code" + Integer.toString(voteSize) + ":" + codes[i][j][voteSize - 1] + "\n");
		writer.write("}" + comma + "\n"); /* close "{\n" */
	}

	private void writeLayer(PrintWriter writer, int voteSize, int numberOfVotes, String[][][] codes, int i,
			String comma) {
		writer.write("{\nlayerNum:" + Integer.toString(i) + ",\n");
		writer.write("layerVotes:[\n");
		for (int j = 0; j < numberOfVotes - 1; ++j)
		/* one time less than numberOfVotes */
		{
			writeVote(writer, voteSize, codes, i, j, ",");
		}
		writeVote(writer, voteSize, codes, i, numberOfVotes - 1, "");
		writer.write("]\n"); /* close "layerVotes:[\n" */
		writer.write("}" + comma + "\n"); /* close "{\nlayerNum:i,\n" */
	}

	private int[] NirsFormula(int n, int i, int j) {
		assert 1 <= i && i <= 2 * n && 1 <= j && j <= Math.pow(2, n - 1);
		int a = Math.min(i, 2 * n - i);
		int b = (int) (j + (((j - 1) / (Math.pow(2, i-1))) * Math.pow(2, i-1)));
		return new int[] { b, a + b };
	}
}
