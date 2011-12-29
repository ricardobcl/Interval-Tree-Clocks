package Test;

import util.Bag;
import itc.Stamp;
import java.io.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Test_cassandra implements Runnable {

	private final static String PATH = "/Volumes/Varios/Dropbox/MI/2ano/tese/test_runs";
	private final static String TEST_NAME = "itc_test_cass_";
	private File runs = null;
	private BufferedWriter out = null;
	private int number_test;
	private int number_processes;
	private int number_processes_real;
	private int number_iterations;
	private float ratio_loss;

	public Test_cassandra(int np, int npr, int ni, float rl) {
		number_processes = np;
		number_iterations = ni;
		ratio_loss = rl;
		number_processes_real = npr;

		start();
	}

	public void start() {
		try {

			runs = new File(PATH);
			number_test = 1;
			File[] files = runs.listFiles();
			for (File f : files) {
				if (f.getName().startsWith(TEST_NAME + number_processes + "_" + number_processes_real + ratio_loss)) {
					number_test++;
				}
			}

			TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
			GregorianCalendar gc = new GregorianCalendar(tz);
			String time = ((gc.getTimeInMillis()) + "");
			runs = new File(PATH + File.separator + TEST_NAME + number_processes + "_" + number_processes_real + ratio_loss + "_" + number_test + "_" + time + ".dat");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void average_results(int nRep) {

		//average all
		if (nRep == 0) {
			//code
			return;
		}

		//average files with nRep replicas


	}

	public void run() {
		try {
			out = new BufferedWriter(new FileWriter(runs));

			testITC_data_causality(number_processes, number_processes_real, number_iterations, ratio_loss);
			//		testITC_process_causality(number_processes,number_iterations,0.5f,1.0f);

			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void testITC_data_causality(int nRep, int nRepReal, int nIter, float ratio_loss) throws IOException {

		out.write("# Automatic Test for ITC\n");
		out.write("# Number of Replicas: " + nRep + "\n");
		out.write("# Number of Replicas working: " + nRepReal + "\n");
		out.write("# Number of Iterations: " + nIter + "\n");
		out.write("# Ration Loss: " + ratio_loss + "\n");
		out.write("# Test Number: " + number_test + "\n");

		// ITC mechanism
		Bag<Stamp> bag = new Bag<Stamp>();
		Stamp seed = new Stamp();
		bag.push(seed);

		int i, j;


		//create working ids
		int nRepReal_square = (int) (Math.log(nRepReal) / Math.log(2));
//		System.out.println("fake1:"+nRepReal_square+ " de:"+nRepReal);
		for (i = 0; i < nRepReal_square; i++) {

			ArrayList<Stamp> temp = new ArrayList<Stamp>();

//		System.out.println("fake:"+i+" not:"+bag.getList().size());
			for (j = 0; j < (Math.pow(2, i)); j++) {
				Stamp s = bag.pop();
				Stamp s2 = s.fork();
				temp.add(s);
				temp.add(s2);
			}

			for (Stamp ss : temp) {
				bag.push(ss);
			}
//
//			//size
//			int size = 0;
//			int size_for_one = 0;
//			for(Stamp st : bag.getList()) {
//				size += st.sizeInBytes();
//			}
//			size_for_one = (int)(size/nRepReal);
//
//			//printing
//			if(i%5000==0) {
//				System.out.println("It "+i+" - nRep "+bag.getSize()+" - sizeBytes T: "+size+" A:"+size_for_one);
//			}
//			out.write(i+"\t"+size_for_one+"\n");
		}

		int nRep_div = (int) nRep / nRepReal;


		int nRep_div_square = (int) (Math.log(nRep_div) / Math.log(2));

		//create remaining ids
		Stamp worker;
		Bag<Stamp> bag_not_work = new Bag<Stamp>();
		Bag<Stamp> bag_work = new Bag<Stamp>();

		for (Stamp sss : bag.getList()) {
			worker = sss;
			Bag<Stamp> bag_work2 = new Bag<Stamp>();
//			bag_work2.push(worker);

			for (i = 0; i < nRep_div_square; i++) {

				ArrayList<Stamp> temp = new ArrayList<Stamp>();

				boolean first = true;
				for (j = 0; j < (Math.pow(2, i)); j++) {
					Stamp s, s2;
					if (first) {
						first = false;
						s2 = worker.fork();
						temp.add(s2);
					} else {
						s = bag_work2.pop();
						s2 = s.fork();
						temp.add(s);
						temp.add(s2);
					}
				}

				for (Stamp ss : temp) {
					bag_work2.push(ss);
				}
			}

			for (Stamp sss2 : bag_work2.getList()) {
				bag_not_work.push(sss2);
			}
//			bag_work.push(worker);
		}
		bag_work = bag;


		System.out.println("\nATenTION!!!\n\tNOT WORKING:" + bag_not_work.getList().size() + "\n\tWORKING:" + bag_work.getList().size());

		for (; i < nIter; i++) {
			Stamp s;
			Stamp s2;


			//event
			s = bag_work.pop();
			s.event();
			bag_work.push(s);

			//peek
			s = bag_work.pop();
			s2 = s.peek();
			bag_work.push(s);

			//join
			s = bag_work.pop();
			s = Stamp.join(s, s2);
			bag_work.push(s);

			//size
			float size = 0.0f;
			float size_for_one = 0.0f;
			for (Stamp st : bag_work.getList()) {
				size += st.sizeInBytes();
			}
			size_for_one = (size / nRepReal);

			//printing
			if (i % 500 == 0) {
				System.out.println("It " + i + " - nRep " + bag_work.getSize() + " - sizeBytes T: " + size + " A:" + size_for_one);
			}
			out.write(i + "\t" + size_for_one + "\n");
		}
	}

//
//	public void testITC_data_causality(int nRep, int nRepReal, int nIter, float ratio_loss) throws IOException {
//
//		out.write("# Automatic Test for ITC\n");
//		out.write("# Number of Replicas: "+nRep+"\n");
//		out.write("# Number of Replicas working: "+nRepReal+"\n");
//		out.write("# Number of Iterations: "+nIter+"\n");
//		out.write("# Ration Loss: "+ratio_loss+"\n");
//		out.write("# Test Number: "+number_test+"\n");
//
//		// ITC mechanism
//		Bag<Stamp> bag = new Bag<Stamp>();
//		Stamp seed = new Stamp();
//		bag.push(seed);
//
//
//		Dice dice = new Dice();
//		int i,j;
//
//
//		//create working ids
//		int nRepReal_square = (int) (Math.log(nRepReal) / Math.log(2));
////		System.out.println("fake1:"+nRepReal_square+ " de:"+nRepReal);
//		for(i=0 ; i<nRepReal_square ; i++) {
//
//			ArrayList<Stamp> temp = new ArrayList<Stamp>();
//
////		System.out.println("fake:"+i+" not:"+bag.getList().size());
//			for(j=0;j<(Math.pow(2, i));j++) {
//				Stamp s = bag.pop();
//				Stamp s2 = s.fork();
//				temp.add(s);
//				temp.add(s2);
//			}
//
//			for(Stamp ss : temp) {
//				bag.push(ss);
//			}
////
////			//size
////			int size = 0;
////			int size_for_one = 0;
////			for(Stamp st : bag.getList()) {
////				size += st.sizeInBytes();
////			}
////			size_for_one = (int)(size/nRepReal);
////
////			//printing
////			if(i%5000==0) {
////				System.out.println("It "+i+" - nRep "+bag.getSize()+" - sizeBytes T: "+size+" A:"+size_for_one);
////			}
////			out.write(i+"\t"+size_for_one+"\n");
//		}
//
//		int nRep_div = (int) nRep/nRepReal;
//
//		Bag<Stamp> bag_work = new Bag<Stamp>();
//		Stamp stamp_work = bag.pop();
//		bag_work.push(stamp_work);
//
//		int nRep_div_square = (int) (Math.log(nRep_div) / Math.log(2));;
////		System.out.println("fake2:"+nRep_div_square);
//		for(i=0 ; i<nRep_div_square ; i++) {
//
//			ArrayList<Stamp> temp = new ArrayList<Stamp>();
//
////		System.out.println("fake:"+i+" not:"+bag_work.getList().size());
//			for(j=0;j<(Math.pow(2, i));j++) {
//				Stamp s = bag_work.pop();
//				Stamp s2 = s.fork();
//				temp.add(s);
//				temp.add(s2);
//			}
//
//			for(Stamp ss : temp) {
//				bag_work.push(ss);
//			}
//		}
//
//
//		//create remaining ids
//		Bag<Stamp> bag_not_work = new Bag<Stamp>();
//		for(Stamp sss : bag.getList()) {
//			Bag<Stamp> bag_work2 = new Bag<Stamp>();
//			bag_work2.push(sss);
//
//			for(i=0 ; i<nRep_div_square ; i++) {
//
//				ArrayList<Stamp> temp = new ArrayList<Stamp>();
//
//				for(j=0;j<(Math.pow(2, i));j++) {
//					Stamp s = bag_work2.pop();
//					Stamp s2 = s.fork();
//					temp.add(s);
//					temp.add(s2);
//				}
//
//				for(Stamp ss : temp) {
//					bag_work2.push(ss);
//				}
//			}
//
//			for(Stamp sss2 : bag_work2.getList()) {
//				bag_not_work.push(sss2);
//			}
//		}
//
//
////		System.out.println("\nATenTION!!!\n\tNOT WORKING:"+bag_not_work.getList().size()+"\n\tWORKING:"+bag_work.getList().size());
//
//		for(; i<nIter ; i++) {
//			Stamp s;
//			Stamp s2;
//
//
//			//event
//			s = bag_work.pop().event();
//			bag_work.push(s);
//
//			//peek
//			s = bag_work.pop();
//			s2 = s.peek();
//			bag_work.push(s);
//
//			//join
//			s = bag_work.pop();
//			s = Stamp.join(s, s2);
//			bag_work.push(s);
//
//			//size
//			int size = 0;
//			int size_for_one = 0;
//			for(Stamp st : bag_work.getList()) {
//				size += st.sizeInBytes();
//			}
//			size_for_one = (int)(size/nRepReal);
//
//			//printing
//			if(i%500==0) {
//				System.out.println("It "+i+" - nRep "+bag_work.getSize()+" - sizeBytes T: "+size+" A:"+size_for_one);
//			}
//			out.write(i+"\t"+size_for_one+"\n");
//		}
//	}
	public static void main(String[] args) {
//
		for(int i = 0 ; i< 2 ; i++) {
			Thread t = new Thread(new Test_cassandra(32,32,10000,0.0f));
			t.setPriority(Thread.MAX_PRIORITY);
			t.start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
//
////
//		String PATH2 = "/Volumes/Varios/Dropbox/MI/2ano/tese/test_runs";
//		String TEST_NAME2 = "itc_test_cass_";
//		File runs2 = null;
//
//		ArrayList<File> files2 = new ArrayList<File>();
//
//		try {
//
//			runs2 = new File(PATH2);
//			File[] files = runs2.listFiles();
//			for (File f : files) {
//				if (f.getName().startsWith(TEST_NAME2 + "128_1280.0_")) {
//					System.out.println("YES: " + f.getName());
//					files2.add(f);
//				}
//			}

//			HashMap<Integer, Float> results = new HashMap<Integer, Float>();
//			int number_files = files2.size();
//
//			for (File ff : files2) {
//
//				System.out.println("DOING " + ff.getName());
//
//				// command line parameter
//				FileInputStream fstream = new FileInputStream(ff);
//				// Get the object of DataInputStream
//				DataInputStream in = new DataInputStream(fstream);
//				BufferedReader br = new BufferedReader(new InputStreamReader(in));
//				String strLine;
//				//Read File Line By Line
//				while ((strLine = br.readLine()) != null) {
//					// Print the content on the console
//					if (!strLine.startsWith("#")) {
////						System.out.println (strLine.trim());
//						String[] numbers = strLine.trim().split("\t");
//						int it = Integer.parseInt(numbers[0].trim());
//						float size = Float.parseFloat(numbers[1].trim());
////						System.out.println ("IT:"+it+" size:"+size);
//						if (results.containsKey(it)) {
//							float res = results.get(it) + (size / number_files);
//							results.put(it, res);
//						} else {
//							float res = (size / number_files);
//							results.put(it, res);
//						}
//					}
//
//				}
//
//				in.close();
//
//			}
//
////			TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
////			GregorianCalendar gc = new GregorianCalendar(tz);
////			String time = ((gc.getTimeInMillis()) + "");
////			runs2 = new File(PATH2 + File.separator + TEST_NAME2 + number_processes + "_" + ratio_loss + "_" + number_test + "_" + time + ".dat");
//
//
//			File resultado = new File(PATH2 + File.separator + "itc_test_cass_128_128_0.0.dat");
//
//			// Create file
//			FileWriter fstream = new FileWriter(resultado);
//			BufferedWriter out = new BufferedWriter(fstream);
//			for (Integer key : results.keySet()) {
//				out.write(key + "\t" + results.get(key) + "\n");
//			}
//			//Close the output stream
//			out.close();
//			//Read File Line By Line
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}



//


//
//
//		String PATH2 = "/Volumes/Varios/Dropbox/MI/2ano/tese/test_runs";
//		String TEST_NAME2 = "itc_test_cass_";
//		File runs2 = null;
//
//		TreeMap<Integer,File> files2 = new TreeMap<Integer,File>();
//
//		try {
//
//			runs2 = new File(PATH2);
//			File[] files = runs2.listFiles();
//			for(File f : files) {
//				if(f.getName().startsWith(TEST_NAME2+ "8_8_0.0.dat")) {
//					System.out.println("YES: "+f.getName());
//					files2.put(0,f);
//				} else if(f.getName().startsWith(TEST_NAME2+ "128_8_0.0.dat")) {
//					System.out.println("YES: "+f.getName());
//					files2.put(1,f);
//				} else if(f.getName().startsWith(TEST_NAME2+ "128_8_v2_0.0.dat")) {
//					System.out.println("YES: "+f.getName());
//					files2.put(2,f);
//				}
//			}
//
//			HashMap<Integer,Float[]> results = new HashMap<Integer,Float[]>();
//			int number_files = files2.size();
//
//			for(int ii : files2.keySet()) {
//				File ff = files2.get(ii);
//
//						System.out.println ("DOING "+ff.getName());
//
//				// command line parameter
//				FileInputStream fstream = new FileInputStream(ff);
//				// Get the object of DataInputStream
//				DataInputStream in = new DataInputStream(fstream);
//				BufferedReader br = new BufferedReader(new InputStreamReader(in));
//				String strLine;
//				//Read File Line By Line
//				while ((strLine = br.readLine()) != null)   {
//				  // Print the content on the console
//					if(!strLine.startsWith("#")) {
////						System.out.println (strLine.trim());
//						String[] numbers = strLine.trim().split("\t");
//						int it = Integer.parseInt(numbers[0].trim());
//						float size = Float.parseFloat(numbers[1].trim());
////						System.out.println ("IT:"+it+" size:"+size);
//						if(results.containsKey(it)) {
//							Float[] new_arr = results.get(it);
//							new_arr[ii] = size;
//							results.put(it, new_arr);
//						} else {
//							Float[] new_arr = new Float[3];
//							new_arr[0] = 0.0f;
//							new_arr[1] = 0.0f;
//							new_arr[2] = 0.0f;
//							new_arr[ii] = size;
//							results.put(it, new_arr);
//						}
//					}
//
//				}
//
//				in.close();
//
//			}
////
////			TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
////			GregorianCalendar gc = new GregorianCalendar(tz);
////			String time = ((gc.getTimeInMillis()) + "");
////			runs2 = new File(PATH2 + File.separator + TEST_NAME2 + number_processes + "_" + ratio_loss + "_" + number_test + "_" + time + ".dat");
////
////
//			File resultado = new File(PATH2+ File.separator +"itc_test_cass_merge_0.0.dat");
//
//			// Create file
//			FileWriter fstream = new FileWriter(resultado);
//				BufferedWriter out = new BufferedWriter(fstream);
//			for(Integer key : results.keySet()){
//				Float[] new_arr = results.get(key);
//				out.write(key+"\t"+new_arr[0]+"\t"+new_arr[1]+"\t"+new_arr[2]+"\n");
//			}
//			//Close the output stream
//			out.close();
//			//Read File Line By Line
//
//		} catch(Exception e) {
//			e.printStackTrace();
//		}


	}
}


