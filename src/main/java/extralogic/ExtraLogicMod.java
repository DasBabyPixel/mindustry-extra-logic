package extralogic;

import arc.Events;
import arc.util.Log;
import extralogic.content.ExtraLogicBlocks;
import extralogic.content.ExtraLogicContent;
import extralogic.content.ExtraLogicUI;
import extralogic.logic.ExtraLogicIO;
import extralogic.logic.statements.ExtraLStatements;
import mindustry.game.EventType;
import mindustry.mod.Mod;

public class ExtraLogicMod extends Mod {

	public ExtraLogicMod() {
		Log.info("Initializing ExtraLogic!");
		Events.on(EventType.WorldLoadEvent.class, e->{
			ExtraLogicIO.regenerateStatements();
		});
	}

	@Override
	public void loadContent() {
		Log.info("Loading ExtraLogic content!");
		ExtraLogicBlocks.load();
		ExtraLStatements.load();
		ExtraLogicIO.regenerateStatements();
	}

	@Override
	public void init() {
		ExtraLogicContent.ui = new ExtraLogicUI();
		ExtraLogicContent.logicVars.init();
	}

}
