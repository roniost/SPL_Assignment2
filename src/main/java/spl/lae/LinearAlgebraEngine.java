package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.LinkedList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
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
        if(node.getChildren().size()>2)
            throw new IllegalArgumentException("node has more than 2 operands");
        
        List<Runnable> lst;
        switch (node.getNodeType()) {
            case ADD:
                if(node.getChildren().size()<2)
                    throw new IllegalArgumentException("add node has less than 2 operands");
                leftMatrix.loadRowMajor(node.getChildren().get(0).getMatrix());
                rightMatrix.loadRowMajor(node.getChildren().get(1).getMatrix());
                lst = createAddTasks();
                executor.submitAll(lst);
                node.resolve(leftMatrix.readRowMajor());
                break;
            
            case MULTIPLY:
                if(node.getChildren().size()<2)
                    throw new IllegalArgumentException("mult node has less than 2 operands");
                leftMatrix.loadRowMajor(node.getChildren().get(0).getMatrix());
                rightMatrix.loadColumnMajor(node.getChildren().get(1).getMatrix());
                lst = createMultiplyTasks();
                executor.submitAll(lst);
                node.resolve(leftMatrix.readRowMajor());
                break;
            
            case NEGATE:
                if(node.getChildren().size()>1)
                    throw new IllegalArgumentException("negation node has more than 1 operands");
                leftMatrix.loadRowMajor(node.getChildren().get(0).getMatrix());
                lst = createNegateTasks();
                executor.submitAll(lst);
                node.resolve(leftMatrix.readRowMajor());
                break;
            
            case TRANSPOSE:
                if(node.getChildren().size()>1)
                    throw new IllegalArgumentException("transpose node has more than 1 operands");
                leftMatrix.loadRowMajor(node.getChildren().get(0).getMatrix());
                lst = createTransposeTasks();
                executor.submitAll(lst);
                node.resolve(leftMatrix.readRowMajor());
                break;

            case MATRIX:
                throw new IllegalArgumentException("node is already solved");
        }
    }

    public List<Runnable> createAddTasks() {
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
        {//errors
        if(leftMatrix.get(0).length()!=rightMatrix.get(0).length())
            throw new IllegalArgumentException("matricies are not of compatable size for multiplication");
        if(leftMatrix.getOrientation()!=VectorOrientation.ROW_MAJOR)
            throw new IllegalArgumentException("left is not in ROW MAJOR");
        //if(rightMatrix.getOrientation()!=VectorOrientation.COLUMN_MAJOR)
        //    throw new IllegalArgumentException("right is not in COLUMN MAJOR");
        }
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
        return executor.getWorkerReport();
    }
}
