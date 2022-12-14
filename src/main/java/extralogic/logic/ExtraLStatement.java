package extralogic.logic;

import static mindustry.logic.LCanvas.*;

import arc.Core;
import arc.func.Cons;
import arc.func.Cons2;
import arc.math.Interp;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Tmp;
import extralogic.logic.ExtraLCanvas.ExtraStatementElem;
import extralogic.logic.ExtraLExecutor.ExtraLInstruction;
import mindustry.gen.LogicIO;
import mindustry.gen.Tex;
import mindustry.logic.LAssembler;
import mindustry.logic.LCanvas;
import mindustry.logic.LCategory;
import mindustry.logic.LStatement;
import mindustry.ui.Styles;

/**
 * From {@link LStatement}
 * 
 * @author DasBabyPixel
 */
public abstract class ExtraLStatement {

	public transient @Nullable ExtraStatementElem elem;

	public transient String name;

	public abstract void build(Table table);

	public abstract ExtraLInstruction build(ExtraLAssembler builder);

	public ExtraLCategory category() {
		return ExtraLCategory.fromLCategory(LCategory.unknown);
	}

	public ExtraLStatement copy() {
		StringBuilder build = new StringBuilder();
		write(build);
		// assume privileged when copying, because there's no way privileged
		// instructions can appear here anyway, and the instructions get validated on
		// load anyway
		Seq<ExtraLStatement> read = ExtraLAssembler.read(build.toString(), true);
		if (read.size == 0)
			return null;
		ExtraLStatement f = read.first();
		f.name = name;
		return f;
	}

	public boolean hidden() {
		return false;
	}

	/** Privileged instructions are only allowed in world processors. */
	public boolean privileged() {
		return false;
	}

	/**
	 * If true, this statement is considered useless with privileged processors and
	 * is not allowed in them.
	 */
	public boolean nonPrivileged() {
		return false;
	}

	// protected methods are only for internal UI layout utilities

	protected void param(Cell<Label> label) {
		String text = name() + "." + label.get().getText().toString().trim();
		tooltip(label, text);
	}

	protected String sanitize(String value) {
		if (value.length() == 0) {
			return "";
		} else if (value.length() == 1) {
			if (value.charAt(0) == '"' || value.charAt(0) == ';' || value.charAt(0) == ' ') {
				return "invalid";
			}
		} else {
			StringBuilder res = new StringBuilder(value.length());
			if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
				res.append('\"');
				// strip out extra quotes
				for (int i = 1; i < value.length() - 1; i++) {
					if (value.charAt(i) == '"') {
						res.append('\'');
					} else {
						res.append(value.charAt(i));
					}
				}
				res.append('\"');
			} else {
				// otherwise, strip out semicolons, spaces and quotes
				for (int i = 0; i < value.length(); i++) {
					char c = value.charAt(i);
					res.append(switch (c) {
					case ';' -> 's';
					case '"' -> '\'';
					case ' ' -> '_';
					default -> c;
					});
				}
			}

			return res.toString();
		}

		return value;
	}

	protected Cell<TextField> field(Table table, String value, Cons<String> setter) {
		return table.field(value, Styles.nodeField, s -> setter.get(sanitize(s)))
				.size(144f, 40f)
				.pad(2f)
				.color(table.color)
				.maxTextLength(LAssembler.maxTokenLength);
	}

	protected Cell<TextField> fields(Table table, String desc, String value, Cons<String> setter) {
		table.add(desc).padLeft(10).left().self(this::param);
		return field(table, value, setter).width(85f).padRight(10).left();
	}

	protected Cell<TextField> fields(Table table, String value, Cons<String> setter) {
		return field(table, value, setter).width(85f);
	}

	protected void row(Table table) {
		if (LCanvas.useRows()) {
			table.row();
		}
	}

	protected <T> void showSelect(Button b, T[] values, T current, Cons<T> getter, int cols, Cons<Cell<?>> sizer) {
		showSelectTable(b, (t, hide) -> {
			ButtonGroup<Button> group = new ButtonGroup<>();
			int i = 0;
			t.defaults().size(60f, 38f);

			for (T p : values) {
				sizer.get(t.button(p.toString(), Styles.logicTogglet, () -> {
					getter.get(p);
					hide.run();
				}).self(c -> {
					if (p instanceof Enum<?> e) {
						tooltip(c, e);
					}
				}).checked(current == p).group(group));

				if (++i % cols == 0)
					t.row();
			}
		});
	}

	protected <T> void showSelect(Button b, T[] values, T current, Cons<T> getter) {
		showSelect(b, values, current, getter, 4, c -> {
		});
	}

	protected void showSelectTable(Button b, Cons2<Table, Runnable> hideCons) {
		Table t = new Table(Tex.paneSolid) {

			@Override
			public float getPrefHeight() {
				return Math.min(super.getPrefHeight(), Core.graphics.getHeight());
			}

			@Override
			public float getPrefWidth() {
				return Math.min(super.getPrefWidth(), Core.graphics.getWidth());
			}

		};
		t.margin(4);

		// triggers events behind the element to simulate deselection
		Element hitter = new Element();

		Runnable hide = () -> {
			Core.app.post(hitter::remove);
			t.actions(Actions.fadeOut(0.3f, Interp.fade), Actions.remove());
		};

		hitter.fillParent = true;
		hitter.tapped(hide);

		Core.scene.add(hitter);
		Core.scene.add(t);

		t.update(() -> {
			if (b.parent == null || !b.isDescendantOf(Core.scene.root)) {
				Core.app.post(() -> {
					hitter.remove();
					t.remove();
				});
				return;
			}

			b.localToStageCoordinates(Tmp.v1.set(b.getWidth() / 2f, b.getHeight() / 2f));
			t.setPosition(Tmp.v1.x, Tmp.v1.y, Align.center);
			if (t.getWidth() > Core.scene.getWidth())
				t.setWidth(Core.graphics.getWidth());
			if (t.getHeight() > Core.scene.getHeight())
				t.setHeight(Core.graphics.getHeight());
			t.keepInStage();
			t.invalidateHierarchy();
			t.pack();
		});
		t.actions(Actions.alpha(0), Actions.fadeIn(0.3f, Interp.fade));

		t.top().pane(inner -> {
			inner.top();
			hideCons.get(inner, hide);
		}).pad(0f).top().scrollX(false);

		t.pack();
	}

	public void afterRead() {
	}

	public void write(StringBuilder builder) {
		LogicIO.write(this, builder);
	}

	public void setupUI() {

	}

	public void saveUI() {

	}

	public String name() {
		if (name != null)
			return name;
		return name = Strings.insertSpaces(getClass().getSimpleName().replace("Statement", ""));
	}

}
