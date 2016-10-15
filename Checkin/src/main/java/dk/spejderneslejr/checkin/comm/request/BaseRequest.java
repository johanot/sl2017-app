package dk.spejderneslejr.checkin.comm.request;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import dk.spejderneslejr.checkin.comm.HttpClient;
import dk.spejderneslejr.checkin.comm.HttpResponse;

import java.util.concurrent.ExecutionException;

public abstract class BaseRequest
{
    private final transient Context context;

    public BaseRequest()
    {
        this(null);
    }

    public BaseRequest(Context context)
    {
        this.context = context;
    }

    protected class Task extends AsyncTask<Void, Void, HttpResponse>
    {
        @Override
        protected HttpResponse doInBackground(Void... params)
        {
            HttpResponse initialError = errorDetection();

            if (initialError == null)
            {
                HttpClient client = new HttpClient(getURL(), toJSONString(), getRetryStrategy());
                return client.post();
            }
            else
            {
                return initialError;
            }
        }

        public Task execute()
        {
            return (Task) super.execute((Void)null);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public HttpResponse errorDetection()
        {
            if (context == null) return null;

            if (Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0)

                return new HttpResponse(HttpClient.ConnState.AIRPLANE_MODE);

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm.getActiveNetworkInfo() == null)
                return new HttpResponse(HttpClient.ConnState.NO_INTERNET);


            return null;
        }
    }

    protected abstract String toJSONString();

    public HttpClient.RetryStrategy getRetryStrategy()
    {
        return HttpClient.RetryStrategy.ON_CONNECT_FAILURE;
    }

    public HttpResponse perform()
    {
        Task task = new Task();
        task = task.execute();
        try
        {
            return task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public abstract String getURL();

}
