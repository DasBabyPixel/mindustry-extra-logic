package extralogic.logic;

import static mindustry.type.ItemStack.*;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.blocks.logic.LogicBlock;

public class ExtraLogicProcessor extends LogicBlock {

	public ExtraLogicProcessor() {
		super("extra-logic-processor");
		
        requirements(Category.logic, with(Items.lead, 320, Items.silicon, 80, Items.graphite, 60, Items.thorium, 50));

        instructionsPerTick = 35;

        range = 8 * 28;

        size = 2;
	}

}
