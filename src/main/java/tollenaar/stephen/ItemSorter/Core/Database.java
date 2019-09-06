package tollenaar.stephen.ItemSorter.Core;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import tollenaar.stephen.ItemSorter.Util.Book;
import tollenaar.stephen.ItemSorter.Util.Frame;
import tollenaar.stephen.ItemSorter.Util.Hopper;

public class Database {

	private ItemSorter plugin;
	private Connection connection;
	private static final String VERSION = "1.1";

	public Database(ItemSorter plugin) {
		this.plugin = plugin;
		databaseCheck();
	}

	public void databaseCheck() {
		connection = getConnection();
		initialize();
	}

	public void saveHoppers(Location hopperLocation, Location frameLocation) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement(
					"SELECT * FROM `Hoppers` WHERE `hopperX`=? AND `hopperY`=? AND `hopperZ`=? AND `hopperWorld`=?;");
			pst.setDouble(1, hopperLocation.getX());
			pst.setDouble(2, hopperLocation.getY());
			pst.setDouble(3, hopperLocation.getZ());
			pst.setString(4, hopperLocation.getWorld().getName());
			ResultSet rs = pst.executeQuery();

			if (!rs.next()) {
				pst.close();
				pst = getConnection().prepareStatement("INSERT INTO `Hoppers` ("
						+ "`hopperX`, `hopperY`, `hopperZ`, `hopperWorld`) " + "VALUES (?,?,?,?);");

				pst.setDouble(1, hopperLocation.getX());
				pst.setDouble(2, hopperLocation.getY());
				pst.setDouble(3, hopperLocation.getZ());
				pst.setString(4, hopperLocation.getWorld().getName());
				pst.execute();
			}
			saveFrames(hopperLocation, frameLocation);

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
	}

	public void saveFrames(Location hopperLocation, Location frameLocation) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement(
					"SELECT * FROM Hoppers WHERE `hopperX`=? AND `hopperY`=? AND `hopperZ`=? AND `hopperWorld`=?;");
			pst.setDouble(1, hopperLocation.getX());
			pst.setDouble(2, hopperLocation.getY());
			pst.setDouble(3, hopperLocation.getZ());
			pst.setString(4, hopperLocation.getWorld().getName());
			ResultSet rs = pst.executeQuery();
			rs.next();

			new Hopper(rs);
			saveFrames(rs.getInt("id"), frameLocation);

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
	}

	private void saveFrames(int hopperID, Location frameLocation) {

		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement(
					"INSERT INTO `Frames` (`hopper_id`, `frameX`, `frameY`, `frameZ`, `frameWorld`,`frameYaw`, `framePitch`) "
							+ "VALUES (?,?,?,?,?,?,?);");

			pst.setInt(1, hopperID);
			pst.setDouble(2, frameLocation.getX());
			pst.setDouble(3, frameLocation.getY());
			pst.setDouble(4, frameLocation.getZ());
			pst.setString(5, frameLocation.getWorld().getName());
			pst.setFloat(6, frameLocation.getYaw());
			pst.setFloat(7, frameLocation.getPitch());
			pst.execute();

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
	}

	public void savePlayer(UUID playerUUID, int frameID) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("INSERT INTO `UserConfigs` (`userUUID`, `frame_id`) VALUES (?,?);");

			pst.setString(1, playerUUID.toString());
			pst.setInt(2, frameID);
			pst.execute();

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
	}

	public void savePlayer(UUID playerUUID, String bookValue) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `EditUserConfigs` WHERE `userUUID`=?;");
			pst.setString(1, playerUUID.toString());
			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				pst.close();
				pst = getConnection()
						.prepareStatement("UPDATE `EditUserConfigs` SET `bookValue`=? WHERE `userUUID`=?;");
			} else {
				pst.close();
				pst = getConnection()
						.prepareStatement("INSERT INTO `EditUserConfigs` (`bookValue`, `userUUID`) VALUES (?,?);");
			}
			pst.setString(1, bookValue);
			pst.setString(2, playerUUID.toString());

			pst.execute();

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
	}

	public boolean hasSavedHopper(Location hopper) {
		if (Hopper.getHOPPER(hopper) != null) {
			return true;
		}

		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Hoppers` WHERE "
					+ "`hopperX`=? AND `hopperY`=? AND `hopperZ`=? AND `hopperWorld`=?;");

			pst.setDouble(1, hopper.getX());
			pst.setDouble(2, hopper.getY());
			pst.setDouble(3, hopper.getZ());
			pst.setString(4, hopper.getWorld().getName());

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				new Hopper(rs);
				return true;
			}

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
		return false;
	}

	public boolean hasSavedItemFrame(Location frameLocation) {
		if (Frame.getFRAME(frameLocation) != null) {
			return true;
		}

		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Frames` WHERE "
					+ "`frameX`=? AND `frameY`=? AND `frameZ`=? AND `frameWorld`=? AND `frameYaw`=? AND `framePitch`=?;");

			pst.setDouble(1, frameLocation.getX());
			pst.setDouble(2, frameLocation.getY());
			pst.setDouble(3, frameLocation.getZ());
			pst.setString(4, frameLocation.getWorld().getName());
			pst.setFloat(5, frameLocation.getYaw());
			pst.setFloat(6, frameLocation.getPitch());
			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				new Frame(rs);
				return true;
			}

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
		return false;
	}

	public boolean hasSavedPlayerWithItemFrame(UUID player, int frameID) {
		PreparedStatement pst = null;
		try {
			pst = getConnection()
					.prepareStatement("SELECT * FROM `UserConfigs` WHERE " + "`userUUID`=? AND `frame_id`=?;");

			pst.setString(1, player.toString());
			pst.setInt(2, frameID);

			ResultSet rs = pst.executeQuery();
			return rs.next();

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
		return false;
	}

	public boolean hasSavedPlayer(String player) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `EditUserConfigs` WHERE `userUUID`=?;");

			pst.setString(1, player);

			ResultSet rs = pst.executeQuery();
			return rs.next();

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
		return false;
	}

	public Object getSavedHopperByLocation(Location hopper, String field) {
		if (Hopper.getHOPPER(hopper) != null) {
			return Hopper.getHOPPER(hopper).getField(field);
		}

		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Hoppers` WHERE "
					+ "`hopperX`=? AND `hopperY`=? AND `hopperZ`=? AND `hopperWorld`=?;");

			pst.setDouble(1, hopper.getX());
			pst.setDouble(2, hopper.getY());
			pst.setDouble(3, hopper.getZ());
			pst.setString(4, hopper.getWorld().getName());

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				new Hopper(rs);
				return rs.getObject(field);
			}

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
		return false;
	}

	public Object getSavedItemFrameByHopperID(int hopperID, String field) {
		List<Object> frames = new ArrayList<>();
		for (Location frameLocation : Frame.getFrames()) {
			if (Frame.getFRAME(frameLocation).getHopperID() == hopperID) {
				frames.add(Frame.getFRAME(frameLocation).getField(field));
			}
		}
		if (!frames.isEmpty()) {
			return frames;
		}

		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Frames` WHERE " + "`hopper_id`=?;");

			pst.setInt(1, hopperID);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Frame fr = new Frame(rs);
				frames.add(fr.getField(field));
			}

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
		return frames;
	}

	public Object getSavedItemFrameByLocation(Location frameLocation, String field) {
		if (Frame.getFRAME(frameLocation) != null) {
			return Frame.getFRAME(frameLocation).getField(field);
		}

		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Frames` WHERE "
					+ "`frameX`=? AND `frameY`=? AND `frameZ`=? AND `frameWorld`=? AND `frameYaw`=? AND `framePitch`=?;");

			pst.setDouble(1, frameLocation.getX());
			pst.setDouble(2, frameLocation.getY());
			pst.setDouble(3, frameLocation.getZ());
			pst.setString(4, frameLocation.getWorld().getName());
			pst.setFloat(5, frameLocation.getYaw());
			pst.setFloat(6, frameLocation.getPitch());

			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				new Frame(rs);
				return rs.getObject(field);
			}

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
		return null;
	}

	public Object getSavedItemFrameByID(int frameID, String field) {
		if (Frame.getFRAME(frameID) != null) {
			return Frame.getFRAME(frameID).getField(field);
		}

		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Frames` WHERE " + "`id`=?;");

			pst.setInt(1, frameID);

			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				new Frame(rs);
				return rs.getObject(field);
			}

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
		return null;
	}

	public String getSavedPlayer(String bookValue) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `EditUserConfigs` WHERE " + "`bookValue`=?;");

			pst.setString(1, bookValue);

			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				return rs.getString("userUUID");
			}

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
		return null;
	}

	public void deleteHopper(Location hopperLocation) {
		Hopper.removeHOPPER(hopperLocation);

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				PreparedStatement pst = null;
				try {
					pst = getConnection().prepareStatement("DELETE FROM `Hoppers` WHERE "
							+ "`hopperX`=? AND `hopperY`=? AND `hopperZ`=? AND `hopperWorld`=?;");

					pst.setDouble(1, hopperLocation.getX());
					pst.setDouble(2, hopperLocation.getY());
					pst.setDouble(3, hopperLocation.getZ());
					pst.setString(4, hopperLocation.getWorld().getName());
					pst.execute();

				} catch (SQLException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
				} finally {
					try {
						if (pst != null) {
							pst.close();
						}
					} catch (SQLException e) {
						Bukkit.getLogger().log(Level.SEVERE, e.toString());
					}
				}
			}
		});
	}

	public void deleteFrame(Location frameLocation) {
		Frame.removeFRAME(frameLocation);

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				PreparedStatement pst = null;
				try {
					pst = getConnection().prepareStatement("DELETE FROM `Frames` WHERE "
							+ "`frameX`=? AND `frameY`=? AND `frameZ`=? AND `frameWorld`=? AND `frameYaw`=? AND `framePitch`=?;");

					pst.setDouble(1, frameLocation.getX());
					pst.setDouble(2, frameLocation.getY());
					pst.setDouble(3, frameLocation.getZ());
					pst.setString(4, frameLocation.getWorld().getName());
					pst.setFloat(5, frameLocation.getYaw());
					pst.setFloat(6, frameLocation.getPitch());
					pst.execute();

				} catch (SQLException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
				} finally {
					try {
						if (pst != null) {
							pst.close();
						}
					} catch (SQLException e) {
						Bukkit.getLogger().log(Level.SEVERE, e.toString());
					}

				}
			}
		});

	}

	public void deletePlayerWithFrame(UUID player, int frameID) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				PreparedStatement pst = null;
				try {
					pst = getConnection()
							.prepareStatement("DELETE FROM `UserConfigs` WHERE " + "`userUUID`=? AND `frame_id`=?;");

					pst.setString(1, player.toString());
					pst.setInt(2, frameID);
					pst.execute();

				} catch (SQLException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
				} finally {
					try {
						if (pst != null) {
							pst.close();
						}
					} catch (SQLException e) {
						Bukkit.getLogger().log(Level.SEVERE, e.toString());
					}
				}
			}
		});
	}

	public void deleteEditHopper(UUID player, String bookValue) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				PreparedStatement pst = null;
				try {
					pst = getConnection().prepareStatement(
							"DELETE FROM `EditUserConfigs` WHERE " + "`userUUID`=? AND `bookValue`=?;");

					pst.setString(1, player.toString());
					pst.setString(2, bookValue);
					pst.execute();

				} catch (SQLException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
				} finally {
					try {
						if (pst != null) {
							pst.close();
						}
					} catch (SQLException e) {
						Bukkit.getLogger().log(Level.SEVERE, e.toString());
					}
				}
			}
		});
	}

	public void loadHoppers() {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Hoppers`;");

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				new Hopper(rs);
			}

		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
	}

	public void loadFrames() {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Frames`;");

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Frame frame = new Frame(rs);
				ItemFrame fr = frame.getEntityFrame();
				if (fr != null && fr.getItem() != null && fr.getItem().getType() == Material.WRITTEN_BOOK
						&& fr.getItem().getItemMeta().hasLore()) {
					Book book = Book.fromString(fr.getItem().getItemMeta().getLore().get(0).replace("§", ""));
					book.addSelf(frame.getId());
				}
			}

		} catch (SQLException | ClassNotFoundException | IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
	}

	public void loadVersion() {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Version`;");
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				plugin.getLogger().log(Level.WARNING,
						"Newly created table, adding values and reloading the loaded configurations. This can tkae a while!!");
				pst.close();
				pst = getConnection().prepareStatement("INSERT INTO `Version` (`version`) VALUES (1.0);");
				pst.execute();
				load();
			} else {
				if (rs.getString("version").equals("1.0")) {
					plugin.getLogger().log(Level.WARNING,
							"Old non supported version detected, updating current deployed books. This can take a while!");
					for (Location loc : Frame.getFrames()) {
						ItemFrame fr = Frame.getFRAME(loc).getEntityFrame();
						if (fr != null && fr.getItem() != null && fr.getItem().getType() == Material.WRITTEN_BOOK) {
							ItemStack item = fr.getItem();
							BookMeta meta = (BookMeta) item.getItemMeta();

							if (!meta.getPage(meta.getPageCount()).contains("To edit the configuration click here.")) {

								Book book = Book.fromString(meta.getLore().get(0).replace("§", ""));

								BaseComponent[] editPage = new ComponentBuilder("To edit the configuration click here.")
										.event(new ClickEvent(ClickEvent.Action.OPEN_URL,
												plugin.getConfig().getString("URL")
														+ plugin.getConfig().getString("editPageResponse")
														+ "?configData=" + URLEncoder.encode(book.toString(), "UTF-8")))
										.create();

								meta.spigot().addPage(editPage);

								// changing the item frame item
								item.setItemMeta(meta);
							}
						}
					}
				}
				if (!rs.getString("version").equals(VERSION)) {
					plugin.getLogger().log(Level.WARNING, "Updating Version.");
					String old = rs.getString("version");
					pst.close();
					pst = getConnection().prepareStatement("UPDATE `Version` SET `version`=? WHERE `version`=?;");
					pst.setString(1, VERSION);
					pst.setString(2, old);
					pst.execute();
				}
			}

		} catch (SQLException | ClassNotFoundException |

				IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.toString());
			}
		}
	}

	public void load() {
		plugin.getLogger().log(Level.INFO, "Loading Frames.");
		loadFrames();
		plugin.getLogger().log(Level.INFO, "Loading Hoppers.");
		loadHoppers();
		plugin.getLogger().log(Level.INFO, "Checking Version.");
		loadVersion();
	}

	public void initialize() {
		connection = getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Hoppers (id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " hopperX REAL NOT NULL," + " hopperY REAL NOT NULL," + " hopperZ REAL NOT NULL,"
					+ " hopperWorld TEXT NOT NULL);"
					+ "CREATE TABLE IF NOT EXISTS Frames (id INTEGER PRIMARY KEY AUTOINCREMENT, hopper_id INTEGER NOT NULL, "
					+ "frameX REAL NOT NULL," + " frameY REAL NOT NULL,"
					+ " frameZ REAL NOT NULL, frameYaw REAL NOT NULL, framePitch REAL NOT NULL,"
					+ " frameWorld TEXT NOT NULL," + " CONSTRAINT fk_Hoppers" + " FOREIGN KEY (hopper_id)"
					+ " REFERENCES Hoppers(id)" + " ON DELETE CASCADE);" + "PRAGMA foreign_keys=ON;"
					+ "CREATE TABLE IF NOT EXISTS UserConfigs "
					+ "(userUUID TEXT NOT NULL, frame_id INTEGER UNIQUE NOT NULL, "
					+ "CONSTRAINT fk_Frames FOREIGN KEY (frame_id) REFERENCES Frames(id) ON DELETE CASCADE); PRAGMA foreign_keys=ON;"
					+ "CREATE TABLE IF NOT EXISTS EditUserConfigs "
					+ "(userUUID TEXT NOT NULL, bookValue TEXT NOT NULL);"
					+ "CREATE TABLE IF NOT EXISTS Version (version TEXT NOT NULL);");
			statement.close();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, e.getMessage());
		}

	}

	public Connection getConnection() {
		File db = new File(plugin.getDataFolder(), "hoppers.db");
		if (!db.exists()) {

			try {
				db.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "File write error: hoppers.db");
			}
		}
		try {
			// checking connection and creating new one if none established
			if (connection == null || connection.isClosed()) {

				Class.forName("org.sqlite.JDBC");

				connection = DriverManager.getConnection("jdbc:sqlite:" + db);

			}
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
		}

		return connection;
	}
}
