package de.flowment.stormy.ui;

import android.app.ListActivity;
import android.os.Bundle;

import de.flowment.stormy.R;

public class DailyForecastActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
    }
}
