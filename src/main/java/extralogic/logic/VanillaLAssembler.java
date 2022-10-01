package extralogic.logic;

import mindustry.logic.LAssembler;

public class VanillaLAssembler extends LAssembler {

	private final ExtraLAssembler handle;

	private boolean usable = false;

	public VanillaLAssembler(ExtraLAssembler handle) {
		this.handle = handle;
		usable = true;
	}

	@Override
	public BVar getVar(String name) {
		if (usable)
			return handle.getVar(name);
		return new BVar(-1);
	}

	@Override
	public BVar putConst(String name, Object value) {
		if (usable)
			return handle.putConst(name, value);
		return new BVar(-1);
	}

	@Override
	public BVar putVar(String name) {
		if (usable)
			return handle.putVar(name);
		return new BVar(-1);
	}

	@Override
	public int var(String symbol) {
		if (usable)
			return handle.var(symbol);
		return 0;
	}

}
