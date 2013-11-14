import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LocationVertex {
	private int xCord;
	private int yCord;

	private int weight = Integer.MAX_VALUE;

	private List<LocationVertex> nearests;

	LocationVertex(int xCord, int yCord) {
		this.xCord = xCord;
		this.yCord = yCord;
		this.nearests = new ArrayList<>();
	}


    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if(!(obj instanceof LocationVertex))
            return false;

        LocationVertex other = (LocationVertex) obj;
        return xCord == other.getXCord() && yCord == other.getYCord();
    }

    /**
	 * @param vertex
	 *            nearest vertex to add
	 * @throws IllegalArgumentException
	 *             if vertex is null
	 */
	public void addNearest(LocationVertex vertex) {
		if (vertex == null) {
			throw new IllegalArgumentException("vertex can't be null");
		}
		this.nearests.add(vertex);
	}

	public Collection<LocationVertex> getNearests() {
		return Collections.unmodifiableCollection(nearests);
	}

	public int getXCord() {
		return xCord;
	}

	public int getYCord() {
		return yCord;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
}
