package com.example.weatherapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class CityFragment extends Fragment {

    private List<String> listCities;
    private MaterialSearchBar searchBar;

    ImageView imgWeather;
    TextView textCity, textHumidity, textSunrise, textSunset, textPressure, textTemperature, textDescription, textDateTime, textGeoCoord;
    LinearLayout weatherPanel;
    ProgressBar loading;

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;
    static CityFragment instance;

    public static CityFragment getInstance() {
        if(instance == null)
            instance = new CityFragment();
            return instance;
    }

    public CityFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_city, container, false);

        imgWeather = itemView.findViewById(R.id.img_weather);
        textCity = itemView.findViewById(R.id.txt_city_name);
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

        searchBar = itemView.findViewById(R.id.search_bar);
        searchBar.setEnabled(false);

        new LoadCities().execute();  //AsyncTask to load city's list

        return itemView;
    }

    public class LoadCities extends SimpleAsyncTask<List<String>>{

        @Override
        protected List<String> doInBackgroundSimple() {
            listCities = new ArrayList<>();
            try {
                StringBuilder builder = new StringBuilder();
                InputStream inputStream = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);

                InputStreamReader reader = new InputStreamReader(gzipInputStream);
                BufferedReader in = new BufferedReader(reader);

                String readed;
                while ((readed = in.readLine()) != null)
                    builder.append(readed);
                listCities = new Gson().fromJson(builder.toString(), new TypeToken<List<String>>(){}.getType());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return listCities;
        }

        @Override
        protected void onSuccess(final List<String> listCity) {
            super.onSuccess(listCity);

            searchBar.setEnabled(true);
            searchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    List<String> suggest = new ArrayList<>();
                    for (String search : listCity){
                        if (search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                            suggest.add(search);
                    }
                    searchBar.setLastSuggestions(suggest);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {
                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    getWeatherInformation(text.toString());
                    searchBar.setLastSuggestions(listCity);
                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });
            searchBar.setLastSuggestions(listCity);

            loading.setVisibility(View.GONE);
        }
    }

    private void getWeatherInformation(String cityName) {
        compositeDisposable.add(mService.getWeatherByCityName(cityName,
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
                                .append(".png").toString()).into(imgWeather);

                        //Load information
                        textCity.setText(weatherResult.getName());
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