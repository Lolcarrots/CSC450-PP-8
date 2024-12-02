import java.util.concurrent.atomic.AtomicLong;

public class ThreadedCounter {
    private static class CountWorker implements Runnable {
        private final ThreadedCounter counter;
        private final boolean isCountingUp;
        // Worker thread for counting
        private CountWorker(ThreadedCounter counter, boolean isCountingUp) {
            this.counter = counter;
            this.isCountingUp = isCountingUp;
        }
        // Calls either countUp or countDown based on the thread label
        @Override
        public void run() {
            String threadLabel = counter.getThreadLabel();
            if (isCountingUp) {
                counter.countUp(threadLabel);
            } else {
                counter.countDown(threadLabel);
            }
        }
    }

    private final AtomicLong firstThreadId;
    private int count;
    private volatile boolean countingUpComplete;
    // Initializing instance variables
    public ThreadedCounter() {
        this.firstThreadId = new AtomicLong();
        this.count = 0;
        this.countingUpComplete = false;
    }
    // Setting the first thread's ID
    private void setFirstThreadId(long id) {
        firstThreadId.set(id);
    }
    // Determining the label of the current thread
    private String getThreadLabel() {
        return (Thread.currentThread().threadId() == firstThreadId.get()) ? "Thread 1: " : "Thread 2: ";
    }
    // Counting up to 20 and setting countingUpComplete to true
    private void countUp(String threadLabel) {
        while (true) {
            synchronized (this) {
                if (count >= 20) break;
                System.out.println(threadLabel + (++count));
                if (count == 20) {
                    countingUpComplete = true;
                    break;
                }
            }
        }
    }
    // Counting down from 20 when countingUpComplete is true
    private void countDown(String threadLabel) {
        while (true) {
            synchronized (this) {
                if (countingUpComplete && count <= 0) break;
                System.out.println(threadLabel + (--count));
            }
        }
    }

    public static void main(String[] args) {
        try {
            ThreadedCounter counter = new ThreadedCounter();
            // Setting up the first thread
            Thread thread1 = new Thread(new CountWorker(counter, true));
            counter.setFirstThreadId(thread1.threadId());
            thread1.start();
            // Setting up the second thread
            Thread thread2 = new Thread(new CountWorker(counter, false));
            thread2.start();
            thread1.join();
            thread2.join();           
            System.out.println("Counting completed!");
        // Exception specific to a particular scenario
        } catch (InterruptedException e) {
            System.err.println("A thread was interrupted!");
            e.printStackTrace();
            System.exit(1);
        // Generic exception to handle things not covered by the previous exception
        } catch (Exception e) {
            System.err.println("An error occurred!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}