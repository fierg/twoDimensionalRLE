package interpreter;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * The Class Tape. Simuliert das Band einer TM mit einem aktuellen Symbol und
 * den Bewegungen L und R.
 */
public class Tape {

	/** The Constant LATEX_ESCAPE_SYMBOLS. */
	public static final Set<String> LATEX_ESCAPE_SYMBOLS = new HashSet<String>(
			Arrays.asList(new String[] { "#", "\\", "$" }));

	/** The Constant COMP_SYMBOL_Q. */
	private static final String COMP_SYMBOL_Q = "q";

	/** The Constant COMP_SYMBOL_S. */
	private static final String COMP_SYMBOL_S = "s";

	/** The Constant REGEX_Q. */
	private static final String REGEX_Q = "(.+;.+;[+];.+)";

	/** The Constant REGEX_S. */
	private static final String REGEX_S = "(.+;.+;[-];.+)";

	/** The left tape. */
	private Stack<String> leftTape;

	/** The right tape. */
	private Stack<String> rightTape;

	/** The current symbol. */
	private String currentSymbol;

	/** The q symbol. */
	private String qSymbol;

	/** The s symbol. */
	private String sSymbol;

	/**
	 * Instantiates a new tape.
	 */
	public Tape() {
		leftTape = new Stack<>();
		rightTape = new Stack<>();
		currentSymbol = "";
	}

	/**
	 * Move left.
	 */
	public void moveLeft() {
		rightTape.push(currentSymbol);
		if (leftTape.isEmpty()) {
			currentSymbol = "#";
		} else {
			currentSymbol = leftTape.pop();
		}
	}

	/**
	 * Move right.
	 */
	public void moveRight() {
		leftTape.push(currentSymbol);
		if (rightTape.isEmpty()) {
			currentSymbol = "#";
		} else {
			currentSymbol = rightTape.pop();
		}
	}

	/**
	 * Read tape from string.
	 *
	 * @param tape
	 *            the tape
	 */
	// konstruiert ein Tape aus einem String
	public void readTapeFromString(String tape) {
		String[] tapeArray = tape.split(" ");
		int index = 0;

		try {
			while (!tapeArray[index].equals("[")) {
				leftTape.push(tapeArray[index]);
				index++;
			}

			currentSymbol = tapeArray[index + 1];
			index = tapeArray.length - 1;

			while (!tapeArray[index].equals("]")) {
				rightTape.push(tapeArray[index]);
				index--;
			}

		} catch (IndexOutOfBoundsException e) {
			System.err.println("malformed Tape-Description!");
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder tape = new StringBuilder();
		tape.append("# # ");

		for (int i = 0; i < leftTape.size(); i++) {
			tape.append(leftTape.elementAt(i) + " ");
		}

		tape.append("[ ");
		tape.append(currentSymbol + " ");
		tape.append("] ");

		for (int i = rightTape.size() - 1; i >= 0; i--) {
			tape.append(rightTape.elementAt(i) + " ");
		}

		tape.append("# # ");

		return tape.toString();
	}

	/**
	 * To string 2 states.
	 *
	 * @param texOutput
	 *            the tex output
	 * @return the string
	 */
	public String toString2States(boolean texOutput) {
		StringBuilder tape = new StringBuilder();
		if (texOutput) {
			tape.append("$");
			tape.append("\\# \\#");
		} else {
			tape.append("# # ");
		}

		for (int i = 0; i < leftTape.size(); i++) {
			if (Pattern.matches(REGEX_Q, leftTape.elementAt(i))) {
				tape.append(COMP_SYMBOL_Q + " ");
				qSymbol = leftTape.elementAt(i);
			} else if (Pattern.matches(REGEX_S, leftTape.elementAt(i))) {
				tape.append(COMP_SYMBOL_S + " ");
				sSymbol = leftTape.elementAt(i);
			} else if (texOutput && LATEX_ESCAPE_SYMBOLS.contains(leftTape.elementAt(i))) {
				tape.append("\\" + leftTape.elementAt(i) + " ");
			} else {
				tape.append(leftTape.elementAt(i) + " ");
			}
		}

		tape.append("[ ");

		if (Pattern.matches(REGEX_Q, currentSymbol)) {
			tape.append(COMP_SYMBOL_Q + " ");
			qSymbol = currentSymbol;
		} else if (Pattern.matches(REGEX_S, currentSymbol)) {
			tape.append(COMP_SYMBOL_S + " ");
			sSymbol = currentSymbol;
		} else if (texOutput && Tape.LATEX_ESCAPE_SYMBOLS.contains(currentSymbol)) {
			tape.append("\\" + currentSymbol + " ");
		} else {
			tape.append(currentSymbol + " ");
		}

		tape.append("] ");

		for (int i = rightTape.size() - 1; i >= 0; i--) {
			if (Pattern.matches(REGEX_Q, rightTape.elementAt(i))) {
				tape.append(COMP_SYMBOL_Q + " ");
				qSymbol = rightTape.elementAt(i);
			} else if (Pattern.matches(REGEX_S, rightTape.elementAt(i))) {
				tape.append(COMP_SYMBOL_S + " ");
				sSymbol = rightTape.elementAt(i);
			} else if (texOutput && LATEX_ESCAPE_SYMBOLS.contains(rightTape.elementAt(i))) {
				tape.append("\\" + rightTape.elementAt(i) + " ");
			} else {
				tape.append(rightTape.elementAt(i) + " ");
			}
		}

		if (!texOutput) {
			tape.append("# # ");
			if (qSymbol != null) {
				tape.append("\nComplexSymbol " + COMP_SYMBOL_Q + " = " + qSymbol);
			}
			if (sSymbol != null) {
				tape.append("\nComplexSymbol " + COMP_SYMBOL_S + " = " + sSymbol);
			}
		} else {
			tape.append("\\# \\# $ \\\\");
			if (qSymbol != null) {
				tape.append("\nComplexSymbol " + COMP_SYMBOL_Q + " = $" + State.getNameAsTex(qSymbol) + "$ \\\\");
			}
			if (sSymbol != null) {
				tape.append("\nComplexSymbol " + COMP_SYMBOL_S + " = $" + State.getNameAsTex(sSymbol)
						+ "$ \\\\");
			}
			tape.append( "\n \\medskip");
		}
		qSymbol = null;
		sSymbol = null;
		return tape.toString();
	}

	/**
	 * Gets the current symbol.
	 *
	 * @return the current symbol
	 */
	public String getCurrentSymbol() {
		return currentSymbol;
	}

	/**
	 * Sets the current symbol.
	 *
	 * @param currentSymbol
	 *            the new current symbol
	 */
	public void setCurrentSymbol(String currentSymbol) {
		this.currentSymbol = currentSymbol;
	}

}
