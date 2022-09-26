package extralogic.logic;

import static mindustry.Vars.*;

import arc.struct.IntSet;
import arc.struct.LongSeq;
import arc.util.Nullable;
import extralogic.logic.ExtraLogicBlock.ExtraLogicBuilding;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Damage;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LExecutor;
import mindustry.logic.LExecutor.Var;
import mindustry.logic.Remote;

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
	}

	// region utility

	private static boolean invalid(double d) {
		return Double.isNaN(d) || Double.isInfinite(d);
	}

	public ExtraVar var(int index) {
		// global constants have variable IDs < 0, and they are fetched from the global
		// constants object after being negated
		return index < 0 ? logicVars.get(-index) : vars[index];
	}

	public @Nullable Building building(int index) {
		Object o = var(index).objval;
		return var(index).isobj && o instanceof Building building ? building : null;
	}

	public @Nullable Object obj(int index) {
		Object o = var(index).objval;
		return var(index).isobj ? o : null;
	}

	public @Nullable Team team(int index) {
		Var v = var(index);
		if (v.isobj) {
			return v.objval instanceof Team t ? t : null;
		}
		int t = (int) v.numval;
		if (t < 0 || t >= Team.all.length)
			return null;
		return Team.all[t];
	}

	public boolean bool(int index) {
		Var v = var(index);
		return v.isobj ? v.objval != null : Math.abs(v.numval) >= 0.00001;
	}

	public double num(int index) {
		Var v = var(index);
		return v.isobj ? v.objval != null ? 1 : 0 : invalid(v.numval) ? 0 : v.numval;
	}

	public float numf(int index) {
		Var v = var(index);
		return v.isobj ? v.objval != null ? 1 : 0 : invalid(v.numval) ? 0 : (float) v.numval;
	}

	public int numi(int index) {
		return (int) num(index);
	}

	public void setbool(int index, boolean value) {
		setnum(index, value ? 1 : 0);
	}

	public void setnum(int index, double value) {
		Var v = var(index);
		if (v.constant)
			return;
		if (invalid(value)) {
			v.objval = null;
			v.isobj = true;
		} else {
			v.numval = value;
			v.objval = null;
			v.isobj = false;
		}
	}

	public void setobj(int index, Object value) {
		Var v = var(index);
		if (v.constant)
			return;
		v.objval = value;
		v.isobj = true;
	}

	public void setconst(int index, Object value) {
		Var v = var(index);
		v.objval = value;
		v.isobj = true;
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

	@Remote(called = Loc.server)
	public static void setMapArea(int x, int y, int w, int h) {
		checkMapArea(x, y, w, h, true);
	}

	@Remote(called = Loc.server, unreliable = true)
	public static void logicExplosion(Team team, float x, float y, float radius, float damage, boolean air,
			boolean ground, boolean pierce) {
		if (damage < 0f)
			return;

		Damage.damage(team, x, y, radius, damage, pierce, air, ground);
		if (pierce) {
			Fx.spawnShockwave.at(x, y, World.conv(radius));
		} else {
			Fx.dynamicExplosion.at(x, y, World.conv(radius) / 8f);
		}
	}

}
