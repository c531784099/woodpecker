package cn.bidlink.framework.search.engine;

public enum OperationType {

	ADD, DELETE, UPDATE;
	
	public static OperationType get(int ordinal) {
		for(OperationType type : OperationType.values()) {
			if(type.ordinal() == ordinal) {
				return type;
			}
		}
		return null;
	}
	
	public static void main(String[] args){
		System.out.println(ADD.ordinal());
	}

}
