package extralogic.content;

import java.util.concurrent.atomic.AtomicReference;

import arc.Events;
import arc.func.Cons;
import extralogic.logic.ExtraLogicBlock;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.world.Block;

public class ExtraLogicBlocks extends ExtraLogicContent {

	public static Block micro_processor, logic_processor, hyper_processor;

	public static void load() {
		micro_processor = new ExtraLogicBlock("micro-processor") {

			{
				registerParent(this, Blocks.microProcessor);

			}

		};
		logic_processor = new ExtraLogicBlock("logic-processor") {

			{
				registerParent(this, Blocks.logicProcessor);
			}

		};
		hyper_processor = new ExtraLogicBlock("hyper-processor") {

			{
				registerParent(this, Blocks.hyperProcessor);
			}

		};
	}

	private static void registerParent(Block self, Block parent) {
		self.requirements(parent.category, parent.requirements, unlockAll || parent.unlocked());
		final AtomicReference<Cons<EventType.UnlockEvent>> ref = new AtomicReference<>(null);
		final Cons<EventType.UnlockEvent> eventHandler = e -> {
			if (e.content == parent) {
				self.unlock();
				Events.remove(EventType.UnlockEvent.class, ref.get());
			}
		};
		ref.set(eventHandler);
	}

}
