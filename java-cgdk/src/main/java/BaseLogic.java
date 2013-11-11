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
		int moveCost = getMoveCost(trooperStance, game);

		if (self.getActionPoints() < moveCost) {
			return;
		}

		// TODO: logic for checking nearest enemies, health of the comrades,
		// etc.
		action(self, world, game, move);

		move(self, world, game, move);
	}

	private int getMoveCost(TrooperStance trooperStance, Game game) {
		int moveCost = -1;

		switch (trooperStance) {
		case PRONE:
			moveCost = game.getProneMoveCost();
			break;
		case KNEELING:
			moveCost = game.getKneelingMoveCost();
			break;
		case STANDING:
			moveCost = game.getStandingMoveCost();
			break;
		default:
			throw new LogicError();
		}

		return moveCost;
	}

	private void move(Trooper self, World world, Game game, Move move) {
		Direction dir = SquardController.getDirection(self.getTeammateIndex(), self.getX(),  self.getY());
		move.setDirection(dir);
		move.setAction(ActionType.MOVE);
	}

	protected abstract void action(Trooper self, World world, Game game,
			Move move);
}
