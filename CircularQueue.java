public class CircularQueue {

    private final job[] buffer;
    private final int   capacity;
    private int         head = 0;
    private int         tail = 0;
    private int         size = 0;

    public CircularQueue(int capacity)
    {
        this.capacity = capacity;
        this.buffer = new job[capacity];
    }

    public boolean enqueue(job Job)
    {
        if (size == capacity) return false;
        buffer[tail] = Job;
        tail = (tail + 1) % capacity;
        size ++;
        return true;
    }

    public job dequeue()
    {
        if (size == 0) return null;
        job Job = buffer[head];
        head = (head+1) % capacity;
        size--;
        return Job;
    }

    public boolean isEmpty() {return size == 0;}
    public boolean isFull(){ return size == capacity;}
    public int size() {return size;}

}
