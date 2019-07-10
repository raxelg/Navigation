package com.rtg.navigationwtd;

public class btInstructions {

    public static char read_instructions(String strInstructions) {
        char instruction = ' ';
        if ("Make a slight left".equals(strInstructions)) { instruction = 'e'; }
        else if ("Make a slight right".equals(strInstructions)) { instruction = 'f'; }
        else if ("Make a sharp left".equals(strInstructions)) { instruction = 'g'; }
        else if ("Make a sharp right".equals(strInstructions)) { instruction = 'h'; }
        else if ("Turn left".equals(strInstructions)) { instruction = 'c'; }
        else if ("Turn right".equals(strInstructions)) { instruction = 'd'; }
        else if ("U turn".equals(strInstructions)) { instruction = 'i'; }
        else if ("Go north".equals(strInstructions)) { instruction = 'a'; }
        else if ("Go northwest".equals(strInstructions)) { instruction = 'a'; }
        else if ("Go northeast".equals(strInstructions)) { instruction = 'a'; }
        else if ("Go south".equals(strInstructions)) { instruction = 'a'; }
        else if ("Go southeast".equals(strInstructions)) { instruction = 'a'; }
        else if ("Go southwest".equals(strInstructions)) { instruction = 'a'; }
        else if ("Go west".equals(strInstructions)) { instruction = 'a'; }
        else if ("Go east".equals(strInstructions)) { instruction = 'a'; }
        else if ("You have arrived at your destination".equals(strInstructions)) { instruction = 'i'; }
        return instruction;
    }

}

