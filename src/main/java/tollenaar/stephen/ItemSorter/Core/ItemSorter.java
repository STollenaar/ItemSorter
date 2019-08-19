package tollenaar.stephen.ItemSorter.Core;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import io.javalin.Javalin;
import tollenaar.stephen.ItemSorter.Events.HopperHandler;
import tollenaar.stephen.ItemSorter.Events.SignHandler;

public class ItemSorter extends JavaPlugin {

	private Database database;
	private FileConfiguration config;
	private static Javalin app;
	
	@Override
	public void onEnable() {
		config = this.getConfig();
		config.options().copyDefaults(true);
		saveConfig();

		database = new Database(this);

		// registering events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new SignHandler(database), this);
		pm.registerEvents(new HopperHandler(database), this);

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
		app = Javalin.create(config -> config.addStaticFiles("/extracted/web")).start(config.getInt("port"));
		Thread.currentThread().setContextClassLoader(classLoader);
	}
}
