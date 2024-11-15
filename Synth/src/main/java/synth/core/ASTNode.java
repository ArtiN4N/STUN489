package synth.core;

import synth.cfg.Symbol;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class ASTNode implements Comparable<ASTNode> {
    private final Symbol symbol;
    private Float cost;
    private boolean complete;
    private final List<ASTNode> children;
    private int maxValue;

    public static int minMaxValue = 0;

    public static int maxX = 0;
    public static int maxY = 0;
    public static int maxZ = 0;

    public ASTNode(Symbol symbol, List<ASTNode> children) {
        this.symbol = symbol;
        this.children = Collections.unmodifiableList(children);

        this.cost = symbol.getCost();
        this.complete = !this.symbol.isNonTerminal();

        this.maxValue = minMaxValue;
        if (this.symbol.getName().equals("1")) this.maxValue = 1;
        else if (this.symbol.getName().equals("2"))  this.maxValue = 2;
        else if (this.symbol.getName().equals("3"))  this.maxValue = 3;
        else if (this.symbol.getName().equals("x"))  this.maxValue = maxX;
        else if (this.symbol.getName().equals("y"))  this.maxValue = maxY;
        else if (this.symbol.getName().equals("z"))  this.maxValue = maxZ;
        else if (this.symbol.getName().equals("Add")) {
            this.maxValue = this.getChild(0).getMaxValue() + this.getChild(1).getMaxValue();
        }
        else if (this.symbol.getName().equals("Multiply")) {
            this.maxValue = this.getChild(0).getMaxValue() * this.getChild(1).getMaxValue();
        }
        else if (this.symbol.getName().equals("Ite")) {
            this.maxValue = Math.max(this.getChild(1).getMaxValue(), this.getChild(2).getMaxValue());
        }


        for (ASTNode child : children) {
            this.cost += child.getCost();
            this.complete = this.complete && child.isComplete();
        }
    }

    public int getMaxValue() {
        return maxValue;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public Float getCost() {
        return this.cost;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public ASTNode getChild(int index) {
        return children.get(index);
    }

    @Override
    public int compareTo(ASTNode other) {
        return Float.compare(this.cost, other.getCost());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(symbol);
        String separator = "";
        if (!children.isEmpty()) {
            builder.append("(");
            for (ASTNode child : children) {
                builder.append(separator);
                separator = ", ";
                builder.append(child);
            }
            builder.append(")");
        }
        return builder.toString();
    }
}
