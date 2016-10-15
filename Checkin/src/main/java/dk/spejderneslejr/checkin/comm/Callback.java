package dk.spejderneslejr.checkin.comm;

public interface Callback<T>
{
    public void call(T object);

    public void call(HttpClient.ConnState object);
}
