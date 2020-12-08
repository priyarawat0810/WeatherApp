package com.example.weatherapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.weatherapp.Api;
import com.example.weatherapp.IOpenWeatherMap;
import com.example.weatherapp.R;
import com.example.weatherapp.RetrofitClient;
import com.example.weatherapp.adapter.WeatherForecastAdapter;
import com.example.weatherapp.WeatherForecastResult;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class ForecastFragment extends Fragment {

    static ForecastFragment instance;

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;

    TextView textCity, textGeoCoord;
    RecyclerView recycler_forecast;

    public ForecastFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }

    public static ForecastFragment getInstance() {
      if (instance == null)
          instance = new ForecastFragment();
      return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_forecast, container, false);

        textCity = itemView.findViewById(R.id.txt_city_name);
        textGeoCoord = itemView.findViewById(R.id.text_geo_coord);
        recycler_forecast = itemView.findViewById(R.id.recycler_forecast);

        recycler_forecast.setHasFixedSize(true);
        recycler_forecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        getForecastWeatherInformation();
        return itemView;
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

    private void getForecastWeatherInformation() {
        compositeDisposable.add(mService.getWeatherForecastByLatLng(
                String.valueOf(Api.current_location.getLatitude()),
                String.valueOf(Api.current_location.getLongitude()),
                Api.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherForecastResult>() {
                    @Override
                    public void accept(WeatherForecastResult weatherForecastResult) throws Exception {
                        displayForecastWeather(weatherForecastResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d("ERROR", ""+throwable.getMessage());
                    }
                })
        );
    }
    private  void displayForecastWeather(WeatherForecastResult weatherForecastResult){
        textCity.setText(new StringBuilder(weatherForecastResult.city.name));
        textGeoCoord.setText(new StringBuilder(weatherForecastResult.city.coord.toString()));

        WeatherForecastAdapter adapter =  new WeatherForecastAdapter(getContext(), weatherForecastResult);
        recycler_forecast.setAdapter(adapter);
    }

}