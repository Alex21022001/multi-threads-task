import com.alexsitiy.task.Debouncer;
import com.alexsitiy.task.DebouncerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentTaskTest {

    private Debouncer<Runnable> db;

    @BeforeEach
    void init() {
        db = new DebouncerImpl();
    }

    @Test
    void test() throws InterruptedException {
        AtomicInteger rx_count = new AtomicInteger();
        AtomicInteger ry_count = new AtomicInteger();

        Runnable rx = () -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("x");
            rx_count.incrementAndGet();
        };
        Runnable ry = () -> {
            System.out.println("y");
            ry_count.incrementAndGet();
        };

        for (int i = 0; i < 8; i++) {
            Thread.sleep(50);
            db.call(rx);
            Thread.sleep(50);
            db.call(ry);
        }
        Thread.sleep(200); // expecting x and y
        assertEquals(1, rx_count.get());
        assertEquals(1, ry_count.get());

        for (int i = 0; i < 10000; i++) {
            db.call(rx);
        }
        Thread.sleep(2_400); // expecting only x
        assertEquals(2, rx_count.get());
        assertEquals(1, ry_count.get());

        db.call(ry);
        Thread.sleep(1_100); // expecting only y
        assertEquals(2, rx_count.get());
        assertEquals(2, ry_count.get());
        db.shutdown();
    }
}
