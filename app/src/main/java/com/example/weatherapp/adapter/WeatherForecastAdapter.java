package com.example.weatherapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.Api;
import com.example.weatherapp.R;
import com.example.weatherapp.WeatherForecastResult;
import com.squareup.picasso.Picasso;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.MyViewHolder> {

    Context context;
    WeatherForecastResult weatherForecastResult;

    public WeatherForecastAdapter(Context context, WeatherForecastResult weatherForecastResult) {
        this.context = context;
        this.weatherForecastResult = weatherForecastResult;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_weather_forecast, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                .append(weatherForecastResult.list.get(position).weather.get(0).getIcon())
                .append(".png").toString()).into(holder.imgWeather);

        holder.textDateTime.setText(new StringBuilder(Api.convertUnixToDate(weatherForecastResult.list.get(position).dt)));

        holder.textDescription.setText(new StringBuilder(weatherForecastResult.list.get(position).weather.get(0).getDescription()));

        holder.textTemperature.setText(new StringBuilder(String.valueOf(weatherForecastResult.list.get(position).main.getTemp())).append("°C"));
    }

    @Override
    public int getItemCount() {
        return weatherForecastResult.list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textDateTime, textDescription, textTemperature;
        ImageView imgWeather;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgWeather = itemView.findViewById(R.id.img_weather);
            textDateTime = itemView.findViewById(R.id.text_date);
            textDescription = itemView.findViewById(R.id.text_description);
            textTemperature = itemView.findViewById(R.id.txt_temperature);
        }
    }
}
