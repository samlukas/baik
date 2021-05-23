package com.example.baik.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.baik.R;
import com.example.baik.databinding.FragmentNotificationsBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    RequestQueue queue;

    private checkIn[] checkIns;
    LineChart sleepHoursChart;
    LineChart sleepQualityChart;
    LineChart moodChart;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        sleepHoursChart = root.findViewById(R.id.sh_chart);
        sleepHoursChart.setDescription(new Description());
        sleepQualityChart = root.findViewById(R.id.sq_chart);
        sleepQualityChart.setDescription(new Description());
        moodChart = root.findViewById(R.id.mood_chart);
        moodChart.setDescription(new Description());

        queue = Volley.newRequestQueue(getContext());
        getUserData();

        return root;
    }

    private void initGraphs() {
        List<Entry> sleepHourEntries = new ArrayList<Entry>();
        for (checkIn c : checkIns) {
            // turn your data into Entry objects
            sleepHourEntries.add(new Entry(c.date, c.sleepHours));
        }

        Collections.sort(sleepHourEntries, new EntryXComparator());
        LineDataSet dataSet = new LineDataSet(sleepHourEntries, "");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(dataSet);
        sleepHoursChart.setData(lineData);
        sleepHoursChart.invalidate(); // refresh

        List<Entry> sleepQualityEntries = new ArrayList<Entry>();
        for (checkIn c : checkIns) {
            // turn your data into Entry objects
            sleepQualityEntries.add(new Entry(c.date, c.sleepQuality));
        }

        Collections.sort(sleepQualityEntries, new EntryXComparator());
        LineDataSet SQdataSet = new LineDataSet(sleepQualityEntries, "");
        SQdataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData SQlineData = new LineData(SQdataSet);
        sleepQualityChart.setData(SQlineData);
        sleepQualityChart.invalidate(); // refresh

        List<Entry> moodEntries = new ArrayList<Entry>();
        for (checkIn c : checkIns) {
            // turn your data into Entry objects
            moodEntries.add(new Entry(c.date, c.mood));
        }

        Collections.sort(moodEntries, new EntryXComparator());
        LineDataSet MdataSet = new LineDataSet(moodEntries, "");
        MdataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData MlineData = new LineData(MdataSet);
        moodChart.setData(MlineData);
        moodChart.invalidate(); // refresh
    }

    private void getUserData() {
        String url = "http://139.177.198.184:3000/user?name=Sam";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        JSONObject user = (JSONObject) res.get("user");
                        JSONArray checks = (JSONArray) user.get("checkIns");
                        Log.d("Graphs Screen", "CheckIns length: " + checks.length());

                        checkIns = new checkIn[checks.length()];

                        for (int i = 0; i < checks.length(); i++) {
                            JSONObject obj = checks.getJSONObject(i);
                            checkIn ci = new checkIn(
                                    obj.getInt("sleepHours"),
                                    obj.getInt("sleepQuality"),
                                    obj.getInt("mood"),
                                    obj.getBoolean("exercise") ? 1 : 0,
                                    obj.getInt("date")
                            );
                            checkIns[i] = ci;
                        }

                        initGraphs();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class checkIn {
        public int sleepHours;
        public int sleepQuality;
        public int mood;
        public int exercise;
        public int date;

        public checkIn(int sh, int sq, int m, int e, int d) {
            this.sleepHours = sh;
            this.sleepQuality = sq;
            this.mood = m;
            this.exercise = e;
            this.date = d;
        }
    }
}