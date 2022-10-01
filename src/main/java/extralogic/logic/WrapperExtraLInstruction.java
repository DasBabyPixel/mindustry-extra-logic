package extralogic.logic;

import extralogic.logic.ExtraLExecutor.ExtraLInstruction;
import mindustry.logic.LExecutor.LInstruction;
import mindustry.logic.LExecutor.SetI;

public class WrapperExtraLInstruction implements ExtraLInstruction {

	public final LInstruction handle;

	public WrapperExtraLInstruction(LInstruction handle) {
		this.handle = handle;
	}

	@Override
	public void run(ExtraLExecutor exec) {
		try {
			handle.run(exec.vanilla);
		} catch (Throwable ex) {
			if (handle instanceof SetI set) {
				throw new RuntimeException("Error while setting variable from " + set.from + " to " + set.to, ex);
			}
			throw ex;
		}
	}

}
