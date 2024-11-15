package synth.cfg;

import java.util.HashMap;
import java.util.Map;

public abstract class Symbol {
    protected final String name;

    private static final Map<String, Float> costMap = new HashMap<>();
    static {
        costMap.put("x", 0f);
        costMap.put("y", 0f);
        costMap.put("z", 0f);
        costMap.put("1", 0f);
        costMap.put("2", 0f);
        costMap.put("3", 0f);
        costMap.put( "Ite", 1.2f);
        costMap.put("Add", 1.1f);
        costMap.put("Multiply", 1.1f);
        costMap.put("Lt", 1.1f);
        costMap.put("Eq", 1.1f);
        costMap.put("Not", 1.0f);
        costMap.put("And", 1.1f);
        costMap.put("Or", 1.1f);
    }

    public Symbol(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Float getCost() {
        return costMap.getOrDefault(this.name, 0.0f);
    }

    public abstract boolean isTerminal();

    public abstract boolean isNonTerminal();
}
