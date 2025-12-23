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
        // TODO: create executor with given thread count
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        return null;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        leftMatrix.loadRowMajor(leftMatrix.readRowMajor());
        rightMatrix.loadRowMajor(rightMatrix.readRowMajor());
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
        leftMatrix.loadRowMajor(leftMatrix.readRowMajor());
        rightMatrix.loadColumnMajor(rightMatrix.readRowMajor()); //temporary
        if(leftMatrix.get(0).length()!=rightMatrix.get(0).length())
            throw new IllegalArgumentException("matricies are not of compatable size for multiplication");
        List<Runnable> lst = new LinkedList<>();
        Runnable e;
        for(int i=0;i<leftMatrix.length();i++) {
            for(int j=0;j<rightMatrix.length();j++) {
                int left = i;
                int right = j;
                e = () -> {leftMatrix.get(left).dot(rightMatrix.get(right));};
                lst.add(e);
            }
        }
        return lst;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        List<Runnable> lst = new LinkedList<>();
        Runnable e;
        for(int i=0;i<rightMatrix.length();i++) {
            int index = i;
            e = () -> {rightMatrix.get(index).negate();};
            lst.add(e);
        }
        return lst;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        List<Runnable> lst = new LinkedList<>();
        Runnable e;
        for(int i=0;i<rightMatrix.length();i++) {
            int index = i;
            e = () -> {rightMatrix.get(index).transpose();};
            lst.add(e);
        }
        return lst;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return null;
    }
}
