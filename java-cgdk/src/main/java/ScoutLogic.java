import model.*;

import java.util.ArrayList;
import java.util.List;


public final class ScoutLogic extends BaseLogic {
    @Override
    public void action(Trooper self, World world, Game game, Move move) {

        if(ActionHelper.isNeedAttack(game,world,self,move)){
            return;
        }
        else{
            move(self,world,game,move);
        }
    }


}
