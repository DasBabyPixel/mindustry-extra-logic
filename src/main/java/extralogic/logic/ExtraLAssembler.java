package extralogic.logic;

import arc.func.Func;
import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Strings;
import extralogic.content.ExtraLogicContent;
import extralogic.logic.ExtraLExecutor.ExtraLInstruction;
import mindustry.Vars;
import mindustry.logic.LAssembler;
import mindustry.logic.LAssembler.BVar;

/**
 * Alternative {@link LAssembler}
 * 
 * @author DasBabyPixel
 */
public class ExtraLAssembler {

	public static ObjectMap<String, Func<String[], ExtraLStatement>> customParsers = new ObjectMap<>();

	public static final int maxTokenLength = 36;

	private static final int invalidNum = Integer.MIN_VALUE;

	private int lastVar;

	/** Maps names to variable IDs. */
	public ObjectMap<String, ExtraBVar> vars = new ObjectMap<>();

	/** All instructions to be executed. */
	public ExtraLInstruction[] instructions;

	private VanillaLAssembler vanillaClone = null;

	public ExtraLAssembler() {
		// instruction counter
		putVar("@counter").value = 0;
		// currently controlled unit
		putConst("@unit", null);
		// reference to self
		putConst("@this", null);
	}

	public VanillaLAssembler vanillaClone() {
		if(vanillaClone==null) {
			vanillaClone = new VanillaLAssembler(this);
		}
		return vanillaClone;
	}

	public static ExtraLAssembler assemble(String data, boolean privileged) {
		ExtraLAssembler asm = new ExtraLAssembler();

		Seq<ExtraLStatement> st = read(data, privileged);

		asm.instructions = st.map(l -> l.build(asm)).filter(l -> l != null).toArray(ExtraLInstruction.class);
		return asm;
	}

	public static String write(Seq<ExtraLStatement> statements) {
		StringBuilder out = new StringBuilder();
		for (ExtraLStatement s : statements) {
			s.write(out);
			out.append("\n");
		}

		return out.toString();
	}

	/** Parses a sequence of statements from a string. */
	public static Seq<ExtraLStatement> read(String text, boolean privileged) {
		// don't waste time parsing null/empty text
		if (text == null || text.isEmpty())
			return new Seq<>();
		return new ExtraLParser(text, privileged).parse();
	}

	/**
	 * @return a variable ID by name. This may be a constant variable referring to a
	 *         number or object.
	 */
	public int var(String symbol) {
		int constId = Vars.logicVars.get(symbol);
		if (constId > 0) {
			// global constants are *negated* and stored separately
			return -constId;
		}
		if (constId == -1) {
			constId = ExtraLogicContent.logicVars.get(symbol);
			if (constId > 0) {
				return -constId;
			}
		}

		symbol = symbol.trim();

		// string case
		if (!symbol.isEmpty() && symbol.charAt(0) == '\"' && symbol.charAt(symbol.length() - 1) == '\"') {
			return putConst("___" + symbol, symbol.substring(1, symbol.length() - 1).replace("\\n", "\n")).id;
		}

		// remove spaces for non-strings
		symbol = symbol.replace(' ', '_');

		double value = parseDouble(symbol);

		if (value == invalidNum) {
			return putVar(symbol).id;
		}
		// this creates a hidden const variable with the specified value
		return putConst("___" + value, value).id;
	}

	double parseDouble(String symbol) {
		// parse hex/binary syntax
		if (symbol.startsWith("0b"))
			return Strings.parseLong(symbol, 2, 2, symbol.length(), invalidNum);
		if (symbol.startsWith("0x"))
			return Strings.parseLong(symbol, 16, 2, symbol.length(), invalidNum);
		if (symbol.startsWith("%") && (symbol.length() == 7 || symbol.length() == 9))
			return parseColor(symbol);

		return Strings.parseDouble(symbol, invalidNum);
	}

	double parseColor(String symbol) {
		int r = Strings.parseInt(symbol, 16, 0, 1, 3), g = Strings.parseInt(symbol, 16, 0, 3, 5),
				b = Strings.parseInt(symbol, 16, 0, 5, 7),
				a = symbol.length() == 9 ? Strings.parseInt(symbol, 16, 0, 7, 9) : 255;

		return Color.toDoubleBits(r, g, b, a);
	}

	/** Adds a constant value by name. */
	public ExtraBVar putConst(String name, Object value) {
		ExtraBVar var = putVar(name);
		var.constant = true;
		var.value = value;
		return var;
	}

	/** Registers a variable name mapping. */
	public ExtraBVar putVar(String name) {
		if (vars.containsKey(name)) {
			return vars.get(name);
		}
		ExtraBVar var = new ExtraBVar(lastVar++);
		vars.put(name, var);
		return var;
	}

	@Nullable
	public ExtraBVar getVar(String name) {
		return vars.get(name);
	}

	/** A variable "builder". */
	public static class ExtraBVar {

		public int id;

		public boolean constant;

		public Object value;

		public ExtraBVar(int id) {
			this.id = id;
		}

		ExtraBVar() {
		}

		public BVar vanillaClone() {
			BVar bv = new BVar(id);
			bv.constant = constant;
			bv.value = value;
			return bv;
		}

		@Override
		public String toString() {
			return "BVar{" + "id=" + id + ", constant=" + constant + ", value=" + value + '}';
		}

	}

}
