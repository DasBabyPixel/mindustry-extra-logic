package extralogic.logic;

import arc.func.Prov;
import arc.struct.Seq;
import mindustry.gen.LogicIO;
import mindustry.logic.LAssembler;
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

	public static ExtraLStatement read(String[] tokens, int tok) {
		if (ExtraLAssembler.customParsers.containsKey(tokens[0])) {
			return ExtraLAssembler.customParsers.get(tokens[0]).get(tokens);
		}
		LStatement vanilla = LogicIO.read(tokens, tok);
		if (vanilla != null) {
			return new WrapperExtraLStatement(vanilla);
		}
		if (LAssembler.customParsers.containsKey(tokens[0])) {
			return new WrapperExtraLStatement(LAssembler.customParsers.get(tokens[0]).get(tokens));
		}
		return null;
	}

}
