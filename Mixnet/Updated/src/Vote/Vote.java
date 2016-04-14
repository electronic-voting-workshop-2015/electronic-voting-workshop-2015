package Vote;


public class Vote {
	public GroupMember key;
	public GroupMember message;
	
	public Vote(GroupMember key, GroupMember message) {
		super();
		this.key = key;
		this.message = message;
	}
	
	public Vote(GroupMember[] member) {
		super();
		this.key = member[0];
		this.message = member[1];
	}

	@Override
	public String toString() {
		return key.toString() + message.toString();
	}
	
	public String[] toArrString() {
		return new String[] {key.toString(), message.toString()};
	}
	
	public GroupMember[] getArr() {
		GroupMember[] arr = new GroupMember[2];
		arr[0] = key;
		arr[1] = message;
		return arr;
	}
}
