package com.rtg.navigationwtd;

public class btInstructions {

    public static char read_instructions(String strInstructions) {
        char instruction = ' ';
        if ("Turn slight left".equals(strInstructions)) { instruction = 'e'; }
        else if ("Turn slight right".equals(strInstructions)) { instruction = 'f'; }
        else if ("Turn sharp left".equals(strInstructions)) { instruction = 'g'; }
        else if ("Turn sharp right".equals(strInstructions)) { instruction = 'h'; }
        else if ("Turn left".equals(strInstructions)) { instruction = 'c'; }
        else if ("Turn right".equals(strInstructions)) { instruction = 'd'; }
        else if ("Make a U-turn".equals(strInstructions)) { instruction = 'i'; }
        else if ("Continue".equals(strInstructions)) { instruction = 'a'; }
        else if ("Keep right".equals(strInstructions)) { instruction = 'a'; }
        else if ("Keep left".equals(strInstructions)) { instruction = 'a'; }
//        else if ("Go south".equals(strInstructions)) { instruction = 'a'; }
//        else if ("Go southeast".equals(strInstructions)) { instruction = 'a'; }
//        else if ("Go southwest".equals(strInstructions)) { instruction = 'a'; }
//        else if ("Go west".equals(strInstructions)) { instruction = 'a'; }
//        else if ("Go east".equals(strInstructions)) { instruction = 'a'; }
        else if ("Arrive at destination".equals(strInstructions)) { instruction = 'i'; }
        return instruction;
    }

}

