package com.unitrier.teaching.debugging.home;

import static java.lang.System.out;

public final class Main
{
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            usage();
            return;
        }
        try
        {
            int iterations = Integer.parseInt(args[0]);
            double result = Math.computePi(iterations);
            out.printf("Number of iterations = %d. Resulting approximation of pi = %f.\n", iterations, result);
        } catch (NumberFormatException e)
        {
            out.printf("Could not parse provided input to a valid integer value. %s\n", e.getMessage());
        }
    }

    private static void usage()
    {
        out.printf("Please provide an integer determining the number of iterations to use in the Leipniz formula.\n");
    }
} 
