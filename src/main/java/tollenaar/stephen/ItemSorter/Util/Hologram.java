package tollenaar.stephen.ItemSorter.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class Hologram {

	private static Map<Location, Hologram> HOLOGRAMS;
	
	private Location hostLocation;
	private List<Location> hologramLocations;

	public Hologram(Entity host, String text) {
		this.hologramLocations = new ArrayList<>();
		this.hostLocation = host.getLocation();
		
		spawnText(hostLocation, text);
		if(HOLOGRAMS == null){
			HOLOGRAMS = new HashMap<>();
		}
		HOLOGRAMS.put(hostLocation, this);
	}

	private void spawnText(Location loc, String text) {
		List<String> inputConfigList = new ArrayList<String>(Arrays.asList(text.split(", ")));
		List<String> lines = new ArrayList<>();
		for (int i = 0; i < inputConfigList.size(); i += 4) {
			int maxSub = Math.min(inputConfigList.size() - i, 3);
			String line = inputConfigList.subList(i, i + maxSub).toString().replace("]", "").replace("[", "")
					.replaceAll(",", "");
			lines.add(line);
		}
		loc.setY(loc.getY()-2);
		for (String line : lines) {
			loc.setY(loc.getY()+0.25);
			ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
			stand.setVisible(false);
			stand.setGravity(false);
			stand.setCanPickupItems(false);
			stand.setInvulnerable(true);
			stand.setCustomNameVisible(true);
			stand.setCustomName(line);
			hologramLocations.add(stand.getLocation());
		}
	}
	
	private void removeText(){
		for (Entity ent : hostLocation.getChunk().getEntities()) {
			if(ent instanceof ArmorStand && hologramLocations.contains(ent.getLocation())){
				hologramLocations.remove(ent.getLocation());
				ent.remove();
			}
		}
	}
	
	public void deleteHologram(){
		removeText();
		HOLOGRAMS.remove(hostLocation);
	}
	
	public static Hologram getHologram(Location location){
		if(hologramExistsAtLocation(location)){
			return HOLOGRAMS.get(location);
		}
		return null;
	}
	
	public static boolean hologramExistsAtLocation(Location location){
		if(HOLOGRAMS == null){
			HOLOGRAMS = new HashMap<>();
		}
		return HOLOGRAMS.get(location) != null;
	}
	
	public static void removeHologramAtLocation(Location location){
		if(hologramExistsAtLocation(location)){
			getHologram(location).deleteHologram();
		}
	}
}
