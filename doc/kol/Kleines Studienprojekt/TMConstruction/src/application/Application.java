package application;

import java.util.concurrent.TimeUnit;

import construction.TM2generator;
import interpreter.TuringMachine;
import utils.Utilities;

/**
 * Anwendung Kapselt die eigentliche Anwendung und handelt Parameter ab.
 */
public class Application {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		boolean debug = false;
		boolean print2File = false;
		boolean printDetails = false;
		boolean texOutput = false;
		long delayMilis = 0;

		if (args.length < 1) {
			System.err.println(
					"Usage: <TM as .tur file> <milis delay> (optional delay between tape jumps) \n-p (print simulation history to file) \n-pd (print also details) \n-c (convert to 2 State TM before simulating) \n-tex (output from two state TM as .tex file)\n-d (debug)");
			System.exit(1);
		}

		if (args.length >= 2 && args[1].chars().allMatch(Character::isDigit)) {
			delayMilis = Long.parseLong(args[1]);
		}

		String filename = args[0];
		if (Utilities.contains(args, "-d")) {
			debug = true;
		}
		if (Utilities.contains(args, "-p")) {
			print2File = true;
		}
		if (Utilities.contains(args, "-pd")) {
			print2File = true;
			printDetails = true;
		}
		if(Utilities.contains(args, "-tex")) {
			if(Utilities.contains(args, "-c") && print2File) {
				texOutput = true;
			} else {
				System.err.println("\n.tex Output only for converted 2State TM and if history flag is set!\n");
			}
		}
		if (Utilities.contains(args, "-c")) {
			System.out.println("Init 2 State TM Generator...\n");
			TM2generator gen = new TM2generator(args[0]);
			gen.generate2StateTM();
			if (debug) {
				gen.printTransitions();
			}
			filename = args[0].split(".tur")[0] + "_2S.tur";
			System.out.println("\n\nwriting 2 State TM to file " + filename);
			gen.writeTM2toFile(filename);
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		TuringMachine tm = new TuringMachine(debug);

		System.out.println("Reading TM from file\t" + filename);
		tm.readTMfromFile(filename);

		System.out.println("Starting simulator!");
		tm.run(delayMilis ,  texOutput);

		if (print2File && !texOutput) {
			System.out.println("Printing history to file:\t" + filename.split(".tur")[0] + ".history");
			tm.writeHistoryToFile(filename.split(".tur")[0] + ".history", printDetails , texOutput);
		} else if (print2File && texOutput) {
			System.out.println("Printing history to file:\t" + filename.split(".tur")[0] + "history.tex");
			tm.writeHistoryToFile(filename.split(".tur")[0] + "history.tex", printDetails , texOutput);
		}

	}


}
