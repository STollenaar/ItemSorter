package tollenaar.stephen.ItemSorter.Util;

import java.io.Serializable;

public class Ratio implements Serializable{

	private static final long serialVersionUID = -2636942689733341355L;
	private int first;
	private int second;
	private boolean firstSelector = true;
	private int count = 0;

	public Ratio(int first, int second) {
		this.first = first;
		this.second = second;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public boolean isFirstSelector() {
		return firstSelector;
	}

	public void setFirstSelector(boolean firstSelector) {
		this.firstSelector = firstSelector;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void addCount() {
		this.count++;
		if (isFirstSelector()) {
			if (getCount() >= getFirst()) {
				setCount(0);
				setFirstSelector(false);
			}
		} else {
			if (getCount() >= getSecond()) {
				setCount(0);
				setFirstSelector(true);
			}
		}
	}
}
