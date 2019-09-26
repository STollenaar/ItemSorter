package tollenaar.stephen.ItemSorter.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Hopper {
	private static Map<Location, Hopper> HOPPERS;

	private final int id;
	private double x;
	private double y;
	private double z;
	private String world;

	public Hopper(ResultSet rs) throws SQLException {
		this(rs.getInt("id"), rs.getDouble("hopperX"), rs.getDouble("hopperY"), rs.getDouble("hopperZ"),
				rs.getString("hopperWorld"));
	}

	public Hopper(int id, double x, double y, double z, String world) {
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

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public Object getField(String field) {
		switch (field) {
		case "id":
			return getId();
		case "hopperX":
			return getX();
		case "hopperY":
			return getY();
		case "hopperZ":
			return getZ();
		case "hopperWorld":
			return getWorld();
		default:
			throw new NullPointerException("Unknown Field");
		}
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
	
	public static boolean isJunction(Block hopper) {
		BlockFace facing = ((org.bukkit.block.data.type.Hopper) hopper.getBlockData()).getFacing();
		if (facing != BlockFace.DOWN) {
			return hopper.getRelative(BlockFace.DOWN).getType() == Material.HOPPER
					|| hopper.getRelative(BlockFace.DOWN).getType() == Material.HOPPER_MINECART;
		}
		return false;
	}
}
