package com.example.weathermoniter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    private static final String OPEN_WEATHER_MAP_URL="http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric";
    private static final String OPEN_WEATHER_MAP_API="25529d5a310e68b1f64b4f1dfbe17a57";

    TextView city,details,temperature,humidity,pressure,weather,update;
    Typeface weatherfont;
    static String latitude;
    static String longitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        requestPermissions();

        FusedLocationProviderClient mFusedLocationProviderClient;
        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(MainActivity.this,ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    latitude=String.valueOf(location.getLatitude());
                    longitude=String.valueOf(location.getLongitude());

                    weatherfont=Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/weathericons-regular-webfont.ttf");

                    city=findViewById(R.id.cityfield);
                    temperature=findViewById(R.id.temperaturefield);
                    update=findViewById(R.id.updatedfield);
                    details=findViewById(R.id.detailsfield);
                    humidity=findViewById(R.id.humidityfield);
                    pressure=findViewById(R.id.pressurefield);
                    weather=findViewById(R.id.weatherfield);
                    weather.setTypeface(weatherfont);

                    String[] jsonData=getJSONResponse();

                    city.setText(jsonData[0]);
                    details.setText(jsonData[1]);
                    temperature.setText(jsonData[2]);
                    humidity.setText(jsonData[3]);
                    pressure.setText(jsonData[4]);
                    update.setText(jsonData[5]);
                    weather.setText(Html.fromHtml(jsonData[6]));


                }
            }
        });
    }
    public String[] getJSONResponse(){
        String[] jsonData=new String[7];
        JSONObject jsonWeather=null;
        try{
            jsonWeather=getWeatherJSON(latitude,longitude);
        }catch(Exception e){
            Log.d("Error","Cannot process JSON results",e);
        }
        try {
            if(jsonWeather!=null){
                JSONObject details=jsonWeather.getJSONArray("weather").getJSONObject(0);
                JSONObject main=jsonWeather.getJSONObject("main");
                DateFormat df=DateFormat.getDateInstance();

                String city=jsonWeather.getString("name")+", "+jsonWeather.getJSONObject("sys").getString("country");
                String description=details.getString("description").toLowerCase(Locale.US);
                String temperature =String.format("%.0f",main.getDouble("temp"))+"*";
                String humidity=main.getString("humidity")+"%";
                String pressure=main.getString("pressure")+"hPa";
                String updatedOn=df.format(new Date(jsonWeather.getLong("dt")*1000));
                String iconText=setWeatherIcon(details.getInt("id"),jsonWeather.getJSONObject("sys").getLong("sunrise")*1000,jsonWeather.getJSONObject("sys").getLong("sunset")*1000);

                jsonData[0]=city;
                jsonData[1]=description;
                jsonData[2]=temperature;
                jsonData[3]=humidity;
                jsonData[4]=pressure;
                jsonData[5]=updatedOn;
                jsonData[6]=iconText;

            }


        }catch (Exception e){

        }
        return jsonData;
    }
    public static String setWeatherIcon(int actualId,long sunrise,long sunset){
        int id=actualId/100;
        String icon="";
        if(actualId==800)
        {
            long currentTime= new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset){
                icon="&#xf00d;";
            }
            else {
                icon="&#xf02e";
            }
        }
        else {
            switch (id){
                case 2:
                    icon="&#xf01e;";
                    break;
                case 3:
                    icon="&#xf01c;";
                    break;
                case 7:
                    icon="&#xf014;";
                    break;
                case 8:
                    icon="&#xf013;";
                    break;
                case 6:
                    icon="&#xf01b;";
                    break;
                case 5:
                    icon="&#xf019;";
                    break;
            }
        }
        return  icon;
    }
    public  static  JSONObject getWeatherJSON(String lat,String lon){
        try{
            URL url=new URL(String.format(OPEN_WEATHER_MAP_URL,lat,lon));
            HttpURLConnection connection=(HttpURLConnection) url.openConnection();
            connection.addRequestProperty("x-api-key",OPEN_WEATHER_MAP_API);
            BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine()) !=null){
                json.append(tmp).append("\n");
            }
            reader.close();
            JSONObject data=new JSONObject(json.toString());
            if(data.getInt("cod")!=200){
                return null;
            }
            return data;
        }catch (Exception e){
            return  null;
        }
    }
    private void requestPermissions(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION}, 1);
    }
}