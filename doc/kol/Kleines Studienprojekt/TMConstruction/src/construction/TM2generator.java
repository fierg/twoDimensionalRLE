package construction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import interpreter.Tape;
import utils.Utilities;

/**
 * Generiert eine TM mit nur 2 Zuständen aus einer gegeben TM (im .tur Format)
 *
 */
public class TM2generator {

	// statische indizes der Symbole in den Transitionarrays
	/** The Constant OLD_STATE. */
	public static final int OLD_STATE = 0;

	/** The Constant NEW_STATE. */
	public static final int NEW_STATE = 1;

	/** The Constant OLD_SYMBOL. */
	public static final int OLD_SYMBOL = 2;

	/** The Constant NEW_SYMBOL. */
	public static final int NEW_SYMBOL = 3;

	/** The Constant DIRECTION. */
	public static final int DIRECTION = 4;

	// statische Namen der flags in den .tur Dateien
	/** The Constant STATES. */
	public static final String STATES = "states";

	/** The Constant TRANSITIONS. */
	public static final String TRANSITIONS = "transitions";

	/** The Constant SYMBOLS. */
	public static final String SYMBOLS = "symbols";

	/** The Constant TAPE. */
	public static final String TAPE = "tape";

	/** The Constant ALPHA. */
	// Namen für die beiden Zustände nach dem Umwandeln
	public static final String ALPHA = "q0";

	/** The Constant BETA. */
	public static final String BETA = "q1";

	// hält alle komlexen Symbole die für neue TM benötigt werden
	/** The comp symbol table. */
	private String[][] compSymbolTable;

	// Zustände, Aphabet und Übergänge der ausgangs TM
	/** The sigma. */
	private String[] sigma;

	/** The states. */
	private String[] states;

	/** The transitions. */
	private String[] transitions;

	// Neue Übergänge der TM mit 2 Zständen
	/** The transitions new. */
	private LinkedList<String> transitionsNew;

	// Tape wird in neue .tur kopiert
	/** The tape. */
	private String tape;

	/**
	 * The main method.
	// Generator lässt sich separat auf eine .tur datei anwenden und erstellt eine
	// äquivalente TM mit 2 Zuständen
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		if (args.length >= 1) {
			String filename = args[0];
			TM2generator gen = new TM2generator(args[0]);
			gen.generate2StateTM();
			if (args.length == 2 && "-debug".equals(args[1])) {
				System.out.println("Transitions:\n\n");
				gen.printTransitions();
			}
			gen.writeTM2toFile(filename.split(".tur")[0] + "_2S" + ".tur");
		} else {
			System.out.println("Usage: <path> (.tur file needed!) <-debug>");
		}
	}

	/**
	 * Instantiates a new TM 2 generator.
	// Konstruktor
	 *
	 * @param path
	 *            the path
	 */
	public TM2generator(String path) {
		readTMfromFile(path);
	}

	/**
	 * Generate 2 state TM.
	// generiert aus eingelesener TM eine 2 state TM
	 */
	public void generate2StateTM() {
		if (sigma == null || states == null || transitions == null || tape == null || tape == "") {
			throw new IllegalArgumentException("sigma, states, tape or transitions equals null!");
		}
		// erstellt Tabelle mit komplexen Symbolen
		generateComSymbolTable();
		// erstellt die zusätzlichen Übergänge nach Shannons Konstruktion
		generateCompTransitions();
		// erstellt herkömmliche Übergänge
		generateNativeTransitions();
		// passt Startsymbol an
		modifyInitialSymbol();

	}

	/**
	 * Prints the transitions.
	// gibt neu generierte Übergänge aus
	 */
	public void printTransitions() {
		for (String string : transitionsNew) {
			System.out.println(string);
		}
	}

	/**
	 * Write TM 2 to file.
	 *
	// schriebt die modifizierten Übergänge in eine Datei
	 * @param filename
	 *            the filename
	 */
	public void writeTM2toFile(String filename) {
		try (PrintStream out = new PrintStream(new FileOutputStream(filename))) {
			out.print(get2StateTM());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the 2 state TM.
	 *
	 * @return the 2 state TM

	 * erstellt .tur Datei zur erstellten TM mit 2 Zuständen dazu verwendetes Schema
	 * in FORMAT_EXAMPLE.tur beschrieben
	 */
	public String get2StateTM() {
		StringBuilder sb = new StringBuilder();

		sb.append(STATES + "\n" + ALPHA + "\n" + BETA + "\n\n");

		sb.append(TRANSITIONS + "\n");
		for (String string : transitionsNew) {
			sb.append(string);
			sb.append("\n");
		}
		sb.append("\n" + SYMBOLS + "\n");
		for (String symbol : sigma) {
			sb.append(symbol);
			sb.append(" ");
		}
		for (String[] symbols : compSymbolTable) {
			for (String symbol : symbols) {
				sb.append(symbol);
				sb.append(" ");
			}
		}

		sb.append("\n\n" + TAPE + "\n");
		sb.append(this.tape);

		return sb.toString();
	}

	/**
	 * Read T mfrom file.
	 *
	 * @param path
	 *            the path
	
	 * ließt eine TM im .tur Format ein
	 */
	private void readTMfromFile(String path) {
		LinkedList<String> states = new LinkedList<>();
		LinkedList<String> transitions = new LinkedList<>();
		String[] symbols = null;

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			for (String line; (line = br.readLine()) != null;) {
				if (STATES.equals(line)) {
					while ((line = br.readLine()) != null && !"transitions".equals(line) && !"".equals(line)) {
						states.add(line);
					}
				} else if (TRANSITIONS.equals(line)) {
					while ((line = br.readLine()) != null && !"symbols".equals(line) && !"".equals(line)) {
						transitions.add(line);
					}
				} else if (SYMBOLS.equals(line)) {
					line = br.readLine();
					symbols = line.split(" ");
				} else if (TAPE.equals(line)) {
					this.tape = br.readLine();
				}
			}

			this.transitionsNew = new LinkedList<>();
			this.states = states.toArray(new String[states.size()]);
			this.sigma = symbols;
			this.transitions = transitions.toArray(new String[transitions.size()]);

		} catch (IOException e) {
			System.err.println("Failed to read TM from File!");
			e.getMessage();
		}
	}

	/**
	 * Generate symbol array.
	 *
	 * erstellt zu einem nativen Symbol alle komplexen Symbole mit allen Zuständen
	 * und weiteren Informationen
	 * 
	 * @param symbol
	 *            the symbol
	 * @param states
	 *            the states
	 * @return the string[]
	 */
	private String[] generateSymbolArray(String symbol, String[] states) {
		LinkedList<String> result = new LinkedList<>();

		/*
		 * Ein Symbol exisitiert pro Zustand 4 mal, mit Informationsüberschuss/defizit
		 * im Feld und in beiden Richtungen siehe 4*m*n neue Symbole im Paper von
		 * Shannon
		 */
		for (String state : states) {
			result.add(new ComplexSymbol(symbol, state, ComplexSymbol.INFORMATION_MINUS, ComplexSymbol.DIRECTION_R)
					.toString());
			result.add(new ComplexSymbol(symbol, state, ComplexSymbol.INFORMATION_MINUS, ComplexSymbol.DIRECTION_L)
					.toString());
			result.add(new ComplexSymbol(symbol, state, ComplexSymbol.INFORMATION_PLUS, ComplexSymbol.DIRECTION_R)
					.toString());
			result.add(new ComplexSymbol(symbol, state, ComplexSymbol.INFORMATION_PLUS, ComplexSymbol.DIRECTION_L)
					.toString());
		}

		return result.toArray(new String[4 * states.length]);

	}

	/**
	 * Generate com symbol table. erstellt für jedes Symbol alle komplexen Symbole
	 *
	 * @return the string[][]
	 */
	private String[][] generateComSymbolTable() {
		compSymbolTable = new String[sigma.length][];

		for (int index = 0; index < sigma.length; index++) {
			compSymbolTable[index] = generateSymbolArray(sigma[index], states);
		}

		return compSymbolTable;
	}

	/**
	 * Generate native transitions. generiert Übergänge nach Gleichung (6) wie im
	 * Paper beschrieben zuerst werden exisitierende Übergänge eingelesen,
	 * anschließend wird für jeden dieser Übergänge ein neuer Übergang erstellt der
	 * zwischen den neuen Symbolen gültig ist.
	 */
	private void generateNativeTransitions() {

		for (String trans : transitions) {

			// native Übergänge sind nur aus zustand ALPHA möglich
			String result = ALPHA + "\t";
			String[] transArray = trans.split(" ");

			int oldSymbolIndex = Utilities.indexOf(sigma, transArray[OLD_SYMBOL]);
			int oldStateIndex = Utilities.indexOf(states, transArray[OLD_STATE]);
			int newSymbolIndex = Utilities.indexOf(sigma, transArray[NEW_SYMBOL]);
			int newStateIndex = Utilities.indexOf(states, transArray[NEW_STATE]);

			// je nach Richtung des Übergangs ist Folgezustand entweder ALPHA oder BETA
			if ("R".equals(transArray[DIRECTION])) {
				try {
					// Übergang vom alten symbol mit R im Index
					result = result + BETA + "\t";
					result = result + compSymbolTable[oldSymbolIndex][oldStateIndex * 4] + "\t";
					result = result + compSymbolTable[newSymbolIndex][newStateIndex * 4 + 2] + "\t";
					result = result + "R\t";

					transitionsNew.add(result);

					// übergang mit altem Symbol mit L im Index
					result = ALPHA + "\t" + BETA + "\t";
					result = result + compSymbolTable[oldSymbolIndex][oldStateIndex * 4 + 1] + "\t";
					result = result + compSymbolTable[newSymbolIndex][newStateIndex * 4 + 2] + "\t";
					result = result + "R\t";

				} catch (ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException("invalid transition!");
				}
			} else if ("L".equals(transArray[DIRECTION])) {
				try {
					// Übergang vom alten symbol mit R im Index
					result = ALPHA + "\t" + ALPHA + "\t";
					result = result + compSymbolTable[oldSymbolIndex][oldStateIndex * 4] + "\t";
					result = result + compSymbolTable[newSymbolIndex][newStateIndex * 4 + 3] + "\t";
					result = result + "L\t";

					transitionsNew.add(result);

					// Übergang vom alten symbol mit L im Index
					result = ALPHA + "\t" + ALPHA + "\t";
					result = result + compSymbolTable[oldSymbolIndex][oldStateIndex * 4 + 1] + "\t";
					result = result + compSymbolTable[newSymbolIndex][newStateIndex * 4 + 3] + "\t";
					result = result + "L\t";
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException("invalid transition! " + e.getCause());
				}
			} else {
				throw new IllegalArgumentException("invalid transition! direction of transition was not L or R!");
			}

			transitionsNew.add(result);
		}
	}

	/**
	 * Generate comp transitions.
	 */
	private void generateCompTransitions() {

		/*
		 * generiere Übergänge nach Gleichung (1) für jedes native Symbol exisitiert ein
		 * Übergang in das erste komplexes Symbol, aus Zustand ALPHA mit Kopfbewegung
		 * nach R
		 */
		for (int index = 0; index < sigma.length; index++) {
			transitionsNew.add(ALPHA + "\t" + ALPHA + "\t" + sigma[index] + "\t" + compSymbolTable[index][0] + "\tR");
		}

		/*
		 * generiere Übergänge nach Gleichung (2) für jedes native Symbol exisitiert ein
		 * Übergang in ein komplexes Symbol, aus Zustand BETA mit Kopfbewegung nach L
		 */
		for (int index = 0; index < sigma.length; index++) {
			transitionsNew.add(BETA + "\t" + ALPHA + "\t" + sigma[index] + "\t" + compSymbolTable[index][1] + "\tL");
		}

		/*
		 * generiere Übergänge nach Gleichung (3) aus jedem komplexen Symbol exisitiert
		 * ein Übergang aus BETA mit dem komplexen Symbol mit höherem Zustannd im Index
		 * als neues Symbol. Dies befindet sich 4 indizes weiter in der compSymbolTable.
		 * Übergänge exisitieren in beide Richtungen.
		 */
		for (int symbolclass = 0; symbolclass < compSymbolTable.length; symbolclass++) {
			for (int index = 0; index < compSymbolTable[symbolclass].length - 4; index = index + 4) {
				transitionsNew.add(BETA + "\t" + ALPHA + "\t" + compSymbolTable[symbolclass][index] + "\t"
						+ compSymbolTable[symbolclass][index + 4] + "\tR");
			}
		}
		for (int symbolclass = 0; symbolclass < compSymbolTable.length; symbolclass++) {
			for (int index = 1; index < compSymbolTable[symbolclass].length - 4; index = index + 4) {
				transitionsNew.add(BETA + "\t" + ALPHA + "\t" + compSymbolTable[symbolclass][index] + "\t"
						+ compSymbolTable[symbolclass][index + 4] + "\tL");
			}
		}

		/*
		 * generiere Übergänge nach Gleichung (4) aus jedem komplexen Symbol exisitiert
		 * ein Übergang aus beiden Zuständen, mit dem komplexen Symbol mit niedirigerem
		 * Zustannd im Index als neues Symbol. Dies befindet sich 4 indizes vorher in
		 * der compSymbolTable. Übergänge exisitieren zudem in beide Richtungen,
		 * folglich gibt es 4 neue Übergänge pro Symbol pro Zustand
		 */
		for (int symbolclass = 0; symbolclass < compSymbolTable.length; symbolclass++) {
			for (int index = 6; index < compSymbolTable[symbolclass].length; index = index + 4) {
				transitionsNew.add(ALPHA + "\t" + BETA + "\t" + compSymbolTable[symbolclass][index] + "\t"
						+ compSymbolTable[symbolclass][index - 4] + "\tR");
			}
		}
		for (int symbolclass = 0; symbolclass < compSymbolTable.length; symbolclass++) {
			for (int index = 7; index < compSymbolTable[symbolclass].length; index = index + 4) {
				transitionsNew.add(ALPHA + "\t" + BETA + "\t" + compSymbolTable[symbolclass][index] + "\t"
						+ compSymbolTable[symbolclass][index - 4] + "\tL");
			}
		}

		for (int symbolclass = 0; symbolclass < compSymbolTable.length; symbolclass++) {
			for (int index = 6; index < compSymbolTable[symbolclass].length; index = index + 4) {
				transitionsNew.add(BETA + "\t" + BETA + "\t" + compSymbolTable[symbolclass][index] + "\t"
						+ compSymbolTable[symbolclass][index - 4] + "\tR");
			}
		}
		for (int symbolclass = 0; symbolclass < compSymbolTable.length; symbolclass++) {
			for (int index = 7; index < compSymbolTable[symbolclass].length; index = index + 4) {
				transitionsNew.add(BETA + "\t" + BETA + "\t" + compSymbolTable[symbolclass][index] + "\t"
						+ compSymbolTable[symbolclass][index - 4] + "\tL");
			}
		}

		/*
		 * generiere Übergänge nach Gleichung(5) aus einem komplexen Symbol mit zustand
		 * 1 (q0) exisitiert ein Übergang in das native Symbol Dieser Übergang
		 * exisitiert aus beiden Zuständen und beiden Richtungen
		 */

		for (int index = 0; index < compSymbolTable.length; index++) {
			transitionsNew.add(ALPHA + "\t" + ALPHA + "\t" + compSymbolTable[index][2] + "\t" + sigma[index] + "\tR");
			transitionsNew.add(BETA + "\t" + ALPHA + "\t" + compSymbolTable[index][2] + "\t" + sigma[index] + "\tR");
			transitionsNew.add(ALPHA + "\t" + ALPHA + "\t" + compSymbolTable[index][3] + "\t" + sigma[index] + "\tL");
			transitionsNew.add(BETA + "\t" + ALPHA + "\t" + compSymbolTable[index][3] + "\t" + sigma[index] + "\tL");
		}
	}

	/**
	 * Modify initial symbol. in TM mit 2 Zuständen muss das Startsymbol angepasst
	 * werden in neuer TM muss das Symbol unter dem Lesekopf in das äquivalente
	 * komplexe Symbol umgewandelt werden mit dem Startzustand der alten TM im index
	 */
	private void modifyInitialSymbol() {
		if (tape.trim().equals("") || tape.equals(null)) {
			throw new IllegalArgumentException("Tape is empty!");
		}
		Tape t = new Tape();
		t.readTapeFromString(tape);

		t.setCurrentSymbol(compSymbolTable[0][0]);

		tape = t.toString();
	}

}
