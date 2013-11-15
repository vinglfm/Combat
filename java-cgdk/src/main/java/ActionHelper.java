import model.*;

import java.util.*;

/**
 * (c) Author LostSoul
 */
public class ActionHelper extends BaseActionHelper {


    static public boolean isNeedHeal(Game game, World world, Trooper self, Move move) {

        Trooper weakestTroop = null;
        if (self.getActionPoints() >= game.getFieldMedicHealCost()) {
            for (Trooper trooper : world.getTroopers()) {
                if (trooper.isTeammate() && (trooper.getMaximalHitpoints() - trooper.getHitpoints() > game.getFieldMedicHealBonusHitpoints())) {       //TODO if current HP is not full
                    if (weakestTroop == null) {
                        weakestTroop = trooper;
                    }
                    if (self.getDistanceTo(trooper) < self.getDistanceTo(weakestTroop)) {
                        weakestTroop = trooper;
                    }                                 // TODO need to check path instead distance
                }
            }

        }
        if (weakestTroop == null) {
            return false;
        }

//        if (self.getDistanceTo(weakestTroop) > 1 && self.getActionPoints() > getMoveCost(self.getStance(), game)) {        //TODO go to the trooper
//
//            move.setAction(ActionType.MOVE);
//            move.setX(weakestTroop.getX());
//            move.setY(weakestTroop.getY());
//
//            return true;
//        } else
        if (self.getDistanceTo(weakestTroop) <= 1) {
            move.setAction(ActionType.HEAL);
            move.setX(weakestTroop.getX());
            move.setY(weakestTroop.getY());
            return true;
        }
        return false;

    }


    static public boolean isNeedTreatment(Game game, World world, Trooper self, Move move) {
        if (self.isHoldingMedikit() && self.getActionPoints() > game.getMedikitUseCost()) {
            Trooper weakestTroop = null;

            for (Trooper trooper : world.getTroopers()) {
                if (trooper.isTeammate() && (trooper.getMaximalHitpoints() - trooper.getHitpoints() > game.getMedikitHealSelfBonusHitpoints())) {       //TODO if current HP is not full
                    if (weakestTroop == null) {
                        weakestTroop = trooper;
                    }
                    if (self.getDistanceTo(trooper) < self.getDistanceTo(weakestTroop)) {
                        weakestTroop = trooper;
                    }                                 // TODO need to check path instead distance
                }
            }


            if (weakestTroop == null) {
                return false;
            }
            if (self.getDistanceTo(weakestTroop) <= 1) {
                move.setAction(ActionType.USE_MEDIKIT);
                move.setX(weakestTroop.getX());
                move.setY(weakestTroop.getY());
                return true;
            }


        }
        return false;

    }


    static public boolean isNeedAttack(Game game, World world, Trooper self, Move move) {

        if (isShotWillDeny(game, world, self, move)) {
            return true;
        }
        if (self.getActionPoints() >= self.getShootCost()) {
            Trooper weakestEnemy = null;
            for (Trooper enemy : getNearestEnemies(world)) {

                if (canAttack(self, enemy, world)) {
                    if (weakestEnemy == null) {
                        weakestEnemy = enemy;
                    }
                    if (weakestEnemy.getHitpoints() > enemy.getHitpoints()) {
                        weakestEnemy = enemy;
                    }
                }
            }

            if (weakestEnemy != null) {
                if (isNeedThrowGrenade(game, world, self, move, weakestEnemy)) {
                    return true;
                }
                if (isNeedEat(game, world, self, move)) {
                    return true;
                }


                move.setAction(ActionType.SHOOT);
                move.setX(weakestEnemy.getX());
                move.setY(weakestEnemy.getY());
                return true;
            }
        }

        return false;
    }


    static public boolean isShotWillDeny(Game game, World world, Trooper self, Move move) {
        if (self.getActionPoints() >= self.getShootCost())
            for (Trooper enemy : getNearestEnemies(world)) {
                if (enemy.getHitpoints() < self.getDamage() && canAttack(self, enemy, world)) {
                    move.setAction(ActionType.SHOOT);
                    move.setX(enemy.getX());
                    move.setY(enemy.getY());
                    return true;
                }
            }

        return false;
    }

    public static boolean isNeedThrowGrenade(Game game, World world, Trooper self, Move move, Trooper enemy) {
        if (self.getActionPoints() >= game.getGrenadeThrowCost()) {
            if (self.isHoldingGrenade() && world.isVisible(self.getVisionRange(), self.getX(), self.getY(), self.getStance(), enemy.getX(), enemy.getY(), enemy.getStance())
                    && self.getDistanceTo(enemy) < game.getGrenadeThrowRange()) {

                move.setAction(ActionType.THROW_GRENADE);
                move.setX(enemy.getX());
                move.setY(enemy.getY());
                return true;
            } else if (self.isHoldingGrenade() && world.isVisible(self.getVisionRange(), self.getX(), self.getY(), self.getStance(), enemy.getX(), enemy.getY(), enemy.getStance()) // If we are too far but can come closer and throw grenade
                    && (self.getDistanceTo(enemy) - game.getGrenadeThrowRange()) == 1 && (getMoveCost(self.getStance(), game) + game.getGrenadeThrowCost() == 10)) {
                move.setAction(ActionType.MOVE);
                move.setX(enemy.getX());
                move.setY(enemy.getY());
                return true;
            } else if (self.isHoldingGrenade() && world.isVisible(self.getVisionRange(), self.getX(), self.getY(), self.getStance(), enemy.getX(), enemy.getY(), enemy.getStance())  // If we are too far but can eat and come closer and throw grenade
                    && (self.getDistanceTo(enemy) - game.getGrenadeThrowRange()) == 2 && self.isHoldingFieldRation() && (getMoveCost(self.getStance(), game) * 2 + game.getGrenadeThrowCost() <= 13)) {

                if (isNeedEat(game, world, self, move)) {
                    return true;
                }
            }

        }
        return false;
    }


    public static int getMoveCost(TrooperStance trooperStance, Game game) {
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

    public static boolean isNeedToHide(Game game, World world, Trooper self, Move move) {
       /* if (self.getActionPoints() >= game.getStanceChangeCost()) {                                 //TODO worked incorrect
            for (Trooper enemy : getNearestEnemies(world)) {

                if (world.isVisible(enemy.getVisionRange(), enemy.getX(), enemy.getY(), self.getStance(), self.getX(), self.getY(), self.getStance())) {                //If exists better position
                    if (!world.isVisible(enemy.getVisionRange(), enemy.getX(), enemy.getY(), self.getStance(), self.getX(), self.getY(), TrooperStance.KNEELING) &&
                            (world.isVisible(self.getVisionRange(), self.getX(), self.getY(), TrooperStance.KNEELING, enemy.getX(), enemy.getY(), enemy.getStance()))) {
                        move.setAction(ActionType.LOWER_STANCE);
                        move.setDirection(Direction.CURRENT_POINT);
                        return  true;


                    }
                    if (!world.isVisible(enemy.getVisionRange(), enemy.getX(), enemy.getY(), self.getStance(), self.getX(), self.getY(), TrooperStance.PRONE) &&
                            (world.isVisible(self.getVisionRange(), self.getX(), self.getY(), TrooperStance.PRONE, enemy.getX(), enemy.getY(), enemy.getStance()))) {
                        move.setAction(ActionType.LOWER_STANCE);
                        move.setDirection(Direction.CURRENT_POINT);
                        return  true;

                    }
                }


            }

        }   */


        return false;

    }

    public static boolean isNeedToGetBonus(Game game, World world, Trooper self, Move move) {  //TODO  need logic to finding nearest bonuses
        if (self.getActionPoints() > getMoveCost(self.getStance(), game)) {

            for (Bonus bonus : world.getBonuses()) {
                if (bonus.getDistanceTo(self) < 2 && !isHoldingBonus(self,bonus)) {
                    if ((self.getX() + 1 == bonus.getX() && self.getY() == bonus.getY()) && !isHoldingBonus(self,bonus) ) {

                        move.setAction(ActionType.MOVE);
                        move.setX(bonus.getX());
                        move.setY(bonus.getY());
                        return true;
                    } else if (self.getX() - 1 == bonus.getX() && self.getY() == bonus.getY()&& !isHoldingBonus(self,bonus)) {
                        move.setAction(ActionType.MOVE);
                        move.setX(bonus.getX());
                        move.setY(bonus.getY());
                        return true;
                    } else if (self.getX() == bonus.getX() && self.getY() + 1 == bonus.getY()&& !isHoldingBonus(self,bonus)) {
                        move.setAction(ActionType.MOVE);
                        move.setX(bonus.getX());
                        move.setY(bonus.getY());
                        return true;
                    } else if (self.getX() == bonus.getX() && self.getY() - 1 == bonus.getY()&& !isHoldingBonus(self,bonus)) {
                        move.setAction(ActionType.MOVE);
                        move.setX(bonus.getX());
                        move.setY(bonus.getY());
                        return true;
                    }
                }


            }


        }

        return false;
    }

    public static boolean isNeedToChangeState(Game game, World world, Trooper self, Move move) {


        if (self.getActionPoints() >= game.getStanceChangeCost()) {
            for (Trooper enemy : getNearestEnemies(world)) {
                if (isNeedAttack(game, world, self, move) && self.getStance().equals(TrooperStance.STANDING) && !isNeedThrowGrenade(game,world,self,move,enemy)) {
                    if (self.getType().equals(TrooperType.SOLDIER) && isChangingStanceProper(game, world, self, move, enemy, TrooperStance.KNEELING) && (self.getActionPoints() == self.getInitialActionPoints())) {                //if after lowering stance our damage will increase
                        move.setAction(ActionType.LOWER_STANCE);
                        move.setDirection(Direction.CURRENT_POINT);
                        return true;
                    }


                }
                if (isNeedAttack(game, world, self, move) && self.getStance().equals(TrooperStance.KNEELING) && (self.getActionPoints() == self.getInitialActionPoints()&& !isNeedThrowGrenade(game,world,self,move,enemy))) {
                    //if after lowering stance our damage will increase
                    if (self.getType().equals(TrooperType.SOLDIER) && isChangingStanceProper(game, world, self, move, enemy, TrooperStance.PRONE)) {
                        move.setAction(ActionType.LOWER_STANCE);
                        move.setDirection(Direction.CURRENT_POINT);
                        return true;
                    }


                }

                if (!(world.isVisible(self.getVisionRange(), self.getX(), self.getY(), self.getStance(), enemy.getX(), enemy.getY(), enemy.getStance()) &&          //If enemy can see us but we cant
                        world.isVisible(enemy.getVisionRange(), enemy.getX(), enemy.getY(), enemy.getStance(), self.getX(), self.getY(), self.getStance())) &&
                        self.getVisionRange() >= enemy.getVisionRange()) {

                    if (world.isVisible(self.getVisionRange(), self.getX(), self.getY(), TrooperStance.KNEELING, enemy.getX(), enemy.getY(), enemy.getStance()) && self.getStance().equals(TrooperStance.PRONE)) {
                        move.setAction(ActionType.RAISE_STANCE);
                        move.setDirection(Direction.CURRENT_POINT);
                        return true;
                    }

                    if (world.isVisible(self.getVisionRange(), self.getX(), self.getY(), TrooperStance.STANDING, enemy.getX(), enemy.getY(), enemy.getStance()) && self.getStance().equals(TrooperStance.KNEELING)) {    //If enemy can see us but we cant
                        move.setAction(ActionType.RAISE_STANCE);
                        move.setDirection(Direction.CURRENT_POINT);
                        return true;
                    }

                    return false;
                }
            }
            if (getNearestEnemies(world).isEmpty() && !self.getStance().equals(TrooperStance.STANDING)) {
                move.setAction(ActionType.RAISE_STANCE);
                move.setDirection(Direction.CURRENT_POINT);
                return true;
            }
        }


        return false;
    }

    public static boolean isNeedToCallAirSupport(Game game, World world, Trooper self, Move move) {

        return false;

    }

    public static boolean isNeedGetBack(Game game, World world, Trooper self, Move move) {

        if (self.getActionPoints() >= getMoveCost(self.getStance(), game)) {


            return true;


        }
        return false;


    }


}
