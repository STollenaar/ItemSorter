package tollenaar.stephen.ItemSorter.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Frame {
	private static Map<Location, Frame> FRAMES;

	private final int id;
	private int hopperID;
	private double x;
	private double y;
	private double z;
	private float pitch;
	private float yaw;
	private String world;

	public Frame(ResultSet rs) throws SQLException {
		this(rs.getInt("id"), rs.getInt("hopper_id"), rs.getDouble("frameX"), rs.getDouble("frameY"),
				rs.getDouble("frameZ"), rs.getFloat("frameYaw"), rs.getFloat("framePitch"), rs.getString("frameWorld"));
	}

	public Frame(int id, int hopperID, double x, double y, double z, float yaw, float pitch, String world) {
		this.id = id;
		this.hopperID = hopperID;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
		this.world = world;

		if (FRAMES == null) {
			FRAMES = new HashMap<>();
		}
		FRAMES.put(new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch), this);
	}

	public int getId() {
		return id;
	}

	public int getHopperID() {
		return hopperID;
	}

	public void setHopperID(int hopperID) {
		this.hopperID = hopperID;
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

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
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
		case "framePitch":
			return getPitch();
		case "frameYaw":
			return getYaw();
		case "hopper_id":
			return getHopperID();
		default:
			throw new NullPointerException("Unknown Field");
		}
	}

	public static Frame getFRAME(Location location) {
		if (FRAMES == null) {
			FRAMES = new HashMap<>();
		}

		return FRAMES.get(location);
	}

	public static Frame getFRAME(int id) {
		if (FRAMES == null) {
			FRAMES = new HashMap<>();
		}

		for (Location key : FRAMES.keySet()) {
			if (FRAMES.get(key).getId() == id) {
				return FRAMES.get(key);
			}
		}
		return null;
	}

	public static Set<Location> getFrames(){
		return FRAMES.keySet();
	}
	
	public static void removeFRAME(Location location) {
		if (FRAMES == null) {
			FRAMES = new HashMap<>();
		}

		FRAMES.remove(location);
	}
}