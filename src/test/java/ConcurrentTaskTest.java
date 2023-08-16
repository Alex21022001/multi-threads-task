import com.alexsitiy.task.Debouncer;
import com.alexsitiy.task.DebouncerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.alexsitiy.util.TimeWaiter.waitTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentTaskTest {

    private Debouncer<Runnable> db;

    @BeforeEach
    void init() {
        db = new DebouncerImpl(1000L);
    }

    @Test
    void test() {
        AtomicInteger rx_count = new AtomicInteger();
        AtomicInteger ry_count = new AtomicInteger();

        Runnable rx = () -> {
            System.out.println("x");
            rx_count.incrementAndGet();
            waitTime(500);
        };
        Runnable ry = () -> {
            System.out.println("y");
            ry_count.incrementAndGet();
        };
        db.call(rx);
        db.call(ry);
        waitTime(50); //I've added this because if I run it, the main Thread will run XR and YR tasks but the main Thread will be faster and invoke assert.
        assertEquals(1, rx_count.get());
        assertEquals(1, ry_count.get());
        waitTime(950);// In order to wait so that XR and YR tasks' intervals finished.

        for (int i = 0; i < 8; i++) {
            waitTime(50);
            db.call(rx);
            waitTime(50);
            db.call(ry);
        }

        waitTime(200); // expecting x and y
        assertEquals(2, rx_count.get());
        assertEquals(2, ry_count.get());

        for (int i = 0; i < 10000; i++) {
            db.call(rx);
        }
        waitTime(2400); // expecting only x
        assertEquals(3, rx_count.get());
        assertEquals(2, ry_count.get());

        db.call(ry);
        waitTime(1_100); // expecting only y
        assertEquals(3, rx_count.get());
        assertEquals(3, ry_count.get());
        db.shutdown();
    }

}
