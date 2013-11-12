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



    protected void move(Trooper self, World world, Game game, Move move) {
        Direction dir = SquardController.getDirection(self.getTeammateIndex(), self.getX(), self.getY());
        if(dir.equals(Direction.CURRENT_POINT)){
            move.setDirection(Direction.WEST);
        } else {
            move.setDirection(dir);
        }
        move.setAction(ActionType.MOVE);
    }

    protected abstract void action(Trooper self, World world, Game game,
                                   Move move);
}
