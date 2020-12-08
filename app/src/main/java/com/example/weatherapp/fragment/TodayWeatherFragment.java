package com.example.weatherapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.Api;
import com.example.weatherapp.IOpenWeatherMap;
import com.example.weatherapp.R;
import com.example.weatherapp.RetrofitClient;
import com.example.weatherapp.WeatherResult;
import com.squareup.picasso.Picasso;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class TodayWeatherFragment extends Fragment {

    ImageView img_weather;
    TextView textCityName, textHumidity, textSunrise, textSunset, textPressure, textTemperature, textDescription, textDateTime, textGeoCoord;
    LinearLayout weatherPanel;
    ProgressBar loading;

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;


    static TodayWeatherFragment instance;

    public static  TodayWeatherFragment getInstance(){
        if(instance == null){
            instance = new TodayWeatherFragment();
        }
        return instance;
    }

    public TodayWeatherFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }

    public static TodayWeatherFragment newInstance() {
        TodayWeatherFragment fragment = new TodayWeatherFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_today_weather, container, false);
        img_weather = itemView.findViewById(R.id.img_weather);
        textCityName = itemView.findViewById(R.id.txt_city_name);
        textDateTime = itemView.findViewById(R.id.txt_date_time);
        textDescription = itemView.findViewById(R.id.txt_description);
        textGeoCoord = itemView.findViewById(R.id.text_geo_coord);
        textHumidity = itemView.findViewById(R.id.text_humidity);
        textPressure = itemView.findViewById(R.id.text_pressure);
        textSunrise = itemView.findViewById(R.id.text_sunrise);
        textSunset = itemView.findViewById(R.id.text_sunset);
        textTemperature = itemView.findViewById(R.id.txt_temperature);

        weatherPanel = itemView.findViewById(R.id.weather_panel);
        loading = itemView.findViewById(R.id.loading);

        getWeatherInformation();
        
        return itemView;
    }

    private void getWeatherInformation() {
        compositeDisposable.add(mService.getWeatherByLatLng(String.valueOf(Api.current_location.getLatitude()),
                String.valueOf(Api.current_location.getLongitude()),
                Api.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                               @Override
                               public void accept(WeatherResult weatherResult) throws Exception {
                                   //load image
                                   Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                                           .append(weatherResult.getWeather().get(0).getIcon())
                                   .append(".png").toString()).into(img_weather);

                                   //Load information
                                   textCityName.setText(weatherResult.getName());
                                   textDescription.setText(new StringBuilder("Weather in ")
                                   .append(weatherResult.getName()).toString());
                                   textTemperature.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp())).append("°C").toString());
                                   textDateTime.setText(Api.convertUnixToDate(weatherResult.getDt()));
                                   textPressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                                   textHumidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                                   textSunrise.setText(Api.covertUnixToHour(weatherResult.getSys().getSunrise()));
                                   textSunset.setText(Api.covertUnixToHour(weatherResult.getSys().getSunset()));
                                   textGeoCoord.setText(new StringBuilder(weatherResult.getCoord().toString()).toString());

                                   //Display panel
                                   weatherPanel.setVisibility(View.VISIBLE);
                                   loading.setVisibility(View.GONE);

                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Toast.makeText(getActivity(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           }
                )
        );
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}