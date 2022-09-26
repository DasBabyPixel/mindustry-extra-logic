package extralogic.logic;

import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import extralogic.logic.statements.ExtraLStatements;
import extralogic.logic.statements.ExtraLStatements.ExtraEndStatement;
import extralogic.logic.statements.ExtraLStatements.ExtraJumpStatement;
import mindustry.gen.LogicIO;
import mindustry.logic.LAssembler;
import mindustry.logic.LStatement;
import mindustry.logic.LStatements.EndStatement;
import mindustry.logic.LStatements.JumpStatement;

/**
 * From {@link LogicIO}
 * 
 * @author DasBabyPixel
 */
public class ExtraLogicIO {

	public static Seq<Prov<ExtraLStatement>> allStatements() {
		if (allStatements != null)
			return allStatements;
		ObjectMap<Class<? extends ExtraLStatement>, Class<? extends LStatement>> overridden = new ObjectMap<>();
		ObjectMap<Class<? extends LStatement>, Prov<LStatement>> overriddenVanillaProvs = new ObjectMap<>();
		Seq<Prov<ExtraLStatement>> statements = new Seq<>();
		for (Prov<LStatement> vanilla : LogicIO.allStatements) {
			Class<? extends LStatement> cls = vanilla.get().getClass();
			if (overrideVanillas.containsKey(cls)) {
				overriddenVanillaProvs.put(cls, vanilla);
				overridden.put(overrideVanillas.get(cls), cls);
				continue;
			}
			statements.add(() -> new WrapperExtraLStatement(vanilla.get()));
		}
		for (Prov<ExtraLStatement> prov : allExtraStatements) {
			Class<? extends ExtraLStatement> cls = prov.get().getClass();
			if (overridden.containsKey(cls)) {
				String name = overriddenVanillaProvs.get(overridden.get(cls)).get().name();
				statements.add(() -> {
					ExtraLStatement s = prov.get();
					s.name = name;
					return s;
				});
				continue;
			}
			statements.add(prov);
		}
		return allStatements = statements;
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

	private static Seq<Prov<ExtraLStatement>> allStatements = null;

	@SuppressWarnings("unchecked")
	public static Seq<Prov<ExtraLStatement>> allExtraStatements = Seq.<Prov<ExtraLStatement>>with(
			ExtraLStatements.ExtraJumpStatement::new, ExtraLStatements.ExtraEndStatement::new);

	public static ObjectMap<Class<? extends LStatement>, Class<? extends ExtraLStatement>> overrideVanillas = new ObjectMap<>();
	static {
		overrideVanillas.put(JumpStatement.class, ExtraJumpStatement.class);
		overrideVanillas.put(EndStatement.class, ExtraEndStatement.class);
	}

}
