import java.util.Stack;

public class TaskMRU implements Runnable{
    private int[] sequence;
    private int maxMemoryFrames;
    private int maxPageReference;
    private int[] pageFaults;
    private int pageFaultCount = 0;
    private Stack<Integer> memoryStack = new Stack<>();
    public TaskMRU(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults){
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;

    }
    public void run(){
        for (int i = 0; i < sequence.length; i++){
            int curFrame = sequence[i];

            if (memoryStack.contains(curFrame)){
                memoryStack.remove((Integer) curFrame);
                memoryStack.push(curFrame);
            }
            else{
                pageFaultCount++;
                if (memoryStack.size() >= maxMemoryFrames) {
                    memoryStack.pop();
                }
                memoryStack.push(curFrame);
            }
        }

        pageFaults[maxMemoryFrames] = pageFaultCount;

    }
}
