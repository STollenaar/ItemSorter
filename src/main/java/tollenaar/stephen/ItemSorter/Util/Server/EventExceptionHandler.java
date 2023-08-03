package tollenaar.stephen.ItemSorter.Util.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang3.Validate;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

public class EventExceptionHandler {
	private JavaPlugin plugin;
	private Date date;

	public EventExceptionHandler(JavaPlugin plugin) {
		this.plugin = plugin;
		if(this.date == null) {
			this.date = new Date();
		}
	}

	// For wrapping a registered listener
	private static class ExceptionRegisteredListener extends RegisteredListener {
		/**
		 * Represents an event executor that does nothing. This is not really necessary
		 * in the current implementation of CraftBukkit, but we will take no chances.
		 */
		private static EventExecutor NULL_EXECUTOR = new EventExecutor() {
			@Override
			public void execute(Listener listener, Event event) throws EventException {
				// Do nothing
			}
		};

		private final RegisteredListener delegate;
		private final EventExceptionHandler handler;

		public ExceptionRegisteredListener(RegisteredListener delegate, EventExceptionHandler handler) {
			super(delegate.getListener(), NULL_EXECUTOR, delegate.getPriority(), delegate.getPlugin(),
					delegate.isIgnoringCancelled());
			this.delegate = delegate;
			this.handler = handler;
		}

		@Override
		public void callEvent(Event event) throws EventException {
			try {
				delegate.callEvent(event);
			} catch (EventException e) {
				if (!handler.handle(e.getCause(), event)) {
					throw e;
				}
			} catch (Throwable e) {
				if (!handler.handle(e, event)) {
					doThrow(e);
				}
			}
		}

		// WARNING: HORRIBLE, HORRIBLE HACK to get around checked exceptions
		private static void doThrow(Throwable e) {
			ExceptionRegisteredListener.<RuntimeException>doThrowInner(e);
		}

		@SuppressWarnings("unchecked")
		private static <E extends Throwable> void doThrowInner(Throwable e) throws E {
			// TODO add logging to file/site
			throw (E) e;
		}
	}

	/**
	 * Register Bukkit event handlers with a given exception handler.
	 * 
	 * @param listener - a class of event handlers.
	 * @param plugin   - the current plugin.
	 * @param handler  - exception handler.
	 */
	public static void registerEvents(Listener listener, Plugin plugin, EventExceptionHandler handler) {
		Validate.notNull(plugin, "Plugin cannot be NULL.");

		registerEvents(plugin.getServer().getPluginManager(), listener, plugin, handler);
	}

	/**
	 * Register Bukkit event handlers with a given exception handler.
	 * 
	 * @param manager  - the current plugin manager.
	 * @param listener - a class of event handlers.
	 * @param plugin   - the current plugin.
	 * @param handler  - exception handler.
	 */
	public static void registerEvents(PluginManager manager, Listener listener, Plugin plugin,
			EventExceptionHandler handler) {
		Validate.notNull(manager, "Manager cannot be NULL.");
		Validate.notNull(listener, "Listener cannot be NULL.");
		Validate.notNull(plugin, "Plugin cannot be NULL.");
		Validate.notNull(handler, "Handler cannot be NULL.");

		if (!plugin.isEnabled()) {
			throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
		}

		// Create normal listeners
		for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginLoader()
				.createRegisteredListeners(listener, plugin).entrySet()) {

			// Wrap these listeners in our exception handler
			getHandlerList(entry.getKey()).registerAll(wrapAll(entry.getValue(), handler));
		}
	}

	/**
	 * Wrap every listener in the given collection around an exception handler.
	 * 
	 * @param listeners - the listeners to wrap.
	 * @param handler   - the exception handler to add.
	 * @return The wrapped listeners.
	 */
	private static Collection<RegisteredListener> wrapAll(Collection<RegisteredListener> listeners,
			EventExceptionHandler handler) {
		List<RegisteredListener> output = Lists.newArrayList();

		for (RegisteredListener listener : listeners) {
			output.add(new ExceptionRegisteredListener(listener, handler));
		}
		return output;
	}

	/**
	 * Retrieve the handler list associated with the given class.
	 * 
	 * @param clazz - given event class.
	 * @return Associated handler list.
	 */
	private static HandlerList getHandlerList(Class<? extends Event> clazz) {
		// Class must have Event as its superclass
		while (clazz.getSuperclass() != null && Event.class.isAssignableFrom(clazz.getSuperclass())) {
			try {
				Method method = clazz.getDeclaredMethod("getHandlerList");
				method.setAccessible(true);
				return (HandlerList) method.invoke(null);
			} catch (NoSuchMethodException e) {
				// Keep on searching
				clazz = clazz.getSuperclass().asSubclass(Event.class);
			} catch (Exception e) {
				throw new IllegalPluginAccessException(e.getMessage());
			}
		}
		throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName());
	}

	/**
	 * Handle a given exception.
	 * 
	 * @param ex    - the exception to handle.
	 * @param event - the event that was being handled.
	 * @return TRUE to indicate that the exception has been handled, FALSE to
	 *         rethrow it.
	 */
	public boolean handle(Throwable ex, Event event) {
		if (plugin.getConfig().getBoolean("verboseLogging")
				&& new Date().getTime() - this.date.getTime() >= 20*60*1000) {
			plugin.getLogger().log(Level.SEVERE, "Error " + ex.getMessage() + " occured and has been logged");
		}

		File logFoler = new File(plugin.getDataFolder(), "logs");
		if (!logFoler.exists()) {
			logFoler.mkdir();
		}

		File dayLog = new File(logFoler, new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");
		if (!dayLog.exists()) {
			try {
				dayLog.createNewFile();

			} catch (IOException e) {
				e.printStackTrace();
				PluginManager pm = plugin.getServer().getPluginManager();
				pm.disablePlugin(plugin);
				return false;
			}
		}
		FileWriter writer;
		try {
			writer = new FileWriter(dayLog, true);
		} catch (IOException e) {
			PluginManager pm = plugin.getServer().getPluginManager();
			pm.disablePlugin(plugin);
			e.printStackTrace();
			return false;
		}
		PrintWriter pw = new PrintWriter(new BufferedWriter(writer));
		pw.write("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] ");
		ex.printStackTrace(pw);
		pw.close();

		// Don't pass it on
		return true;
		// Use Bukkit's default exception handler
		// return false;
	}

}