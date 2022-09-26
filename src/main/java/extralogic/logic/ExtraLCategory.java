package extralogic.logic;

import arc.struct.Seq;
import mindustry.logic.LCategory;

/**
 * From {@link LCategory}
 * 
 * @author DasBabyPixel
 */
public class ExtraLCategory implements Comparable<ExtraLCategory> {

	public static final Seq<ExtraLCategory> all = new Seq<>();

	public final LCategory handle;
	
	private ExtraLCategory(LCategory handle) {
		this.handle = handle;
		all.add(this);
	}

	@Override
	public int compareTo(ExtraLCategory o) {
		return handle.compareTo(o.handle);
	}

	public static ExtraLCategory fromLCategory(LCategory category) {
		for (ExtraLCategory cat : all) {
			if (cat.handle.equals(category)) {
				return cat;
			}
		}
		return new ExtraLCategory(category);
	}

}
