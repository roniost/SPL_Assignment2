package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        if(vector.length == 0)
            throw new IllegalArgumentException("empty vector");
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        if(index>=vector.length || index<0)
            throw new IllegalArgumentException("index out of bounds");
        return vector[index];
    }

    public int length() {
        return vector.length;
    }

    public VectorOrientation getOrientation() {
        return this.orientation;
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        //writeLock();
        try {
            if(orientation == VectorOrientation.ROW_MAJOR)
                orientation = VectorOrientation.COLUMN_MAJOR;
            else
                orientation = VectorOrientation.ROW_MAJOR;
        }
        finally {
            //writeUnlock();
        }
    }

    public void add(SharedVector other) {
        if(length() != other.length())
            throw new IllegalArgumentException("vectors not the same length");
        //readLock();
        //other.readLock();
        try {
        double[] res = new double[vector.length];
        for(int i=0;i<vector.length;i++)
            res[i] = get(i) + other.get(i);
        //other.readUnlock();
        //readUnlock();
        //writeLock();
        vector = res;
        //writeUnlock();
        }
        finally {
            //other.readUnlock();
            //readUnlock();
            //writeUnlock();
        }
    }

    public void negate() {
        //writeLock();
        try {
        for(int i=0;i<vector.length;i++)
            vector[i] = -vector[i];
        }
        finally {
            //writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        {//errors
        if(length() != other.length())
            throw new IllegalArgumentException("vectors not the same length");
        if(getOrientation()==VectorOrientation.COLUMN_MAJOR || other.getOrientation()==VectorOrientation.ROW_MAJOR)
            throw new IllegalArgumentException("vectors are not in row · column orientation" + other.getOrientation().toString());
        }
        //readLock();
        //other.readLock();
        try {
            double val = 0.0;
            for(int i=0;i<vector.length;i++) {
                val += get(i)*other.get(i);
            }
            //other.readUnlock();
            //readUnlock();
            return val;
        }
        finally {
            //other.readUnlock();
            //readUnlock();
        }
    }

    public void vecMatMul(SharedMatrix matrix) {
        {//errors
        if(getOrientation()!=VectorOrientation.ROW_MAJOR)
            throw new IllegalAccessError("this is not a row vector");
        if(matrix.getOrientation()==VectorOrientation.ROW_MAJOR)
            if(length()!=matrix.length())
                throw new IllegalArgumentException("matrix and vector arnt of compatable size for multiplication");
        else if(length()!=matrix.get(0).length())
            throw new IllegalArgumentException("matrix and vector arnt of compatable size for multiplication");
        }
        
        try {
            if(matrix.getOrientation()==VectorOrientation.ROW_MAJOR)
                throw new IllegalArgumentException("matrix is in ROW MAJOR");
                //matrix.loadColumnMajor(matrix.readRowMajor());

            double[] vec = new double[matrix.length()];
            //readLock();
            for(int i=0;i<matrix.length();i++) {
                vec[i] = this.dot(matrix.get(i));
            }
            //readUnlock();
            //writeLock();
            vector = vec;
        }
        finally {
            //readUnlock();
            //writeUnlock();
        }
    }
}
