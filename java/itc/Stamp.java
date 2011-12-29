package itc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import util.BitArray;
import util.ByteInt;

public class Stamp {

	private Event event;
	private Id id;

	public Stamp() {
		event = new Event();
		id = new Id();
	}

	protected Stamp(Id i, Event e) {
		this.id = i;
		this.event = e;
	}

	protected Stamp(Stamp s) {
		this.event = s.getEvent();
		this.id = s.getId();
	}

	public Stamp fork() {

		Stamp st = new Stamp();
		st.setEvent(this.event.clone());
//		st.setId(this.id.split());

		Id[] par = this.id.split();
		this.id = par[0];
		st.setId(par[1]);

		return st;
	}

	public static Stamp[] fork(Stamp st) {
		Stamp b = new Stamp();
		b.setEvent(st.getEvent().clone());
//
//		if(b.getEvent().equals(st.getEvent()) == false) {
//			System.out.println("Fail @ Fork: events not equal -> EV1:"+b.getEvent().toString()+"\nEV2:"+st.getEvent().toString());
//		}

		Id[] par = st.getId().split();
		st.setId(par[0]);
		b.setId(par[1]);

		Stamp[] res = new Stamp[2];
		res[0] = st;
		res[1] = b;

		return res;
	}

	public void join(Stamp s2) { // joins two stamps becoming itself the result stamp
		Id.sum(this.id, s2.getId());
		Event.join(this.event,s2.getEvent());
	}

	public static Stamp join(Stamp s1, Stamp s2) { // joins two stamps, returning the resulting stamp
		Id.sum(s1.getId(), s2.getId());
		Event.join(s1.getEvent(), s2.getEvent());
		return s1;
	}

	public Stamp peek() {
		Id i = new Id(0);
		Event ev = this.event.clone();
		return new Stamp(i, ev);
	}

	public static Stamp peek(Stamp st) {
		Id i = new Id(0);
		Event ev = st.getEvent().clone();
		return new Stamp(i, ev);
	}

	public void event() {
		Event old = event.clone();
		Stamp.fill(id, event);
		boolean not_filled = old.equals(event);

		if (not_filled) {
			Stamp.grow(id, event);
		}
	}

	public static Stamp event(Stamp st) { // returns the new updated Stamp
		Stamp res = st.clone();
		Stamp.fill(st.getId(), st.getEvent());
		boolean not_filled = st.getEvent().equals(res.getEvent());

		if (not_filled) {
			Stamp.grow(st.getId(), st.getEvent());
		}

		return res;
	}

	public boolean leq(Stamp s2) {
		return this.event.leq(s2.getEvent());
	}

	protected static void fill(Id i, Event e) {

		if (i.isLeaf && i.getValue() == 0) {
		} else if (i.isLeaf && i.getValue() == 1) {
			e.height();
		} else if (e.isLeaf) {
		} else if (i.isLeaf == false && i.getLeft().isLeaf && i.getLeft().getValue() == 1) {
			Stamp.fill(i.getRight(), e.getRight());
			e.getLeft().height();
			e.getLeft().setValue(Math.max(e.getLeft().getValue(), e.getRight().getValue()));
			e.normalize();
		} else if (i.isLeaf == false && i.getRight().isLeaf && i.getRight().getValue() == 1) {
			Stamp.fill(i.getLeft(), e.getLeft());
			e.getRight().height();
			e.getRight().setValue(Math.max(e.getRight().getValue(), e.getLeft().getValue()));
			e.normalize();
		} else if (i.isLeaf == false) {
			Stamp.fill(i.getLeft(), e.getLeft());
			Stamp.fill(i.getRight(), e.getRight());
			e.normalize();
		} else {
			System.out.println("ERROR Fill\n ID:"+ i.toString()+"\n Ev:"+e.toString());
		}
	}

	protected static int grow(Id i, Event e) {
		int cost;

		if (i.isLeaf && i.getValue() == 1 && e.isLeaf) {
			e.setValue(e.getValue()+1);
			return 0;
		} else if (e.isLeaf) {
			e.setAsNode();
			cost = Stamp.grow(i, e);
			return cost + 1000;
		} else if (i.isLeaf == false && i.getLeft().isLeaf && i.getLeft().getValue() == 0) {
			cost = Stamp.grow(i.getRight(), e.getRight());
			return cost + 1;
		} else if (i.isLeaf == false && i.getRight().isLeaf && i.getRight().getValue() == 0) {
			cost = Stamp.grow(i.getLeft(), e.getLeft());
			return cost + 1;
		} else if (i.isLeaf == false) {
			Event el = e.getLeft().clone();
			Event er = e.getRight().clone();
			int costr = Stamp.grow(i.getRight(), e.getRight());
			int costl = Stamp.grow(i.getLeft(), e.getLeft());
			if (costl < costr) {
				e.setRight(er);
				return costl + 1;
			} else {
				e.setLeft(el);
				return costr + 1;
			}
		} else {
			System.out.println("ERROR GROW\n ID:"+ i.toString()+"\n Ev:"+e.toString());

		}

		return -1;
	}

	public char[] Encode() {

		BitArray bt = new BitArray();
		bt = this.id.encode(bt);
		bt = this.event.encode(bt);

		return bt.getBits();
	}

	public void Decode(char[] bits) {
		BitArray bt = new BitArray();
		bt.setBits(bits);

		this.id.decode(bt);
		this.event.decode(bt);
	}

	public BitArray encode() {
		return this.event.encode(null);
	}

	public char[] dEncode() {
		char[] res;

		char[] idcode = this.id.dEncode();
		char[] eventcode = this.event.dEncode();

		int dts = 16;
		int isize = 3 + ((int) (idcode[0] + idcode[1]) / dts);
		int esize = 3 + ((int) (eventcode[0] + eventcode[1]) / dts);

		res = new char[isize + esize];
		System.arraycopy(idcode, 0, res, 0, isize);
		System.arraycopy(eventcode, 0, res, isize, esize);

		return res;
	}

	//size of stamp in bits
	public int sizeInBits() {
		int Ibits = this.id.encode(null).getSizeBits();
		int Ebits = this.event.encode(null).getSizeBits();
		return Ibits + Ebits;
	}
	
	//size of stamp in byts
	public int sizeInBytes() {
		int Ibits = this.id.encode(null).getSizeBits();
		int Ebits = this.event.encode(null).getSizeBits();
		return (int) ((Ibits + Ebits + 4) / 8);
	}
//
//	//size of stamp in bits
//	public int sizeInBits(char[] array){
//		int ilen = (array[0])/8;
//		int ilen2 = 3 + ((array[0] + array[1])/16);
//		int elen = (array[ilen2])/8;
//		return ilen+elen;
//	}

	public void decode(BitArray bt) {
		this.event.decode(bt);
	}

	public void dDecode(char[] array) { // tamanhos completos, incluindo metadados (ub, fb, sb)
		int ilen = 3 + ((array[0] + array[1]) / 16);
		int elen = 3 + ((array[ilen] + array[ilen + 1]) / 16);
		char[] icode = new char[ilen];
		char[] ecode = new char[elen];

		System.arraycopy(array, 0, icode, 0, ilen);
		System.arraycopy(array, ilen, ecode, 0, elen);

		BitArray bi = new BitArray(icode);
		BitArray be = new BitArray(ecode);

		this.id.decode(bi);
		this.event.decode(be);
	}

	protected Event getEvent() {
		return this.event;
	}

	protected Id getId() {
		return this.id;
	}

	protected void setEvent(Event e) {
		this.event = e;
	}

	protected void setId(Id i) {
		this.id = i;
	}

	@Override
	public String toString() {
		return new String("( " + id.toString() + ", " + event.toString() + " )");
	}

	@Override
	public Stamp clone() {
		Stamp res = new Stamp();
		res.event = this.getEvent().clone();
		res.id = this.getId().clone();
		return res;
	}

	public void saveToFile(DataOutputStream out) {
		char[] array = this.Encode();

		long len = (array.length);
		byte[] n = ByteInt.intToByteArray((long) (len * 2) & 0xffffffffl);

		try {
			// writes the number of bytes to be saved
			for (int j = 0; j < 4; j++) {
				out.writeByte(n[j]);
			}

			// writes the bytes of the encoded stamp
			for (int j = (int) len - 1; j >= 0; j--) {
				out.writeChar(array[j]);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void loadFromFile(DataInputStream in) {
		try {
			byte[] ene = new byte[4];
			for (int j = 0; j < 4; j++) {
				ene[j] = (byte) in.read();
			}
			long n = ByteInt.byteArrayToInt(ene);

			n /= 2;
//			System.out.println(" coiso " + n);
			char[] array = new char[(int) n];
			for (int j = (int) n - 1; j >= 0; j--) {
				array[j] = in.readChar();
			}

			this.Decode(array);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
