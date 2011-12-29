package Test;

import util.Bag;
import util.Dice;
import itc.Stamp;
import java.io.*;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Test implements Runnable {

	private final static String PATH = "/Volumes/Varios/Dropbox/MI/2ano/tese/test_runs";
	private final static String TEST_NAME = "itc_test_";
	private File runs = null;
	private BufferedWriter out = null;
	private int number_test;
	private int number_processes;
	private int number_iterations;
	private float ratio_loss;

	public Test(int np, int ni, float rl) {
		number_processes = np;
		number_iterations = ni;
		ratio_loss = rl;

		start();
	}

	public void start() {
		try {

			runs = new File(PATH);
			number_test = 1;
			File[] files = runs.listFiles();
			for (File f : files) {
				if (f.getName().startsWith(TEST_NAME + number_processes + "_" + ratio_loss)) {
					number_test++;
				}
			}

			TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
			GregorianCalendar gc = new GregorianCalendar(tz);
			String time = ((gc.getTimeInMillis()) + "");
			runs = new File(PATH + File.separator + TEST_NAME + number_processes + "_" + ratio_loss + "_" + number_test + "_" + time + ".dat");
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

			testITC_data_causality(number_processes, number_iterations, ratio_loss);
			//		testITC_process_causality(number_processes,number_iterations,0.5f,1.0f);

			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void testITC_data_causality(int nRep, int nIter, float ratio_loss) throws IOException {

		out.write("# Automatic Test for ITC\n");
		out.write("# Number of Replicas: " + nRep + "\n");
		out.write("# Number of Iterations: " + nIter + "\n");
		out.write("# Ration Loss: " + ratio_loss + "\n");
		out.write("# Test Number: " + number_test + "\n");

		// ITC mechanism
		Bag<Stamp> bag = new Bag<Stamp>();
		Stamp seed = new Stamp();
		bag.push(seed);

		Dice dice = new Dice();
		int i, j;

		for (i = 0; i < nRep - 1; i++) {

			//fork
			Stamp s = bag.pop();
			Stamp s2 = s.fork();
			bag.push(s);
			bag.push(s2);

			//size
			float size = 0.0f;
			float size_for_one = 0.0f;
			for (Stamp st : bag.getList()) {
				size += st.sizeInBytes();
			}
			size_for_one = (size / nRep);

			//printing
			if (i % 5000 == 0) {
				System.out.println("It " + i + " - nRep " + bag.getSize() + " - sizeBytes T: " + size + " A:" + size_for_one);
			}
			out.write(i + "\t" + size_for_one + "\n");
		}

		for (; i < nIter; i++) {
			Stamp s;
			Stamp s2;

			//fork
			int sizeBag = bag.getSize();
			int nForks = (nRep + 1) - sizeBag;
//			System.out.println("nForks "+nForks);
			for (j = 0; j < nForks; j++) {
				s = bag.pop();
				s2 = s.fork();
				bag.push(s);
				bag.push(s2);
			}

			//event
			s = bag.pop();
			s.event();
			bag.push(s);

			//join
			s = bag.pop();
			s2 = bag.pop();
			if (dice.roll() >= (ratio_loss * 100)) {
				s = Stamp.join(s, s2);
			} else {
//				System.out.println("LOSS");
			}
			bag.push(s);

			//size
			float size = 0.0f;
			float size_for_one = 0.0f;
			for (Stamp st : bag.getList()) {
				size += st.sizeInBytes();
			}
			size_for_one = (size / nRep);

			//printing
			if (i % 50 == 0) {
				System.out.println("It " + i + " - nRep " + bag.getSize() + " - sizeBytes T: " + size + " A:" + size_for_one);
			}
			out.write(i + "\t" + size_for_one + "\n");
		}
	}

	public void testITC_process_causality(int nRep, int nIter, float ratio_events, float ratio) {

		// ITC mechanism
		Bag<Stamp> bag = new Bag<Stamp>();
		Stamp seed = new Stamp();
		bag.push(seed);

		int i, j;

		int nEvents = (int) (nRep * ratio_events);

		for (i = 0; i < nRep - 1; i++) {
			//fork
			Stamp s = bag.pop();
			Stamp s2 = s.fork();
			bag.push(s);
			bag.push(s2);

			//size
			float size = 0.0f;
			for (Stamp st : bag.getList()) {
				size += st.sizeInBytes();
			}

			//printing
			System.out.println("It " + i + " - nRep " + bag.getSize() + " - sizeBytes T: " + size + " A:" + (size / nRep));
		}

		for (i = 0; i < nIter; i++) {
			//peek
			Stamp s = bag.pop();
			Stamp s2 = bag.pop();
			Stamp peek = s.peek();
			s2.join(peek);
			bag.push(s);
			bag.push(s2);

			//events
			for (j = 0; j < nEvents; j++) {
				s = bag.pop();
				s.event();
				bag.push(s);
			}

			//size
			int size = 0;
			for (Stamp st : bag.getList()) {
				size += st.sizeInBytes();
			}

			//printing
			System.out.println("It " + i + " - nRep " + bag.getSize() + " - sizeBytes T: " + size + " A:" + (int) (size / nRep));
		}
	}

	public static void main(String[] args) {

		for (int i = 0; i < 2; i++) {
			Thread t = new Thread(new Test(8, 100000, 0.0f));
			t.setPriority(Thread.MAX_PRIORITY);
			t.start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

//
//		String PATH2 = "/Volumes/Varios/Dropbox/MI/2ano/tese/test_runs";
//		String TEST_NAME2 = "itc_test_";
//		File runs2 = null;
//
//		ArrayList<File> files2 = new ArrayList<File>();
//
//		try {
//
//			runs2 = new File(PATH2);
//			File[] files = runs2.listFiles();
//			for(File f : files) {
//				if(f.getName().startsWith(TEST_NAME2+ "16_0")) {
//				}
//				else if(f.getName().startsWith(TEST_NAME2+ "16_")) {
//					System.out.println("YES: "+f.getName());
//					files2.add(f);
//				}
//			}
//
//			HashMap<Integer,Float> results = new HashMap<Integer,Float>();
//			int number_files = files2.size();
//
//			for(File ff : files2) {
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
//						int size = Integer.parseInt(numbers[1].trim());
////						System.out.println ("IT:"+it+" size:"+size);
//						if(results.containsKey(it)) {
//							float res = results.get(it) + (size/number_files);
//							results.put(it, res);
//						} else {
//							float res = (size/number_files);
//							results.put(it, res);
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
//
//
//			File resultado = new File(PATH2+ File.separator +"itc_test_16_0.0");
//
//			// Create file
//			FileWriter fstream = new FileWriter(resultado);
//				BufferedWriter out = new BufferedWriter(fstream);
//			for(Integer key : results.keySet()){
//					out.write(key+"\t"+results.get(key)+"\n");
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


