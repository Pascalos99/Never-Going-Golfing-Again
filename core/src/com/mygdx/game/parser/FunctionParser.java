package com.mygdx.game.parser;

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
import java.util.AbstractMap.SimpleEntry;

public class FunctionParser {
    public static boolean DEBUG = true;
    private static final int TYPOLOGY_NUMBER = 0, TYPOLOGY_VARIABLE = 2, TYPOLOGY_FUNCTION = 3, TYPOLOGY_OPERATOR = 4;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String function = input.nextLine();

        List<SimpleEntry<String, Integer>> arr = preprocess(function);
        arr = groupFunctions(arr);

        System.out.print("Input <-- ");
        for (SimpleEntry<String, Integer> e : arr)
            System.out.print(e.getKey() + " ");

        System.out.println("");

        Atom f = parse(function);
        System.out.println("Function: " + f.toString());
        System.out.println("Simplified: " + f.simplify().toString());
        System.out.println("Derivative: " + f.simplify().derivate("x").simplify().toString());
    }

    public static List<SimpleEntry<String, Integer>> preprocess(String expression) {
        String s = expression.replaceAll("\\s+", "").trim();
        List<SimpleEntry<String, Integer>> arr = new ArrayList<SimpleEntry<String, Integer>>();

        int i = 0;
        while (i < s.length()) {
            String stack = "" + s.charAt(i);
            int type = getTypology(stack);

            if (type == TYPOLOGY_OPERATOR || type == TYPOLOGY_VARIABLE) {
                arr.add(new SimpleEntry<String, Integer>(stack, type));
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

                arr.add(new SimpleEntry<String, Integer>(stack, type));
                i = j;
            }

        }

        for (int n = 0; n < arr.size() - 1; n++) {
            SimpleEntry<String, Integer> left = arr.get(n);
            SimpleEntry<String, Integer> right = arr.get(n + 1);

            if ((left.getValue() == TYPOLOGY_NUMBER && right.getValue() == TYPOLOGY_VARIABLE)
                    || (left.getValue() == TYPOLOGY_VARIABLE && right.getValue() == TYPOLOGY_VARIABLE)
                    || (left.getValue() == TYPOLOGY_NUMBER && right.getKey().equals("("))
                    || (left.getValue() == TYPOLOGY_VARIABLE && right.getKey().equals("("))) {
                arr.add(n + 1, new SimpleEntry<String, Integer>("*", TYPOLOGY_OPERATOR));

            }

        }

        return arr;
    }

    public static List<SimpleEntry<String, Integer>> groupFunctions(List<SimpleEntry<String, Integer>> arr) {
        List<String> patterns = new ArrayList<String>();
        patterns.add("c*o*s*");
        patterns.add("s*i*n*");
        patterns.add("s*c*a*l*e");

        patterns.sort(Comparator.comparing(String::length).reversed());

        for (String p : patterns) {
            List<String> chars = Arrays.asList(p.split(""));

            lev2_loop: for (int i = 0; i < arr.size() - chars.size(); i++) {
                String tag = "";

                for (int j = 0; j < chars.size(); j++) {
                    SimpleEntry<String, Integer> tuple = arr.get(i + j);
                    String token = tuple.getKey();
                    int type = tuple.getValue();

                    if (!token.equals(chars.get(j)))
                        continue lev2_loop;

                    if (type == TYPOLOGY_VARIABLE)
                        tag += token;

                }

                SimpleEntry<String, Integer> new_tuple = new SimpleEntry<String, Integer>(tag, TYPOLOGY_FUNCTION);

                for (int k = 0; k < chars.size(); k++)
                    arr.remove(i);

                arr.add(i, new_tuple);
            }

        }

        return arr;
    }

    public static Atom parse(String s) {
        List<SimpleEntry<String, Integer>> arr = groupFunctions(preprocess(s));
        ListIterator<SimpleEntry<String, Integer>> iter = arr.listIterator();

        Queue<String> output = new ConcurrentLinkedQueue<String>();
        Stack<String> operators = new Stack<String>();

        while (iter.hasNext()) {
            SimpleEntry<String, Integer> tuple = iter.next();
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

                /*else if (token.equals("scale")) {
                    a = new ConstantAtom(WORLD_SCALING);
                }*/

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

        for(int i = 0; i < c.length(); i++) {

            if (!("0123456789.".contains(c.substring(i, i + 1))))
                return false;

        }

        return true;
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

        if (!(atom instanceof ConstantAtom) && !(atom instanceof VariableAtom) && !(atom instanceof FunctionAtom))
            base = "(" + base + ")";

        return base + "^" + power;
    }

    @Override
    public Atom simplify() {
        atom = atom.simplify();

        if(power == 1.0){
            return atom;
        }

        if(power == 0.0){

            if(atom instanceof ConstantAtom){
                double value = ((ConstantAtom)atom).getConstant();

                if(value == 0.0)
                    return new IndefiniteAtom(this);

            }

        }

        if(atom instanceof  ConstantAtom){
            double value = ((ConstantAtom)atom).getConstant();
            double result = Math.pow(value, power);
            return new ConstantAtom(result);
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

        if (!(left instanceof ConstantAtom) && !(left instanceof VariableAtom) && !(left instanceof PowerAtom) && !(left instanceof FunctionAtom))
            l = "(" + l + ")";

        if (!(right instanceof ConstantAtom) && !(right instanceof VariableAtom) && !(right instanceof PowerAtom) && !(right instanceof FunctionAtom))
            r = "(" + r + ")";

        return l + " * " + r;
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

        if (left instanceof ConstantAtom && ((ConstantAtom) left).getConstant() == 1.0)
            return right;

        if (right instanceof ConstantAtom && ((ConstantAtom) right).getConstant() == 1.0)
            return left;

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
            return new ConstantAtom(((ConstantAtom) left).getConstant() + ((ConstantAtom) right).getConstant());

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

    abstract protected double solve(double n);

    public Atom derivate(String variable){
        return new ProductAtom(partialDerivate(variable), atom.derivate(variable));
    }

    public double apply(Map<String, Double> variables){
        return solve(atom.apply(variables));
    }

    @Override
    public Atom simplify() {
        atom = atom.simplify();

        if(atom instanceof ConstantAtom){
            double value = ((ConstantAtom)atom).getConstant();
            return new ConstantAtom(solve(value));
        }

        return this;
    }

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
    public double solve(double n){
        return Math.cos(n);
    }

    @Override
    public String toString() {
        return "cos(" + atom.toString() + ")";
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
    public double solve(double n){
        return Math.sin(n);
    }

    @Override
    public String toString() {
        return "sin(" + atom.toString() + ")";
    }

}

class IndefiniteAtom extends Atom {
    protected Atom atom;

    IndefiniteAtom(Atom atom) {
        this.atom = atom;
    }

    @Override
    public Atom derivate(String variable) {
        throw new ArithmeticException("Indefinite expression (" + atom.toString() + ") cannot be derivated.");
    }

    @Override
    public double apply(Map<String, Double> variables) {
        throw new ArithmeticException("Indefinite expression (" + atom.toString() + ") cannot be resolved.");
    }

    @Override
    public String toString() {
        return "indef(" + atom.toString() + ")";
    }

    @Override
    public Atom simplify() {
        throw new ArithmeticException("Indefinite expression (" + atom.toString() + ") cannot be simplified.");
    }

}