package extralogic.logic;

import mindustry.gen.Building;
import mindustry.world.Block;

public class ExtraLogicBlock extends Block {

	public int instructionsPerTick;

	public int range = 8 * 10;

	public ExtraLogicBlock() {
		super("extra-logic-processor");
	}

	public class ExtraLogicBuilding extends Building {
	}

}
