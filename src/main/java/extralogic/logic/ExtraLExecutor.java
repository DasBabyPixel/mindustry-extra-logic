package extralogic.logic;

import arc.struct.IntSet;
import arc.util.Nullable;
import extralogic.logic.ExtraLogicBlock.ExtraLogicBuilding;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LAssembler;
import mindustry.logic.LExecutor;
import mindustry.logic.LExecutor.Var;

/**
 * Alternative {@link LExecutor}
 * @author DasBabyPixel
 */
public class ExtraLExecutor {

	
	public boolean privileged;
	public @Nullable ExtraLogicBuilding build;
    public Building[] links = {};
    public IntSet linkIds = new IntSet();
	public Var[] vars = {};
	public Team team;
	public void load(ExtraLAssembler asm) {
	}
	public void load(LAssembler assemble) {
	}
	public boolean initialized() {
		return false;
	}
	public void runOnce() {
	}
}
