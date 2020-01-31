package interpreter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import construction.TM2generator;
import utils.Utilities;

/**
 * The Class TuringMachine.
 */
public class TuringMachine {

	/** The Constant OLD_STATE. */
	// statische indizes der Symbole in den Transitionarrays
	private static final int OLD_STATE = 0;
	
	/** The Constant NEW_STATE. */
	private static final int NEW_STATE = 1;
	
	/** The Constant OLD_SYMBOL. */
	private static final int OLD_SYMBOL = 2;
	
	/** The Constant NEW_SYMBOL. */
	private static final int NEW_SYMBOL = 3;
	
	/** The Constant DIRECTION. */
	private static final int DIRECTION = 4;

	/** The terminated. */
	private boolean terminated;
	
	/** The debug. */
	private boolean debug;
	
	/** The nr of states. */
	private int nrOfStates = 0;

	/** The tape. */
	// eigentliche TM
	private Tape tape;
	
	/** The current state. */
	private State currentState;
	
	/** The transitions. */
	private Map<State, HashMap<String, Transition>> transitions;

	/** The history. */
	// Verlauf, kann nach Terminierung ausgegeben werden
	private LinkedList<String> history;
	
	/** The history details. */
	private LinkedList<String> historyDetails;

	/**
	 * Instantiates a new turing machine.
	 *
	 * @param debug the debug
	 */
	// Konstruktor
	public TuringMachine(boolean debug) {
		history = new LinkedList<>();
		historyDetails = new LinkedList<>();
		this.debug = debug;
		terminated = false;
		tape = new Tape();
		transitions = new HashMap<State, HashMap<String, Transition>>();
	}

	/**
	 * Write history to file.
	// schreibt verlauf der TM in Datei
	 *
	 * @param filename the filename
	 * @param includeDetails the include details
	 * @param tex the tex
	 */
	public void writeHistoryToFile(String filename, boolean includeDetails, boolean tex) {
		try (PrintStream out = new PrintStream(new FileOutputStream(filename))) {
			if (tex) {
				out.print(getHistoryAsTex(includeDetails));
			} else {
				out.print(getHistory(includeDetails));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the history.
	 *
	 * @param includeDetails the include details
	 * @return the history
	 */
	public String getHistory(boolean includeDetails) {
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (String entry : history) {
			sb.append(entry + "\n");
			if (includeDetails) {
				if (historyDetails.size() > index) {
					sb.append(historyDetails.get(index++) + "\n");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Read T mfrom file.
	// ließt TM von Datei ein
	 *
	 * @param path the path
	 */
	public void readTMfromFile(String path) {
		LinkedList<String> states = new LinkedList<>();
		LinkedList<String> transitions = new LinkedList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			for (String line; (line = br.readLine()) != null;) {
				if (TM2generator.STATES.equals(line)) {
					while ((line = br.readLine()) != null && !"transitions".equals(line) && !"".equals(line)) {
						states.add(line);
					}
				} else if (TM2generator.TRANSITIONS.equals(line)) {
					while ((line = br.readLine()) != null && !"symbols".equals(line) && !"".equals(line)) {
						transitions.add(line);
					}
				} else if (TM2generator.TAPE.equals(line)) {
					tape.readTapeFromString(br.readLine());
				}
			}

			String[] statesArray = states.toArray(new String[states.size()]);
			String[] transitionsArray = transitions.toArray(new String[transitions.size()]);

			setTransitionMap(statesArray, transitionsArray);

		} catch (Exception e) {
			System.err.println("Failed to read TM from File!" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Runs a tm.
	 *
	 * @param timeoutMili            TESTMETHODE
	 * @param texOut the tex out
	 */
	public void run(long timeoutMili, boolean texOut) {
		if (this.currentState.equals(null) || this.nrOfStates == 0 || this.tape.equals(null)
				|| this.transitions.equals(null)) {
			throw new IllegalArgumentException("Unable to run this TM!");
		}
		if (nrOfStates == 2) {
			runTM(timeoutMili, true, texOut);
		} else {
			runTM(timeoutMili, false, false);
		}
	}

	/**
	 * Sets the transition map.
	 *
	// erstellt Übergangs-Map aus eingelesenen Übergängen
	 * @param states the states
	 * @param transitionsArray the transitions array
	 */
	private void setTransitionMap(String[] states, String[] transitionsArray) {
		boolean first = true;
		nrOfStates = states.length;
		for (String stateName : states) {
			stateName.trim();
			State s = new State(stateName);
			transitions.put(s, new HashMap<String, Transition>());
			if (first) {
				currentState = s;
				first = false;
			}
		}

		// jeder Übergang wird zunäachst normiert und anschließend in Map mit übergängen
		// gespeichert
		for (String transitionString : transitionsArray) {

			String[] tr = transitionString.replace("\t", " ").split(" ");

			State oldState = null;
			State newState = null;
			boolean foundOldState = false;
			boolean foundNewState = false;
			Iterator<State> i = transitions.keySet().iterator();

			while (i.hasNext()) {
				State curr = i.next();
				if (curr.getName().equals(tr[OLD_STATE].trim())) {
					oldState = curr;
					foundOldState = true;
				}
				if (curr.getName().equals(tr[NEW_STATE].trim())) {
					newState = curr;
					foundNewState = true;
				}
				if (foundNewState && foundOldState) {
					break;
				}
			}

			if (!foundNewState || !foundOldState) {
				System.err
						.println("Error!\nFound old State:\t" + foundOldState + "\nFound new State:\t" + foundNewState);
				throw new IllegalArgumentException();
			}

			if (debug) {
				System.out.println("Transition: " + Arrays.toString(tr));
				System.out.println("Map before: " + transitions.get(oldState).entrySet().toString());
			}

			// wenn neuer Übergang noch nicht exisitiert, wird er hinzugefügt
			if (transitions.get(oldState).put(tr[OLD_SYMBOL],
					new Transition(newState, tr[NEW_SYMBOL].trim(), tr[DIRECTION].trim())) == null) {
				if (debug) {
					System.out.println("Map after: " + transitions.get(oldState).entrySet().toString());
				}

				// andernfalls wird überprüft ob neuer Übergang von altem abweicht, falls ja ist
				// TM nicht deterministisch und kann nicht durch diesen Simulator simuliert
				// werden.
			} else {
				System.err.println("Current transition:\t" + transitions.get(oldState).get(tr[OLD_SYMBOL]).toString()
						+ "\nNew transition:\t" + new Transition(newState, tr[NEW_SYMBOL], tr[DIRECTION]).toString());
				if (transitions.get(oldState).get(tr[OLD_SYMBOL]).toString()
						.equals(new Transition(newState, tr[NEW_SYMBOL], tr[DIRECTION]).toString())) {
					System.out.println("Transitions matching, should be ok.");
				} else {
					throw new IllegalArgumentException("TM nondeterministic !!!");
				}

			}

		}

	}

	/**
	 * Step.
	// führ einen schritt der TM aus
	 */
	private void step() {

		// zu beginn wird Übergang aus Map geladen, falls kein Übergang exisitiert wird
		// abgebrochen.
		Transition t = transitions.get(currentState).get(tape.getCurrentSymbol());
		if (t == null || t.equals(null)) {
			System.out.println(
					"Current state: " + currentState.getName() + "\nCurrent symbol: " + tape.getCurrentSymbol());
			terminated = true;
			throw new NullPointerException("No existing transition for current state and symbol found!");
		}

		// anschließend wird aktueller Schritt vollzogen und das Band bewegt
		tape.setCurrentSymbol(t.getSymbolOut());
		if (t.getDirection().equals("R")) {
			tape.moveRight();
		} else if (t.getDirection().equals("L")) {
			tape.moveLeft();
		} else {
			System.err.println("Standing still currently not tested...");
		}

		currentState = t.getNewState();

		// wenn aktueller Zustand final ist, wird die Simulation beendet.
		if (currentState.isFinal()) {
			terminated = true;
		}
	}

	/**
	 * Gets the history as tex.
	 *
	 * @param includeDetails the include details
	 * @return the history as tex
	 */
	private String getHistoryAsTex(boolean includeDetails) {
		StringBuilder sb = new StringBuilder();
		int index = 0;
	
		for (String entry : history) {
			sb.append(entry + "\n");
			if (includeDetails) {
				if (historyDetails.size() > index) {
					sb.append(historyDetails.get(index++) + "\n");
				}
			}
		}
	
		sb.append("\\end{document}");
	
		return sb.toString();
	}

	/**
	 * Run TM.
	// Führt eine TM vollstaändig aus
	 *
	 * @param timeoutMili the timeout mili
	 * @param twoStates the two states
	 * @param texOutput the tex output
	 */
	private void runTM(long timeoutMili, boolean twoStates, boolean texOutput) {

		if (transitions.get(currentState).isEmpty()) {
			System.out.println("No transitions! unable to simulate TM!\n");
			return;
		}

		System.out.println("Starting TM with Tape:");
		if (texOutput) {
			history.add(Utilities.buildStaticTex());
			history.add(tape.toString2States(texOutput) );
		} else {
			history.add(tape.toString());
		}
		System.out.println(tape.toString() + "\n\n");

		// Solange TM nicht terminiert, werden einzeln Schritte vollzogen und der
		// Verlauf gespeichert
		while (!terminated) {
			if (texOutput) {
				if (Tape.LATEX_ESCAPE_SYMBOLS.contains(tape.getCurrentSymbol())) {
					historyDetails.add("Current state: " + currentState.getName() + "\t Current Symbol: \t $\\"
							+ State.getNameAsTex(tape.getCurrentSymbol()) + "$\\\\");
				} else {
					historyDetails.add("Current state: " + currentState.getName() + "\t Current Symbol: \t $"
							+ State.getNameAsTex(tape.getCurrentSymbol()) + "$\\\\");
				}
			} else {
				historyDetails.add("Current state: " + currentState.getName() + "\t Current Transition: \t"
						+ transitions.get(currentState).get(tape.getCurrentSymbol()));
			}
			if (debug) {
				System.out.println("Current state: " + currentState.getName());
				System.out.println("Transition: " + transitions.get(currentState).get(tape.getCurrentSymbol()) + "\n");
			}
			try {
				step();
				TimeUnit.MILLISECONDS.sleep(timeoutMili);
			} catch (IllegalArgumentException | InterruptedException | NullPointerException e) {
				if (!(e instanceof NullPointerException)) {
					historyDetails.add("Failed to simulate TM!\n" + e.getMessage());
					System.err.println("Failed to simulate TM!\n" + e.getMessage());
					break;
				}
			}
			if (!twoStates) {
				history.add(tape.toString());
			} else {
				history.add(tape.toString2States(texOutput));
			}

			System.out.println(tape.toString());

		}

		// Nachdem TM terminiert, wird entschieden ob eingabe akzeptiert oder abgelehnt
		// wird
		String output = "TM terminated!\t";

		if (twoStates) {
			// in der TM mit 2 Zuständen befindet sich diese Information im aktuellen Symbol
			// und muss erst extrahiert werden.
			State s = new State(tape.getCurrentSymbol().split(";")[1]);
			if (s.isAccepting()) {
				output += "TM accepts input";
			} else if (s.isDeclining()) {
				output += "TM declines input";
			} else {
				output += "TM seems to have failed. There was no next step possible due to missing transition.";
			}

		} else {
			if (currentState.isAccepting()) {
				output = output + "TM accepts input!";
			} else if (currentState.isDeclining()) {
				output = output + "TM declines input!";
			} else if (currentState.isFinal()) {
				output = output + "TM terminates as planned!";
			} else {
				output += "TM seems to have failed. There was no next step possible due to missing transition.";
			}
		}

		history.add(output);
		System.out.println(output);

	}

}
