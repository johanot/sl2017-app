package dk.spejderneslejr.checkin.comm;

import com.google.gson.Gson;
import dk.spejderneslejr.checkin.comm.response.BaseResponse;
import okhttp3.ResponseBody;

public class HttpResponse
{
    private int code;
    private ResponseBody body;
    private HttpClient.ConnState state;

    public HttpResponse(int code, ResponseBody body)
    {
        this.code = code;
        this.body = body;
        this.state = HttpClient.ConnState.OK;
    }

    public HttpResponse(HttpClient.ConnState state)
    {
        this.state = state;
    }

    public int getCode()
    {
        return code;
    }

    public ResponseBody getBody()
    {
        return body;
    }

    public HttpClient.ConnState getState()
    {
        return state;
    }

    public <T extends BaseResponse> T getResponseObject(Class<T> type)
    {
        Gson gson = new Gson();
        return gson.fromJson(body.charStream(), type);
    }
}
