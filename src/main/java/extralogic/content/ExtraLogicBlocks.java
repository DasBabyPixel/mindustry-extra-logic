package extralogic.content;

import extralogic.logic.ExtraLogicBlock;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class ExtraLogicBlocks {

	public static Block micro_processor, extra_processor, hyper_processor;

	public static void load() {
		micro_processor = new ExtraLogicBlock() {

			{
				requirements(Category.logic, ItemStack.with(Items.lead, 50), true);
			}

		};
	}

}
