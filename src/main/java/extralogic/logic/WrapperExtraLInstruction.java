package extralogic.logic;

import extralogic.logic.ExtraLExecutor.ExtraLInstruction;
import mindustry.logic.LExecutor.LInstruction;

public class WrapperExtraLInstruction implements ExtraLInstruction {

	public final LInstruction handle;

	public WrapperExtraLInstruction(LInstruction handle) {
		this.handle = handle;
	}

	@Override
	public void run(ExtraLExecutor exec) {
	}

}
