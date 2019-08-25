package tollenaar.stephen.ItemSorter.Core;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import tollenaar.stephen.ItemSorter.Util.Frame;
import tollenaar.stephen.ItemSorter.Util.Hopper;

public class Database {

	private ItemSorter plugin;
	private Connection connection;

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
			pst.setInt(1, hopperLocation.getBlockX());
			pst.setInt(2, hopperLocation.getBlockY());
			pst.setInt(3, hopperLocation.getBlockZ());
			pst.setString(4, hopperLocation.getWorld().getName());
			ResultSet rs = pst.executeQuery();

			if (!rs.next()) {
				pst.close();
				pst = getConnection().prepareStatement("INSERT INTO `Hoppers` ("
						+ "`hopperX`, `hopperY`, `hopperZ`, `hopperWorld`) " + "VALUES (?,?,?,?);");

				pst.setInt(1, hopperLocation.getBlockX());
				pst.setInt(2, hopperLocation.getBlockY());
				pst.setInt(3, hopperLocation.getBlockZ());
				pst.setString(4, hopperLocation.getWorld().getName());
				pst.execute();
			}
			saveFrames(hopperLocation, frameLocation);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
			}
		}
	}

	public void saveFrames(Location hopperLocation, Location frameLocation) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement(
					"SELECT * FROM Hoppers WHERE `hopperX`=? AND `hopperY`=? AND `hopperZ`=? AND `hopperWorld`=?;");
			pst.setInt(1, hopperLocation.getBlockX());
			pst.setInt(2, hopperLocation.getBlockY());
			pst.setInt(3, hopperLocation.getBlockZ());
			pst.setString(4, hopperLocation.getWorld().getName());
			ResultSet rs = pst.executeQuery();
			rs.next();

			new Hopper(rs);
			saveFrames(rs.getInt("id"), frameLocation);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
			}
		}
	}

	private void saveFrames(int hopperID, Location frameLocation) {

		PreparedStatement pst = null;
		try {
			pst = getConnection()
					.prepareStatement("INSERT INTO `Frames` (`hopper_id`, `frameX`, `frameY`, `frameZ`, `frameWorld`) "
							+ "VALUES (?,?,?,?,?);");

			pst.setInt(1, hopperID);
			pst.setInt(2, frameLocation.getBlockX());
			pst.setInt(3, frameLocation.getBlockY());
			pst.setInt(4, frameLocation.getBlockZ());
			pst.setString(5, frameLocation.getWorld().getName());
			pst.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
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
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
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

			pst.setInt(1, hopper.getBlockX());
			pst.setInt(2, hopper.getBlockY());
			pst.setInt(3, hopper.getBlockZ());
			pst.setString(4, hopper.getWorld().getName());

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				new Hopper(rs);
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
			}
		}
		return false;
	}

	public boolean hasSavedItemFrame(Location frame) {
		if (Frame.getFRAME(frame) != null) {
			return true;
		}

		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement(
					"SELECT * FROM `Frames` WHERE " + "`frameX`=? AND `frameY`=? AND `frameZ`=? AND `frameWorld`=?;");

			pst.setInt(1, frame.getBlockX());
			pst.setInt(2, frame.getBlockY());
			pst.setInt(3, frame.getBlockZ());
			pst.setString(4, frame.getWorld().getName());

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				new Frame(rs);
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
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
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
			}
		}
		return false;
	}

	public Object getSavedItemFrame(Location frame, String field) {
		if (Frame.getFRAME(frame) != null) {
			return Frame.getFRAME(frame).getField(field);
		}

		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement(
					"SELECT * FROM `Frames` WHERE " + "`frameX`=? AND `frameY`=? AND `frameZ`=? AND `frameWorld`=?;");

			pst.setInt(1, frame.getBlockX());
			pst.setInt(2, frame.getBlockY());
			pst.setInt(3, frame.getBlockZ());
			pst.setString(4, frame.getWorld().getName());

			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				new Frame(rs);
				return rs.getObject(field);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
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

					pst.setInt(1, hopperLocation.getBlockX());
					pst.setInt(2, hopperLocation.getBlockY());
					pst.setInt(3, hopperLocation.getBlockZ());
					pst.setString(4, hopperLocation.getWorld().getName());
					pst.execute();

				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					try {
						if (pst != null) {
							pst.close();
						}
					} catch (SQLException ex) {
						System.out.println(ex.getStackTrace());
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
							+ "`frameX`=? AND `frameY`=? AND `frameZ`=? AND `frameWorld`=?;");

					pst.setInt(1, frameLocation.getBlockX());
					pst.setInt(2, frameLocation.getBlockY());
					pst.setInt(3, frameLocation.getBlockZ());
					pst.setString(4, frameLocation.getWorld().getName());
					pst.execute();

				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					try {
						if (pst != null) {
							pst.close();
						}
					} catch (SQLException ex) {
						System.out.println(ex.getStackTrace());
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
					e.printStackTrace();
				} finally {
					try {
						if (pst != null) {
							pst.close();
						}
					} catch (SQLException ex) {
						System.out.println(ex.getStackTrace());
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
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
			}
		}
	}

	public void loadFrames() {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Frames`;");

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				new Frame(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getStackTrace());
			}
		}
	}

	public void load() {
		loadFrames();
		loadHoppers();
	}

	public void initialize() {
		connection = getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Hoppers (id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " hopperX INTEGER NOT NULL," + " hopperY INTEGER NOT NULL," + " hopperZ INTEGER NOT NULL,"
					+ " hopperWorld TEXT NOT NULL);"
					+ "CREATE TABLE IF NOT EXISTS Frames (id INTEGER PRIMARY KEY AUTOINCREMENT, hopper_id INTEGER NOT NULL, "
					+ "frameX INTEGER NOT NULL," + " frameY INTEGER NOT NULL," + " frameZ INTEGER NOT NULL,"
					+ " frameWorld TEXT NOT NULL," + " CONSTRAINT fk_Hoppers" + " FOREIGN KEY (hopper_id)"
					+ " REFERENCES Hoppers(id)" + " ON DELETE CASCADE);" + "PRAGMA foreign_keys=ON;"
					+ "CREATE TABLE IF NOT EXISTS UserConfigs "
					+ "(userUUID TEXT PRIMARY KEY, frame_id INTEGER NOT NULL, "
					+ "CONSTRAINT fk_Frames FOREIGN KEY (frame_id) REFERENCES Frames(id) ON DELETE CASCADE); PRAGMA foreign_keys=ON;");
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
