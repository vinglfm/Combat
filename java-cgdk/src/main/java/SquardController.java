import model.CellType;
import model.Direction;
import model.Trooper;
import model.World;

import java.util.*;

public class SquardController {
    private static World world;
    private static MovementHelper movementHelper;

    //LOTS OF SPIKES...;)
    private static PathHelper pathToTarget;
    private static Map<Integer, PathHelper> pathesToCurrentPosition = new HashMap<>();
    private static Queue<LocationVertex> freeCliches = new ArrayDeque<>();
    private static List<Integer> calculated = new ArrayList<>();
    private static int troopsToReach = 0;
    private static int gap = 0;
    private static int duration = 4;

    public static void init(World curWorld) {
        if (world == null) {
            world = curWorld;

            movementHelper = new MovementHelper(curWorld.getCells());
            movementHelper.updateGoalLocation();
        }
    }

    public static Direction getDirection(Trooper forTrooper) {

        if (pathToTarget == null) {
            pathToTarget = new PathHelper(movementHelper.updatePath(
                    forTrooper.getX(), forTrooper.getY()));
        }

        if (pathToTarget.isTargetLocationAchieved()) {
            movementHelper.updateGoalLocation();
            pathToTarget = null;

            calculated.clear();
            freeCliches.clear();
            pathesToCurrentPosition.clear();

            return Direction.CURRENT_POINT;
        }

        return directionToNextPoint(forTrooper);
    }

    private static Direction directionToNextPoint(Trooper forTrooper) {
        PathHelper pathHelper = pathesToCurrentPosition.get(forTrooper.getTeammateIndex());
        if (pathHelper == null && !calculated.contains(forTrooper.getTeammateIndex())) {

            if (freeCliches.isEmpty()) {
                freeCliches = freeLocations(pathToTarget.getCurrentLocation());
                troopsToReach = freeCliches.size() - gap;
            }

            List<LocationVertex> path = movementHelper.pathTo(forTrooper.getX(), forTrooper.getY(),
                    retriveLocation());
            pathHelper = new PathHelper(path);
            pathesToCurrentPosition.put(forTrooper.getTeammateIndex(), pathHelper);
            calculated.add(forTrooper.getTeammateIndex());
        }
        if(pathHelper == null) {
            --duration;
            if(duration == 0) {
                duration = 4;
                ++gap;
            }
            return Direction.CURRENT_POINT;
        }

        if (pathHelper.isTargetLocationAchieved()) {
            pathesToCurrentPosition.remove(forTrooper.getTeammateIndex());
            --troopsToReach;
//            freeCliches.remove(pathHelper.getCurrentLocation());
            if (troopsToReach < 2) { //TODO:temprorary changed from 1 to 2... think what positions should be returned
                pathToTarget.moveCurrentPosition();
                calculated.clear();
                freeCliches.clear();
                pathesToCurrentPosition.clear();
            }
            return Direction.CURRENT_POINT;
        }

        return pathHelper.getDirection();
    }

    private static LocationVertex retriveLocation() {
        if (freeCliches.size() > 1) {
            return freeCliches.poll();
        } else {
            return freeCliches.peek();
        }

    }

    private static Queue<LocationVertex> freeLocations(LocationVertex near) {
        Queue<LocationVertex> freeLocs = new ArrayDeque<>();
        freeLocs.add(near);
        for (LocationVertex nearest : near.getNearests()) {
            if (!pathToTarget.notNext(nearest))
                freeLocs.add(nearest);
        }
        return freeLocs;
    }

//    public static void setFightGoalLocation(int destinationX, int destinationY) {
//        movementHelper.updateGoalLocation(destinationX, destinationY);
//        pathToTarget.clear();
//    }

    private static final class PathHelper {
        private final List<LocationVertex> path;
        private int currentPosition;

        private PathHelper(List<LocationVertex> path) {
            this.path = Collections.unmodifiableList(path);
        }

        private boolean isTargetLocationAchieved() {
            if (currentPosition >= path.size() - 1)
                return true;
            return false;
        }

        private LocationVertex getCurrentLocation() {
            return path.get(currentPosition);
        }

        private boolean notNext(LocationVertex vertex) {
            //TODO: check correctness...
            if (isTargetLocationAchieved())
                return false;
            return vertex.equals(path.get(currentPosition + 1));
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

        public void moveCurrentPosition() {
            ++currentPosition;
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

        private List<LocationVertex> pathTo(int fromX, int fromY, LocationVertex to) {
            LocationVertex from = fromLocation(fromX, fromY);
            return path(from, to);
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

            if (newGoalLocation == null)
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
