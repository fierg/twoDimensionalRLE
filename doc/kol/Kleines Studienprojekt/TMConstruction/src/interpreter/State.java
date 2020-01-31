package interpreter;

/**
 * The Class State.
 * Kapselt Zustände der TM und bestimmt ob Zustand final/akzeptierend/ablehnend

 */
public class State {

	/** The name. */
	private String name;
	
	/** The is final. */
	private boolean isFinal = false;
	
	/** The is accepting. */
	private boolean isAccepting = false;
	
	/** The is declining. */
	private boolean isDeclining = false;

	/**
	 * Instantiates a new state.
	 *
	 * @param name the name
	 */
	public State(String name) {
		this.name = name;
		if (name.endsWith("a")) {
			setAccepting();
		} else if (name.endsWith("d")) {
			setDeclining();
		} else if (name.endsWith("f")) {
			setFinal(true);
		}
	}

	/**
	 * Gets the name as tex.
	 *
	 * @param name the name
	 * @return the name as tex
	 */
	public static String getNameAsTex(String name) {
		try {
		String[] symbolArray = name.split(";");

		if (symbolArray.length > 2) {
			StringBuilder sb = new StringBuilder();
			if(Tape.LATEX_ESCAPE_SYMBOLS.contains(symbolArray[0])) {
				sb.append("\\");
				sb.append(symbolArray[0]);
			} else {
				sb.append(symbolArray[0]);
			}
			sb.append("_{");
			sb.append(symbolArray[1]);
			sb.append(",");
			sb.append(symbolArray[2]);
			sb.append(",");
			sb.append(symbolArray[3]);
			sb.append("}");
			
			return sb.toString();
		}
		}catch (NullPointerException e) {
		}
		return name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Checks if is final.
	 *
	 * @return true, if is final
	 */
	public boolean isFinal() {
		return isFinal;
	}

	/**
	 * Sets the final.
	 *
	 * @param isFinal the new final
	 */
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
		if (!isFinal) {
			isAccepting = false;
			isDeclining = false;
		}
	}

	/**
	 * Checks if is accepting.
	 *
	 * @return true, if is accepting
	 */
	public boolean isAccepting() {
		return isAccepting;
	}

	/**
	 * Sets the accepting.
	 */
	public void setAccepting() {
		this.isAccepting = true;
		this.isFinal = true;
		if (isDeclining) {
			isDeclining = false;
		}
	}

	/**
	 * Checks if is declining.
	 *
	 * @return true, if is declining
	 */
	public boolean isDeclining() {
		return isDeclining;
	}

	/**
	 * Sets the declining.
	 */
	public void setDeclining() {
		this.isDeclining = true;
		this.isFinal = true;
		if (isAccepting) {
			isAccepting = false;
		}
	}

}
