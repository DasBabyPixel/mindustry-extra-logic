package extralogic.logic;

import mindustry.gen.Building;
import mindustry.logic.LAssembler;

/**
 * Alternative {@link LAssembler}
 * 
 * @author DasBabyPixel
 */
public class ExtraLAssembler {

	public static ExtraLAssembler assemble(String str, boolean privileged) {
		return null;
	}

	public void putConst(String name, Building build) {
	}

	public void putConst(String name, int width) {
	}

	public BVar getVar(String name) {
		return null;
	}

	/** A variable "builder". */
	public static class BVar {

		public int id;

		public boolean constant;

		public Object value;

		public BVar(int id) {
			this.id = id;
		}

		BVar() {
		}

		@Override
		public String toString() {
			return "BVar{" + "id=" + id + ", constant=" + constant + ", value=" + value + '}';
		}

	}

	public void putConst(String name, float conv) {
	}

}
