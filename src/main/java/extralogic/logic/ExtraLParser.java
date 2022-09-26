package extralogic.logic;

import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Strings;
import mindustry.logic.LParser;
import mindustry.logic.LStatements.InvalidStatement;
import mindustry.logic.LStatements.JumpStatement;

/**
 * From {@link LParser}
 * 
 * @author DasBabyPixel
 */
public class ExtraLParser {

	private static final String[] tokens = new String[16];

	private static final int maxJumps = 500;

	private static final StringMap opNameChanges = StringMap.of("atan2", "angle", "dst", "len");

	private static final Seq<JumpIndex> jumps = new Seq<>();

	private static final ObjectIntMap<String> jumpLocations = new ObjectIntMap<>();

	Seq<ExtraLStatement> statements = new Seq<>();

	public char[] chars;

	public int pos, line, tok;

	public boolean privileged;

	public ExtraLParser(String text, boolean privileged) {
		this.privileged = privileged;
		this.chars = text.toCharArray();
	}

	void comment() {
		// read until \n or eof
		while (pos < chars.length && chars[pos++] != '\n')
			;
	}

	void error(String message) {
		throw new RuntimeException("Invalid code. " + message);
	}

	String string() {
		int from = pos;

		while (++pos < chars.length) {
			var c = chars[pos];
			if (c == '\n') {
				error("Missing closing quote \" before end of line.");
			} else if (c == '"') {
				break;
			}
		}

		if (pos >= chars.length || chars[pos] != '"')
			error("Missing closing quote \" before end of file.");

		return new String(chars, from, ++pos - from);
	}

	String token() {
		int from = pos;

		while (pos < chars.length) {
			char c = chars[pos];
			if (c == '\n' || c == ' ' || c == '#' || c == '\t' || c == ';')
				break;
			pos++;
		}

		return new String(chars, from, pos - from);
	}

	/** Apply changes after reading a list of tokens. */
	void checkRead() {
		if (tokens[0].equals("op")) {
			// legacy name change
			tokens[1] = opNameChanges.get(tokens[1], tokens[1]);
		}
	}

	/** Reads the next statement until EOL/EOF. */
	void statement() {
		boolean expectNext = false;
		tok = 0;

		while (pos < chars.length) {
			char c = chars[pos];
			if (tok >= tokens.length)
				error("Line too long; may only contain " + tokens.length + " tokens");

			// reached end of line, bail out.
			if (c == '\n' || c == ';')
				break;

			if (expectNext && c != ' ' && c != '#' && c != '\t') {
				error("Expected space after string/token.");
			}

			expectNext = false;

			if (c == '#') {
				comment();
				break;
			} else if (c == '"') {
				tokens[tok++] = string();
				expectNext = true;
			} else if (c != ' ' && c != '\t') {
				tokens[tok++] = token();
				expectNext = true;
			} else {
				pos++;
			}
		}

		// only process lines with at least 1 token
		if (tok > 0) {
			checkRead();

			// store jump location, always ends with colon
			if (tok == 1 && tokens[0].charAt(tokens[0].length() - 1) == ':') {
				if (jumpLocations.size >= maxJumps) {
					error("Too many jump locations. Max jumps: " + maxJumps);
				}
				jumpLocations.put(tokens[0].substring(0, tokens[0].length() - 1), line);
			} else {
				boolean wasJump;
				String jumpLoc = null;
				// clean up jump position before parsing
				if (wasJump = (tokens[0].equals("jump") && tok > 1 && !Strings.canParseInt(tokens[1]))) {
					jumpLoc = tokens[1];
					tokens[1] = "-1";
				}

				for (int i = 1; i < tok; i++) {
					if (tokens[i].equals("@configure"))
						tokens[i] = "@config";
					if (tokens[i].equals("configure"))
						tokens[i] = "config";
				}

				ExtraLStatement st;

				try {
					st = ExtraLogicIO.read(tokens, tok);
				} catch (Exception e) {
					// replace invalid statements
					st = new WrapperExtraLStatement(new InvalidStatement());
				}

				// discard misplaced privileged instructions
				if (!privileged && st != null && st.privileged()) {
					st = new WrapperExtraLStatement(new InvalidStatement());
				}

				// store jumps that use labels
//				if (st instanceof JumpStatement jump && wasJump) {
				if (st instanceof WrapperExtraLStatement
						&& ((WrapperExtraLStatement) st).handle instanceof JumpStatement jump && wasJump) {
					jumps.add(new JumpIndex(jump, jumpLoc));
				}

				if (st != null) {
					statements.add(st);
				} else {
					// attempt parsing using custom parser if a match is found; this is for mods
					if (ExtraLAssembler.customParsers.containsKey(tokens[0])) {
						statements.add(ExtraLAssembler.customParsers.get(tokens[0]).get(tokens));
					} else {
						// unparseable statement
						statements.add(new WrapperExtraLStatement(new InvalidStatement()));
					}
				}
				line++;
			}
		}

	}

	Seq<ExtraLStatement> parse() {
		jumps.clear();
		jumpLocations.clear();

		while (pos < chars.length /* && line < LExecutor.maxInstructions */) {
			switch (chars[pos]) {
			case '\n', ';', ' ' -> pos++; // skip newlines and spaces
			case '\r' -> pos += 2; // skip the newline after the \r
			default -> statement();
			}
		}

		// load destination indices
		for (var i : jumps) {
			if (!jumpLocations.containsKey(i.location)) {
				error("Undefined jump location: \"" + i.location
						+ "\". Make sure the jump label exists and is typed correctly.");
			}
			i.jump.destIndex = jumpLocations.get(i.location, -1);
		}

		return statements;
	}

	static class JumpIndex {

		JumpStatement jump;

		String location;

		public JumpIndex(JumpStatement jump, String location) {
			this.jump = jump;
			this.location = location;
		}

	}

}