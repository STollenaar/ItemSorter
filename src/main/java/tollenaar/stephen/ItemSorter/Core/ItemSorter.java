package tollenaar.stephen.ItemSorter.Core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import io.javalin.Javalin;
import tollenaar.stephen.ItemSorter.Commands.CommandsHandler;
import tollenaar.stephen.ItemSorter.Events.HopperHandler;
import tollenaar.stephen.ItemSorter.Events.HopperInteractHandler;
import tollenaar.stephen.ItemSorter.Util.Server.Book;
import tollenaar.stephen.ItemSorter.Util.Web.Attributes;
import tollenaar.stephen.ItemSorter.Util.Web.Image;
import tollenaar.stephen.ItemSorter.Util.Web.Item;

public class ItemSorter extends JavaPlugin {

	private Database database;
	private HopperConfiguring hopperConfig;
	private FileConfiguration config;
	private static Javalin app;
	private static Attributes attributes;

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

		getCommand("ItemSorter").setExecutor(new CommandsHandler(this));

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

			Enumeration<? extends ZipEntry> entries = jar.entries();

			List<Image> tmp = new ArrayList<>();
			while (entries.hasMoreElements()) {
				ZipEntry en = entries.nextElement();
				if (en.getName().contains("images/gui/") && en.getName().split("images/gui/").length > 1) {
					tmp.add(new Image(en.getName().replace("web/", ""), loadImage(jar.getInputStream(en))));
				}
			}

			List<Item> t = new ArrayList<>();
			for (Enchantment ent : Enchantment.values()) {
				t.add(new Item(ent.hashCode(), 1, ent.getKey().getKey(),
						WordUtils.capitalizeFully(ent.getKey().getKey().replace("_", " "))));
			}

			List<Item> t2 = new ArrayList<>();
			for (PotionEffectType ent : PotionEffectType.values()) {
				t2.add(new Item(ent.hashCode(), 1, ent.getName(), WordUtils.capitalizeFully(ent.getName().replace("_"," " ))));
			}

			attributes = new Attributes(gson.fromJson(reader, ITEM_TYPE), tmp, t, t2);

			reader.close();
			jar.close();
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
			// disabling this server
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
			Bukkit.getLogger().log(Level.SEVERE, "App stopped with errors");
		}
	}

	private static void addSoftwareLibrary(File file)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, MalformedURLException {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file.toURI().toURL() });
	}

	public void startWebServer() {

		try {
			addSoftwareLibrary(new File(getDataFolder().getAbsoluteFile() + File.separator + "lib" + File.separator
					+ "websocket-server-9.4.20.v20190813.jar"));
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ItemSorter.class.getClassLoader());
		app = Javalin.create(config -> {
			config.addStaticFiles("/web");
			config.showJavalinBanner = false;
			config.requestCacheSize = 30000L;
		}).start(config.getInt("port"));
		appInitalize();

		Thread.currentThread().setContextClassLoader(classLoader);
	}

	private void appInitalize() {
		app.post("/" + config.getString("postConfigResponse"), ctx -> {
			try {
				String userCode = ctx.formParam("userCode");
				int frameID = Integer.parseInt(ctx.formParam("frameID"));
				if (ctx.formParamMap().size() > config.getInt("maxInputItems")) {
					ctx.attribute("response",
							"To many input options are selected. Please lower the amount. Current amount: "
									+ ctx.formParamMap().size() + " , max allowed: " + config.getInt("maxInputItems"));
				} else if (database.hasSavedPlayerWithItemFrame(UUID.fromString(userCode), frameID)) {
					hopperConfig.configureHopper(frameID, UUID.fromString(userCode), ctx.formParamMap());
					ctx.attribute("response", "Thank you. You can close this page now.");
					database.deletePlayerWithFrame(UUID.fromString(userCode), frameID);
				} else {
					ctx.attribute("response", "Conflicting data while posting your configuration set up.");
				}
			} catch (Exception e) {
				ctx.attribute("response", "Internal server error while posting your configuration set up. ("
						+ e.toString().replace("java.lang.", "") + ")");
			}
			ctx.render("/web/response.html");
		});

		app.get("/" + config.getString("initialPageResponse"), ctx -> {
			try {
				String userCode = ctx.queryParam("userCode");
				int frameID = Integer.parseInt(ctx.queryParam("frameID"));
				ctx.attribute("userCode", userCode);
				ctx.attribute("frameID", frameID);
				ctx.attribute("attributes", attributes);
				ctx.attribute("postAction", "./" + config.getString("postConfigResponse"));
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
				String user = ctx.queryParam("configData");
				String bookValue = database.getSavedEdit(UUID.fromString(user)).getBookValue();
				List<String> checkItems = Book.fromString(bookValue).toItems();
				ctx.attribute("bookValue", bookValue);
				ctx.attribute("userCode", user);
				ctx.attribute("attributes", attributes);
				ctx.attribute("postAction", "./" + config.getString("postEditPageResponse"));
				ctx.attribute("checkItems", checkItems);
				ctx.render("/web/index.html");
				if (!database.hasSavedPlayer(UUID.fromString(user))) {
					ctx.attribute("response", "You're not supposed to be here!!");
					ctx.render("/web/response.html");
				}

			} catch (Exception e) {
				ctx.attribute("response", "Internal server error while posting your configuration set up. ("
						+ e.toString().replace("java.lang.", "") + ")");
				ctx.render("/web/response.html");
			}

		});

		app.post("/" + config.getString("postEditPageResponse"), ctx -> {
			try {
				String bookValue = ctx.formParam("bookValue");
				String userCode = ctx.formParam("userCode");
				if (ctx.formParamMap().size() > config.getInt("maxInputItems")) {
					ctx.attribute("response",
							"To many input options are selected. Please lower the amount. Current amount: "
									+ ctx.formParamMap().size() + " , max allowed: " + config.getInt("maxInputItems"));
				} else if (database.hasSavedPlayer(UUID.fromString(userCode))) {
					hopperConfig.editConfigureHopper(UUID.fromString(userCode), bookValue, ctx.formParamMap());
					ctx.attribute("response", "Thank you. You can close this page now.");
					database.deleteEditHopper(UUID.fromString(userCode));
				} else {
					ctx.attribute("response", "Conflicting data while posting your configuration set up.");
				}
			} catch (Exception e) {
				ctx.attribute("response", "Internal server error while posting your configuration set up. ("
						+ e.toString().replace("java.lang.", "") + ")");
			}
			ctx.render("/web/response.html");
		});

		app.get("/", ctx -> {
			ctx.attribute("response", "You're not supposed to be here!!");
			ctx.render("/web/response.html");
		});
	}

	public Database getDatabase() {
		return database;
	}

	private BufferedImage loadImage(InputStream file) {
		BufferedImage buff = null;
		try {
			buff = ImageIO.read(file);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
			return null;
		}
		return buff;

	}
}
