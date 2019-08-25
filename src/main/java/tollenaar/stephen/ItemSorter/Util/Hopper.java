package tollenaar.stephen.ItemSorter.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Hopper {
	private static Map<Location, Hopper> HOPPERS;

	private int id;
	private int x;
	private int y;
	private int z;
	private String world;

	public Hopper(ResultSet rs) throws SQLException {
		this(rs.getInt("id"), rs.getInt("hopperX"), rs.getInt("hopperY"), rs.getInt("hopperZ"),
				rs.getString("hopperWorld"));
	}

	public Hopper(int id, int x, int y, int z, String world) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;

		if (HOPPERS == null) {
			HOPPERS = new HashMap<>();
		}
		HOPPERS.put(new Location(Bukkit.getWorld(world), x, y, z), this);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public static Hopper getHOPPER(Location location) {
		if (HOPPERS == null) {
			HOPPERS = new HashMap<>();
		}

		return HOPPERS.get(location);
	}

	public static void removeHOPPER(Location location) {
		if (HOPPERS == null) {
			HOPPERS = new HashMap<>();
		}
		
		HOPPERS.remove(location);
	}
}
