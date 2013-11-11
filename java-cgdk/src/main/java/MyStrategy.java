import model.Game;
import model.Move;
import model.Trooper;
import model.World;

public final class MyStrategy implements Strategy {

	@Override
	public void move(Trooper self, World world, Game game, Move move) {

		SquardController.init(world);

		BaseLogic logic = Logics.getLogicByType(self.getType());

		logic.movement(self, world, game, move);

	}
}
