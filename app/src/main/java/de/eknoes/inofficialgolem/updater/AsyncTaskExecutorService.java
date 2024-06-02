package de.eknoes.inofficialgolem.updater;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class AsyncTaskExecutorService<Params> {
    private final ExecutorService executorService;
    private Handler handler;
    private Future<?> future;

    protected AsyncTaskExecutorService() {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Handler getHandler() {
        if (handler == null) {
            synchronized (AsyncTaskExecutorService.class) {
                handler = new Handler(Looper.getMainLooper());
            }
        }
        return handler;
    }

    protected void onPreExecute() {

    }

    protected abstract GolemFetcher.FETCH_STATE doInBackground(Params params);

    protected abstract void onPostExecute(GolemFetcher.FETCH_STATE result);

    public void execute() {
        execute(null);
    }

    public void execute(Params params) {
        getHandler().post(() -> {
            onPreExecute();
            future = executorService.submit(() -> {
                GolemFetcher.FETCH_STATE result = doInBackground(params);
                getHandler().post(() -> onPostExecute(result));
            });
        });
    }

    public void shutDown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public boolean isCancelled() {
        return executorService == null || executorService.isTerminated() || executorService.isShutdown();
    }

    public RunningState getStatus(){
        return future.isDone() ? RunningState.FINISHED : RunningState.RUNNING;
    }

}
