package dk.spejderneslejr.checkin.comm;

import android.util.Log;
import okhttp3.*;
import okhttp3.Request;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class HttpClient
{
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    public static final int HTTP_OK = 200;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;

    private static final int MAX_RETRIES = 3;
    private static final int CONN_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 30000;

    private String url, content;
    private RetryStrategy strategy;
    private int retries = 0;

    public class ConnectTimeoutException extends SocketTimeoutException { }
    public class ReadTimeoutException extends SocketTimeoutException { }

    public enum RetryStrategy
    {
        NONE,
        ON_CONNECT_FAILURE,
        ON_READ_FAILURE,
        ALWAYS
    }

    public enum ConnState
    {
        OK,
        CONNECTION_TIMEOUT,
        READ_TIMEOUT,
        CONNECTION_FAILED,
        TEMPORARILY_UNAVAILABLE,
        BROKEN,
        AIRPLANE_MODE,
        NO_INTERNET
    }

    public HttpClient(String url, String content)
    {
        this(url, content, RetryStrategy.ON_CONNECT_FAILURE);
    }

    public HttpClient(String url, String content, RetryStrategy strategy)
    {
        this.url = url;
        this.content = content;
        this.strategy = strategy;
    }

    private OkHttpClient createHttpClient()
    {
        return new OkHttpClient.Builder()
                .connectTimeout(CONN_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS).build();
    }

    public HttpResponse post()
    {
        OkHttpClient client = createHttpClient();

        try
        {
            RequestBody body = RequestBody.create(JSON, content);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.code() == HTTP_SERVICE_UNAVAILABLE)
                fail(ConnState.TEMPORARILY_UNAVAILABLE);

            return new HttpResponse(response.code(), null);
        }
        catch (SocketTimeoutException e)
        {
            if (e.getMessage().contains("connect"))
                return fail(ConnState.CONNECTION_TIMEOUT, e);
            else
                return fail(ConnState.READ_TIMEOUT, e);
        }
        catch (UnknownHostException e)
        {
            return fail(ConnState.CONNECTION_FAILED, e);
        }
        catch (IOException e)
        {
            return fail(ConnState.BROKEN, e);
        }
    }

    private HttpResponse fail(ConnState state)
    {
        return fail(state, null);
    }

    private HttpResponse fail(ConnState state, IOException e)
    {
        if (retries < MAX_RETRIES)
        {
            if (strategy == RetryStrategy.ALWAYS) return retry();

            switch (state)
            {
                case CONNECTION_FAILED:
                case CONNECTION_TIMEOUT:
                case TEMPORARILY_UNAVAILABLE:
                    if (strategy == RetryStrategy.ON_CONNECT_FAILURE) return retry();
                    break;
                case READ_TIMEOUT:
                case BROKEN:
                    if (strategy == RetryStrategy.ON_READ_FAILURE) return retry();
                    break;
            }
        }

        Log.e(getClass().getName(), "Giving up: ", e);

        return new HttpResponse(state);
    }

    private HttpResponse retry()
    {
        Log.d(getClass().getName(), "Retrying " + retries + " / " + MAX_RETRIES);

        retries++;
        return post();
    }
}
