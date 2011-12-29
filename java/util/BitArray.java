package util;

public class BitArray {

	private static byte dts = 16; // sizeof char
	private char[] array;
	private int startb; // used only to readbits
	private int freebs;
	private int usedbs;

	public BitArray() {
		this.array = new char[1];
		this.array[0] = 0;

		this.startb = 0;
		this.usedbs = 0;
		this.freebs = BitArray.dts; // only one is allocated, but not used yet
	}

	public BitArray(char[] array) {
		this.usedbs = array[0];
		this.freebs = array[1];
		this.startb = array[2];

		int len = (int) (this.usedbs + this.freebs) / BitArray.dts;
		this.array = new char[len];
		System.arraycopy(array, 3, this.array, 0, len);
	}

	// Métodos de instância
	public void expand(int nb) { // doubles the length
		int len = (this.usedbs + this.freebs) / BitArray.dts;
		int ene = len;
		while (this.freebs < nb) {
			ene *= 2;
			this.freebs += ((ene / 2) * BitArray.dts);
		}

		char[] novo;
		novo = new char[ene];
		System.arraycopy(this.array, 0, novo, 0, len);
		this.array = new char[ene];
		System.arraycopy(novo, 0, this.array, 0, len);
	}

	public void addbits(int val, int nb) {
		if (nb > this.freebs) {
			this.expand(nb);
		}

		if ((this.usedbs % BitArray.dts) > 0 && (this.freebs % dts) < nb) {
			char bitsleft = (char) (freebs % BitArray.dts);
			char mask = (char) (Math.pow((double) 2, (double) bitsleft) - 1);

			char now = (char) (val & mask);

			char jump = (char) (this.usedbs / BitArray.dts);
			char bjump = (char) (this.usedbs % BitArray.dts);

			now = (char) (now << bjump);
			this.array[jump] = (char) (this.array[jump] | now);

			char then = (char) (val >> bitsleft);
			this.array[jump + 1] = (char) (this.array[jump + 1] | then);
		} else {
			char jump = (char) (this.usedbs / BitArray.dts);
			char bjump = (char) (this.usedbs % BitArray.dts);

			char now = (char) (val << bjump);
			array[jump] = (char) (array[jump] | (char) now);
		}

		this.usedbs += nb;
		this.freebs -= nb;
	}

	public int readbits(int nb) {
		int jump = (char) (this.startb / dts);
		int bjump = (char) (this.startb % dts);

		if (bjump > 0 && (dts - bjump) < nb) {
			char bitsleft = (char) (dts - bjump);

			char mask = (char) (Math.pow((double) 2, (double) bitsleft) - 1);
			mask = (char) (mask << bjump);

			char now = (char) (this.array[jump] & mask);
			now = (char) (now >> bjump);

			mask = 0;
			mask = (char) (Math.pow((double) 2, (double) (nb - bitsleft)) - 1);

			char then = (char) (this.array[jump + 1] & mask);
			then = (char) (then << bitsleft);

			now = (char) (now | then);

			this.startb += nb;
			return now;
		} else {
			char mask = (char) (Math.pow((double) 2, (double) nb) - 1);
			mask = (char) (mask << bjump);

			char res = (char) (this.array[jump] & mask);
			res = (char) (res >> bjump);

			this.startb += nb;
			return res;
		}
	}

	public char[] unify() {
		int nfree = (int) this.freebs % BitArray.dts;
		int len = (this.usedbs + nfree) / BitArray.dts;

		char[] res;
		res = new char[3 + len];
		res[0] = (char) this.usedbs;
		res[1] = (char) nfree;
		res[2] = (char) this.startb;
		System.arraycopy(this.array, 0, res, 3, len);

		return res;
	}

	// General methods
	public char getIndex(int ind) {
		return this.array[ind];
	}

	public int getLen() {
		return (int) (this.freebs + this.usedbs) / BitArray.dts;
	}

	public int getSizeBits() {
		return this.usedbs;
	}

	public char[] getBits() {
		return this.array;
	}

	public void setBits(char[] bits) {
		this.array = new char[bits.length];
		System.arraycopy(bits, 0, this.array, 0, bits.length);

		this.usedbs = bits.length / 16;
	}

	public static char[] invertEdianess(char[] in) {
		char[] out = new char[in.length];

		for (int j = 0; j < in.length; j++) {
			out[j] = in[in.length - 1 - j];
		}

		return out;
	}
}

