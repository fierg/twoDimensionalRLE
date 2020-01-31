package construction;

/**
 * Boilerplate-Code für die zusätzlich erstellten Symbole beim Umwandeln einer TM.
 */
public class ComplexSymbol {
	
	/** The Constant INFORMATION_PLUS. */
	public static final String INFORMATION_PLUS = "+";
	
	/** The Constant INFORMATION_MINUS. */
	public static final String INFORMATION_MINUS = "-";
	
	/** The Constant DIRECTION_R. */
	public static final String DIRECTION_R = "R";
	
	/** The Constant DIRECTION_L. */
	public static final String DIRECTION_L = "L";
	
	/** The symbol. */
	private String symbol;
	
	/** The state. */
	private String state;
	
	/** The information. */
	private String information;
	
	/** The direction. */
	private String direction;

	/**
	 * Instantiates a new complex symbol.
	 *
	 * @param symbol the symbol
	 * @param state the state
	 * @param information the information
	 * @param direction the direction
	 */
	public ComplexSymbol(String symbol, String state, String information, String direction) {
		this.symbol = symbol;
		this.state = state;
		this.information = information;
		this.direction = direction;
	}

	/**
	 * Gets the symbol.
	 *
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Sets the symbol.
	 *
	 * @param symbol the new symbol
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Gets the information.
	 *
	 * @return the information
	 */
	public String getInformation() {
		return information;
	}

	/**
	 * Sets the information.
	 *
	 * @param information the new information
	 */
	public void setInformation(String information) {
		this.information = information;
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
	 * @param direction the new direction
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return symbol + ";" + state + ";" + information + ";" + direction;
	}
}
