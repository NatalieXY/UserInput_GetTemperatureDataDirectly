package bluetooth.promise;

import android.os.AsyncTask;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DeferedPromise<T> implements Promise<T> {
    public static final int PENDING = 0;
    public static final int RESOLVED = 1;
    public static final int REJECTED = 2;

    private AtomicInteger status = new AtomicInteger(PENDING);
    private AtomicReference<T> result = new AtomicReference<>(null);
    private AtomicReference<Exception> error = new AtomicReference<>(null);
    private ConcurrentLinkedQueue<Runnable> linkedResult = new ConcurrentLinkedQueue<>();

    public void resolve(T result){
        if(status.compareAndSet(PENDING, RESOLVED)){
            this.result.compareAndSet(null, result);
            for(Runnable runnable : linkedResult){
                AsyncTask.execute(runnable);
            }
        }else{
            throw new IllegalStateException("resolve or rejected is already called");
        }
    }
    public void reject(Exception exception){
        if(status.compareAndSet(PENDING, REJECTED)){
            this.error.compareAndSet(null, exception);
            for(Runnable runnable : linkedResult){
                AsyncTask.execute(runnable);
            }
        }else{
            throw new IllegalStateException("resolve or rejected is already called");
        }
    }

    private void registerTask(Runnable runnable){
        if(status.get() == PENDING){
            linkedResult.add(runnable);
        }else{
            AsyncTask.execute(runnable);
        }
    }

    @Override
    public <R> Promise<R> map(Mapper<T, R> mapper) {
        DeferedPromise<R> promise = new DeferedPromise<>();
        Runnable runnable = () -> {
            if(status.get() == RESOLVED){
                try{
                    promise.resolve(mapper.map(result.get()));
                }catch (Exception e){
                    promise.reject(e);
                }
            }else if(status.get() == REJECTED){
                promise.reject(error.get());
            }
        };
        registerTask(runnable);
        return promise;
    }

    @Override
    public <R> Promise<R> flatMap(Mapper<T, Promise<R>> mapper) {
        DeferedPromise<R> promise = new DeferedPromise<>();
        Runnable runnable = () -> {
            if(status.get() == RESOLVED){
                try{
                    Promise<R> middle = mapper.map(result.get());
                    middle.then(r -> promise.resolve(r));
                }catch (Exception e){
                    promise.reject(e);
                }
            }else if(status.get() == REJECTED){
                promise.reject(error.get());
            }
        };
        registerTask(runnable);
        return promise;
    }

    @Override
    public void then(Runner<T> runner) {
        this.map(r -> {
            runner.run(r);
            return null;
        });
    }

    @Override
    public Promise<T> catchError(Mapper<Exception, T> mapper) {
        DeferedPromise<T> promise = new DeferedPromise<>();
        Runnable runnable = () -> {
            if(status.get() == RESOLVED){
                promise.resolve(result.get());
            }else if(status.get() == REJECTED){
                try{
                    promise.resolve(mapper.map(error.get()));
                }catch (Exception e){
                    promise.reject(e);
                }
            }
        };
        registerTask(runnable);
        return promise;
    }

    @Override
    public void onError(Runner<Exception> runner) {
        catchError(err -> {
            runner.run(err);
            return null;
        });
    }
}
