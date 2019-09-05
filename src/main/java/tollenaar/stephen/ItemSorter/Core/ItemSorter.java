package tollenaar.stephen.ItemSorter.Core;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import io.javalin.Javalin;
import tollenaar.stephen.ItemSorter.Events.HopperHandler;
import tollenaar.stephen.ItemSorter.Events.HopperInteractHandler;
import tollenaar.stephen.ItemSorter.Util.Book;
import tollenaar.stephen.ItemSorter.Util.Item;

public class ItemSorter extends JavaPlugin {
	public Database database;
	private HopperConfiguring hopperConfig;
	private FileConfiguration config;
	private static Javalin app;
	private static List<Item> minecraftItems;

	@Override
	public void onEnable() {
		config = this.getConfig();
		config.options().copyDefaults(true);
		saveConfig();

		database = new Database(this);
		hopperConfig = new HopperConfiguring(this);

		// registering events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new HopperInteractHandler(this), this);
		pm.registerEvents(new HopperHandler(database), this);

		try {
			// getting all the items in minecraft and processing them into a
			// java object
			File plugins = Bukkit.getPluginManager().getPlugin("ItemSorter").getDataFolder().getParentFile();
			File plugin = new File(plugins.getAbsolutePath() + "/ItemSorter.jar");
			ZipFile jar = new ZipFile(plugin);
			ZipEntry entry = jar.getEntry("web/items.json");

			Type ITEM_TYPE = new TypeToken<List<Item>>() {
			}.getType();
			Gson gson = new Gson();

			JsonReader reader = new JsonReader(new InputStreamReader(jar.getInputStream(entry)));
			minecraftItems = gson.fromJson(reader, ITEM_TYPE);
			reader.close();
			jar.close();
		} catch (IOException e) {
			e.printStackTrace();
			// disbling this server
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				startWebServer();
			}
		});

		Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				database.load();
			}
		});
	}

	@Override
	public void onDisable() {
		try {
			app.stop();
		} catch (Exception e) {
			System.out.println("App stopped with errors");
		}
	}

	private static void addSoftwareLibrary(File file) throws Exception {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file.toURI().toURL() });
	}

	public void startWebServer() {

		try {
			addSoftwareLibrary(new File(getDataFolder().getAbsoluteFile() + File.separator + "lib" + File.separator
				+ "websocket-server-9.4.20.v20190813.jar"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ItemSorter.class.getClassLoader());
		app = Javalin.create(config -> config.addStaticFiles("/web")).start(config.getInt("port"));

		app.post("/" + config.getString("postConfigResponse"), ctx -> {
			try {
				String userCode = (String) ctx.formParam("userCode");
				int frameID = Integer.parseInt(ctx.formParam("frameID"));
				if (database.hasSavedPlayerWithItemFrame(UUID.fromString(userCode), frameID)) {
					hopperConfig.configureHopper(frameID, UUID.fromString(userCode), ctx.formParamMap());
					ctx.attribute("response", "Thank you. You can close this page now.");
					database.deletePlayerWithFrame(UUID.fromString(userCode), frameID);
				} else {
					ctx.attribute("response", "Conflicting data while posting your configuration set up.");
				}
			} catch (Exception e) {
				ctx.attribute("response",
					"Internal server error while posting your configuration set up. (" + e.getCause() + ")");
			}
			ctx.render("/web/response.html");
		});

		app.get("/" + config.getString("initialPageResponse"), ctx -> {
			try {
				String userCode = (String) ctx.queryParam("userCode");
				int frameID = Integer.parseInt(ctx.queryParam("frameID"));
				ctx.attribute("userCode", userCode);
				ctx.attribute("frameID", frameID);
				ctx.attribute("postAction", "./" + config.getString("postConfigResponse"));
				ctx.attribute("items", minecraftItems);
				ctx.attribute("checkItems", new ArrayList<String>());
				ctx.render("/web/index.html");
				if (!database.hasSavedPlayerWithItemFrame(UUID.fromString(userCode), frameID)) {
					ctx.attribute("response", "You're not supposed to be here!!");
					ctx.render("/web/response.html");
				}
			} catch (Exception ex) {
				ctx.attribute("response", "You're not supposed to be here!!");
				ctx.render("/web/response.html");
			}
		});

		app.get("/" + config.getString("editPageResponse"), ctx -> {
			try {
				String bookValue = (String) ctx.queryParam("configData");
				List<String> checkItems = ((Book) Book.fromString(bookValue)).toItems();
				String user = database.getSavedPlayer(bookValue);
				ctx.attribute("bookValue", bookValue);
				ctx.attribute("userCode", user);
				ctx.attribute("postAction", "./" + config.getString("postEditPageResponse"));
				ctx.attribute("items", minecraftItems);
				ctx.attribute("checkItems", checkItems);
				ctx.attribute("edit", true);
				ctx.render("/web/index.html");
				if (!database.hasSavedPlayer(bookValue)) {
					ctx.attribute("response", "You're not supposed to be here!!");
					ctx.render("/web/response.html");
				}
			} catch (Exception e) {
				ctx.attribute("response",
					"Internal server error while posting your configuration set up. (" + e.getCause() + ")");
				ctx.render("/web/response.html");
			}
		});

		app.post("/" + config.getString("postEditPageResponse"), ctx -> {
			try {
				String bookValue = (String) ctx.formParam("bookValue");
				String userCode = (String) ctx.formParam("userCode");
				if (database.hasSavedPlayer(bookValue)) {
					hopperConfig.editConfigureHopper(UUID.fromString(userCode), bookValue, ctx.formParamMap());
					ctx.attribute("response", "Thank you. You can close this page now.");
					database.deleteEditHopper(UUID.fromString(userCode), bookValue);
				} else {
					ctx.attribute("response", "Conflicting data while posting your configuration set up.");
				}
			} catch (Exception e) {
				ctx.attribute("response",
					"Internal server error while posting your configuration set up. (" + e.getCause() + ")");
			}
			ctx.render("/web/response.html");
		});

		app.get("/", ctx -> {
			ctx.attribute("response", "You're not supposed to be here!!");
			ctx.render("/web/response.html");
		});

		Thread.currentThread().setContextClassLoader(classLoader);
	}
}
