package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        if(vector.length == 0)
            throw new IllegalArgumentException("empty vector");
        this.vector = vector;
        this.orientation = VectorOrientation.ROW_MAJOR;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        if(index>=vector.length || index<0)
            throw new IllegalArgumentException("index out of bounds");
        readLock();
        double val = vector[index];
        readUnlock();
        return val;
    }

    public int length() {
        // TODO: return vector length
        return vector.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        return this.orientation;
    }

    public void writeLock() {
        // TODO: acquire write lock
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        // TODO: release write lock
        lock.writeLock().unlock();
    }

    public void readLock() {
        // TODO: acquire read lock
        lock.readLock().lock();
    }

    public void readUnlock() {
        // TODO: release read lock
        lock.readLock().unlock();
    }

    public void transpose() {
        // TODO: transpose vector
        writeLock();
        if(orientation == VectorOrientation.ROW_MAJOR)
            orientation = VectorOrientation.COLUMN_MAJOR;
        orientation = VectorOrientation.ROW_MAJOR;
        writeUnlock();
    }

    public void add(SharedVector other) {
        // TODO: add two vectors
        if(length() != other.length())
            throw new IllegalArgumentException("vectors not the same length");
        readLock();
        other.readLock();
        double[] res = new double[vector.length];
        for(int i=0;i<vector.length;i++)
            res[i] = get(i) + other.get(i);
        other.readUnlock();
        readUnlock();
        writeLock();
        vector = res;
        writeUnlock();
    }

    public void negate() {
        // TODO: negate vector
        writeLock();
        for(int i=0;i<vector.length;i++) {
            vector[i] = -vector[i];
        }
        writeUnlock();
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        {//errors
        if(length() != other.length())
            throw new IllegalArgumentException("vectors not the same length");
        if(getOrientation()==VectorOrientation.COLUMN_MAJOR || other.getOrientation()==VectorOrientation.ROW_MAJOR)
            throw new IllegalArgumentException("vectors with same orientation cannot be multiplied");
        }
        readLock();
        other.readLock();
        double val = 0.0;
        for(int i=0;i<vector.length;i++) {
            val += get(i)*other.get(i);
        }
        other.readUnlock();
        readUnlock();
        return val;
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
        {//errors
        if(getOrientation()!=VectorOrientation.ROW_MAJOR)
            throw new IllegalAccessError("this is not a row vector");
        if(matrix.getOrientation()==getOrientation())
            if(length()!=matrix.length())
                throw new IllegalArgumentException("matrix and vector arnt of compatable size for multiplication");
        if(length()!=matrix.get(0).length())
            throw new IllegalArgumentException("matrix and vector arnt of compatable size for multiplication");
        }
        
        double[][] copyMat = matrix.readRowMajor();
        if(matrix.getOrientation()==VectorOrientation.ROW_MAJOR)
            matrix.loadColumnMajor(copyMat);
        
        double[] vec = new double[length()];
        readLock();
        for(int i=0;i<length();i++) {
            vec[i] = dot(matrix.get(i));
        }
        readUnlock();
        writeLock();
        vector = vec;
        writeUnlock();
    }
}
