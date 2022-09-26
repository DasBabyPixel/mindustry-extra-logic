package extralogic.logic;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.Scene;
import arc.scene.event.ClickListener;
import arc.scene.event.ElementGestureListener;
import arc.scene.event.HandCursorListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.logic.LCanvas;
import mindustry.logic.LStatements.JumpStatement;
import mindustry.ui.Styles;

/**
 * From {@link LCanvas}
 * 
 * @author DasBabyPixel
 */
public class ExtraLCanvas extends Table {

	public static final int maxJumpsDrawn = 100;

	// ew static variables
	static ExtraLCanvas canvas;

	public ExtraDragLayout statements;

	public ScrollPane pane;

	public Group jumps;

	public ExtraStatementElem dragging;

	public ExtraStatementElem hovered;

	public float targetWidth;

	public int jumpCount = 0;

	public boolean privileged;

	public Seq<Tooltip> tooltips = new Seq<>();

	public ExtraLCanvas() {
		canvas = this;

		Core.scene.addListener(new InputListener() {

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
				// hide tooltips on tap
				for (var t : tooltips) {
					t.container.toFront();
				}
				Core.app.post(() -> {
					tooltips.each(Tooltip::hide);
					tooltips.clear();
				});
				return super.touchDown(event, x, y, pointer, button);
			}

		});

		rebuild();
	}

	/** @return if statement elements should have rows. */
	public static boolean useRows() {
		return Core.graphics.getWidth() < Scl.scl(900f) * 1.2f;
	}

	public static void tooltip(Cell<?> cell, String key) {
		String lkey = key.toLowerCase().replace(" ", "");
		if (Core.settings.getBool("logichints", true) && Core.bundle.has(lkey)) {
			var tip = new Tooltip(t -> t.background(Styles.black8)
					.margin(4f)
					.add("[lightgray]" + Core.bundle.get(lkey))
					.style(Styles.outlineLabel));

			// mobile devices need long-press tooltips
			if (Vars.mobile) {
				cell.get().addListener(new ElementGestureListener(20, 0.4f, 0.43f, 0.15f) {

					@Override
					public boolean longPress(Element element, float x, float y) {
						tip.show(element, x, y);
						canvas.tooltips.add(tip);
						// prevent touch down for other listeners
						for (var list : cell.get().getListeners()) {
							if (list instanceof ClickListener cl) {
								cl.cancel();
							}
						}
						return true;
					}

				});
			} else {
				cell.get().addListener(tip);
			}

		}
	}

	public static void tooltip(Cell<?> cell, Enum<?> key) {
		String cl = key.getClass().getSimpleName().toLowerCase() + "." + key.name().toLowerCase();
		if (Core.bundle.has(cl)) {
			tooltip(cell, cl);
		} else {
			tooltip(cell, "lenum." + key.name());
		}
	}

	public void rebuild() {
		targetWidth = useRows() ? 400f : 900f;
		float s = pane != null ? pane.getScrollPercentY() : 0f;
		String toLoad = statements != null ? save() : null;

		clear();

		statements = new ExtraDragLayout();
		jumps = new WidgetGroup();

		pane = pane(t -> {
			t.center();
			t.add(statements).pad(2f).center().width(targetWidth);
			t.addChild(jumps);

			jumps.cullable = false;
		}).grow().get();
		pane.setFlickScroll(false);

		// load old scroll percent
		Core.app.post(() -> {
			pane.setScrollPercentY(s);
			pane.updateVisualScroll();
		});

		if (toLoad != null) {
			load(toLoad);
		}
	}

	@Override
	public void draw() {
		jumpCount = 0;
		super.draw();
	}

	public void add(ExtraLStatement statement) {
		statements.addChild(new ExtraStatementElem(statement));
	}

	public String save() {
		Seq<ExtraLStatement> st = statements.getChildren().<ExtraStatementElem>as().map(s -> s.st);
		st.each(ExtraLStatement::saveUI);

		return ExtraLAssembler.write(st);
	}

	public void load(String asm) {
		jumps.clear();

		Seq<ExtraLStatement> statements = ExtraLAssembler.read(asm, privileged);
//        statements.truncate(ExtraLExecutor.maxInstructions);
		this.statements.clearChildren();
		for (ExtraLStatement st : statements) {
			add(st);
		}

		for (ExtraLStatement st : statements) {
			st.setupUI();
		}

		this.statements.layout();
	}

	ExtraStatementElem checkHovered() {
		Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
		if (e != null) {
			while (e != null && !(e instanceof ExtraStatementElem)) {
				e = e.parent;
			}
		}
		if (e == null || isDescendantOf(e))
			return null;
		return (ExtraStatementElem) e;
	}

	@Override
	public void act(float delta) {
		super.act(delta);

		hovered = checkHovered();

		if (Core.input.isTouched()) {
			float y = Core.input.mouseY();
			float dst = Math.min(y - this.y, Core.graphics.getHeight() - y);
			if (dst < Scl.scl(100f)) { // scroll margin
				int sign = Mathf.sign(Core.graphics.getHeight() / 2f - y);
				pane.setScrollY(pane.getScrollY() + sign * Scl.scl(15f) * Time.delta);
			}
		}
	}

	public class ExtraDragLayout extends WidgetGroup {

		float space = Scl.scl(10f), prefWidth, prefHeight;

		Seq<Element> seq = new Seq<>();

		int insertPosition = 0;

		boolean invalidated;

		{
			setTransform(true);
		}

		@Override
		public void layout() {
			invalidated = true;
			float cy = 0;
			seq.clear();

			float totalHeight = getChildren().sumf(e -> e.getHeight() + space);

			height = prefHeight = totalHeight;
			width = prefWidth = Scl.scl(targetWidth);

			// layout everything normally
			for (int i = 0; i < getChildren().size; i++) {
				Element e = getChildren().get(i);

				// ignore the dragged element
				if (dragging == e)
					continue;

				e.setSize(width, e.getPrefHeight());
				e.setPosition(0, height - cy, Align.topLeft);
				((ExtraStatementElem) e).updateAddress(i);

				cy += e.getPrefHeight() + space;
				seq.add(e);
			}

			// insert the dragged element if necessary
			if (dragging != null) {
				// find real position of dragged element top
				float realY = dragging.getY(Align.top) + dragging.translation.y;

				insertPosition = 0;

				for (int i = 0; i < seq.size; i++) {
					Element cur = seq.get(i);
					// find fit point
					if (realY < cur.y && (i == seq.size - 1 || realY > seq.get(i + 1).y)) {
						insertPosition = i + 1;
						break;
					}
				}

				float shiftAmount = dragging.getHeight() + space;

				// shift elements below insertion point down
				for (int i = insertPosition; i < seq.size; i++) {
					seq.get(i).y -= shiftAmount;
				}
			}

			invalidateHierarchy();

			if (parent != null && parent instanceof Table) {
				setCullingArea(parent.getCullingArea());
			}
		}

		@Override
		public float getPrefWidth() {
			return prefWidth;
		}

		@Override
		public float getPrefHeight() {
			return prefHeight;
		}

		@Override
		public void draw() {
			Draw.alpha(parentAlpha);

			// draw selection box indicating placement position
			if (dragging != null && insertPosition <= seq.size) {
				float shiftAmount = dragging.getHeight();
				float lastX = x;
				float lastY = insertPosition == 0 ? height + y : seq.get(insertPosition - 1).y + y - space;

				Tex.pane.draw(lastX, lastY - shiftAmount, width, dragging.getHeight());
			}

			if (invalidated) {
				children.each(c -> c.cullable = false);
			}

			super.draw();

			if (invalidated) {
				children.each(c -> c.cullable = true);
				invalidated = false;
			}
		}

		void finishLayout() {
			if (dragging != null) {
				// reset translation first
				for (Element child : getChildren()) {
					child.setTranslation(0, 0);
				}
				clearChildren();

				// reorder things
				for (int i = 0; i <= insertPosition - 1 && i < seq.size; i++) {
					addChild(seq.get(i));
				}

				addChild(dragging);

				for (int i = insertPosition; i < seq.size; i++) {
					addChild(seq.get(i));
				}

				dragging = null;
			}

			layout();
		}

	}

	public class ExtraStatementElem extends Table {

		public ExtraLStatement st;

		public int index;

		Label addressLabel;

		public ExtraStatementElem(ExtraLStatement st) {
			this.st = st;
			st.elem = this;

			background(Tex.whitePane);
			setColor(st.category().handle.color);
			margin(0f);
			touchable = Touchable.enabled;

			table(Tex.whiteui, t -> {
				t.color.set(color);
				t.addListener(new HandCursorListener());

				t.margin(6f);
				t.touchable = Touchable.enabled;

				t.add(st.name()).style(Styles.outlineLabel).name("statement-name").color(color).padRight(8);
				t.add().growX();

				addressLabel = t.add(index + "").style(Styles.outlineLabel).color(color).padRight(8).get();

				t.button(Icon.copy, Styles.logici, () -> {
				}).size(24f).padRight(6).get().tapped(this::copy);

				t.button(Icon.cancel, Styles.logici, () -> {
					remove();
					dragging = null;
					statements.layout();
				}).size(24f);

				t.addListener(new InputListener() {

					float lastx, lasty;

					@Override
					public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {

						if (button == KeyCode.mouseMiddle) {
							copy();
							return false;
						}

						Vec2 v = localToParentCoordinates(Tmp.v1.set(x, y));
						lastx = v.x;
						lasty = v.y;
						dragging = ExtraStatementElem.this;
						toFront();
						statements.layout();
						return true;
					}

					@Override
					public void touchDragged(InputEvent event, float x, float y, int pointer) {
						Vec2 v = localToParentCoordinates(Tmp.v1.set(x, y));

						translation.add(v.x - lastx, v.y - lasty);
						lastx = v.x;
						lasty = v.y;

						statements.layout();
					}

					@Override
					public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
						statements.finishLayout();
					}

				});
			}).growX().height(38);

			row();

			table(t -> {
				t.left();
				t.marginLeft(4);
				t.setColor(color);
				st.build(t);
			}).pad(4).padTop(2).left().grow();

			marginBottom(7);
		}

		public void updateAddress(int index) {
			this.index = index;
			addressLabel.setText(index + "");
		}

		public void copy() {
			st.saveUI();
			ExtraLStatement copy = st.copy();

			if (copy instanceof WrapperExtraLStatement wrap && wrap.handle instanceof JumpStatement st
					&& st.destIndex != -1) {
				int index = statements.getChildren().indexOf(this);
				if (index != -1 && index < st.destIndex) {
					st.destIndex++;
				}
			}

			if (copy != null) {
				ExtraStatementElem s = new ExtraStatementElem(copy);

				statements.addChildAfter(ExtraStatementElem.this, s);
				statements.layout();
				copy.elem = s;
				copy.setupUI();
			}
		}

		@Override
		public void draw() {
			float pad = 5f;
			Fill.dropShadow(x + width / 2f, y + height / 2f, width + pad, height + pad, 10f, 0.9f * parentAlpha);

			Draw.color(0, 0, 0, 0.3f * parentAlpha);
			Fill.crect(x, y, width, height);
			Draw.reset();

			super.draw();
		}

	}

	public static class JumpButton extends ImageButton {

		Color hoverColor = Pal.place;

		Color defaultColor = Color.white;

		Prov<ExtraStatementElem> to;

		boolean selecting;

		float mx, my;

		ClickListener listener;

		public JumpCurve curve;

		public JumpButton(Prov<ExtraStatementElem> getter, Cons<ExtraStatementElem> setter) {
			super(Tex.logicNode, new ImageButtonStyle() {

				{
					imageUpColor = Color.white;
				}

			});

			to = getter;
			addListener(listener = new ClickListener());

			addListener(new InputListener() {

				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode code) {
					selecting = true;
					setter.get(null);
					mx = x;
					my = y;
					return true;
				}

				@Override
				public void touchDragged(InputEvent event, float x, float y, int pointer) {
					mx = x;
					my = y;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode code) {
					localToStageCoordinates(Tmp.v1.set(x, y));
					ExtraStatementElem elem = canvas.hovered;

					if (elem != null && !isDescendantOf(elem)) {
						setter.get(elem);
					} else {
						setter.get(null);
					}
					selecting = false;
				}

			});

			update(() -> {
				if (to.get() != null && to.get().parent == null) {
					setter.get(null);
				}

				setColor(listener.isOver() ? hoverColor : defaultColor);
				getStyle().imageUpColor = this.color;
			});

			curve = new JumpCurve(this);
		}

		@Override
		protected void setScene(Scene stage) {
			super.setScene(stage);

			if (stage == null) {
				curve.remove();
			} else {
				canvas.jumps.addChild(curve);
			}
		}

	}

	public static class JumpCurve extends Element {

		public JumpButton button;

		public JumpCurve(JumpButton button) {
			this.button = button;
		}

		@Override
		public void act(float delta) {
			super.act(delta);

			if (button.listener.isOver()) {
				toFront();
			}
		}

		@Override
		public void draw() {
			canvas.jumpCount++;

			if (canvas.jumpCount > maxJumpsDrawn && !button.selecting && !button.listener.isOver()) {
				return;
			}

			Element hover = button.to.get() == null && button.selecting ? canvas.hovered : button.to.get();
			boolean draw = false;
			Vec2 t = Tmp.v1, r = Tmp.v2;

			Group desc = canvas.pane;

			button.localToAscendantCoordinates(desc, r.set(0, 0));

			if (hover != null) {
				hover.localToAscendantCoordinates(desc, t.set(hover.getWidth(), hover.getHeight() / 2f));

				draw = true;
			} else if (button.selecting) {
				t.set(r).add(button.mx, button.my);
				draw = true;
			}

			float offset = canvas.pane.getVisualScrollY() - canvas.pane.getMaxY();

			t.y += offset;
			r.y += offset;

			if (draw) {
				drawCurve(r.x + button.getWidth() / 2f, r.y + button.getHeight() / 2f, t.x, t.y);

				float s = button.getWidth();
				Draw.color(button.color);
				Tex.logicNode.draw(t.x + s * 0.75f, t.y - s / 2f, -s, s);
				Draw.reset();
			}
		}

		public void drawCurve(float x, float y, float x2, float y2) {
			Lines.stroke(4f, button.color);
			Draw.alpha(parentAlpha);

			float dist = 100f;

			Lines.curve(x, y, x + dist, y, x2 + dist, y2, x2, y2, Math.max(18, (int) (Mathf.dst(x, y, x2, y2) / 6)));
		}

	}

}
