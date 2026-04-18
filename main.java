public class Main {
    public static void main(String[] args) throws InterruptedException {
        ThreadPool pool = new ThreadPool(3, 10);

        for (int i = 1; i <= 6; i++) {
            final int jobId = i;
            pool.submit(new job() {
                public void execute() {
                    System.out.println("Job " + jobId + " running on " + Thread.currentThread().getName());
                }
            });
        }

        pool.shutdown();
    }
}
