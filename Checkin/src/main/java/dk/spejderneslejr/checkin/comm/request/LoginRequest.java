package dk.spejderneslejr.checkin.comm.request;

import android.content.Context;
import com.google.gson.Gson;
import dk.spejderneslejr.checkin.comm.Callback;
import dk.spejderneslejr.checkin.comm.HttpClient;
import dk.spejderneslejr.checkin.comm.HttpClient.ConnState;
import dk.spejderneslejr.checkin.comm.HttpResponse;

public class LoginRequest extends BaseRequest
{
    protected String username;
    protected String password;

    public enum Status
    {
        NOT_CONNECTED,
        AIRPLANE_MODE,
        UNAVAILABLE,
        DENIED,
        INVALID_CREDENTIALS,
        SUCCESS
    }

    public LoginRequest(String username, String password, Context context)
    {
        super(context);

        this.username = username;
        this.password = password;
    }

    public void perform(Callback<Status> callback)
    {
        HttpResponse response = super.perform();

        if (response.getState() == ConnState.OK)
        {
            switch (response.getCode())
            {
                case HttpClient.HTTP_OK:
                    callback.call(Status.SUCCESS);
                    break;
                case HttpClient.HTTP_FORBIDDEN:
                    callback.call(Status.DENIED);
                    break;
            }
        }
        else
        {
            callback.call(response.getState());
        }
    }

    @Override
    protected String toJSONString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);

    }

    @Override
    public String getURL()
    {
        return "http://aegg.dk";
    }

}
