package spl.lae;

import java.util.LinkedList;
import java.util.List;

import memory.SharedMatrix;
import parser.ComputationNode;
import parser.ComputationNodeType;
import scheduling.TiredExecutor;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        ComputationNode n;
        while(computationRoot.getNodeType()!=ComputationNodeType.MATRIX) {
            n = computationRoot.findResolvable();
            if(n.getChildren().size()>2)
                n.associativeNesting();
            else
                loadAndCompute(n);
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        if(node.getChildren().size()>2)
            throw new IllegalArgumentException("node has more than 2 operands");
        
        List<Runnable> lst;
        switch (node.getNodeType()) {
            case ADD:
                if(node.getChildren().size()<2)
                    throw new IllegalArgumentException("add node has less than 2 operands");
                leftMatrix = new SharedMatrix(node.getChildren().get(0).getMatrix());
                rightMatrix = new SharedMatrix(node.getChildren().get(1).getMatrix());
                lst = createAddTasks();
                executor.submitAll(lst);
                node.resolve(leftMatrix.readRowMajor());
                break;
            
            case MULTIPLY:
                if(node.getChildren().size()<2)
                    throw new IllegalArgumentException("mult node has less than 2 operands");
                leftMatrix = new SharedMatrix(node.getChildren().get(0).getMatrix());
                rightMatrix = new SharedMatrix(node.getChildren().get(1).getMatrix());
                lst = createMultiplyTasks();
                executor.submitAll(lst);
                node.resolve(leftMatrix.readRowMajor());
                break;

            case NEGATE:
                if(node.getChildren().size()>1)
                    throw new IllegalArgumentException("negation node has more than 1 operands");
                leftMatrix = new SharedMatrix(node.getChildren().get(1).getMatrix());//get(0)?
                lst = createNegateTasks();
                executor.submitAll(lst);
                node.resolve(leftMatrix.readRowMajor());
                break;
            
            case TRANSPOSE:
                if(node.getChildren().size()>1)
                    throw new IllegalArgumentException("transpose node has more than 1 operands");
                leftMatrix = new SharedMatrix(node.getChildren().get(1).getMatrix());//get 0?
                lst = createTransposeTasks();
                executor.submitAll(lst);
                node.resolve(leftMatrix.readRowMajor());
                break;

            case MATRIX:
                throw new IllegalArgumentException("node is already solved");
        }
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        leftMatrix.loadRowMajor(leftMatrix.readRowMajor());//gem says redundant
        rightMatrix.loadRowMajor(rightMatrix.readRowMajor()); //gem says redundant
        if(leftMatrix.length()!=rightMatrix.length() || leftMatrix.get(0).length()!=rightMatrix.get(0).length())
            throw new IllegalArgumentException("matricies are not of the same size");
        List<Runnable> lst = new LinkedList<>();
        Runnable e;
        for(int i=0;i<leftMatrix.length();i++) {
            int index = i;
            e = () -> {leftMatrix.get(index).add(rightMatrix.get(index));};
            lst.add(e);
        }
        return lst;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        leftMatrix.loadRowMajor(leftMatrix.readRowMajor()); //gem says redundant
        rightMatrix.loadColumnMajor(rightMatrix.readRowMajor());
        if(leftMatrix.get(0).length()!=rightMatrix.get(0).length())
            throw new IllegalArgumentException("matricies are not of compatable size for multiplication");
        List<Runnable> lst = new LinkedList<>();
        Runnable e;
        for(int i=0;i<leftMatrix.length();i++) {
            int index = i;
            e = () -> leftMatrix.get(index).vecMatMul(rightMatrix);
            lst.add(e);
        }
        return lst;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        List<Runnable> lst = new LinkedList<>();
        Runnable e;
        for(int i=0;i<leftMatrix.length();i++) {
            int index = i;
            e = () -> {leftMatrix.get(index).negate();};
            lst.add(e);
        }
        return lst;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        List<Runnable> lst = new LinkedList<>();
        Runnable e;
        for(int i=0;i<leftMatrix.length();i++) {
            int index = i;
            e = () -> {leftMatrix.get(index).transpose();};
            lst.add(e);
        }
        return lst;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return executor.getWorkerReport();
    }
}