import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DebouncerImpl implements Debouncer<Runnable> {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void call(Runnable runnable) {
        executorService.schedule(runnable,1,TimeUnit.SECONDS);
        System.out.println("Schedule is started");
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

}
