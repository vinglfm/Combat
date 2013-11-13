import model.*;

import java.util.ArrayList;
import java.util.List;


public final class FieldMedicLogic extends BaseLogic {

    @Override
    public void action(Trooper self, World world, Game game, Move move) {
        if(ActionHelper.isShotWillDeny(game, world,self,move)){
            return;
        }
        else if (ActionHelper.isNeedTreatment(game, world, self, move)) {
            return;
        }
        else if(ActionHelper.isNeedHeal(game, world, self , move)){
            return;
        }
        else if(ActionHelper.isNeedAttack(game, world, self , move)){
            return;
        }
        else {
            move(self,world, game, move);
        }
    }



}
