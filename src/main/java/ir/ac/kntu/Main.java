package ir.ac.kntu;

import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;

/*
 *in this program after inputing loop commands you should enter a empty line or what you have input will be ignord.
 *this is for preventig disorder in printing outputs.
 */

public class Main {

    public static void main(String[] args) {
        HashMap<String, Integer> variables = new HashMap<>(16, 0.75f);
        while (true) {
            String command = getCommand();
            if (command.equals("") || command.matches("^\\s+$")) {
                continue;
            }
            switch (command.charAt(0)) {
                case '>':
                    input(command, variables);
                    break;
                case '<':
                    output(command, variables);
                    break;
                default:
                    if (command.equals("exit_0")) {
                        return;
                    } else if (command.matches("^beEzaye [a-zA-Z]+.+")) {
                        boolean checkForExit = loop(command, variables);
                        if (checkForExit) {
                            return;
                        }
                    } else {
                        replace(command, variables);
                    }
            }
        }
    }

    public static String getCommand() {
        Scanner scanner = new Scanner(System.in);
        String command = "";
        command = scanner.nextLine();
        command = command.trim();
        return command;
    }

    public static void input(String command, HashMap<String, Integer> variables) {
        String regex = "^> *[a-zA-Z]+ *$";
        command = checkStyle(command, regex, "input");
        if (command.equals("CheckStyle_Error")) {
            return;
        }
        Scanner scanner = new Scanner(System.in);
        String strValue = scanner.nextLine();
        boolean valueCheckStyle = strValue.matches("^ *[+-]?[0-9]+ *$");
        if (!valueCheckStyle) {
            System.out.println("Invalid input. Please declare the variable again.");
            return;
        }
        strValue = strValue.replaceAll("\\s", "");
        int value = Integer.parseInt(strValue);
        variables.put(command, value);
    }

    public static void output(String command, HashMap<String, Integer> variables) {
        String regex = "^< *[+-]? *([a-zA-Z]+|[0-9]+) *([+-] *([a-zA-Z]+|[0-9]+) *)* *$";
        command = checkStyle(command, regex, "output");
        if (command.equals("CheckStyle_Error")) {
            return;
        }
        String[] data = command.replaceFirst("[+-]", "").split("[+-]");
        boolean notDefined = printNotDefined(data, variables);
        if (notDefined) {
            return;
        }
        int outputValue = 0, counter = 0;
        for (int i = 0; i < command.length(); i++) {
            if (command.charAt(i) == '+') {
                outputValue += valueOf(data[counter], variables);
                counter++;
            } else if (command.charAt(i) == '-') {
                outputValue -= valueOf(data[counter], variables);
                counter++;
            }
        }
        System.out.println(outputValue);
    }

    public static void replace(String command, HashMap<String, Integer> variables) {
        String regex = "^[a-zA-Z]+ *= *[+-]? *([a-zA-Z]+|[0-9]+) *([+-] *([a-zA-Z]+|[0-9]+) *)* *$";
        command = checkStyle(command, regex, "replace");
        if (command.equals("CheckStyle_Error")) {
            return;
        }
        String[] data = command.split("\\W+");
        // It doesn't matter that the first element of array is defined or not so we
        // don't check it
        boolean notDefined = printNotDefined(Arrays.copyOfRange(data, 1, data.length), variables);
        if (notDefined) {
            return;
        }
        int replaceValue = 0, counter = 1;
        for (int i = 0; i < command.length(); i++) {
            if (command.charAt(i) == '+') {
                replaceValue += valueOf(data[counter], variables);
                counter++;
            } else if (command.charAt(i) == '-') {
                replaceValue -= valueOf(data[counter], variables);
                counter++;
            }
        }
        variables.put(data[0], replaceValue);
    }

    // this method returns true if user inputs exit_0. else returns false.
    public static boolean loop(String command, HashMap<String, Integer> variables) {
        String regex = "^beEzaye +[a-zA-Z]+ *= *([a-zA-Z]+|[+-]?[0-9]+) +ta +([a-zA-Z]+|[+-]?[0-9]+) *: *";
        command = checkStyle(command, regex, "loop");
        if (command.equals("CheckStyle_Error")) {
            return false;
        }
        String[] data = command.split("[=,]");
        boolean notDefined = printNotDefined(Arrays.copyOfRange(data, 1, 3), variables);
        if (notDefined) {
            return false;
        }
        String loopVariable = data[0];
        int start = valueOf(data[1], variables), end = valueOf(data[2], variables);
        variables.put(loopVariable, start);
        if (start < 0 || end < 0 || start >= end) {
            System.out.println("Inputed incorrect values for loop parameters");
            return false;
        }
        String[] loopCommands = getLoopCommands();
        if (loopCommands[loopCommands.length - 1].equals("exit_0")) {
            return true;
        }
        if (!loopCommandsCheckStyle(loopCommands) || !isloopCommandsVariablesDefined(loopCommands, variables)) {
            return false;
        }
        doLoopOperations(loopCommands, variables, loopVariable, start, end);
        return false;
    }

    public static void doLoopOperations(String[] loopCommands, HashMap<String, Integer> variables, String loopVariable,
            int start, int end) {
        for (int i = start; i < end; i++) {
            variables.put(loopVariable, i);
            for (int j = 0; j < loopCommands.length - 1; j++) {
                switch (loopCommands[j].charAt(0)) {
                    case '>':
                        input(loopCommands[j], variables);
                        break;
                    case '<':
                        output(loopCommands[j], variables);
                        break;
                    default:
                        replace(loopCommands[j], variables);
                }
            }
        }
    }

    public static String[] getLoopCommands() {
        String[] loopCommands = new String[50];
        int counter = 0;
        for (int i = 0; true; i++) {
            loopCommands[i] = getCommand();
            counter++;
            if (!loopCommands[i].matches("^\\*.*")) {
                break;
            }
            loopCommands[i] = loopCommands[i].substring(1); // removing '*'
            loopCommands[i] = loopCommands[i].trim();
        }
        String[] loopCommands2 = Arrays.copyOfRange(loopCommands, 0, counter);
        return loopCommands2;
        // the last command of array is not a loop command
    }

    public static boolean loopCommandsCheckStyle(String[] loopCommands) {
        if (loopCommands.length == 1) {
            System.out.println("Loop should have at least one statement");
            return false;
        }
        // command should match input or output or replace regex
        String regex = "(^> *[a-zA-Z]+ *$)" + "|" + "(^< *[+-]? *([a-zA-Z]+|[0-9]+) *([+-] *([a-zA-Z]+|[0-9]+) *)* *$)"
                + "|" + "(^[a-zA-Z]+ *= *[+-]? *([a-zA-Z]+|[0-9]+) *([+-] *([a-zA-Z]+|[0-9]+) *)* *$)";
        for (int i = 0; i < loopCommands.length - 1; i++) {
            if (!loopCommands[i].matches(regex)) {
                System.out.println("Invalid input for loop commands");
                return false;
            }
        }
        return true;
    }

    public static boolean isloopCommandsVariablesDefined(String[] loopCommands, HashMap<String, Integer> variables) {
        String[] loopCommandsCopy = makeAloopCommandsCopy(loopCommands);
        boolean isDefined = true;
        for (int i = 0; i < loopCommandsCopy.length - 1; i++) {
            loopCommands[i] = loopCommands[i].replaceAll("\\s", "");
            switch (loopCommandsCopy[i].charAt(0)) {
                case '>':
                    variables.put(loopCommandsCopy[i].substring(1), 0);
                    break;
                case '<':
                    loopCommandsCopy[i] = loopCommandsCopy[i].substring(1);
                    if (!loopCommandsCopy[i].matches("^[+-].+")) {
                        loopCommandsCopy[i] = '+' + loopCommandsCopy[i];
                    }
                    String[] data1 = loopCommandsCopy[i].replaceFirst("[+-]", "").split("[+-]");
                    if (printNotDefined(data1, variables)) {
                        isDefined = false;
                    }
                    break;
                default:
                    String[] data2 = loopCommandsCopy[i].split("\\W+");
                    if (printNotDefined(Arrays.copyOfRange(data2, 1, data2.length), variables)) {
                        isDefined = false;
                    }
            }
        }
        return isDefined;
    }

    public static String[] makeAloopCommandsCopy(String[] loopCommands) {
        //method has been used in 'isloopCommandsVariablesDefined'
        String[] loopCommandsCopy = new String[loopCommands.length];
        for (int i = 0; i < loopCommands.length; i++) {
            loopCommandsCopy[i] = loopCommands[i];
        }
        return loopCommandsCopy;
    }

    public static int valueOf(String str, HashMap<String, Integer> variables) {
        if (str.matches("[+-]?[0-9]+")) {
            return Integer.parseInt(str);
        }
        return variables.get(str);
    }

    public static String checkStyle(String command, String regex, String method) {
        if (!command.matches(regex)) {
            System.out.println("Invalid input");
            return "CheckStyle_Error";
        }
        if (method.equals("input")) {
            command = command.replaceAll("\\s", "").substring(1); // removing spaces and '>'
        }
        if (method.equals("output")) {
            command = command.replaceAll("\\s", "").substring(1); // removing spaces and '<'
            if (!command.matches("^[+-].+")) {
                command = '+' + command;
            }
        }
        if (method.equals("replace")) {
            command = command.replaceAll("\\s", "");
            for (int i = 0; i < command.length(); i++) {
                if (!command.matches("^.+=[+-].+") && command.charAt(i) == '=') {
                    String temp = command.substring(0, i + 1) + '+';
                    command = temp + command.substring(i + 1);
                    break;
                }
            }
        }
        if (method.equals("loop")) {
            command = command.substring("beEzaye".length()).replaceFirst(":", "");
            command = command.replaceFirst(" ta ", ",").replaceAll("\\s", "");
        }
        return command;
    }

    public static boolean printNotDefined(String[] data, HashMap<String, Integer> variables) {
        boolean notDefined = false;
        for (int i = 0; i < data.length; i++) {
            if (data[i].matches("[a-zA-Z]") && variables.get(data[i]) == null) {
                System.out.println(data[i] + " is not defined");
                notDefined = true;
            }
        }
        return notDefined;
    }
}