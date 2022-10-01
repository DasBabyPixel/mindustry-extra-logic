package extralogic.logic;

import static mindustry.Vars.*;

import arc.struct.IntSet;
import arc.struct.LongSeq;
import arc.util.Nullable;
import extralogic.content.ExtraLogicContent;
import extralogic.logic.ExtraLogicBlock.ExtraLogicBuilding;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LExecutor;
import mindustry.logic.LExecutor.Var;

/**
 * From {@link LExecutor}
 * 
 * @author DasBabyPixel
 */
public class ExtraLExecutor {

//	--- Removed instruction cap ---
//	public static final int maxInstructions = 1000;
	// special variables
	public static final int varCounter = 0, varUnit = 1, varThis = 2;

	public static final int maxGraphicsBuffer = 256, maxDisplayBuffer = 1024, maxTextBuffer = 400;

	public ExtraLInstruction[] instructions = {};

	public ExtraVar[] vars = {};

	public ExtraVar counter;

	public int[] binds;

	public int iptIndex = -1;

	public LongSeq graphicsBuffer = new LongSeq();

	public StringBuilder textBuffer = new StringBuilder();

	public Building[] links = {};

	public @Nullable ExtraLogicBuilding build;

	public IntSet linkIds = new IntSet();

	public Team team = Team.derelict;

	public boolean privileged = false;
	
	public VanillaLExecutor vanilla;

	public boolean initialized() {
		return instructions.length > 0;
	}

	/** Runs a single instruction. */
	public void runOnce() {
		// reset to start
		if (counter.handle.numval >= instructions.length || counter.handle.numval < 0) {
			counter.handle.numval = 0;
		}

		if (counter.handle.numval < instructions.length) {
			instructions[(int) (counter.handle.numval++)].run(this);
		}
	}

	/** Loads with a specified assembler. Resets all variables. */
	public void load(ExtraLAssembler builder) {
		vars = new ExtraVar[builder.vars.size];
		instructions = builder.instructions;
		iptIndex = -1;

		builder.vars.each((name, var) -> {
			ExtraVar dest = new ExtraVar(new Var(name));
			vars[var.id] = dest;
			if (dest.handle.name.equals("@ipt")) {
				iptIndex = var.id;
			}

			dest.handle.constant = var.constant;

			if (var.value instanceof Number number) {
				dest.handle.isobj = false;
				dest.handle.numval = number.doubleValue();
			} else {
				dest.handle.isobj = true;
				dest.handle.objval = var.value;
			}
		});

		counter = vars[varCounter];
		vanilla = new VanillaLExecutor(this);
	}

	// region utility

	private static boolean invalid(double d) {
		return Double.isNaN(d) || Double.isInfinite(d);
	}

	public ExtraVar var(int index) {
		// global constants have variable IDs < 0, and they are fetched from the global
		// constants object after being negated
		return index < 0 ? ExtraLogicContent.logicVars.get(-index) : vars[index];
	}

	public @Nullable Building building(int index) {
		Object o = var(index).handle.objval;
		return var(index).handle.isobj && o instanceof Building building ? building : null;
	}

	public @Nullable Object obj(int index) {
		Object o = var(index).handle.objval;
		return var(index).handle.isobj ? o : null;
	}

	public @Nullable Team team(int index) {
		ExtraVar v = var(index);
		if (v.handle.isobj) {
			return v.handle.objval instanceof Team t ? t : null;
		}
		int t = (int) v.handle.numval;
		if (t < 0 || t >= Team.all.length)
			return null;
		return Team.all[t];
	}

	public boolean bool(int index) {
		ExtraVar v = var(index);
		return v.handle.isobj ? v.handle.objval != null : Math.abs(v.handle.numval) >= 0.00001;
	}

	public double num(int index) {
		ExtraVar v = var(index);
		return v.handle.isobj ? v.handle.objval != null ? 1 : 0 : invalid(v.handle.numval) ? 0 : v.handle.numval;
	}

	public float numf(int index) {
		ExtraVar v = var(index);
		return v.handle.isobj ? v.handle.objval != null ? 1 : 0
				: invalid(v.handle.numval) ? 0 : (float) v.handle.numval;
	}

	public int numi(int index) {
		return (int) num(index);
	}

	public void setbool(int index, boolean value) {
		setnum(index, value ? 1 : 0);
	}

	public void setnum(int index, double value) {
		ExtraVar v = var(index);
		if (v.handle.constant)
			return;
		if (invalid(value)) {
			v.handle.objval = null;
			v.handle.isobj = true;
		} else {
			v.handle.numval = value;
			v.handle.objval = null;
			v.handle.isobj = false;
		}
	}

	public void setobj(int index, Object value) {
		ExtraVar v = var(index);
		if (v.handle.constant)
			return;
		v.handle.objval = value;
		v.handle.isobj = true;
	}

	public void setconst(int index, Object value) {
		ExtraVar v = var(index);
		v.handle.objval = value;
		v.handle.isobj = true;
	}

	// endregion

	/** A logic variable. */
	public static class ExtraVar {

		public final Var handle;

		public ExtraVar(Var handle) {
			this.handle = handle;
		}

	}

	// region instruction types

	public interface ExtraLInstruction {

		void run(ExtraLExecutor exec);

	}

	// endregion
	// region privileged / world instructions

	/** @return whether the map area is already set to this value. */
	static boolean checkMapArea(int x, int y, int w, int h, boolean set) {
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		w = Math.min(world.width(), w);
		h = Math.min(world.height(), h);
		boolean full = x == 0 && y == 0 && w == world.width() && h == world.height();

		if (state.rules.limitMapArea) {
			if (state.rules.limitX == x && state.rules.limitY == y && state.rules.limitWidth == w
					&& state.rules.limitHeight == h) {
				return true;
			} else if (full) {
				// disable the rule, covers the whole map
				if (set) {
					state.rules.limitMapArea = false;
					if (!headless) {
						renderer.updateAllDarkness();
					}
					world.checkMapArea();
					return false;
				}
			}
		} else if (full) { // was already disabled, no need to change anything
			return true;
		}

		if (set) {
			state.rules.limitMapArea = true;
			state.rules.limitX = x;
			state.rules.limitY = y;
			state.rules.limitWidth = w;
			state.rules.limitHeight = h;
			world.checkMapArea();

			if (!headless) {
				renderer.updateAllDarkness();
			}
		}

		return false;
	}

//	@Remote(called = Loc.server)
//	public static void setMapArea(int x, int y, int w, int h) {
//		checkMapArea(x, y, w, h, true);
//	}
//
//	@Remote(called = Loc.server, unreliable = true)
//	public static void logicExplosion(Team team, float x, float y, float radius, float damage, boolean air,
//			boolean ground, boolean pierce) {
//		if (damage < 0f)
//			return;
//
//		Damage.damage(team, x, y, radius, damage, pierce, air, ground);
//		if (pierce) {
//			Fx.spawnShockwave.at(x, y, World.conv(radius));
//		} else {
//			Fx.dynamicExplosion.at(x, y, World.conv(radius) / 8f);
//		}
//	}

}
