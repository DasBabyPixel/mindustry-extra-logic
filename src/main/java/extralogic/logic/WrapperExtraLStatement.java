package extralogic.logic;

import arc.scene.ui.layout.Table;
import mindustry.logic.LExecutor.LInstruction;
import mindustry.logic.LStatement;

public class WrapperExtraLStatement extends ExtraLStatement {

	public final LStatement handle;

	public WrapperExtraLStatement(LStatement handle) {
		this.handle = handle;
	}

	@Override
	public void build(Table table) {
		handle.build(table);
	}

	@Override
	public LInstruction build(ExtraLAssembler builder) {
		return null;
	}

}
