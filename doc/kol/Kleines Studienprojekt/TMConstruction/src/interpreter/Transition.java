package interpreter;

/**
 * The Class Transition.
 * 
 * hält alle relevanten Informationen nach Übergang
 */
public class Transition {

	/** The new state. */
	private State newState;

	/** The symbol out. */
	private String symbolOut;

	/** The direction. */
	private String direction;

	/**
	 * Instantiates a new transition.
	 *
	 * @param state
	 *            the state
	 * @param symbolOut
	 *            the symbol out
	 * @param direction
	 *            the direction
	 */
	public Transition(State state, String symbolOut, String direction) {
		this.newState = state;
		this.symbolOut = symbolOut;
		this.direction = direction;
	}

	/**
	 * Gets the new state.
	 *
	 * @return the new state
	 */
	public State getNewState() {
		return newState;
	}

	/**
	 * Sets the new state.
	 *
	 * @param newState
	 *            the new new state
	 */
	public void setNewState(State newState) {
		this.newState = newState;
	}

	/**
	 * Gets the symbol out.
	 *
	 * @return the symbol out
	 */
	public String getSymbolOut() {
		return symbolOut;
	}

	/**
	 * Sets the symbol out.
	 *
	 * @param symbolOut
	 *            the new symbol out
	 */
	public void setSymbolOut(String symbolOut) {
		this.symbolOut = symbolOut;
	}

	/**
	 * Gets the direction.
	 *
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * Sets the direction.
	 *
	 * @param direction
	 *            the new direction
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		StringBuilder s = new StringBuilder().append(newState.getName()).append(" ").append(symbolOut).append(" ")
				.append(direction);
		return s.toString();
	}

}
