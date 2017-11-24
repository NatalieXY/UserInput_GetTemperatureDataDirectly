package bluetooth.promise;

public interface Mapper<T, R> {
    R map(T t) throws Exception;
}
