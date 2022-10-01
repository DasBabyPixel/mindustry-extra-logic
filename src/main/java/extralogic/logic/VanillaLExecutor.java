package extralogic.logic;

import java.util.Arrays;

import extralogic.logic.ExtraLExecutor.ExtraVar;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LAssembler;
import mindustry.logic.LExecutor;

public class VanillaLExecutor extends LExecutor {

	private final ExtraLExecutor extra;

	public VanillaLExecutor(ExtraLExecutor extra) {
		this.extra = extra;
		this.binds = extra.binds;
		this.build = null;
		this.counter = extra.counter.handle;
		this.graphicsBuffer = extra.graphicsBuffer;
		this.instructions = new LInstruction[extra.instructions.length];
		Arrays.fill(instructions, new NoopI());
		this.iptIndex = extra.iptIndex;
		this.linkIds = extra.linkIds;
		this.links = extra.links;
		this.privileged = extra.privileged;
		this.team = extra.team;
		this.textBuffer = extra.textBuffer;
		this.vars = null;
	}

	@Override
	public boolean initialized() {
		return extra.initialized();
	}

	@Override
	public void runOnce() {
		extra.runOnce();
	}

	@Override
	public void load(LAssembler builder) {
		throw new UnsupportedOperationException("Cannot load this from an LAssembler. Use ExtraLAssembler instead!");
//		extra.load(builder);
	}

	@Override
	public Var var(int index) {
		ExtraVar ev = extra.var(index);
		if (ev == null)
			throw new NullPointerException("Variable at index " + index + " is null");
		return ev.handle;
	}

	@Override
	public Building building(int index) {
		return extra.building(index);
	}

	@Override
	public Object obj(int index) {
		return extra.obj(index);
	}

	@Override
	public Team team(int index) {
		return extra.team(index);
	}

	@Override
	public boolean bool(int index) {
		return extra.bool(index);
	}

	@Override
	public double num(int index) {
		return extra.num(index);
	}

	@Override
	public float numf(int index) {
		return extra.numf(index);
	}

	@Override
	public int numi(int index) {
		return extra.numi(index);
	}

	@Override
	public void setbool(int index, boolean value) {
		extra.setbool(index, value);
	}

	@Override
	public void setnum(int index, double value) {
		extra.setnum(index, value);
	}

	@Override
	public void setobj(int index, Object value) {
		extra.setobj(index, value);
	}

	@Override
	public void setconst(int index, Object value) {
		extra.setconst(index, value);
	}

}
