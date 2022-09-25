package extralogic;

import static mindustry.type.ItemStack.*;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import mindustry.content.Items;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.logic.LExecutor;
import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.logic.LogicBlock;

public class ExtraLogicMod extends Mod {

	public static LogicBlock processor;

	public ExtraLogicMod() {
		Log.info("Loaded ExampleJavaMod constructor.");
//		// listen for game load event
		Events.on(ClientLoadEvent.class, e -> {
			processor.load();
			processor.loadIcon();
			processor.unlock();
			
			// show dialog upon startup
			Time.runTask(10f, () -> {
				BaseDialog dialog = new BaseDialog("frog");
				dialog.cont.add("behold").row();
				// mod sprites are prefixed with the mod name (this mod is called
				// 'example-java-mod' in its config)
				dialog.cont.image(Core.atlas.find("mindustry-extra-logic-frog")).pad(20f).row();
				dialog.cont.button("New Processor Instruction Limit: " + LExecutor.maxInstructions, dialog::hide)
						.size(100f, 50f);
				dialog.show();
			});
		});

	}

	@Override
	public void loadContent() {
		Log.info("Loading some example content.");
		processor = new LogicBlock("elprocessor") {

			{
				requirements(Category.logic,
						with(Items.lead, 320, Items.silicon, 80, Items.graphite, 60, Items.thorium, 50));
				instructionsPerTick = 35;

				range = 8 * 28;

				size = 2;
			}

		};
	}

}
