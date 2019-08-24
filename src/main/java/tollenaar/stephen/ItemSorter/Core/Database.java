package tollenaar.stephen.ItemSorter.Core;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;

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

	public void saveHoppers(Location hopperLocation, Location signLocation) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("INSERT INTO `Hoppers` ("
					+ "`hopperX`, `hopperY`, `hopperZ`, `hopperWorld`, `signX`, `signY`, `signZ`, `signWorld`) "
					+ "VALUES (?,?,?,?,?,?,?,?);");

			pst.setInt(1, hopperLocation.getBlockX());
			pst.setInt(2, hopperLocation.getBlockY());
			pst.setInt(3, hopperLocation.getBlockZ());
			pst.setString(4, hopperLocation.getWorld().getName());
			pst.setInt(5, signLocation.getBlockX());
			pst.setInt(6, signLocation.getBlockY());
			pst.setInt(7, signLocation.getBlockZ());
			pst.setString(8, signLocation.getWorld().getName());
			pst.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
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

	public void saveFrames(Location frameLocation) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement(
					"SELECT id FROM Hoppers WHERE `signX`=? AND `signY`=? AND `signZ`=? AND `signWorld`=?;");
			pst.setInt(1, frameLocation.getBlockX());
			pst.setInt(2, frameLocation.getBlockY());
			pst.setInt(3, frameLocation.getBlockZ());
			pst.setString(4, frameLocation.getWorld().getName());
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			//saveSigns(rs.getInt("id"));
			
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

	private void saveFrames(int hopperID, List<String> signData) {

		for (String line : signData) {
			for (String l : line.split("\n| ")) {
				PreparedStatement pst = null;
				try {
					pst = getConnection()
							.prepareStatement("INSERT INTO `Signs` (`hopper_id`, `itemNameSpace`) " + "VALUES (?,?);");

					pst.setInt(1, hopperID);
					pst.setString(2, l);
					pst.execute();
				} catch (NumberFormatException ex) {

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
		}
	}

	public List<String> getSignDatas(Location hopperLocation) {
		PreparedStatement pst = null;
		List<String> results = new ArrayList<String>();
		try {
			pst = getConnection().prepareStatement(
					"SELECT `itemNameSpace` FROM `Hoppers` INNER JOIN Signs WHERE `hopperX`=? AND `hopperY`=? AND `hopperZ`=? AND `hopperWorld`=?;");
			pst.setInt(1, hopperLocation.getBlockX());
			pst.setInt(2, hopperLocation.getBlockY());
			pst.setInt(3, hopperLocation.getBlockZ());
			pst.setString(4, hopperLocation.getWorld().getName());

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				results.addAll(Arrays.asList(rs.getString("itemNameSpace")));
			}
			pst.close();
			rs.close();

		} catch (

		SQLException e) {
			// TODO Auto-generated catch block
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
		return results;
	}

	public boolean getSavedHopper(Location hopper) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("SELECT * FROM `Hoppers` WHERE "
					+ "`hopperX`=? AND `hopperY`=? AND `hopperZ`=? AND `hopperWorld`=?;");

			pst.setInt(1, hopper.getBlockX());
			pst.setInt(2, hopper.getBlockY());
			pst.setInt(3, hopper.getBlockZ());
			pst.setString(4, hopper.getWorld().getName());

			ResultSet rs = pst.executeQuery();

			return rs.next();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
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

	public boolean getSavedItemFrame(Location sign) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement(
					"SELECT * FROM `Hoppers` WHERE " + "`signX`=? AND `signY`=? AND `signZ`=? AND `signWorld`=?;");

			pst.setInt(1, sign.getBlockX());
			pst.setInt(2, sign.getBlockY());
			pst.setInt(3, sign.getBlockZ());
			pst.setString(4, sign.getWorld().getName());

			ResultSet rs = pst.executeQuery();

			return rs.next();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
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

	public void deleteHopper(Location hopperLocation) {
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
			// TODO Auto-generated catch block
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

	public void deleteFrame(Location signLocation) {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement(
					"DELETE FROM `Hoppers` WHERE " + "`signX`=? AND `signY`=? AND `signZ`=? AND `signWorld`=?;");

			pst.setInt(1, signLocation.getBlockX());
			pst.setInt(2, signLocation.getBlockY());
			pst.setInt(3, signLocation.getBlockZ());
			pst.setString(4, signLocation.getWorld().getName());
			pst.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
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

	public void initialize() {
		connection = getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Hoppers (id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " hopperX INTEGER NOT NULL," + " hopperY INTEGER NOT NULL," + " hopperZ INTEGER NOT NULL,"
					+ " hopperWorld TEXT NOT NULL," + "signX INTEGER NOT NULL," + " signY INTEGER NOT NULL,"
					+ " signZ INTEGER NOT NULL," + " signWorld TEXT NOT NULL);"
					+ " CREATE TABLE IF NOT EXISTS Signs (hopper_id INTEGER NOT NULL, itemNameSpace TEXT NOT NULL,"
					+ " CONSTRAINT fk_Hoppers" + " FOREIGN KEY (hopper_id)" + " REFERENCES Hoppers(id)"
					+ " ON DELETE CASCADE);" + "PRAGMA foreign_keys=ON;");
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
