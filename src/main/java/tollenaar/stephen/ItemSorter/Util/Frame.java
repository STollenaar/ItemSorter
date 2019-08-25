package tollenaar.stephen.ItemSorter.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Frame {
	private static Map<Location, Frame> FRAMES;

	private int id;
	private int hopperID;
	private int x;
	private int y;
	private int z;
	private String world;

	public Frame(ResultSet rs) throws SQLException {
		this(rs.getInt("id"), rs.getInt("hopper_id"), rs.getInt("frameX"), rs.getInt("frameY"), rs.getInt("frameZ"),
				rs.getString("frameWorld"));
	}

	public Frame(int id, int hopperID, int x, int y, int z, String world) {
		this.id = id;
		this.hopperID = hopperID;
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;

		if (FRAMES == null) {
			FRAMES = new HashMap<>();
		}
		FRAMES.put(new Location(Bukkit.getWorld(world), x, y, z), this);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getHopperID() {
		return hopperID;
	}

	public void setHopperID(int hopperID) {
		this.hopperID = hopperID;
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

	public Object getField(String field) {
		switch (field) {
		case "id":
			return getId();
		case "frameX":
			return getX();
		case "frameY":
			return getY();
		case "frameZ":
			return getZ();
		case "frameWorld":
			return getWorld();
		case "hopper_id":
			return getHopperID();
		default:
			throw new NullPointerException("Unknown Field");
		}
	}

	public static Frame getFRAME(Location location) {
		if(FRAMES == null){
			FRAMES = new HashMap<>();
		}
		
		return FRAMES.get(location);
	}
	
	public static void removeFRAME(Location location){
		if(FRAMES == null){
			FRAMES = new HashMap<>();
		}
		
		FRAMES.remove(location);
	}
}
