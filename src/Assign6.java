import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Assign6 {
    public static void main(String[] args) {
        Random rand = new Random();
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        int minFIFOPageFault = 0;
        int minLRUPageFault = 0;
        int minMRUPageFault = 0;

        int FIFODelta = 0;
        int LRUDelta = 0;
        int MRUDelta = 0;

        int FIFODeltaCount = 0;
        int LRUDeltaCount = 0;
        int MRUDeltaCount = 0;

        long timeStart = System.currentTimeMillis();
        StringBuilder anomalyReportFIFO = new StringBuilder();
        StringBuilder anomalyReportLRU = new StringBuilder();
        StringBuilder anomalyReportMRU = new StringBuilder();

        for (int simNum = 1; simNum <= 1000; simNum++){
            ExecutorService pool = Executors.newFixedThreadPool(availableProcessors);

            int[] pageFaultsFIFO = new int[101];
            int[] pageFaultsLRU = new int[101];
            int[] pageFaultsMRU = new int[101];

            int[] randomSequence = new int[1000];
            for (int randVal = 0; randVal < 1000; randVal++){
                randomSequence[randVal] = rand.nextInt(250) + 1;
            }

            for (int mainMemoryFrames = 1; mainMemoryFrames <= 100; mainMemoryFrames++) {
                Runnable fifo = new TaskFIFO(randomSequence, mainMemoryFrames, 250, pageFaultsFIFO);
                Runnable lru = new TaskLRU(randomSequence, mainMemoryFrames, 250, pageFaultsLRU);
                Runnable mru = new TaskMRU(randomSequence, mainMemoryFrames, 250, pageFaultsMRU);

                pool.submit(fifo);
                pool.submit(lru);
                pool.submit(mru);
            }

            pool.shutdown();

            //Wait for the threads to finish
            try {
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow();
            }


            //compare (find page fault comparison and max delta)
            for (int i = 0; i <= 100; i++){

                if (pageFaultsFIFO[i] <= pageFaultsLRU[i] && pageFaultsFIFO[i] <= pageFaultsMRU[i]){
                    minFIFOPageFault++;
                }
                if (pageFaultsLRU[i] <= pageFaultsFIFO[i] && pageFaultsLRU[i] <= pageFaultsMRU[i]){
                    minLRUPageFault++;
                }
                if (pageFaultsMRU[i] <= pageFaultsLRU[i] && pageFaultsMRU[i] <= pageFaultsFIFO[i]){
                    minMRUPageFault++;
                }
                if (i > 1){
                    if (pageFaultsFIFO[i] > pageFaultsFIFO[i-1]){
                        anomalyReportFIFO.append(String.format("\t\tAnomaly detected in simulation #%03d - %d PF's @ %3d frames vs. %d PF's @ %3d frames (Δ%d)\n", simNum, pageFaultsFIFO[i-1], i-1, pageFaultsFIFO[i], i, (pageFaultsFIFO[i] - pageFaultsFIFO[i-1])));
                        FIFODelta = Math.max(FIFODelta, (pageFaultsFIFO[i] - pageFaultsFIFO[i-1]));
                        FIFODeltaCount++;
                    }
                    if (pageFaultsLRU[i] > pageFaultsLRU[i-1]){
                        anomalyReportLRU.append(String.format("\t\tAnomaly detected in simulation #%03d - %d PF's @ %d frames vs. %d PF's @ %d frames (Δ%d)\n", simNum, pageFaultsLRU[i-1], i-1, pageFaultsLRU[i], i, (pageFaultsLRU[i] - pageFaultsLRU[i-1])));
                        LRUDelta = Math.max(LRUDelta, (pageFaultsLRU[i] - pageFaultsLRU[i-1]));
                        LRUDeltaCount++;
                    }
                    if (pageFaultsMRU[i] > pageFaultsMRU[i-1]){
                        anomalyReportMRU.append(String.format("\t\tAnomaly detected in simulation #%03d - %d PF's @ %d frames vs. %d PF's @ %d frames (Δ%d)\n", simNum, pageFaultsMRU[i-1], i-1, pageFaultsMRU[i], i, (pageFaultsMRU[i] - pageFaultsMRU[i-1])));
                        MRUDelta = Math.max(MRUDelta, (pageFaultsMRU[i] - pageFaultsMRU[i-1]));
                        MRUDeltaCount++;
                    }

                }
            }

        }
        long timeEnd = System.currentTimeMillis();


        //Report Section
        System.out.println("Simulation took " + (timeEnd - timeStart) + " ms\n");
        System.out.println("FIFO min PF: " + minFIFOPageFault);
        System.out.println("LRU min PF: " + minLRUPageFault);
        System.out.println("MRU min PF: " + minMRUPageFault);
        System.out.println();

        System.out.println("Belady's Anomaly Report for FIFO");
        System.out.print(anomalyReportFIFO);
        System.out.println("   Anomaly detected " + FIFODeltaCount + " times in 1000 simulations with max delta of " + FIFODelta+"\n");

        System.out.println("Belady's Anomaly Report for LRU");
        System.out.print(anomalyReportLRU);
        System.out.println("   Anomaly detected " + LRUDeltaCount + " times in 1000 simulations with max delta of " + LRUDelta +"\n");

        System.out.println("Belady's Anomaly Report for MRU");
        System.out.print(anomalyReportMRU);
        System.out.println("   Anomaly detected " + MRUDeltaCount + " times in 1000 simulations with max delta of " + MRUDelta+"\n");

//        testLRU();
//        testMRU();
    }
        public static void testLRU() {
            int[] sequence1 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            int[] sequence2 = {1, 2, 1, 3, 2, 1, 2, 3, 4};
            int[] pageFaults = new int[4];  // 4 because maxMemoryFrames is 3

            // Replacement should be: 1, 2, 3, 4, 5, 6, 7, 8
            // Page Faults should be 9
            (new TaskLRU(sequence1, 1, 250, pageFaults)).run();
            System.out.printf("Page Faults: %d\n", pageFaults[1]);

            // Replacement should be: 2, 1, 3, 1, 2
            // Page Faults should be 7
            (new TaskLRU(sequence2, 2, 250, pageFaults)).run();
            System.out.printf("Page Faults: %d\n", pageFaults[2]);

            // Replacement should be: 1
            // Page Faults should be 4
            (new TaskLRU(sequence2, 3, 250, pageFaults)).run();
            System.out.printf("Page Faults: %d\n", pageFaults[3]);
        }
    public static void testMRU() {
        int[] sequence1 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] sequence2 = {1, 2, 1, 3, 2, 1, 2, 3, 4};
        int[] pageFaults = new int[4];  // 4 because maxMemoryFrames is 3

        // Replacement should be: 1, 2, 3, 4, 5, 6, 7, 8
        // Page Faults should be 9
        (new TaskMRU(sequence1, 1, 250, pageFaults)).run();
        System.out.printf("Page Faults: %d\n", pageFaults[1]);

        // Replacement should be: 1, 2, 1, 3
        // Page Faults should be 6
        (new TaskMRU(sequence2, 2, 250, pageFaults)).run();
        System.out.printf("Page Faults: %d\n", pageFaults[2]);

        // Replacement should be: 3
        // Page Faults should be 4
        (new TaskMRU(sequence2, 3, 250, pageFaults)).run();
        System.out.printf("Page Faults: %d\n", pageFaults[3]);
    }
}
