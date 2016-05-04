package de.flowment.stormy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.flowment.stormy.R;
import de.flowment.stormy.weather.Day;

/**
 * Created by
 * Khaled Reguieg (s813812) <a href="mailto:Khaled.Reguieg@gmail.com">Khaled Reguieg, Khaled.Reguieg@gmail.com</a>
 * on 04.05.2016.
 * <p>
 * This class is going to be awesome and needs to be described here.
 */
public class DayAdapter extends BaseAdapter {

    private Context mContext;
    private Day[] mDays;

    public DayAdapter(Context context, Day[] days) {
        mContext = context;
        mDays = days;
    }

    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int position) {
        return mDays[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // new view must be inflated
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.temperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            holder.dayLabel = (TextView) convertView.findViewById(R.id.dayNameTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Day day = mDays[position];
        holder.iconImageView.setImageResource(day.getIconid());
        holder.temperatureLabel.setText(day.getTemperatureMax() + "");
        return null;
    }

    private static class ViewHolder {
        public ImageView iconImageView;
        public TextView temperatureLabel;
        public TextView dayLabel;
    }
}
