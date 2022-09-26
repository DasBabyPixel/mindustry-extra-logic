package extralogic.logic;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;

import arc.Core;
import arc.files.Fi;
import arc.math.Rand;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import extralogic.logic.ExtraLExecutor.ExtraVar;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.logic.GlobalVars;
import mindustry.logic.LAccess;
import mindustry.logic.LExecutor.Var;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Block;

/**
 * From {@link GlobalVars}
 * 
 * @author DasBabyPixel
 */
public class ExtraGlobalVars {

	public ExtraVar get(int id) {
		return this.vars.items[id];
	}

	public int get(String name) {
		return this.namesToIds.get(name, -1);
	}

	public void init() {

		Iterator<?> var14 = Vars.content.items().iterator();

		while (var14.hasNext()) {
			Item item = (Item) var14.next();
			this.put("@" + item.name, item);
		}

		var14 = Vars.content.liquids().iterator();

		while (var14.hasNext()) {
			Liquid liquid = (Liquid) var14.next();
			this.put("@" + liquid.name, liquid);
		}

		var14 = Vars.content.blocks().iterator();

		while (var14.hasNext()) {
			Block block = (Block) var14.next();
			if (Vars.content.item(block.name) == null) {
				this.put("@" + block.name, block);
			}
		}

		this.put("@solid", Blocks.stoneWall);
		var14 = Vars.content.units().iterator();

		while (var14.hasNext()) {
			UnitType type = (UnitType) var14.next();
			this.put("@" + type.name, type);
		}

		LAccess[] var17 = LAccess.all;
		int var2 = var17.length;

		for (int var3 = 0; var3 < var2; ++var3) {
			LAccess sensor = var17[var3];
			this.put("@" + sensor.name(), sensor);
		}

		this.logicIdToContent = new UnlockableContent[ContentType.all.length][];
		this.contentIdToLogicId = new int[ContentType.all.length][];
		Fi ids = Core.files.internal("logicids.dat");
		if (ids.exists()) {
			try {
				DataInputStream in = new DataInputStream(ids.readByteStream());

				try {
					ContentType[] var23 = lookableContent;
					int var24 = var23.length;

					for (int var5 = 0; var5 < var24; ++var5) {
						ContentType ctype = var23[var5];
						short amount = in.readShort();
						this.logicIdToContent[ctype.ordinal()] = new UnlockableContent[amount];
						this.contentIdToLogicId[ctype.ordinal()] = new int[Vars.content.getBy(ctype).size];
						this.put("@" + ctype.name() + "Count", amount);

						for (int i = 0; i < amount; ++i) {
							String name = in.readUTF();
							UnlockableContent fetched = (UnlockableContent) Vars.content.getByName(ctype, name);
							if (fetched != null) {
								this.logicIdToContent[ctype.ordinal()][i] = fetched;
								this.contentIdToLogicId[ctype.ordinal()][fetched.id] = i;
							}
						}
					}
				} catch (Throwable var12) {
					try {
						in.close();
					} catch (Throwable var11) {
						var12.addSuppressed(var11);
					}

					throw var12;
				}

				in.close();
			} catch (IOException var13) {
				Log.err("Error reading logic ID mapping", var13);
			}
		}

	}

	@Nullable
	public Content lookupContent(ContentType type, int id) {
		UnlockableContent[] arr = this.logicIdToContent[type.ordinal()];
		return arr != null && id >= 0 && id < arr.length ? arr[id] : null;
	}

	public int lookupLogicId(UnlockableContent content) {
		int[] arr = this.contentIdToLogicId[content.getContentType().ordinal()];
		return arr != null && content.id >= 0 && content.id < arr.length ? arr[content.id] : -1;
	}

	public int put(String name, Object value) {
		int existingIdx = this.namesToIds.get(name, -1);
		if (existingIdx != -1) {
			Log.debug("Failed to add global logic variable '@', as it already exists.", new Object[] {
					name
			});
			return existingIdx;
		}
		Var var = new Var(name);
		var.constant = true;
		if (value instanceof Number) {
			Number num = (Number) value;
			var.numval = num.doubleValue();
		} else {
			var.isobj = true;
			var.objval = value;
		}

		int index = this.vars.size;
		this.namesToIds.put(name, index);
		this.vars.add(new ExtraVar(var));
		return index;
	}

	public void set(int id, double value) {
		this.get(id).handle.numval = value;
	}

	public void update() {
		this.vars.items[varTime].handle.numval = Vars.state.tick / 60.0D * 1000.0D;
		this.vars.items[varTick].handle.numval = Vars.state.tick;
		this.vars.items[varSecond].handle.numval = Vars.state.tick / 60.0D;
		this.vars.items[varMinute].handle.numval = Vars.state.tick / 60.0D / 60.0D;
		this.vars.items[varWave].handle.numval = Vars.state.wave;
		this.vars.items[varWaveTime].handle.numval = Vars.state.wavetime / 60.0F;
	}

	private ObjectIntMap<String> namesToIds = new ObjectIntMap<>();

	private Seq<ExtraVar> vars = new Seq<>(ExtraVar.class);

	private UnlockableContent[][] logicIdToContent;

	private int[][] contentIdToLogicId;

	public static final int ctrlProcessor = 1;

	public static final int ctrlPlayer = 2;

	public static final int ctrlCommand = 3;

	public static final ContentType[] lookableContent;

	public static final Rand rand;

	private static int varTime;

	private static int varTick;

	private static int varSecond;

	private static int varMinute;

	private static int varWave;

	private static int varWaveTime;

	static {
		lookableContent = new ContentType[] {
				ContentType.block, ContentType.unit, ContentType.item, ContentType.liquid
		};
		rand = new Rand();
	}

}
