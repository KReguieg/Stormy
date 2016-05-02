package de.flowment.stormy.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.flowment.stormy.R;
import de.flowment.stormy.weather.Current;
import de.flowment.stormy.weather.Day;
import de.flowment.stormy.weather.Forecast;
import de.flowment.stormy.weather.Hour;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();
    @Bind(R.id.time_textView)
    TextView mTimeLabel;
    @Bind(R.id.temperature_label)
    TextView mTemperatureLabel;
    @Bind(R.id.humidity_value_textView)
    TextView mHumidityValue;
    @Bind(R.id.precip_value_textView)
    TextView mPrecipValue;
    @Bind(R.id.summary_textView)
    TextView mSummaryLabel;
    @Bind(R.id.icon_imageView)
    ImageView mIconImageView;
    @Bind(R.id.refresh_imageView)
    ImageView mRefreshImageView;
    @Bind(R.id.location_textView)
    TextView mLocationLabel;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;

    private Forecast mForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);
        final double latitude = 52.5243700;
        final double longitude = 13.4105300;

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getForecast(latitude, longitude);
            }
        });

        getForecast(latitude, longitude);

        Log.d(TAG, "Main UI code is running!");
        Log.d(TAG, new Day((long) longitude, "lol", 20, "bestIcon", "berlin").toString());

    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "bc5a85509612dff7c669207612f8f34a";
        String forecastURL = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {

            toggleRefresh();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IOException caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException caught: ", e);
                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.network_notavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        mLocationLabel.setText(current.getTimeZone());
        mTemperatureLabel.setText(String.format("%d", current.getTemperature()));
        mTimeLabel.setText(String.format(getString(R.string.time_label_text), current.getFormattedTime()));
        mHumidityValue.setText(String.format("%d", ((int) current.getHumidity())));
        mPrecipValue.setText(String.format(getString(R.string.precip_value_text), current.getPrecipChance()));
        mSummaryLabel.setText(current.getSummary());
        Drawable iconDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            iconDrawable = getResources().getDrawable(current.getIconId(), null);
        } else {
            iconDrawable = getResources().getDrawable(current.getIconId());
        }
        mIconImageView.setImageDrawable(iconDrawable);
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            days[i] = new Day(
                    jsonDay.getLong("time"),
                    jsonDay.getString("summary"),
                    jsonDay.getDouble("temperatureMax"),
                    jsonDay.getString("icon"),
                    timezone
            );
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");
        Hour[] hours = new Hour[data.length()];
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            hours[i] = new Hour(
                    jsonHour.getLong("time"),
                    jsonHour.getString("summary"),
                    jsonHour.getDouble("temperature"),
                    jsonHour.getString("icon"),
                    timezone
            );
        }
        return hours;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currently = forecast.getJSONObject("currently");
        Log.i(TAG, "From JSON: " + currently);

        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        return current;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

}
