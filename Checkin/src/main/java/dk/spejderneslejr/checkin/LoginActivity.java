package dk.spejderneslejr.checkin;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import dk.spejderneslejr.checkin.comm.Callback;
import dk.spejderneslejr.checkin.comm.HttpClient;
import dk.spejderneslejr.checkin.comm.request.LoginRequest;

public class LoginActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText user = (EditText) findViewById(R.id.user);
        final EditText password = (EditText) findViewById(R.id.password);

        Button clickButton = (Button) findViewById(R.id.submit);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                LoginRequest login = new LoginRequest(user.getText().toString(), password.getText().toString(), LoginActivity.this);
                login.perform(new Callback<LoginRequest.Status>()
                {
                    @Override
                    public void call(LoginRequest.Status status)
                    {
                        if (status == LoginRequest.Status.SUCCESS)
                        {
                            Intent intent = new Intent(LoginActivity.this, NFCActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            ((TextView) findViewById(R.id.data)).setText(status.name());
                        }
                    }

                    @Override
                    public void call(HttpClient.ConnState state) {
                        ((TextView) findViewById(R.id.data)).setText(state.name());
                    }
                });
            }
        });
    }

}
