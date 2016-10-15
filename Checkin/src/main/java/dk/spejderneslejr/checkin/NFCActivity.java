package dk.spejderneslejr.checkin;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;


import java.io.IOException;

public class NFCActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, SearchView.OnQueryTextListener
{
    private TextView mTextView;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.data);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null)
		{
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }
        else
        {
            if (!mNfcAdapter.isEnabled()) {
                mTextView.setText("NFC is disabled.");
            } else {
                mTextView.setText("NFC is ready... Scan something");
            }
        }

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        //searchView.setOnQueryTextListener(this);

        SharedPreferences preferences = getSharedPreferences(
            getString(R.string.tag_list_key), Context.MODE_PRIVATE);
        preferences.getAll();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(mNfcAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public void setupForegroundDispatch(NfcAdapter adapter) {
/*
        Log.d("sl", "Setting up foreground dispatch");

        final Activity activity = this;

        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        /*try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }*/

        /*
        adapter.enableForegroundDispatch(activity, pendingIntent, null, techList);
        */
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        /*if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else */
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action))
		{
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String mifare = MifareClassic.class.getName();

            for (String tech : techList)
			{
                Log.d("sl", "Tech: " + tech);

                if (mifare.equals(tech)) {
                    new MifareReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */
    private class MifareReaderTask extends AsyncTask<Tag, Void, String>
	{
        String result = "";

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            MifareClassic mfc = MifareClassic.get(tag);
            try {
                mfc.connect();
                /*int sectorNumber = 1;

                if (mfc.authenticateSectorWithKeyA(sectorNumber,
                        MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                    Log.d("TAG", "Authorized sector with MAD key");

                } else if (mfc.authenticateSectorWithKeyA(
                        sectorNumber, MifareClassic.KEY_DEFAULT)) {
                    Log.d("TAG",
                            "Authorization granted to sector  with DEFAULT key");

                } else if (mfc
                        .authenticateSectorWithKeyA(sectorNumber,
                                MifareClassic.KEY_NFC_FORUM)) {
                    Log.d("TAG",
                            "Authorization granted to sector with NFC_FORUM key");

                } else {
                    Log.d("TAG", "Authorization denied ");
                }*/
            } catch (IOException e) {
                e.printStackTrace();
            }


            Log.d("sl", "Sectors: " + mfc.getSectorCount() );

            for (int s = 0; s < mfc.getSectorCount(); s++) {

                try {
                    if (mfc.authenticateSectorWithKeyA(s, MifareClassic.KEY_DEFAULT) || mfc.authenticateSectorWithKeyA(s, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY) || mfc.authenticateSectorWithKeyA(s, MifareClassic.KEY_NFC_FORUM)) {
                            Log.d("TAG",
                                    "Authorization granted to sector " + s + "  with DEFAULT key");

                            for (int b = 0; b < mfc.getBlockCount(); b++) {
                                try {
                                    result += bytesToHex(mfc.readBlock(b));
                                    //result += new String(mfc.readBlock(b), "ISO-8859-1");
                                } catch (IOException e) {
                                    //e.printStackTrace();
                                }
                            }

                            result += "\n\n";
                        } else {
                            Log.d("sl", "Authorization denied to sector: " + s);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return result;
        }

        final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
        public String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 3];
            for ( int j = 0; j < bytes.length; j++ ) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
                hexChars[j * 2 + 2] = ' ';

            }
            return new String(hexChars);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null)
            {
                mTextView.setText(result);
            }
        }
    }

}
