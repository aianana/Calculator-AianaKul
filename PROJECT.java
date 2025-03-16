import java.util.*;
import org.fusesource.jansi.Ansi;
import static org.fusesource.jansi.Ansi.ansi;

public class Main {
    private static List<String> history = new ArrayList<>();

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println(ansi().fg(Ansi.Color.BLUE).a("Welcome to the Calculator!").reset());
        while (true) {
            System.out.println(ansi().fg(Ansi.Color.YELLOW)
                    .a("╔═══════════════════════════════════════════════════════════════╗")
                    .a("\n║ Please enter your arithmetic equation where you can find:     ║")
                    .a("\n╠═══════════════════════════════════════════════════════════════╣")
                    .reset());

            System.out.println(ansi().fg(Ansi.Color.CYAN)
                    .a("║  1) Sum               ➝   '+'                                 ║\n")
                    .a("║  2) Difference        ➝   '-'                                 ║\n")
                    .a("║  3) Multiplication    ➝   '*'                                 ║\n")
                    .a("║  4) Division          ➝   '/'                                 ║\n")
                    .a("║  5) Modulus           ➝   '%'                                 ║\n")
                    .a("║  6) Power             ➝   'a pow b' or 'pow(a b)'             ║\n")
                    .a("║  7) Square Root       ➝   'sqrt(number)'                      ║\n")
                    .a("║  8) Round Number      ➝   'round(number)'                     ║\n")
                    .a("║  9) Absolute Value    ➝   'abs(number)'                       ║\n")
                    .a("║                                                               ║\n")
                    .a("║  'history' - View past calculations                           ║\n")
                    .a("║  'exit'    - Quit the calculator                              ║")
                    .a("\n╚═══════════════════════════════════════════════════════════════╝")
                    .reset());
            System.out.println("Please enter your arithmetic expression: ");
            String equation = in.nextLine().trim();

            if (equation.equalsIgnoreCase("history")) {
                showHistory();
                continue;
            }
            if (equation.equalsIgnoreCase("exit")) {
                System.out.println(ansi().fg(Ansi.Color.BLUE).a("Thank you for using the Calculator!").reset());
                break;
            }

            Map<String, Integer> priortet = new HashMap<>();
            priortet.put("+", 1);
            priortet.put("-", 1);
            priortet.put("*", 2);
            priortet.put("/", 2);
            priortet.put("%", 2);
            priortet.put("pow", 3);
            priortet.put("sqrt", 4);
            priortet.put("round", 4);
            priortet.put("abs", 4);

            List<String> tokenss = tokenize(equation);
            List<String> out = RPN(tokenss, priortet);
            double result = calculateD(out);
            System.out.println("Result: " + result);

            // Store calculations in history
            history.add(equation + " = " + result);

            System.out.print(ansi().fg(Ansi.Color.MAGENTA).a("Do you want to continue? (y/n): ").reset());
            String choice = in.nextLine().trim().toLowerCase();
            if (choice.equals("n")) {
                System.out.println(ansi().fg(Ansi.Color.BLUE).a("Thank you for using the Calculator!").reset());
                break;
            }
        }
    }

    public static void showHistory() {
        if (history.isEmpty()) {
            System.out.println(ansi().fg(Ansi.Color.RED).a("No history available.").reset());
        } else {
            System.out.println(ansi().fg(Ansi.Color.GREEN).a("Calculation History:").reset());
            for (int i = 0; i < history.size(); i++) {
                System.out.println((i + 1) + ". " + history.get(i));
            }
        }
    }

    public static double calculateD(List<String> out) {
        Stack<Double> numbers = new Stack<>();
        for (String token : out) {
            try {
                numbers.push(Double.parseDouble(token));
            } catch (NumberFormatException e) {
                switch (token) {
                    case "+":
                        numbers.push(numbers.pop() + numbers.pop());
                        break;
                    case "-":
                        double b = numbers.pop();
                        double a = numbers.pop();
                        numbers.push(a - b);
                        break;
                    case "*":
                        numbers.push(numbers.pop() * numbers.pop());
                        break;
                    case "/":
                        b = numbers.pop();
                        a = numbers.pop();
                        numbers.push(a / b);
                        break;
                    case "%":
                        b = numbers.pop();
                        a = numbers.pop();
                        numbers.push(a % b);
                        break;
                    case "pow":
                        b = numbers.pop();
                        a = numbers.pop();
                        numbers.push(Math.pow(a, b));
                        break;
                    case "sqrt":
                        numbers.push(Math.sqrt(numbers.pop()));
                        break;
                    case "round":
                        numbers.push((double) Math.round(numbers.pop()));
                        break;
                    case "abs":
                        numbers.push(Math.abs(numbers.pop()));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown: " + token);
                }
            }
        }
        return numbers.pop();
    }

    public static List<String> RPN(List<String> tokens, Map<String, Integer> priority) {
        Stack<String> operators = new Stack<>();
        List<String> out = new ArrayList<>();

        for (String token : tokens) {
            if (token.matches("-?\\d+(\\.\\d+)?")) { //match:inetegers 123;-456;0;1,5 doesnt match:abc, 12ab
                out.add(token);
            } else if (priority.containsKey(token)) {
                while (!operators.isEmpty() && priority.containsKey(operators.peek())
                        && priority.get(operators.peek()) >= priority.get(token)) {
                    out.add(operators.pop());
                }
                operators.push(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    out.add(operators.pop());
                }
                operators.pop();
            } else {
                throw new IllegalArgumentException("Unknown: " + token);
            }
        }

        while (!operators.isEmpty()) {
            out.add(operators.pop());
        }

        return out;
    }

    public static List<String> tokenize(String equation) {
        List<String> tokens = new ArrayList<>();
        int length = equation.length();
        int i = 0;

        while (i < length) {
            char ch = equation.charAt(i);

            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            if (Character.isDigit(ch) || ch == '-' && (i == 0 || "+-*/%(".contains(String.valueOf(equation.charAt(i - 1))))) {
                StringBuilder number = new StringBuilder();
                number.append(ch);
                i++;
                while (i < length && (Character.isDigit(equation.charAt(i)) || equation.charAt(i) == '.')) {
                    number.append(equation.charAt(i));
                    i++;
                }
                tokens.add(number.toString());
                continue;
            }

            if ("+-*/%".indexOf(ch) != -1) {
                tokens.add(String.valueOf(ch));
                i++;
                continue;
            }

            if (ch == '(' || ch == ')') {
                tokens.add(String.valueOf(ch));
                i++;
                continue;
            }

            if (Character.isLetter(ch)) {
                StringBuilder function = new StringBuilder();
                while (i < length && Character.isLetter(equation.charAt(i))) {
                    function.append(equation.charAt(i));
                    i++;
                }
                tokens.add(function.toString());
                continue;
            }

            throw new IllegalArgumentException("Invalid: " + ch);
        }

        return tokens;
    }
}
