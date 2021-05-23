package com.example.baik.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.baik.R;
import com.example.baik.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private HashMap<Integer, String> entries;
    RequestQueue queue;
    CalendarView cv;
    EditText et;
    int currDate = 0;

    private JSONArray calendar;
    private JSONArray checkIns;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        entries = new HashMap<>();
        queue = Volley.newRequestQueue(getContext());
        getUserData();

        // Make a state for which Calendar date is currently selected
        // set some kind of onClick listener for calendar dates
        // if a new date is clicked, update state and clear the text input
        // otherwise do nothing
        cv = root.findViewById(R.id.calendarView3);
        et = root.findViewById(R.id.edittext);
        cv.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                String content = (entries.get(currDate) != null) ? entries.get(currDate) : "";
                if (dayOfMonth != currDate) {
                    // get text from the previous selected day
                    String text = et.getText().toString();
                    if (!text.equals(content)) { // content was updated
                        entries.replace(currDate, text);
                        updateNotes();
                    }

                    currDate = dayOfMonth;
                    content = (entries.get(currDate) != null) ? entries.get(currDate) : "";
                    et.setText(content);
                }
                String dt = String.valueOf(currDate);
                Log.d("Calendar Screen", "Current date is " + dt);
            }
        });

        return root;
    }

    private void getUserData() {
        String url = "http://139.177.198.184:3000/user?name=Sam";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        JSONObject user = (JSONObject) res.get("user");
                        calendar = (JSONArray) user.get("calendar");
                        checkIns = (JSONArray) user.get("checkIns");

                        for (int i = 0; i < calendar.length(); i++) {
                            JSONObject c = calendar.getJSONObject(i);
                            entries.put(c.getInt("date"), c.getString("notes"));
                        }

                        Log.d("Calendar Screen", "Sucessfully fetched user data");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d("Calendar Screen", error.getMessage());
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateNotes() {
        String url = "http://139.177.198.184:3000/user/";
        JSONObject postData = new JSONObject();

        entries.forEach((k, v) -> {
            Log.d("Calendar Screen", "Notes entry: " + v);
        });

        try {
            postData.put("name", "Sam");
            postData.put("userID", "hello");
            postData.put("checkIns", checkIns);
            postData.put("calendar", new JSONArray(entries.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url,
                response -> {
                    try {
                        Log.d("Calendar Screen", "Updated user successfully");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d("Calendar Screen", error.getMessage());
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}