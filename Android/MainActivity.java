package haydut.linegraph;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private String TAG = "DATA";

    private ProgressDialog pDialog;

    private static String url = "http://temp.haydut.xyz/api/GetTemp?GetTempToken=xxx";
    ArrayList<Entry> TempValue;
    ArrayList<String> TempTime;
    LineData data;
    LineDataSet dataset;
    LineChart lineChart;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineChart = (LineChart) findViewById(R.id.chart);
        //Grafik ayarları
        //https://github.com/PhilJay/MPAndroidChart/wiki/General-Chart-Settings-&-Styling
        lineChart.setDescription("Sıcaklık");


        TempValue = new ArrayList<>();
        dataset = new LineDataSet(TempValue, "");
        TempTime = new ArrayList<String>();
        dataset.setColors(ColorTemplate.PASTEL_COLORS); //
        dataset.setDrawCubic(true);
        dataset.setDrawFilled(true);
        new GetContacts().execute();
        Button button= (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TempTime.clear();
                TempValue.clear();
                new GetContacts().execute();
            }
        });
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Lütfen Bekleyiniz..");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "URL: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // {"JSONTemp":[{"value":20,"time":"21:33:52"},
                    // {"value":21,"time":"21:33:55"},
                    // {"value":22,"time":"21:33:58"},
                    // {"value":23,"time":"21:34:02"}]}
                    JSONArray contacts = jsonObj.getJSONArray("JSONTemp");

                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String value = c.getString("value");
                        String time = c.getString("time");
                        TempTime.add(time);
                        TempValue.add(new Entry(Integer.parseInt(value), i));
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json hata: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json hata: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Sunucu hatası.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Sunucu hatası!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            data = new LineData(TempTime, dataset);
            Log.v("DATA",data+"");
            lineChart.setData(data);
            lineChart.animateY(500);
            lineChart.animateX(500);
            lineChart.invalidate(); // refresh
        }

    }

}

