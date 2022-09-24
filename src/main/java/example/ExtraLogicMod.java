package example;

import arc.util.Log;
import mindustry.logic.LExecutor;
import mindustry.mod.Mod;

public class ExtraLogicMod extends Mod {

	public ExtraLogicMod() {
		Log.info("Loaded ExampleJavaMod constructor.");
		ReflectionHelper.set(LExecutor.class, "maxInstructions", null, 100000);
//		// listen for game load event
//		Events.on(ClientLoadEvent.class, e -> {
//			// show dialog upon startup
//			Time.runTask(10f, () -> {
//				BaseDialog dialog = new BaseDialog("frog");
//				dialog.cont.add("behold").row();
//				// mod sprites are prefixed with the mod name (this mod is called
//				// 'example-java-mod' in its config)
//				dialog.cont.image(Core.atlas.find("mindustry-extra-logic-frog")).pad(20f).row();
//				dialog.cont.button("I see", dialog::hide).size(100f, 50f);
//				dialog.show();
//			});
//		});

	}

	@Override
	public void loadContent() {
		Log.info("Loading some example content.");
	}

}
