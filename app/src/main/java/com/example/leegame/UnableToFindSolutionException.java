package com.example.leegame;
/**
 * Is thrown when Lee algorithm can't solve map.
 *
 * @author Volodymyr Davybida
 */
public class UnableToFindSolutionException extends Exception{
    public UnableToFindSolutionException() {
        super("\nAlgorithm could not find solution for this map!");
    }
}
