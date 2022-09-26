package extralogic.logic;

import static mindustry.Vars.*;
import static mindustry.logic.LCanvas.*;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.scene.actions.Actions;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.core.GameState.State;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor.PrintI;
import mindustry.logic.LStatements.InvalidStatement;
import mindustry.logic.LogicDialog;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

/**
 * From {@link LogicDialog}
 * 
 * @author DasBabyPixel
 */
public class ExtraLogicDialog extends BaseDialog {

	public ExtraLCanvas canvas;

	public Cons<String> consumer = s -> {
	};

	public boolean privileged;

	@Nullable
	public ExtraLExecutor executor;

	private boolean compact = false;

	public ExtraLogicDialog() {
		super("logic");

		clearChildren();

		canvas = new ExtraLCanvas();
		shouldPause = true;

		addCloseListener();

		shown(this::setup);
		hidden(() -> consumer.get(canvas.save()));
		onResize(() -> {
			setup();

			canvas.rebuild();
		});

		canvas.visible(() -> !compactView());
		add(canvas).grow().name("canvas");

		row();

		add(buttons).growX().name("canvas");
	}

	private BaseDialog lastCompactView = null;

	private boolean compactView() {
		boolean ncompact = canvas.shouldCompact();
		if (ncompact != compact && ncompact) {
			BaseDialog dialog = new BaseDialog("Code too large");
			dialog.cont.add("The Logic code you added exceeds the 1000 instructions limit!").row();
			dialog.cont.add("Switching to compact mode for better performance.").row();
			dialog.cont.add(
					"Compact mode disables adding rows and hides the view. Until better performance is implemented this is the limit")
					.row();
			dialog.cont.button("Understood", dialog::hide).size(300, 50);
			dialog.show();
			dialog.hidden(() -> {
				if (lastCompactView == dialog)
					lastCompactView = null;
			});
			if (lastCompactView != null) {
				lastCompactView.hide();
			}
			lastCompactView = dialog;
		}
		compact = ncompact;
		return ncompact;
	}

	private void setup() {
		buttons.clearChildren();
		buttons.defaults().size(160f, 64f);
		buttons.button("@back", Icon.left, this::hide).name("back");
		buttons.button("@edit", Icon.edit, () -> {
			BaseDialog dialog = new BaseDialog("@editor.export");
			dialog.cont.pane(p -> {
				p.margin(10f);
				p.table(Tex.button, t -> {
					TextButtonStyle style = Styles.flatt;
					t.defaults().size(280f, 60f).left();

					t.button("@schematic.copy", Icon.copy, style, () -> {
						dialog.hide();
						Core.app.setClipboardText(canvas.save());
					}).marginLeft(12f);
					t.row();
					t.button("@schematic.copy.import", Icon.download, style, () -> {
						dialog.hide();
						try {
							canvas.load(Core.app.getClipboardText().replace("\r\n", "\n"));
						} catch (Throwable e) {
							ui.showException(e);
						}
					}).marginLeft(12f).disabled(b -> Core.app.getClipboardText() == null);
				});
			});

			dialog.addCloseButton();
			dialog.show();
		}).name("edit");

		if (Core.graphics.isPortrait())
			buttons.row();

		buttons.button("@variables", Icon.menu, () -> {
			BaseDialog dialog = new BaseDialog("@variables");
			dialog.hidden(() -> {
				if (!wasPaused) {
					state.set(State.paused);
				}
			});

			dialog.shown(() -> {
				if (!wasPaused) {
					state.set(State.playing);
				}
			});

			dialog.cont.pane(p -> {
				p.margin(10f).marginRight(16f);
				p.table(Tex.button, t -> {
					t.defaults().fillX().height(45f);
					for (var es : executor.vars) {
						var s = es.handle;
						if (s.constant)
							continue;

						Color varColor = Pal.gray;
						float stub = 8f, mul = 0.5f, pad = 4;

						Color color = !s.isobj ? Pal.place
								: s.objval == null ? Color.darkGray
										: s.objval instanceof String ? Pal.ammo
												: s.objval instanceof Content ? Pal.logicOperations
														: s.objval instanceof Building ? Pal.logicBlocks
																: s.objval instanceof Unit ? Pal.logicUnits
																		: s.objval instanceof Enum<?> ? Pal.logicIo
																				: Color.white;

						String typeName = !s.isobj ? "number"
								: s.objval == null ? "null"
										: s.objval instanceof String ? "string"
												: s.objval instanceof Content ? "content"
														: s.objval instanceof Building ? "building"
																: s.objval instanceof Unit ? "unit"
																		: s.objval instanceof Enum<?> ? "enum"
																				: "unknown";

						t.add(new Image(Tex.whiteui, varColor.cpy().mul(mul))).width(stub);
						t.stack(new Image(Tex.whiteui, varColor), new Label(" " + s.name + " ", Styles.outlineLabel) {

							{
								setColor(Pal.accent);
							}

						}).padRight(pad);

						t.add(new Image(Tex.whiteui, Pal.gray.cpy().mul(mul))).width(stub);
						t.table(Tex.pane, out -> {
							float period = 15f;
							float[] counter = {
									-1f
							};
							Label label = out.add("")
									.style(Styles.outlineLabel)
									.padLeft(4)
									.padRight(4)
									.width(140f)
									.wrap()
									.get();
							label.update(() -> {
								if (counter[0] < 0 || (counter[0] += Time.delta) >= period) {
									String text = s.isobj ? PrintI.toString(s.objval)
											: Math.abs(s.numval - (long) s.numval) < 0.00001 ? (long) s.numval + ""
													: s.numval + "";
									if (!label.textEquals(text)) {
										label.setText(text);
										if (counter[0] >= 0f) {
											label.actions(Actions.color(Pal.accent), Actions.color(Color.white, 0.2f));
										}
									}
									counter[0] = 0f;
								}
							});
							label.act(1f);
						}).padRight(pad);

						// TODO type name does not update, is this important?
						t.add(new Image(Tex.whiteui, color.cpy().mul(mul))).width(stub);
						t.stack(new Image(Tex.whiteui, color), new Label(" " + typeName + " ", Styles.outlineLabel));

						t.row();

						t.add().growX().colspan(6).height(4).row();
					}
				});
			});

			dialog.addCloseButton();
			dialog.show();
		}).name("variables").disabled(b -> executor == null || executor.vars.length == 0);

		buttons.button("@add", Icon.add, () -> {
			BaseDialog dialog = new BaseDialog("@add");
			dialog.cont.table(table -> {
				table.background(Tex.button);
				table.pane(t -> {
					for (Prov<ExtraLStatement> prov : ExtraLogicIO.allStatements()) {
						ExtraLStatement example = prov.get();
						if (example instanceof WrapperExtraLStatement wrapper
								&& wrapper.handle instanceof InvalidStatement) {
							continue;
						}
						if (example.hidden()) {
							continue;
						}
						if (example.privileged() && !privileged) {
							continue;
						}
						if (example.nonPrivileged() && privileged) {
							continue;
						}

						ExtraLCategory ecategory = example.category();
						LCategory category = ecategory.handle;
						Table cat = t.find(category.name);
						if (cat == null) {
							t.table(s -> {
								if (category.icon != null) {
									s.image(category.icon, Pal.darkishGray).left().size(15f).padRight(10f);
								}
								s.add(category.localized())
										.color(Pal.darkishGray)
										.left()
										.tooltip(category.description());
								s.image(Tex.whiteui, Pal.darkishGray).left().height(5f).growX().padLeft(10f);
							}).growX().pad(5f).padTop(10f);

							t.row();

							cat = t.table(c -> {
								c.top().left();
							}).name(category.name).top().left().growX().fillY().get();
							t.row();
						}

						TextButtonStyle style = new TextButtonStyle(Styles.flatt);
						style.fontColor = category.color;
						style.font = Fonts.outline;

						cat.button(example.name(), style, () -> {
							canvas.add(prov.get());
							dialog.hide();
						}).size(130f, 50f).self(c -> tooltip(c, "lst." + example.name())).top().left();

						if (cat.getChildren().size % 3 == 0)
							cat.row();
					}
				}).grow();
			}).fill().maxHeight(Core.graphics.getHeight() * 0.8f);
			dialog.addCloseButton();
			dialog.show();
		}).disabled(b -> compactView());
		buttons.button("@clear", () -> {
			BaseDialog dialog = new BaseDialog("Confirmation");
			dialog.cont.add("You are about to clear all contents of this processor. Are you sure?");
			dialog.cont.button("Yes", () -> {
				try {
					canvas.load("");
				} catch (Throwable ex) {
					ui.showException(ex);
				}
				dialog.hide();
			}).size(200, 50);
			dialog.cont.button("No", () -> {
				dialog.hide();
			}).size(200, 50);
			dialog.show();
		}).name("clear").disabled(t -> canvas.statements.seq.size == 0);
	}

	public void show(String code, ExtraLExecutor executor, boolean privileged, Cons<String> modified) {
		this.executor = executor;
		this.privileged = privileged;
		canvas.statements.clearChildren();
		canvas.rebuild();
		canvas.privileged = privileged;
		try {
			canvas.load(code);
		} catch (Throwable t) {
			Log.err(t);
			canvas.load("");
		}
		this.consumer = result -> {
			if (!result.equals(code)) {
				modified.get(result);
			}
		};

		show();
	}

}
