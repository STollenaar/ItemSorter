package tollenaar.stephen.ItemSorter.Core;

public class Item {
	private int id;
	private int stackSize;
	private String name;
	private String displayName;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStackSize() {
		return stackSize;
	}
	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
