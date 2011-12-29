/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;

import causal_histories.*;
import itc.Stamp;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import util.*;

public class Test_itv_vs_ch {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		(new Test_itv_vs_ch()).CHvsITC();
		long finish = System.currentTimeMillis();
		System.out.println("TIME:" + (finish - start) / 1000.0f);
	}

	public void CHvsITC() {


		// HC mechanism
		Gerador gen = new Gerador();
		Dice dado = new Dice();
		Bag<CStamp> saco = new Bag<CStamp>();

		CStamp seed = new CStamp();
		seed.seed(gen.seed());
		saco.push(seed);

		// ITC mechanism
		Bag<Stamp> bag = new Bag<Stamp>();

		Stamp seedb = new Stamp();
		bag.push(seedb);

		int forks = 0;
		int joins = 0;
		int events = 0;

		int i;
		int counter = 0;
		for (i = 0; i < 1500; i++) {
			System.out.println(i + 1 + ": bugs->" +counter);
			int tipo = dado.iroll(1, 100);

			if (tipo <= 34 || saco.getSize() == 1) { // fork
				System.out.println("Fork __________________________");
				forks++;
				int ind = saco.getValidIndice();

				// mecanismo hc
				CStamp out = (CStamp) saco.popInd(ind);
				CStamp novo = new CStamp();
				novo = out.fork();

				saco.push(out);
				saco.push(novo);

				// mecanismo itc in place
				Stamp sout = (Stamp) bag.popInd(ind);
				Stamp sin = sout.fork();

				bag.push(sout);
				bag.push(sin);

//				// mecanismo itc funcional
//				Stamp outb = (Stamp) bag.popInd(ind);
//				Stamp[] p = Stamp.fork(outb);
//				Stamp in1 = p[0];
//				Stamp in2 = p[1];
//
//				bag.push(in1);
//				bag.push(in2);
			} else if (tipo <= 66) { // join
				System.out.println("Join __________________________");
				joins++;
				int inda = saco.getValidIndice();

				CStamp outa = (CStamp) saco.popInd(inda);
				Stamp souta = (Stamp) bag.popInd(inda);

				int indb = saco.getValidIndice();

				CStamp outb = (CStamp) saco.popInd(indb);
				Stamp soutb = (Stamp) bag.popInd(indb);

				CStamp novo = new CStamp();
				novo.join(outa, outb);
				saco.push(novo);

//				Stamp novob = Stamp.join(souta, soutb);
//				bag.push(novob);
				souta.join(soutb);
				bag.push(souta);
			} else { // event
				System.out.println("Event _________________________");
				events++;
				int ind = saco.getValidIndice();

				CStamp out = (CStamp) saco.popInd(ind);
				out.event(gen.gera());
				saco.push(out);

				Stamp outb = (Stamp) bag.popInd(ind);
//				System.out.println("ANTES:"+outb.toString());
				outb.event();
//				System.out.println("DPS:"+outb.toString());
				bag.push(outb);
			}

			CStamp tmp = new CStamp();
			tmp = (CStamp) saco.getLast();
			Stamp tmpb = new Stamp();
			tmpb = (Stamp) bag.getLast();
			int len = saco.getSize();

			for (int n = 0; n < len - 1; n++) {
				boolean a = tmp.equals((CStamp) saco.getInd(n));

				Stamp decd = new Stamp();
				char[] coise = bag.getInd(n).Encode();
				decd.Decode(coise);
//				decd.dDecode(bag.getInd(n).dEncode());
				boolean b = tmpb.equals(decd);
//				boolean b = tmpb.equals((Stamp) bag.getInd(n));
				if (!((a && b) || (!a && !b))) {
					System.out.println("Devia ser "+a+", mas e "+b+"\n\t"+tmpb.toString()+ "   E    "+decd.toString());
					counter++;
				}
			}

		}

//		File f = new File("binaryfile");
//
//		try {
//			DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
//
//			bag.saveBag(out);
//
//			out.close();
//
//			DataInputStream in = new DataInputStream(new FileInputStream(f));
//
//			Bag<Stamp> bbb = new Bag();
//			bbb.loadBag(in);
//
//
////			System.out.print(bbb.toString());
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}

		System.out.println(" Bugs : " + counter);
		System.out.println("=======================");
		System.out.println(" Forks  : " + forks);
		System.out.println(" Joins  : " + joins);
		System.out.println(" Events : " + events);
		System.out.println("");
		System.out.println(" Bag final size : " + bag.getSize());
	}
}
