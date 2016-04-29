package cc.fish.fishhttp.thread;

/**
 * Created by fish on 16-4-27.
 */

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ThreadPoolManager {
    private static final String TAG = ThreadPoolManager.class.getSimpleName();

    private ExecutorService service;
    private ExecutorService serialService;
    private ThreadPoolManager(){
        int num = Runtime.getRuntime().availableProcessors();
        service = Executors.newFixedThreadPool(num*2);
        serialService = Executors.newSingleThreadExecutor();
    }

    private static ThreadPoolManager manager;


    public static ThreadPoolManager getInstance(){
        if(manager==null) {
            manager= new ThreadPoolManager();
        }
        return manager;
    }

    public void addTask(Runnable runnable){
        service.submit(runnable);
    }
    public void executeSerialized(final Callable<Void> callable, final long timeout) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final FutureTask<Void> future = new FutureTask<Void>(callable);
                service.execute(future);
                if (timeout <= 0) {
                    return;
                }
                try {
                    future.get(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                } catch (TimeoutException e) {
                    if (!future.isCancelled() && !future.isDone()) {
                        future.cancel(true);
                    }
                }
            }
        };
        serialService.submit(runnable);
    }
}

