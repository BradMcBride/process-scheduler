import java.util.LinkedList;
import java.util.Queue;

public class TaskLRU implements Runnable{
    private int[] sequence;
    private int maxMemoryFrames;
    private int maxPageReference;
    private int[] pageFaults;
    private int pageFaultCount = 0;
    private Queue<Integer> memoryQueue = new LinkedList<>();
    public TaskLRU(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults){
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;

    }
    public void run(){
        for (int i = 0; i < sequence.length; i++){
            int curFrame = sequence[i];

            if (memoryQueue.contains(curFrame)){
                memoryQueue.remove(curFrame);
                memoryQueue.add(curFrame);
            }
            else{
                pageFaultCount++;
                if (memoryQueue.size() >= maxMemoryFrames) {
                    memoryQueue.poll();
                }
                memoryQueue.add(curFrame);
            }
        }

        pageFaults[maxMemoryFrames] = pageFaultCount;

    }
}
