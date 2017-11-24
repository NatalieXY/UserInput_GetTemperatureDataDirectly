package bluetooth.promise;

public interface Runner<T> {
    void run(T t) throws Exception;
}
