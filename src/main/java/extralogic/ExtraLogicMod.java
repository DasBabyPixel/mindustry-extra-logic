package extralogic;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import extralogic.content.ExtraLogicBlocks;
import extralogic.content.ExtraLogicContent;
import extralogic.content.ExtraLogicUI;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.logic.LExecutor;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;

public class ExtraLogicMod extends Mod {

	public ExtraLogicMod() {
		Log.info("Initializing ExtraLogic!");
//		// listen for game load event
		Events.on(ClientLoadEvent.class, e -> {
			// show dialog upon startup
			Time.runTask(10f, () -> {
				ExtraLogicBlocks.micro_processor.unlock();
				BaseDialog dialog = new BaseDialog("frog");
				dialog.cont.add("behold").row();
				// mod sprites are prefixed with the mod name (this mod is called
				// 'example-java-mod' in its config)
				dialog.cont.image(Core.atlas.find("extra-logic-frog")).pad(20f).row();
				dialog.cont.button("Processor Instruction Limit: " + LExecutor.maxInstructions, dialog::hide)
						.size(300f, 50f);
				dialog.show();
			});
		});

	}

	@Override
	public void loadContent() {
		Log.info("Loading ExtraLogic content!");
		ExtraLogicBlocks.load();
	}
	
	@Override
	public void init() {
		ExtraLogicContent.ui = new ExtraLogicUI();
	}

}
