package com.rtg.navigationwtd;

public class btInstructions {

    public static String read_instructions(String strInstructions) {
        String instruction = "";
        if ("Turn slight left".equals(strInstructions)) { instruction = "eeeeeeeeee"; }
        else if ("Turn slight right".equals(strInstructions)) { instruction = "ffffffffff"; }
        else if ("Turn sharp left".equals(strInstructions)) { instruction = "gggggggggg"; }
        else if ("Turn sharp right".equals(strInstructions)) { instruction = "hhhhhhhhhhh"; }
        else if ("Turn left".equals(strInstructions)) { instruction = "cccccccccc"; }
        else if ("Turn right".equals(strInstructions)) { instruction = "dddddddddd"; }
        else if ("Continue".equals(strInstructions)) { instruction = "aaaaaaaaaaa"; }
        else if ("Keep right".equals(strInstructions)) { instruction = "aaaaaaaaaaa"; }
        else if ("Keep left".equals(strInstructions)) { instruction = "aaaaaaaaaaa"; }
        else if ("At roundabout, take exit 1".equals(strInstructions)) { instruction = "aaaaaaaaaaa"; }
//        else if ("Go southeast".equals(strInstructions)) { instruction = 'a'; }
//        else if ("Go southwest".equals(strInstructions)) { instruction = 'a'; }
//        else if ("Go west".equals(strInstructions)) { instruction = 'a'; }
//        else if ("Go east".equals(strInstructions)) { instruction = 'a'; }
        else if ("Arrive at destination".equals(strInstructions)) { instruction = "iiiiiiiiii"; }
        return instruction;
    }

}

