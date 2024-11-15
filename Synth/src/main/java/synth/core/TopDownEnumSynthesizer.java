package synth.core;

import synth.cfg.CFG;
import synth.cfg.Terminal;
import synth.cfg.NonTerminal;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

public class TopDownEnumSynthesizer implements ISynthesizer {
    private int maxOutput = 0;
    private int maxXInput = 0;
    private int maxYInput = 0;
    private int maxZInput = 0;
    /**
     * Synthesize a program f(x, y, z) based on a context-free grammar and examples
     *
     * @param cfg      the context-free grammar
     * @param examples a list of examples
     * @return the program or null to indicate synthesis failure
     */
    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        PriorityQueue<ASTNode> worklist = new PriorityQueue<>();

        for (Example example : examples) {
            if (example.getOutput() > this.maxOutput) this.maxOutput = example.getOutput();
            
            if (example.getInput().get("x") > this.maxXInput) this.maxXInput = example.getInput().get("x");
            if (example.getInput().get("y") > this.maxYInput) this.maxYInput = example.getInput().get("y");
            if (example.getInput().get("z") > this.maxZInput) this.maxZInput = example.getInput().get("z");
        }
        
        ASTNode.maxX = this.maxXInput;
        ASTNode.maxY = this.maxYInput;
        ASTNode.maxZ = this.maxZInput;

        ASTNode.minMaxValue = this.maxOutput;

        ASTNode start = new ASTNode(cfg.getStartSymbol(), Collections.emptyList());
        worklist.add(start);

        //int iter = 0;

        while (!worklist.isEmpty()) {
            ASTNode AST = worklist.remove();

            Program test = new Program(AST);

            if (AST.isComplete()) {
                boolean satisfies = true;
                for (Example example : examples) {
                    if (Interpreter.evaluate(test, example.getInput()) != example.getOutput()) {
                        satisfies = false;
                        break;
                    }
                }
                if (satisfies) return test;
            }
            
            List<ASTNode> expandedList = new ArrayList<>();
            this.expand(AST, AST, cfg, expandedList);
            
            
            worklist.addAll(expandedList);
        }

        return null;
    }

    private static final List<String> expressionfinals = Arrays.asList("x","y","z","1","2","3", "Ite","Add","Multiply");
    private static final List<String> booleanfinals = Arrays.asList("Lt","Eq","Not", "And","Or");
    private void expand(ASTNode rootNode, ASTNode node, CFG cfg, List<ASTNode> expandedList) {

        if (rootNode.isComplete()) return;
        
        if (!node.getSymbol().isNonTerminal()) {
            for (ASTNode child : node.getChildren()) expand(rootNode, child, cfg, expandedList);
            return;
        }

        if (node.getSymbol().getName().equals("B")) {
            for (String sym : booleanfinals) {
                List<ASTNode> newChildren = new ArrayList<>();

                if (sym.equals("Lt") || sym.equals("Eq")) {
                    newChildren.addAll(
                        List.of(
                            new ASTNode(new NonTerminal("E"), Collections.emptyList()),
                            new ASTNode(new NonTerminal("E"), Collections.emptyList())
                        )
                    );
                } else if (sym.equals("And") || sym.equals("Or")) {
                    newChildren.addAll(
                        List.of(
                            new ASTNode(new NonTerminal("B"), Collections.emptyList()),
                            new ASTNode(new NonTerminal("B"), Collections.emptyList())
                        )
                    );
                } else {
                    newChildren.add(
                        new ASTNode(new NonTerminal("B"), Collections.emptyList())
                    );
                }

                ASTNode copyNode = new ASTNode(new Terminal(sym), newChildren);
                ASTNode copyRootNode = deepCopyASTNodeAndAddNodeInPlaceOf(rootNode, copyNode, node);

                if (copyRootNode.getMaxValue() >= this.maxOutput) expandedList.add(copyRootNode);
            }
            return;
        }
        
        for (String sym : expressionfinals) {
            List<ASTNode> newChildren = new ArrayList<>();

            if (sym.equals("Ite")) {
                newChildren.addAll(
                    List.of(
                        new ASTNode(new NonTerminal("B"), Collections.emptyList()),
                        new ASTNode(new NonTerminal("E"), Collections.emptyList()),
                        new ASTNode(new NonTerminal("E"), Collections.emptyList())
                    )
                );
            } else if (sym.equals("Add") || sym.equals("Multiply")) {
                newChildren.addAll(
                    List.of(
                        new ASTNode(new NonTerminal("E"), Collections.emptyList()),
                        new ASTNode(new NonTerminal("E"), Collections.emptyList())
                    )
                );
            }

            ASTNode copyNode = new ASTNode(new Terminal(sym), newChildren);
            ASTNode copyRootNode = deepCopyASTNodeAndAddNodeInPlaceOf(rootNode, copyNode, node);

            if (copyRootNode.getMaxValue() >= this.maxOutput) expandedList.add(copyRootNode);
        }
    }

    private ASTNode deepCopyASTNodeAndAddNodeInPlaceOf(ASTNode rootNode, ASTNode copyNode, ASTNode node) {
        if (node != null) {
            if (rootNode == node) {
                return copyNode;
            }
        }
        
        
        List<ASTNode> copiedChildren = new ArrayList<>();
        for (ASTNode child : rootNode.getChildren()) {
            copiedChildren.add(deepCopyASTNodeAndAddNodeInPlaceOf(child, copyNode, node));
        }
        return new ASTNode(rootNode.getSymbol(), copiedChildren);
    }
}
