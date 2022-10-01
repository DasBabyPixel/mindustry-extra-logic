package extralogic.logic;

import arc.func.Func;
import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.LogicIO;
import mindustry.logic.LAssembler;
import mindustry.logic.LStatement;

/**
 * From {@link LogicIO}
 * 
 * @author DasBabyPixel
 */
public class ExtraLogicIO {

	private static Seq<Prov<ExtraLStatement>> allStatements = null;

	@SuppressWarnings("unchecked")
	private static Seq<Prov<ExtraLStatement>> allExtraStatements = Seq.<Prov<ExtraLStatement>>with();

	private static ObjectMap<Class<? extends LStatement>, Overrider> overrideVanillas = new ObjectMap<>();

	private static ObjectMap<String, Func<String[], ? extends ExtraLStatement>> customParsers = new ObjectMap<>();

	public static Seq<Prov<ExtraLStatement>> allStatements() {
		if (allStatements != null)
			return allStatements;
		regenerateStatements();
		return allStatements;
	}

	public static void regenerateStatements() {
		Log.warn("Generating ExtraLogic statements");
		Seq<Prov<ExtraLStatement>> statements = new Seq<>();
		ObjectMap<Class<? extends LStatement>, Overrider> toOverride = new ObjectMap<>(overrideVanillas);
		for (Prov<LStatement> vanilla : LogicIO.allStatements) {
			LStatement vanillas = vanilla.get();
			Class<? extends LStatement> cls = vanillas.getClass();
			if (toOverride.containsKey(cls)) {
				String name = vanillas.name();
				Overrider o = toOverride.remove(cls);
				Func<String[], ? extends ExtraLStatement> parser = o.parser;
				customParsers.put(o.instruction, tokens -> {
					ExtraLStatement s = parser.get(tokens);
					s.name = name;
					return s;
				});
				Prov<? extends ExtraLStatement> prov = o.provider;
				statements.add(() -> {
					ExtraLStatement s = prov.get();
					s.name = name;
					return s;
				});
				Log.warn("Vanilla statement @ was overridden", cls.getSimpleName());
				continue;
			}
			statements.add(() -> new WrapperExtraLStatement(vanilla.get()));
		}
		for (Prov<ExtraLStatement> prov : allExtraStatements) {
			statements.add(prov);
		}
		for (Class<? extends LStatement> cls : toOverride.keys()) {
			Log.err("Failed to override vanilla statement @", cls.getSimpleName());
		}
		allStatements = statements;
	}

	public static ExtraLStatement read(String[] tokens, int tok) {
		allStatements();
		if (customParsers.containsKey(tokens[0])) {
			return customParsers.get(tokens[0]).get(tokens);
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

	public static void overrideVanilla(Class<? extends LStatement> vanilla, String instruction,
			Prov<? extends ExtraLStatement> provider, Func<String[], ? extends ExtraLStatement> parser) {
		overrideVanillas.put(vanilla, new Overrider(instruction, provider, parser));
	}

	private static class Overrider {

		private String instruction;

		private Prov<? extends ExtraLStatement> provider;

		private Func<String[], ? extends ExtraLStatement> parser;

		public Overrider(String instruction, Prov<? extends ExtraLStatement> provider,
				Func<String[], ? extends ExtraLStatement> parser) {
			this.instruction = instruction;
			this.provider = provider;
			this.parser = parser;
		}

	}

}
