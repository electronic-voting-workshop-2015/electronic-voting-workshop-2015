package workshop;

public class Vote {
	public GroupMember key;
	public GroupMember message;
	
	public Vote(byte[] key, byte[] message) {
		super();
		this.key = new GroupMember(key);
		this.message =  new GroupMember(message);
	}
	public Vote(byte[][] vote) {
		super();
		this.key = new GroupMember(vote[0]);
		this.message =  new GroupMember(vote[1]);
	}
	public Vote(GroupMember key, GroupMember message) {
		super();
		this.key = key;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return key.toString() + message.toString();
	}
	
	public String[] toArrString() {
		return new String[] {key.toString(), message.toString()};
	}
	
	public byte[][] getArr() {
		byte[][] arr = new byte[2][];
		arr[0] = key.member;
		arr[1] = message.member;
		return arr;
	}
	public boolean isZero() {
		
		return key.isZero() || message.isZero();
	}
}
