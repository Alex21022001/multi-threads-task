import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DebouncerInvocationHandler implements InvocationHandler {

    private final DebouncerImpl debouncer;
    private final Set<Runnable> usedTask = ConcurrentHashMap.newKeySet();

    public DebouncerInvocationHandler(DebouncerImpl debouncer) {
        this.debouncer = debouncer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("call")) {
            Runnable runnable = (Runnable) args[0];

            if (!usedTask.contains(runnable)) {

                Runnable task = () -> {
                    runnable.run();
                    usedTask.remove(runnable);
                };

                usedTask.add(runnable);
                return method.invoke(debouncer, task);
            }
            return null;
        } else {
            return method.invoke(debouncer, args);
        }
    }
}
