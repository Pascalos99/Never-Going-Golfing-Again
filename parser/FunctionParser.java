package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.util.Pair;

class FunctionParser {
    public static boolean DEBUG = true;
    private static final int TYPOLOGY_NUMBER = 0, TYPOLOGY_VARIABLE = 2, TYPOLOGY_FUNCTION = 3, TYPOLOGY_OPERATOR = 4;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String function = input.nextLine();

        List<Pair<String, Integer>> arr = preprocess(function);
        arr = groupFunctions(arr);

        System.out.print("Input <-- ");
        for (Pair<String, Integer> e : arr)
            System.out.print(e.getKey() + " ");

        System.out.println("");

        Atom f = parse(function);
        System.out.println("Function: " + f.toString());
        System.out.println("Simplified: " + f.simplify().toString());
        System.out.println("Derivative: " + f.simplify().derivate("x").simplify().toString());
    }

    public static List<Pair<String, Integer>> preprocess(String expression) {
        String s = expression.replaceAll("\\s+", "").trim();
        List<Pair<String, Integer>> arr = new ArrayList<Pair<String, Integer>>();

        int i = 0;
        while (i < s.length()) {
            String stack = "" + s.charAt(i);
            int type = getTypology(stack);

            if (type == TYPOLOGY_OPERATOR || type == TYPOLOGY_VARIABLE) {
                arr.add(new Pair<String, Integer>(stack, type));
                i++;
            }

            else if (type == TYPOLOGY_NUMBER) {

                int j = i + 1;
                while (j < s.length()) {
                    String current = "" + s.charAt(j);

                    if (getTypology(current) == TYPOLOGY_NUMBER)
                        stack += current;

                    else {
                        break;
                    }

                    j++;
                }

                arr.add(new Pair<String, Integer>(stack, type));
                i = j;
            }

        }

        for (int n = 0; n < arr.size() - 1; n++) {
            Pair<String, Integer> left = arr.get(n);
            Pair<String, Integer> right = arr.get(n + 1);

            if ((left.getValue() == TYPOLOGY_NUMBER && right.getValue() == TYPOLOGY_VARIABLE)
                    || (left.getValue() == TYPOLOGY_VARIABLE && right.getValue() == TYPOLOGY_VARIABLE)
                    || (left.getValue() == TYPOLOGY_NUMBER && right.getKey().equals("("))
                    || (left.getValue() == TYPOLOGY_VARIABLE && right.getKey().equals("("))) {
                arr.add(n + 1, new Pair<String, Integer>("*", TYPOLOGY_OPERATOR));

            }

        }

        return arr;
    }

    public static List<Pair<String, Integer>> groupFunctions(List<Pair<String, Integer>> arr) {
        List<String> patterns = new ArrayList<String>();
        patterns.add("c*o*s*");
        patterns.add("s*i*n*");
        patterns.add("m*a*x*");
        /*
         * patterns.add("tan"); patterns.add("atan"); patterns.add("asin");
         * patterns.add("acos");
         */
        patterns.sort(Comparator.comparing(String::length).reversed());

        for (String p : patterns) {
            List<String> chars = Arrays.asList(p.split(""));

            lev2_loop: for (int i = 0; i < arr.size() - chars.size(); i++) {
                String tag = "";

                for (int j = 0; j < chars.size(); j++) {
                    Pair<String, Integer> tuple = arr.get(i + j);
                    String token = tuple.getKey();
                    int type = tuple.getValue();

                    if (!token.equals(chars.get(j)))
                        continue lev2_loop;

                    if (type == TYPOLOGY_VARIABLE)
                        tag += token;

                }

                Pair<String, Integer> new_tuple = new Pair<String, Integer>(tag, TYPOLOGY_FUNCTION);

                for (int k = 0; k < chars.size(); k++)
                    arr.remove(i);

                arr.add(i, new_tuple);
            }

        }

        return arr;
    }

    public static Atom parse(String s) {
        List<Pair<String, Integer>> arr = groupFunctions(preprocess(s));
        ListIterator<Pair<String, Integer>> iter = arr.listIterator();

        Queue<String> output = new ConcurrentLinkedQueue<String>();
        Stack<String> operators = new Stack<String>();

        while (iter.hasNext()) {
            Pair<String, Integer> tuple = iter.next();
            String token = tuple.getKey();
            int type = tuple.getValue();

            if (type == TYPOLOGY_NUMBER || type == TYPOLOGY_VARIABLE) {
                output.add(token);
            }

            if (type == TYPOLOGY_FUNCTION) {
                operators.push(token);
            }

            if (type == TYPOLOGY_OPERATOR && !"()".contains(token)) {

                while (((!operators.empty() && getTypology(operators.peek()) == TYPOLOGY_FUNCTION)
                        || (!operators.empty() && operatorPriority(operators.peek()) > operatorPriority(token))
                        || (!operators.empty() && operatorPriority(operators.peek()) == operatorPriority(token)
                                && isLeftAssociative(token)))
                        && (!operators.empty() && !operators.peek().equals("("))) {
                    output.add(operators.pop());
                }

                operators.push(token);
            }

            if (token.equals("(")) {
                operators.push(token);
            }

            if (token.equals(")")) {

                while (!operators.empty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }

                if (!operators.empty() && operators.peek().equals("(")) {
                    operators.pop();
                }

            }

        }

        while (!operators.empty()) {
            output.add(operators.pop());
        }

        Stack<Atom> atoms = new Stack<Atom>();

        while (!output.isEmpty()) {
            String token = output.poll();
            int type = getTypology(token);

            if (type == TYPOLOGY_NUMBER) {
                atoms.push(new ConstantAtom(Double.parseDouble(token)));
            }

            else if (type == TYPOLOGY_VARIABLE) {
                atoms.push(new VariableAtom(token));
            }

            else if (type == TYPOLOGY_OPERATOR) {
                Atom a = atoms.pop();

                if (!atoms.empty()) {
                    Atom b = atoms.pop();
                    a = invokeConstructor(b, token, a);
                }

                else if (token.equals("-")) {
                    a = new ProductAtom(new ConstantAtom(-1), a);
                }

                atoms.push(a);
            }

            else if (type == TYPOLOGY_FUNCTION) {
                Atom a = atoms.pop();

                if (token.equals("sin")) {
                    a = new SinAtom(a);
                }

                else if (token.equals("cos")) {
                    a = new CosAtom(a);
                }

                else if (token.equals("max")) {
                    Atom b = atoms.pop();
                    a = new MaxAtom(a, b);
                }

                else {
                    throw new ArithmeticException("Unknown function ( " + token + " ).");
                }

                atoms.push(a);
            }

        }

        return atoms.pop();
    }

    private static Atom invokeConstructor(Atom a, String op, Atom b) {

        if (op.equals("+"))
            return new SumAtom(a, b);

        if (op.equals("-"))
            return new SumAtom(a, new ProductAtom(new ConstantAtom(-1), b));

        if (op.equals("*"))
            return new ProductAtom(a, b);

        if (op.equals("/"))
            return new ProductAtom(a, new PowerAtom(b, -1));

        if (op.equals("^"))
            return new PowerAtom(a, ((ConstantAtom) b).getConstant());

        throw new ArithmeticException("Unkown arithmetic operator ( " + op + " ).");
    }

    private static boolean isLeftAssociative(String operator) {

        if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/")
                || operator.equals("(") || operator.equals(")"))
            return true;

        return false;
    }

    private static int operatorPriority(String operator) {

        if (operator.equals("+") || operator.equals("-"))
            return 1;

        if (operator.equals("*") || operator.equals("/"))
            return 2;

        if (operator.equals("^"))
            return 3;

        return 0;
    }

    private static boolean isValidNumber(String c) {

        if ("0123456789.".contains(c))
            return true;

        return false;
    }

    private static boolean isValidOperator(String c) {

        if ("+-*/^()".contains(c))
            return true;

        return false;
    }

    private static boolean isValidVariable(String c) {

        if ("abcdefghijklmnopqrstuvwxyz".contains(c))
            return true;

        return false;
    }

    private static int getTypology(String c) {

        if (isValidNumber(c))
            return TYPOLOGY_NUMBER;

        else if (isValidOperator(c))
            return TYPOLOGY_OPERATOR;

        else if (isValidVariable(c))
            return TYPOLOGY_VARIABLE;

        return TYPOLOGY_FUNCTION;
    }

}

abstract class Atom {

    abstract public Atom derivate(String variable);

    abstract public double apply(Map<String, Double> variables);

    public static PowerAtom wrap(Atom a) {
        return new PowerAtom(a, 1);
    }

    abstract public Atom simplify();

}

class VariableAtom extends Atom {
    protected String name;

    VariableAtom(String name) {
        this.name = name;
    }

    @Override
    public Atom derivate(String variable) {

        if (!name.equals(variable))
            return new ConstantAtom(0);

        return new ConstantAtom(1);
    }

    @Override
    public double apply(Map<String, Double> variables) {

        for (Map.Entry<String, Double> entry : variables.entrySet()) {

            if (entry.getKey().equals(name))
                return entry.getValue();

        }

        throw new ArithmeticException("Variable (" + name + ") is undefined.");
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Atom simplify() {
        return this;
    }

}

class ConstantAtom extends Atom {
    protected double constant;

    ConstantAtom(double constant) {
        this.constant = constant;
    }

    @Override
    public Atom derivate(String variable) {
        return new ConstantAtom(0);
    }

    @Override
    public double apply(Map<String, Double> variables) {
        return constant;
    }

    @Override
    public String toString() {
        return "" + constant;
    }

    public double getConstant() {
        return constant;
    }

    @Override
    public Atom simplify() {
        return this;
    }

}

class PowerAtom extends Atom {
    protected double power;
    protected Atom atom;

    PowerAtom(Atom atom, double power) {
        this.power = power;
        this.atom = atom;
    }

    @Override
    public Atom derivate(String variable) {
        Atom n = new ConstantAtom(power);
        Atom a = new PowerAtom(atom, power - 1);
        Atom b = atom.derivate(variable);

        Atom half_1 = new ProductAtom(n, a);
        Atom half_2 = new ProductAtom(half_1, b);

        return half_2;
    }

    @Override
    public double apply(Map<String, Double> variables) {
        return Math.pow(atom.apply(variables), power);
    }

    @Override
    public String toString() {
        String base = atom.toString();

        if (!(atom instanceof ConstantAtom) && !(atom instanceof VariableAtom))
            base = "(" + base + ")";

        return base + "^" + power;
    }

    @Override
    public Atom simplify() {
        atom = atom.simplify();

        if (power == 0.0) {

            if ((atom instanceof ConstantAtom && ((ConstantAtom) atom).getConstant() != 0.0)
                    || (!(atom instanceof ConstantAtom))) {
                return new ConstantAtom(1);
            }

        }

        if (atom instanceof ConstantAtom && ((ConstantAtom) atom).getConstant() == 0.0) {
            return new ConstantAtom(0);
        }

        return this;
    }

}

class ProductAtom extends Atom {
    protected Atom left, right;

    ProductAtom(Atom left, Atom right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Atom derivate(String variable) {
        Atom f = left;
        Atom g = right;

        Atom _f = f.derivate(variable);
        Atom _g = g.derivate(variable);

        Atom prod_left = new ProductAtom(_f, g);
        Atom prod_right = new ProductAtom(f, _g);

        return new SumAtom(prod_left, prod_right);
    }

    @Override
    public double apply(Map<String, Double> variables) {
        return left.apply(variables) * right.apply(variables);
    }

    @Override
    public String toString() {
        String l = left.toString();
        String r = right.toString();

        if (!(left instanceof ConstantAtom) && !(left instanceof VariableAtom) && !(left instanceof PowerAtom))
            l = "(" + l + ")";

        if (!(right instanceof ConstantAtom) && !(right instanceof VariableAtom) && !(right instanceof PowerAtom))
            r = "(" + r + ")";

        return "(" + l + " * " + r + ")";
    }

    @Override
    public Atom simplify() {
        left = left.simplify();
        right = right.simplify();

        if (left instanceof ConstantAtom && right instanceof ConstantAtom)
            return new ConstantAtom(((ConstantAtom) left).getConstant() * ((ConstantAtom) right).getConstant());

        if (left instanceof ConstantAtom && ((ConstantAtom) left).getConstant() == 0.0)
            return new ConstantAtom(0);

        if (right instanceof ConstantAtom && ((ConstantAtom) right).getConstant() == 0.0)
            return new ConstantAtom(0);

        return this;
    }

}

class SumAtom extends Atom {
    protected Atom left, right;

    SumAtom(Atom left, Atom right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Atom derivate(String variable) {
        Atom _left = left.derivate(variable);
        Atom _right = right.derivate(variable);

        return new SumAtom(_left, _right);
    }

    @Override
    public double apply(Map<String, Double> variables) {
        return left.apply(variables) + right.apply(variables);
    }

    @Override
    public String toString() {

        if (right instanceof ConstantAtom && ((ConstantAtom) right).getConstant() < 0)
            return left.toString() + " + (" + right.toString() + ")";

        return left.toString() + " + " + right.toString();
    }

    @Override
    public Atom simplify() {
        left = left.simplify();
        right = right.simplify();

        if (left instanceof ConstantAtom && right instanceof ConstantAtom)
            return new ConstantAtom(((ConstantAtom) left).getConstant() * ((ConstantAtom) right).getConstant());

        if (left instanceof ConstantAtom && ((ConstantAtom) left).getConstant() == 0.0)
            return right;

        if (right instanceof ConstantAtom && ((ConstantAtom) right).getConstant() == 0.0)
            return left;

        return this;
    }

}

abstract class FunctionAtom extends Atom {
    protected Atom atom;

    FunctionAtom(Atom atom) {
        this.atom = atom;
    }

    abstract protected Atom partialDerivate(String variable);

    public Atom derivate(String variable){
        return new ProductAtom(partialDerivate(variable), atom.derivate(variable));
    }

    abstract public double apply(Map<String, Double> variables);

}

class CosAtom extends FunctionAtom {

    CosAtom(Atom atom) {
        super(atom);
    }

    @Override
    protected Atom partialDerivate(String variable) {
        return new ProductAtom(new ConstantAtom(-1), new SinAtom(atom));
    }

    @Override
    public double apply(Map<String, Double> variables) {
        return Math.cos(atom.apply(variables));
    }

    @Override
    public String toString() {
        return "cos(" + atom.toString() + ")";
    }

    @Override
    public Atom simplify() {
        atom = atom.simplify();
        return this;
    }

}

class SinAtom extends FunctionAtom {

    SinAtom(Atom atom) {
        super(atom);
    }

    @Override
    protected Atom partialDerivate(String variable) {
        return new CosAtom(atom);
    }

    @Override
    public double apply(Map<String, Double> variables) {
        return Math.sin(atom.apply(variables));
    }

    @Override
    public String toString() {
        return "sin(" + atom.toString() + ")";
    }

    @Override
    public Atom simplify() {
        atom = atom.simplify();
        return this;
    }

}

class MaxAtom extends FunctionAtom {
    protected Atom left, right;

    MaxAtom(Atom atom1, Atom atom2) {
        super(null);
        this.left = atom1;
        this.right = atom2;
    }

    @Override
    public Atom partialDerivate(String variable) {
        throw new ArithmeticException("Derivative of max() is not defined yet.");
    }

    @Override
    public double apply(Map<String, Double> variables) {
        return Math.max(left.apply(variables), right.apply(variables));
    }

    @Override
    public String toString() {
        return "max(" + left.toString() + ", " + right.toString() + ")";
    }

    @Override
    public Atom simplify() {
        left = left.simplify();
        right = right.simplify();
        return this;
    }

}