package tollenaar.stephen.ItemSorter.Util;

import org.apache.commons.lang.WordUtils;

public class Image {
	
	private String src;
	private String name;
	
	public Image(String src) {
		this.setSrc(src);
		this.setName(WordUtils.capitalizeFully(src.toLowerCase().replace("_", " ").split("/")[2].replace(".png", "")));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

}
