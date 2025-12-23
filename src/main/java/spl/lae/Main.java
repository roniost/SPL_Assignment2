package spl.lae;
import parser.*;
import scheduling.TiredThread;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Starting TiredThread Test ---");

        TiredThread t1 = new TiredThread(1, 1.0);
        TiredThread t2 = new TiredThread(2, 2.0);

        t1.start();
        t2.start();

        System.out.println("Threads started. Sending tasks...");

        // --- תיקון 1: קיצור זמן המשימה ---
        // שיניתי מ-1000 ל-50 מילישניות.
        // ככה אנחנו בטוחים שהם יסיימו לפני שה-Main (שמחכה 200) יבדוק אותם.
        Runnable heavyWork = () -> {
            try {
                Thread.sleep(50); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        // --- תיקון 2: הפרדת ניסיונות השליחה ---
        // אם t1 זורק שגיאה, אנחנו עדיין רוצים לנסות את t2
        try {
            t1.newTask(heavyWork);
        } catch (IllegalStateException e) {
            System.out.println("Worker 1 busy/full: " + e.getMessage());
        }

        try {
            t2.newTask(heavyWork);
        } catch (IllegalStateException e) {
            System.out.println("Worker 2 busy/full: " + e.getMessage());
        }

        // נחכה 200ms. זה מספיק זמן כי המשימה לוקחת רק 50ms
        Thread.sleep(200);

        // 4. בדיקת תוצאות (Fatigue)
        System.out.println("\n--- Results after task ---");
        System.out.println("Worker 1 (Factor 1.0) Fatigue: " + t1.getFatigue());
        System.out.println("Worker 2 (Factor 2.0) Fatigue: " + t2.getFatigue());

        // בדיקה שהחישוב נכון
        // אנחנו בודקים ש-t2 גדול מ-t1 (וגם ששניהם גדולים מ-0, ליתר ביטחון)
        if (t2.getFatigue() > t1.getFatigue() && t1.getFatigue() > 0) {
            System.out.println(">> SUCCESS: Worker 2 is more tired as expected.");
        } else {
            System.out.println(">> FAIL: Logic error. Fatigues are: " + t1.getFatigue() + ", " + t2.getFatigue());
        }

        // 5. בדיקת המיון (compareTo)
        System.out.println("\n--- Testing compareTo ---");
        if (t1.compareTo(t2) < 0) {
            System.out.println(">> SUCCESS: Worker 1 comes before Worker 2.");
        } else {
            System.out.println(">> FAIL: compareTo logic inverted.");
        }

        // 6. בדיקת כיבוי
        System.out.println("\n--- Testing Shutdown ---");
        t1.shutdown();
        t2.shutdown();

        t1.join();
        t2.join();

        if (!t1.isAlive() && !t2.isAlive()) {
            System.out.println(">> SUCCESS: Both threads shutdown cleanly.");
        } else {
            System.out.println(">> FAIL: Threads stuck.");
        }
    }
}