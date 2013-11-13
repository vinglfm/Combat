import model.ActionType;
import model.Direction;
import model.Game;
import model.Move;
import model.Trooper;
import model.TrooperStance;
import model.World;

abstract class BaseLogic {


    final void movement(Trooper self, World world, Game game, Move move) {
        TrooperStance trooperStance = self.getStance();
        int moveCost = ActionHelper.getMoveCost(trooperStance, game);

        if (self.getActionPoints() < moveCost) {
            move.setAction(ActionType.END_TURN);
            return;
        }

        // TODO: logic for checking nearest enemies, health of the comrades,
        // etc.
        action(self, world, game, move);


    }


    protected void move(Trooper self, World world, Game game, Move move) {     //TODO temp method coz our movement should not be random!
        Direction dir = SquardController.getDirection(self.getTeammateIndex(), self.getX(), self.getY());
        if (dir.equals(Direction.CURRENT_POINT)) {
            if (self.getActionPoints() > game.getStanceChangeCost()) {
                move.setAction(ActionType.LOWER_STANCE);
            }
        } else {
            move.setDirection(dir);
            move.setAction(ActionType.MOVE);
        }

    }

    protected abstract void action(Trooper self, World world, Game game,
                                   Move move);
}
