import java.util.HashMap;
import java.util.Map;

import model.TrooperType;

enum Logics {
	COMMANDER(TrooperType.COMMANDER, new CommanderLogic()), FIELD_MEDIC(TrooperType.FIELD_MEDIC, new FieldMedicLogic()), SOLDIER(
			TrooperType.SOLDIER, new SoldierLogic()), SNIPER(TrooperType.SNIPER, new SniperLogic()), SCOUT(TrooperType.SCOUT,
			new ScoutLogic());
	
	private static Map<TrooperType, BaseLogic> logics = new HashMap<>();
	
	static {
		for(Logics singleLogic : Logics.values())
			logics.put(singleLogic.trooperType, singleLogic.logic);
	}

	TrooperType trooperType;
	BaseLogic logic;

	Logics(TrooperType trooperType, BaseLogic logic) {
		this.trooperType = trooperType;
		this.logic = logic;
	}

	public static BaseLogic getLogicByType(TrooperType type) {
		return logics.get(type);
	}

}
