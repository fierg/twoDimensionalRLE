package com.unitrier.teaching.debugging.home;

final class Math {
	private Math() {
	}

	static double computePi(int iteration) {
		if (iteration > 0) {
			double piDividedBy4 = 0;
			int sign = 1;

			for (double i = 0; i <= iteration * 2; i += 2) {
				piDividedBy4 += sign * (1 / (i + 1));
				sign *= -1;
			}

			return piDividedBy4 * 4;
		} else {
			throw new IllegalArgumentException("Iterations cant be negative or 0!");
		}
	}
}