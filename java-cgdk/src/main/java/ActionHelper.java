import model.*;

import java.util.*;

/**
 * (c) Author LostSoul
 */
public class ActionHelper {
    private final static int ACTION_BONUS_FROM_GENDER = 3;

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
        if (self.getActionPoints() > self.getShootCost()) {
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
        if (self.getActionPoints() > self.getShootCost())
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
        if (self.getActionPoints() > game.getGrenadeThrowCost()) {
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

    private static List<Trooper> getNearestEnemies(World world) {
        List<Trooper> enemyList = new ArrayList<>();
        for (Trooper trooper : world.getTroopers()) {
            if (!trooper.isTeammate()) {
                enemyList.add(trooper);
            }
        }
        return enemyList;
    }


    private static boolean canAttack(Trooper self, Trooper enemy, World world) {

        if (world.isVisible(self.getVisionRange(), self.getX(), self.getY(), self.getStance(), enemy.getX(), enemy.getY(), enemy.getStance())
                && self.getDistanceTo(enemy) < self.getShootingRange()) {
            return true;
        }

        return false;

    }


    private static boolean isNeedEat(Game game, World world, Trooper self, Move move) {
        if (self.isHoldingFieldRation() && self.getActionPoints() > game.getFieldRationEatCost()) {
            if (self.getActionPoints() > self.getInitialActionPoints()) {
                move.setAction(ActionType.EAT_FIELD_RATION);
                move.setDirection(Direction.CURRENT_POINT);
                return true;
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

    public static boolean isNeedGetBack(){
        return false;


    }
    private static Direction getDirection(World world, Trooper self, int destinationX, int destinationY){
            SquardController.init(world);
            //SquardController.getDirection(self.getId(), destinationX,destinationY);
           return  null;

   }

}
