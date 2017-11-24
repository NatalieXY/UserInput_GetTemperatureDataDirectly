package bluetooth.newService;

public interface Mapper<T, R> {
    R map(T t) throws Exception;
}
