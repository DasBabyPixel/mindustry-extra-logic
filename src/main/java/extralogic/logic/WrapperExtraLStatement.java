package extralogic.logic;

import arc.scene.ui.layout.Table;
import extralogic.logic.ExtraLExecutor.ExtraLInstruction;
import mindustry.logic.LStatement;

public class WrapperExtraLStatement extends ExtraLStatement {

	public final LStatement handle;

	public WrapperExtraLStatement(LStatement handle) {
		this.handle = handle;
	}

	@Override
	public void build(Table table) {
		handle.build(table);
	}

	@Override
	public ExtraLInstruction build(ExtraLAssembler builder) {
		return new WrapperExtraLInstruction(handle.build(builder.vanillaClone()));
	}

	@Override
	public String name() {
		return handle.name();
	}

	@Override
	public void afterRead() {
		handle.afterRead();
	}

	@Override
	public ExtraLCategory category() {
		return ExtraLCategory.fromLCategory(handle.category());
	}

	@Override
	public boolean hidden() {
		return handle.hidden();
	}

	@Override
	public boolean privileged() {
		return handle.privileged();
	}

	@Override
	public boolean nonPrivileged() {
		return handle.nonPrivileged();
	}

	@Override
	public void write(StringBuilder builder) {
		handle.write(builder);
	}

	@Override
	public void setupUI() {
		handle.setupUI();
	}

	@Override
	public void saveUI() {
		handle.saveUI();
	}

	@Override
	public String toString() {
		return handle.toString();
	}

}
