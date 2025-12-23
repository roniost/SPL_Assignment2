package scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutorTester {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Starting TiredExecutor Test ---");

        // 1. הקמת המפעל עם 3 עובדים
        int numThreads = 3;
        TiredExecutor executor = new TiredExecutor(numThreads);
        System.out.println("Executor initialized with " + numThreads + " threads.");

        // 2. הכנת אצווה של משימות (Batch)
        // ניצור 15 משימות. מכיוון שיש רק 3 עובדים, הם יצטרכו לעשות "סבבים".
        int totalTasks = 15;
        AtomicInteger completedTasks = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < totalTasks; i++) {
            final int taskId = i;
            tasks.add(() -> {
                try {
                    // כל משימה לוקחת קצת זמן (כדי שנראה את העומס)
                    Thread.sleep(50); 
                    completedTasks.incrementAndGet();
                    // System.out.println("Task " + taskId + " completed."); // אפשר להוריד הערה לדיבאג
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 3. הרצת המשימות ומדידת זמנים
        System.out.println("Submitting " + totalTasks + " tasks using submitAll...");
        long startTime = System.currentTimeMillis();

        // --- כאן קורה הקסם ---
        // הפונקציה הזו חייבת לחסום את ה-Main Thread עד שהכל יסתיים
        executor.submitAll(tasks); 
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("submitAll returned! Total time: " + duration + "ms");

        // 4. אימות תוצאות
        // בדיקה 1: האם כל המשימות בוצעו?
        if (completedTasks.get() == totalTasks) {
            System.out.println(">> SUCCESS: All " + totalTasks + " tasks completed.");
        } else {
            System.out.println(">> FAIL: Only " + completedTasks.get() + " tasks completed. Race condition likely.");
        }

        // בדיקה 2: האם זה באמת לקח זמן? (הוכחה שזה חסם)
        // אם הכל רץ במקביל עם 3 עובדים, הזמן צריך להיות בערך: (15 משימות / 3 עובדים) * 50ms = 250ms
        if (duration >= 200) { 
            System.out.println(">> SUCCESS: Blocking behavior verified (Duration seems logical).");
        } else {
            System.out.println(">> WARNING: Finished too fast? Check if submitAll waits correctly.");
        }

        // 5. הדפסת דוח עובדים (אם מימשת את הבונוס getWorkerReport)
        // זה יראה לנו מי עבד וכמה עייף הוא
        try {
            System.out.println("\n--- Worker Report ---");
            System.out.println(executor.getWorkerReport());
        } catch (Exception e) {
            System.out.println("getWorkerReport not implemented or failed.");
        }

        // 6. סגירת המפעל
        executor.shutdown();
        System.out.println("Executor shutdown initiated.");
    }
}