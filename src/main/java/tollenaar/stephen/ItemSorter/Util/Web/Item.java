package tollenaar.stephen.ItemSorter.Util.Web;

public class Item {
	private final int id;
	private final int stackSize;
	private final String name;
	private final String displayName;

	public Item(int id, int stackSize, String name, String displayName) {
		this.id = id;
		this.stackSize = stackSize;
		this.name = name.toLowerCase();
		this.displayName = displayName;
	}

	public int getId() {
		return id;
	}

	public int getStackSize() {
		return stackSize;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

    @Override
	public String toString() {
		return Integer.toString(id) + ":" + Integer.toString(stackSize) + ":" + name + ":" + displayName;
	}
}
