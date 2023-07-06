package com.example.myweather;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Activity_Main extends AppCompatActivity {
    private String apiKey = "7f235571c0e53035be0796ad4fbe2a2e";
    private SQLiteHelper helper;
    private SQLiteDatabase database;
    private List<Map<String, String>> dataListInHistorys;
    private List<Map<String, String>> dataListInFavorites;
    private SimpleAdapter adapter;
    private Button listButton;
    private Button searchButton;
    private EditText cityIdEditText;
    private ListView favoritesListView;
    private SharedPreferences favoritesPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));

        helper = new SQLiteHelper(this, "my_weather.db", null, 1);
        database = helper.getWritableDatabase();
        dataListInHistorys = helper.getAllData("histories");
        dataListInFavorites = new ArrayList<>();
        listButton = findViewById(R.id.listInMain);
        searchButton = findViewById(R.id.buttonInMain);
        cityIdEditText = findViewById(R.id.cityIdInMain);
        favoritesListView = findViewById(R.id.favoritesListView);
        favoritesPreferences = getSharedPreferences("favorites", MODE_PRIVATE);
        Map<String, ?> allFavorites = favoritesPreferences.getAll();
        if (!allFavorites.isEmpty()) {
            for (Map.Entry<String, ?> entry : allFavorites.entrySet()) {
                String city = entry.getKey();
                Map<String, String> map = helper.getSingleData("histories", city);
                dataListInFavorites.add(map);
            }
        }
        adapter = new SimpleAdapter(
                this,
                dataListInFavorites,
                R.layout.activity_item,
                new String[]{"id", "city", "time", "temperature", "weather", "humidity", "winddirection"},
                new int[]{R.id.idInItem, R.id.cityInItem, R.id.timeInItem, R.id.temperatureInItem, R.id.weatherInItem, R.id.humidityInItem, R.id.winddirectionInItem}
        );
        favoritesListView.setAdapter(adapter);

        // 搜索按钮点击事件
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityIdEditText.getText().toString();
                search(city);
            }
        });

        // 搜索栏回车事件
        cityIdEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)  {
                    String city = cityIdEditText.getText().toString();
                    search(city);
                    return true;
                }
                return false;
            }
        });

        // 列项目点击事件
        favoritesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView cityTextView = view.findViewById(R.id.cityInItem);
                String city = cityTextView.getText().toString();
                search(city);
            }
        });

        // 下拉菜单点击事件
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProvinceMenu();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cityIdEditText.setText("");
        dataListInHistorys = helper.getAllData("histories");
    }

    private void search(String city) {
        if (!city.isEmpty()) {
            int i = isContainedInHistorys(city);
            Log.d("TAG", "isContainedInHistorys: " + i);
            if (i >= 0) {
                if (isValidTime(i, city)) {
                    Log.d("TAG", "isValidTime: " + isValidTime(i, city));
                    Intent intent = new Intent(Activity_Main.this, Activity_Search.class);
                    Map<String, String> map = dataListInHistorys.get(i);
                    Log.d("TAG", "访问histories数据库");
                    intent.putExtra("province", map.get("province"));
                    intent.putExtra("city", map.get("city"));
                    intent.putExtra("time", map.get("time"));
                    intent.putExtra("temperature", map.get("temperature"));
                    intent.putExtra("weather", map.get("weather"));
                    intent.putExtra("humidity", map.get("humidity"));
                    intent.putExtra("winddirection", map.get("winddirection"));
                    startActivity(intent);
                } else {
                    Log.d("TAG", "isValidTime: " + isValidTime(i, city));
                    Log.d("TAG", "更新histories数据库");
                    boolean result = helper.delete("histories", city);
                    Log.d("TAG", "delete " + city + " : " + result);
                    requestWeatherData(city, apiKey);
                }
            } else
                requestWeatherData(city, apiKey);
        } else
            Toast.makeText(Activity_Main.this, "城市名不能为空，请重新输入", Toast.LENGTH_SHORT).show();
    }

    private void requestWeatherData(String city, String apiKey) {
        String url = "https://restapi.amap.com/v3/weather/weatherInfo?key=" + apiKey + "&city=" + city + "&extensions=base&output=json";
        Log.d("TAG", "访问API");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray livesArray = response.getJSONArray("lives");
                            if (livesArray.length() > 0) {
                                JSONObject weatherObject = livesArray.getJSONObject(0);
                                String time = weatherObject.getString("reporttime");
                                String province = weatherObject.getString("province");
                                String city = weatherObject.getString("city");
                                String temperature = weatherObject.getString("temperature") + "°";
                                String weather = weatherObject.getString("weather");
                                String humidity = weatherObject.getString("humidity") + "RH";
                                String winddirection = weatherObject.getString("winddirection") + "风";

                                boolean result = helper.insert("histories", province, city, time, temperature, weather, humidity, winddirection);
                                Log.d("TAG", "insert " + city + " : " + result);

                                Intent intent = new Intent(Activity_Main.this, Activity_Search.class);
                                intent.putExtra("time", time);
                                intent.putExtra("province", province);
                                intent.putExtra("city", city);
                                intent.putExtra("temperature", temperature);
                                intent.putExtra("weather", weather);
                                intent.putExtra("humidity", humidity);
                                intent.putExtra("winddirection", winddirection);
                                startActivity(intent);
                            } else
                                Toast.makeText(Activity_Main.this, "城市名有误，请重新输入", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Activity_Main.this, "城市名有误，请重新输入", Toast.LENGTH_SHORT).show();
                    }
                });
        // 发起网络请求
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private int isContainedInHistorys(String city) {
        for (int i = 0; i < dataListInHistorys.size(); i++) {
            Map<String, String> map = dataListInHistorys.get(i);
            String cityInHistorys = map.get("city");
            if (cityInHistorys.equals(city) || cityInHistorys.substring(0, cityInHistorys.length() - 1).equals(city))
                return i;
        }
        return -1;
    }

    private boolean isValidTime(int i, String city) {
        String time = dataListInHistorys.get(i).get("time");

        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        LocalDateTime currentDateTime = LocalDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);

        LocalDateTime dateTime1 = LocalDateTime.parse(time, formatter);
        LocalDateTime dateTime2 = LocalDateTime.parse(formattedDateTime, formatter);
        Duration duration = Duration.between(dateTime1, dateTime2);
        long hours = duration.toHours();

        if (hours < 3)
            return true;
        return false;
    }

    private void showProvinceMenu() {
        PopupMenu popupMenu = new PopupMenu(this, listButton);
        popupMenu.getMenuInflater().inflate(R.menu.province_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String province = item.getTitle().toString();
                showCityMenu(province);
                return true;
            }
        });
        popupMenu.show();
    }

    private void showCityMenu(String province) {
        PopupMenu popupMenu = new PopupMenu(this, listButton);
        popupMenu.getMenuInflater().inflate(R.menu.city_menu, popupMenu.getMenu());

        // 根据省份设置城市菜单项
        switch (province) {
            case "北京市":
                popupMenu.getMenu().add("北京市");
                popupMenu.getMenu().add("东城区");
                popupMenu.getMenu().add("西城区");
                popupMenu.getMenu().add("朝阳区");
                popupMenu.getMenu().add("丰台区");
                popupMenu.getMenu().add("石景山区");
                popupMenu.getMenu().add("海淀区");
                popupMenu.getMenu().add("门头沟区");
                popupMenu.getMenu().add("房山区");
                popupMenu.getMenu().add("通州区");
                popupMenu.getMenu().add("顺义区");
                popupMenu.getMenu().add("昌平区");
                popupMenu.getMenu().add("大兴区");
                popupMenu.getMenu().add("怀柔区");
                popupMenu.getMenu().add("平谷区");
                popupMenu.getMenu().add("密云区");
                popupMenu.getMenu().add("延庆区");
                break;
            case "天津市":
                popupMenu.getMenu().add("天津市");
                popupMenu.getMenu().add("和平区");
                popupMenu.getMenu().add("河东区");
                popupMenu.getMenu().add("河西区");
                popupMenu.getMenu().add("南开区");
                popupMenu.getMenu().add("河北区");
                popupMenu.getMenu().add("红桥区");
                popupMenu.getMenu().add("东丽区");
                popupMenu.getMenu().add("西青区");
                popupMenu.getMenu().add("津南区");
                popupMenu.getMenu().add("北辰区");
                popupMenu.getMenu().add("武清区");
                popupMenu.getMenu().add("宝坻区");
                popupMenu.getMenu().add("滨海新区");
                popupMenu.getMenu().add("宁河区");
                popupMenu.getMenu().add("静海区");
                popupMenu.getMenu().add("蓟州区");
            case "广东省":
                popupMenu.getMenu().add("广州市");
                popupMenu.getMenu().add("深圳市");
                popupMenu.getMenu().add("珠海市");
                popupMenu.getMenu().add("汕头市");
                popupMenu.getMenu().add("韶关市");
                break;
            case "上海市":
                popupMenu.getMenu().add("上海市");
                break;
            case "江苏省":
                popupMenu.getMenu().add("南京市");
                popupMenu.getMenu().add("苏州市");
                popupMenu.getMenu().add("无锡市");
                popupMenu.getMenu().add("常州市");
                popupMenu.getMenu().add("南通市");
                break;
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String city = item.getTitle().toString();
                search(city);
                return true;
            }
        });
        popupMenu.show();
    }
}
