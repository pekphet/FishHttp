package cc.fish.fishhttp.thread;

/**
 * Created by fish on 16-4-27.
 */
public interface Done<T> {
    void run(T t);
}
