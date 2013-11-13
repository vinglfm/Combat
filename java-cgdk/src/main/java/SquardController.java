import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.CellType;
import model.Direction;
import model.Trooper;
import model.World;

public class SquardController {
	private static World world;
	private static MovementHelper movementHelper;

	private static Map<Integer, PathHelper> pathes = new HashMap<>();

	public static void init(World curWorld) {
		if (world == null) {
			world = curWorld;

			movementHelper = new MovementHelper(curWorld.getCells());
			movementHelper.updateGoalLocation();
		}
	}

	public static Direction getDirection(int forTrooper, int xCord, int yCord) {

		PathHelper pathHelper = pathes.get(forTrooper);
		if (pathHelper == null) {
			List<LocationVertex> pathToTarget = movementHelper.updatePath(
					xCord, yCord);
			pathHelper = new PathHelper(pathToTarget);
			pathes.put(forTrooper, pathHelper);
		}

		if (pathHelper.isTargetLocationAchieved()) {
            pathes.clear();
            movementHelper.updateGoalLocation();
			return Direction.CURRENT_POINT;
		}

		return pathHelper.getDirection();
	}



    public static void setFightGoalLocation(int destinationX, int destinationY) {
        movementHelper.updateGoalLocation(destinationX, destinationY);
        pathes.clear();
    }

	private static final class PathHelper {
		private int currentPosition;
		private final List<LocationVertex> path;

		private PathHelper(List<LocationVertex> path) {
			this.path = Collections.unmodifiableList(path);
		}

		private boolean isTargetLocationAchieved() {
			if (currentPosition >= path.size() - 1)
				return true;
			return false;
		}

		private Direction getDirection() {
			if (currentPosition < path.size() - 1) {
				LocationVertex vertex = path.get(currentPosition++);
				LocationVertex nextVertex = path.get(currentPosition);

				int xDif = nextVertex.getXCord() - vertex.getXCord();
				switch (xDif) {
				case -1:
					return Direction.WEST;
				case 1:
					return Direction.EAST;
				case 0: {
					int yDif = nextVertex.getYCord() - vertex.getYCord();
					switch (yDif) {
					case -1:
						return Direction.NORTH;
					case 1:
						return Direction.SOUTH;
					case 0:
						return Direction.CURRENT_POINT;
					default:
						throw new LogicError();
					}
				}
				default:
					throw new LogicError();
				}
			}
			return Direction.CURRENT_POINT;
		}
	}

	private static final class MovementHelper {
		private static final int OFFSET = 100;
		private final Random rand = new Random();
		private LocationVertex targetLocation;

		private Map<Integer, LocationVertex> vertexes = new HashMap<>();
		private List<Integer> keys;

		MovementHelper(CellType[][] cells) {

			buildGraph(cells);

			keys = new ArrayList<>(vertexes.size());
			for (Integer value : vertexes.keySet()) {
				keys.add(value);
			}
		}

		private void buildGraph(CellType[][] cells) {
			Integer index;
			LocationVertex vertex;
			for (int i = 0; i < cells.length; ++i) {
				for (int j = 0; j < cells[i].length; ++j) {
					if (cells[i][j] == CellType.FREE) {
						index = j * OFFSET + i;
						vertex = vertexes.get(index);
						if (vertex == null) {
							vertex = new LocationVertex(i, j);
							vertexes.put(index, vertex);
						}
						addNearests(vertex, i, j, cells);
					}
				}
			}
		}

		private void addNearests(LocationVertex vertex, int x, int y,
				CellType[][] cells) {
			int nearestIndex;
			LocationVertex nearestVertex;
			if (y - 1 >= 0 && cells[x][y - 1] == CellType.FREE) {
				nearestIndex = (y - 1) * OFFSET + x;
				nearestVertex = vertexes.get(nearestIndex);
				if (nearestVertex == null) {
					nearestVertex = new LocationVertex(x, y - 1);
					vertexes.put(nearestIndex, nearestVertex);
				}
				vertex.addNearest(nearestVertex);
			}

			if (y + 1 < cells[x].length && cells[x][y + 1] == CellType.FREE) {
				nearestIndex = (y + 1) * OFFSET + x;
				nearestVertex = vertexes.get(nearestIndex);
				if (nearestVertex == null) {
					nearestVertex = new LocationVertex(x, y + 1);
					vertexes.put(nearestIndex, nearestVertex);
				}
				vertex.addNearest(nearestVertex);
			}

			if (x - 1 >= 0 && cells[x - 1][y] == CellType.FREE) {
				nearestIndex = y * OFFSET + x - 1;
				nearestVertex = vertexes.get(nearestIndex);
				if (nearestVertex == null) {
					nearestVertex = new LocationVertex(x - 1, y);
					vertexes.put(nearestIndex, nearestVertex);
				}
				vertex.addNearest(nearestVertex);
			}

			if (x + 1 < cells.length && cells[x + 1][y] == CellType.FREE) {
				nearestIndex = y * OFFSET + x + 1;
				nearestVertex = vertexes.get(nearestIndex);
				if (nearestVertex == null) {
					nearestVertex = new LocationVertex(x + 1, y);
					vertexes.put(nearestIndex, nearestVertex);
				}
				vertex.addNearest(nearestVertex);
			}

		}

		private List<LocationVertex> updatePath(int xCord, int yCord) {
			LocationVertex from = fromLocation(xCord, yCord);
			List<LocationVertex> pathToTargetLocation = path(from,
					targetLocation);

			return pathToTargetLocation;
		}

		private LocationVertex fromLocation(int xCord, int yCord) {
			int index = yCord * OFFSET + xCord;
			return vertexes.get(index);
		}

		private void updateGoalLocation() {
			int pos = rand.nextInt(keys.size());

			targetLocation = vertexes.get(keys.get(pos));
		}


        private void updateGoalLocation(int destinationX, int destinationY) {
            LocationVertex newGoalLocation = fromLocation(destinationX, destinationY);

            if(newGoalLocation == null)
                throw new LogicError();

            targetLocation = newGoalLocation;
        }


        private List<LocationVertex> path(LocationVertex from, LocationVertex to) {

			LocationVertex vertex = from;
			beforePathCalculation(vertex);

			List<LocationVertex> workingQueue = new LinkedList<LocationVertex>();
			workingQueue.add(vertex);
			while (!workingQueue.isEmpty()) {
				vertex = workingQueue.get(0);
				for (LocationVertex child : vertex.getNearests()) {
					if (child.getWeight() > vertex.getWeight() + 1) {
						child.setWeight(vertex.getWeight() + 1);
						workingQueue.add(child);
					}
				}
				workingQueue.remove(0);
			}

			LinkedList<LocationVertex> results = new LinkedList<LocationVertex>();
			LocationVertex toVertex = to;
			while (!toVertex.equals(from)) {
				results.addFirst(toVertex);
				for (LocationVertex elem : toVertex.getNearests())
					if (elem.getWeight() == toVertex.getWeight() - 1) {
						toVertex = elem;
						break;
					}
			}
			results.addFirst(from);

			afterPathCalculation();

			return results;
		}

		/**
		 * Uses to prepare vertexes to road calculation
		 */
		private void beforePathCalculation(LocationVertex from) {
			from.setWeight(0);
		}

		/**
		 * Uses to release calculation results from vertexes
		 */
		private void afterPathCalculation() {
			for (LocationVertex vertex : vertexes.values()) {
				vertex.setWeight(Integer.MAX_VALUE);
			}
		}
    }

}
