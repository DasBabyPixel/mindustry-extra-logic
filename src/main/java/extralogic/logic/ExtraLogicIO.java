package extralogic.logic;

import arc.func.Prov;
import arc.struct.Seq;
import mindustry.gen.LogicIO;
import mindustry.logic.LStatement;

/**
 * From {@link LogicIO}
 * 
 * @author DasBabyPixel
 */
public class ExtraLogicIO {

	public static Seq<Prov<ExtraLStatement>> allStatements() {
		Seq<Prov<ExtraLStatement>> statements = new Seq<>();
		for (Prov<LStatement> vanilla : LogicIO.allStatements) {
			statements.add(() -> new WrapperExtraLStatement(vanilla.get()));
		}
		return statements;
	}

}
