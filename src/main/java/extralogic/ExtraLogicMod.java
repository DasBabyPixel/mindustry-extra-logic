package extralogic;

import arc.util.Log;
import extralogic.content.ExtraLogicBlocks;
import extralogic.content.ExtraLogicContent;
import extralogic.content.ExtraLogicUI;
import extralogic.logic.statements.ExtraLStatements;
import mindustry.mod.Mod;

public class ExtraLogicMod extends Mod {

	public ExtraLogicMod() {
		Log.info("Initializing ExtraLogic!");
//		// listen for game load event

	}

	@Override
	public void loadContent() {
		Log.info("Loading ExtraLogic content!");
		ExtraLogicBlocks.load();
		ExtraLStatements.load();
	}
	
	@Override
	public void init() {
		ExtraLogicContent.ui = new ExtraLogicUI();
	}

}
