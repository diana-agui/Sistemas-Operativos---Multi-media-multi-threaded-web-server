import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPool {

    private final CircularQueue queue;
    private final Thread[]      workers;
    private final ReentrantLock lock     = new ReentrantLock();
    private final Condition     hasJob   = lock.newCondition();
    private volatile boolean    shutdown = false;

    public ThreadPool(int threadCount, int queueCapacity) {
        this.queue   = new CircularQueue(queueCapacity);
        this.workers = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int id = i + 1;
            workers[i] = new Thread(() -> workerLoop(id), "Worker-" + id);
            workers[i].setDaemon(false);
            workers[i].start();
        }
    }

    private void workerLoop(int id) {
        while (true) {
            job Job;
            lock.lock();
            try {
                while (queue.isEmpty() && !shutdown) {
                    System.out.println("[Worker-" + id + "] No jobs — sleeping.");
                    hasJob.await();
                }
                if (shutdown && queue.isEmpty()) return;
                Job = queue.dequeue();
                System.out.println("[Worker-" + id + "] Woke up, executing job.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                lock.unlock();
            }

            Job.execute();
        }
    }

    public boolean submit(job Job) {
        lock.lock();
        try {
            if (shutdown) throw new IllegalStateException("Pool is shut down.");
            boolean accepted = queue.enqueue(Job);
            if (accepted) hasJob.signal();
            return accepted;
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() throws InterruptedException {
        lock.lock();
        try {
            shutdown = true;
            hasJob.signalAll();
        } finally {
            lock.unlock();
        }
        for (Thread w : workers) w.join();
        System.out.println("Pool shut down cleanly.");
    }
}
