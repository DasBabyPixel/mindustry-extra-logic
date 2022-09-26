package extralogic.logic.statements;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import extralogic.logic.ExtraLAssembler;
import extralogic.logic.ExtraLCanvas;
import extralogic.logic.ExtraLCanvas.ExtraStatementElem;
import extralogic.logic.ExtraLCategory;
import extralogic.logic.ExtraLExecutor.ExtraLInstruction;
import extralogic.logic.ExtraLStatement;
import extralogic.logic.WrapperExtraLInstruction;
import mindustry.logic.ConditionOp;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor.JumpI;
import mindustry.logic.LStatements;
import mindustry.logic.LStatements.JumpStatement;
import mindustry.ui.Styles;

/**
 * From {@link LStatements}
 * 
 * @author DasBabyPixel
 */
public class ExtraLStatements {

	public static void load() {
		ExtraLAssembler.customParsers.put("jump", tokens -> {
			ExtraJumpStatement res = new ExtraJumpStatement();
			if (tokens.length > 1) {
				res.destIndex = Integer.valueOf(tokens[1]);
			}
			if (tokens.length > 2) {
				res.op = ConditionOp.valueOf(tokens[2]);
			}
			if (tokens.length > 3) {
				res.value = tokens[3];
			}
			if (tokens.length > 4) {
				res.compare = tokens[4];
			}
			res.afterRead();
			return res;
		});
	}

	/**
	 * From {@link JumpStatement}
	 * 
	 * @author DasBabyPixel
	 */
	public static class ExtraJumpStatement extends ExtraLStatement {

		private static Color last = new Color();

		public transient ExtraStatementElem dest;

		public int destIndex;

		public ConditionOp op = ConditionOp.notEqual;

		public String value = "x", compare = "false";

		@Override
		public void build(Table table) {
			table.add("if ").padLeft(4);

			last = table.color;
			table.table(this::rebuild);

			table.add().growX();
			table.add(new ExtraLCanvas.JumpButton(() -> dest, s -> dest = s)).size(30).right().padLeft(-8);

			String name = name();

			// hack way of finding the title label...
			Core.app.post(() -> {
				// must be delayed because parent is added later
				if (table.parent != null) {
					Label title = table.parent.find("statement-name");
					if (title != null) {
						title.update(() -> title.setText((dest != null ? name + " -> " + dest.index : name)));
					}
				}
			});

		}

		void rebuild(Table table) {
			table.clearChildren();
			table.setColor(last);

			if (op != ConditionOp.always)
				field(table, value, str -> value = str);

			table.button(b -> {
				b.label(() -> op.symbol);
				b.clicked(() -> showSelect(b, ConditionOp.all, op, o -> {
					op = o;
					rebuild(table);
				}));
			}, Styles.logict, () -> {
			}).size(op == ConditionOp.always ? 80f : 48f, 40f).pad(4f).color(table.color);

			if (op != ConditionOp.always)
				field(table, compare, str -> compare = str);
		}

		// elements need separate conversion logic
		@Override
		public void setupUI() {
			if (elem != null && destIndex >= 0 && destIndex < elem.parent.getChildren().size) {
				dest = (ExtraStatementElem) elem.parent.getChildren().get(destIndex);
			}
		}

		@Override
		public void saveUI() {
			if (elem != null) {
				destIndex = dest == null ? -1 : dest.parent.getChildren().indexOf(dest);
			}
		}

		@Override
		public ExtraLInstruction build(ExtraLAssembler builder) {
			return new WrapperExtraLInstruction(new JumpI(op, builder.var(value), builder.var(compare), destIndex));
		}

		@Override
		public ExtraLCategory category() {
			return ExtraLCategory.fromLCategory(LCategory.control);
		}

	}

}
