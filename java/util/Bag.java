package util;

import itc.Stamp;
import java.util.ArrayList;


import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Bag<T> {

	private ArrayList<T> list;

	public Bag() {
		this.list = new ArrayList<T>();
	}

	public void push(T s) {
		this.list.add(s);
	}

	public T pop() {
		Dice d = new Dice();
		int ind = d.iroll(0, this.list.size() - 1);

		return this.list.remove(ind);
	}

	public T popInd(int i) {
		//if (i < this.list.size()){
		return this.list.remove(i);
		//}
	}

	public T getInd(int i) {
		//if (i < this.list.size()){
		return this.list.get(i);
		//}
	}

	public T getLast() {
		int ind = this.list.size() - 1;
		// if( ind > 0){
		return this.list.get(ind);
		//}
	}

	public int getValidIndice() {
		Dice d = new Dice();
		return d.iroll(0, this.list.size() - 1);
	}

	public int getSize() {
		return this.list.size();
	}

	public ArrayList<T> getList() {
		return this.list;
	}

	// save bag with stamps
	public void saveBag(DataOutputStream out) {
		try {

			long len = this.getSize() & 0xffffffffl;
			byte[] n = ByteInt.intToByteArray((long) len);

			// writes the number of bytes to be saved
			for (int j = 0; j < 4; j++) {
//				System.out.println(n[j]);
				out.write(n[j]);
			}

			for (T stamp : this.list) {
//				System.out.println(((Stamp) stamp).toString());
				((Stamp) stamp).saveToFile(out);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void loadBag(DataInputStream in) {
		try {
			byte[] ene = new byte[4];

			// reads the number of bytes to be saved
			for (int j = 0; j < 4; j++) {
//				System.out.println(ene[j]);
				ene[j] = (byte) in.read();
			}
			long len = (long) ByteInt.byteArrayToInt(ene);

			for (int j = 0; j < len; j++) {
				Stamp s = new Stamp();
				s.loadFromFile(in);
//				System.out.println("in" + s.toString());
				this.push((T) s);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
