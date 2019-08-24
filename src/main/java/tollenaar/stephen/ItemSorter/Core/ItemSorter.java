package tollenaar.stephen.ItemSorter.Core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
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

public class ItemSorter extends JavaPlugin {

	public Database database;
	private FileConfiguration config;
	private static Javalin app;
	private static List<Item> minecraftItems;

	@Override
	public void onEnable() {
		config = this.getConfig();
		config.options().copyDefaults(true);
		saveConfig();

		database = new Database(this);

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

		startWebServer();
	}

	@Override
	public void onDisable() {
		app.stop();
	}

	private static void addSoftwareLibrary(File file) throws Exception {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file.toURI().toURL() });
	}

	public void startWebServer() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();

		try {
			addSoftwareLibrary(new File(getDataFolder().getAbsoluteFile() + File.separator + "lib" + File.separator
					+ "websocket-server-9.4.20.v20190813.jar"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		URL[] urls = ((URLClassLoader) cl).getURLs();

		for (URL url : urls) {
			System.out.println(url.getFile());
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ItemSorter.class.getClassLoader());
		app = Javalin.create(config -> config.addStaticFiles("/web")).start(config.getInt("port"));

		app.post("/" + config.getString("postConfigResponse"), ctx -> {
			ctx.render("/web/response.html");
		});

		app.get("/" + config.getString("initialPageResponse"), ctx -> {
			ctx.attribute("postAction", "/" + config.getString("postConfigResponse"));
			ctx.attribute("items", minecraftItems);
			ctx.render("/web/index.html");
		});

		Thread.currentThread().setContextClassLoader(classLoader);
	}
}
