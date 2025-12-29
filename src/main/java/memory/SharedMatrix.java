package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        vectors = new SharedVector[matrix.length];
        for(int i=0;i<matrix.length;i++) {
            vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadRowMajor(double[][] matrix) {
        //if(getOrientation()==VectorOrientation.ROW_MAJOR) return;
        SharedVector[] newmatrix = new SharedVector[matrix.length];
        for(int i=0;i<matrix.length;i++) {
            newmatrix[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        //acquireAllVectorWriteLocks(newmatrix);
        vectors = newmatrix;
        //releaseAllVectorWriteLocks(newmatrix);
    }

    public void loadColumnMajor(double[][] matrix) {
        //if(getOrientation()==VectorOrientation.COLUMN_MAJOR) return;
        //acquireAllVectorReadLocks(vectors);
        SharedVector[] newmatrix = new SharedVector[matrix[0].length];
        double[] vec = new double[matrix.length];
        for(int i=0;i<matrix[0].length;i++) {
            for(int j=0;j<matrix.length;j++) {
                vec[j] = matrix[j][i];
            }
            newmatrix[i] = new SharedVector(vec, VectorOrientation.COLUMN_MAJOR);
        }
        //releaseAllVectorReadLocks(newmatrix);
        //acquireAllVectorWriteLocks(newmatrix);
        vectors = newmatrix;
        //releaseAllVectorWriteLocks(newmatrix);
    }

    public double[][] readRowMajor() {
        double[][] matrix;
        if(getOrientation()==VectorOrientation.ROW_MAJOR) {
            matrix = new double[vectors.length][vectors[0].length()];
            for(int i=0;i<vectors.length;i++)
                for(int j=0;j<vectors[0].length();j++)
                    matrix[i][j] = vectors[i].get(j);
        }
        else {
            matrix = new double[vectors[0].length()][vectors.length];
            for(int i=0;i<vectors.length;i++)
                for(int j=0;j<vectors[0].length();j++) 
                    matrix[j][i] = vectors[i].get(j);
        }
        return matrix;
    }

    public SharedVector get(int index) {
        if(index>=vectors.length || index<0)
            throw new IllegalArgumentException("index out of bounds");
        return vectors[index];
    }

    public int length() {
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        if(vectors.length==0)
            throw new IllegalAccessError("empty matrix has no orientation");
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        for(SharedVector vector : vecs) {
            vector.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        for(SharedVector vector : vecs) {
            vector.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        for(SharedVector vector : vecs) {
            vector.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        for(SharedVector vector : vecs) {
            vector.writeUnlock();
        }
    }
}
