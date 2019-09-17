package simulation;

public enum NodeType {
	Struct,
	Leaf,
	Root,
	Raincatcher;

	public static int toInt(NodeType t) {
		switch (t) {
		case Struct:
			return 0;
		case Leaf:
			return 1;
		case Root:
			return 2;
		case Raincatcher:
			return 3;
		}
		
		return -1;
	}
	
	public static NodeType toType(int i) {
		switch (i) {
		case 0:
			return Struct;
		case 1:
			return Leaf;
		case 2:
			return Root;
		case 3:
			return Raincatcher;
		}
		
		return Struct;
	}
}