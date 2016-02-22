package workshop;

public class QRProperties {
		private int level;
		private ECCLevel ecc;
		private int maxNumOfBits;
		public int getLevel() {
			return level;
		}
		public void setLevel(int level) {
			this.level = level;
		}
		public ECCLevel getEcc() {
			return ecc;
		}
		public void setEcc(ECCLevel ecc) {
			this.ecc = ecc;
		}
		public int getMaxNumOfBits() {
			return maxNumOfBits;
		}
		public void setMaxNumOfBits(int maxNumOfBits) {
			this.maxNumOfBits = maxNumOfBits;
		}
		public QRProperties(int level, ECCLevel ecc, int maxNumOfBits){
			this.level=level;
			this.ecc=ecc;
			this.maxNumOfBits=maxNumOfBits;
		}
}
