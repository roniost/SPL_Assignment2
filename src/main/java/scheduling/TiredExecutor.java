package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        this.workers= new TiredThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            double fatigueFactor = Math.random()+0.5; 
            workers[i] = new TiredThread(i, fatigueFactor);
            idleMinHeap.add(workers[i]);
            workers[i].start(); 
        }
    }

    public void submit(Runnable task) {
        synchronized(this){
            while (idleMinHeap.isEmpty()) { // wait until a worker is free
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return; // needed?
                }
            }
            TiredThread worker = idleMinHeap.poll();
            inFlight.incrementAndGet(); //יש חשמל באוויר
            Runnable wrappedTask = () -> {
                try {
                    task.run();
                } finally {
                    synchronized(this) {
                        idleMinHeap.add(worker);
                        inFlight.decrementAndGet(); //החשמל נגמר
                        TiredExecutor.this.notifyAll(); 
                    }
                }
            };
            worker.newTask(wrappedTask);
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        for (Runnable task : tasks) {
            submit(task);
        }

        synchronized(this) {
            while (inFlight.get() > 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
        for (TiredThread worker : workers) {
           if (worker!=null) {
                worker.shutdown();
           }
        }
    }

    public synchronized String getWorkerReport() {
        StringBuilder report = new StringBuilder();
        for (TiredThread worker : workers) {
            report.append("Worker " + worker.getWorkerId() + ":\n")
                  .append(" - Fatigue: " + worker.getFatigue() + "\n")
                  .append(" - Idle:" + worker.getTimeIdle() + "ms\n")
                  .append(" - Work:" + worker.getTimeUsed() + "ms\n");
    }
    return report.toString();
    }
}
