package bluetooth.promise;

/**
 * Promise<T> represent the future value in type T
 * user could not directly fetch the value, instead, user could use
 * map, flatMap, then, catchError, onError and pass a callback to
 * register a future action on it. Some of the member function will
 * return a refresh new promise as result
 * @param <T> the type of the result the promise have
 */
public interface Promise<T> {
    <R> Promise<R> map(Mapper<T, R> mapper);
    <R> Promise<R> flatMap(Mapper<T, Promise<R>> mapper);
    void then(Runner<T> runner);
    Promise<T> catchError(Mapper<Exception, T> mapper);
    void onError(Runner<Exception> runner);

    static <R> Promise<R> immediate(R r){
        DeferedPromise<R> result = new DeferedPromise<>();
        result.resolve(r);
        return result;
    }

    static <R> Promise<R> immediateError(Exception e){
        DeferedPromise<R> result = new DeferedPromise<>();
        result.reject(e);
        return result;
    }

    static <R> DeferedPromise<R> defer(){
        return new DeferedPromise<>();
    }
}
