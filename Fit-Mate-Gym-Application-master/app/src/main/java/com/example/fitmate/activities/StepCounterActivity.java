package com.example.fitmate.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvStepCount, tvCalories, tvDistance, tvDate, tvDuration;
    private Button btnStartStop;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private boolean isStepCounting = false;
    private int stepCount = 0;

    private float lastMagnitude = SensorManager.GRAVITY_EARTH;
    private static final float ALPHA = 0.8f;
    private long lastStepTime = 0;
    private long startTime = 0L;

    private static final float STEP_THRESHOLD = 1.5f; // Lower threshold for sensitive detection
    private static final int STEP_INTERVAL_MS = 300;  // Reduced interval for quicker detection

    private static final float STEP_LENGTH_METERS = 0.75f;
    private static final float CALORIES_PER_STEP = 0.04f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        tvStepCount = findViewById(R.id.tvStepCount);
        tvCalories = findViewById(R.id.tvCalories);
        tvDistance = findViewById(R.id.tvDistance);
        tvDate = findViewById(R.id.tvDate);
        tvDuration = findViewById(R.id.tvDuration);
        btnStartStop = findViewById(R.id.btnStartStop);

        updateDate();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor == null) {
            Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_LONG).show();
            btnStartStop.setEnabled(false);
        }

        btnStartStop.setOnClickListener(v -> {
            if (!isStepCounting) startCounting();
            else stopCounting();
        });

        updateButtonLabel();
    }

    private void updateDate() {
        String currentDate = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText("Date: " + currentDate);
    }

    private void startCounting() {
        isStepCounting = true;
        stepCount = 0;
        lastMagnitude = SensorManager.GRAVITY_EARTH;
        lastStepTime = 0;
        startTime = SystemClock.elapsedRealtime();

        tvStepCount.setText("Steps: 0");
        tvCalories.setText("Calories Burned: 0.0 kcal");
        tvDistance.setText("Distance Walked: 0.0 m");
        tvDuration.setText("Duration: 0s");

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        Toast.makeText(this, "Step counting started", Toast.LENGTH_SHORT).show();
        updateButtonLabel();

        tvDuration.postDelayed(durationUpdater, 1000);
    }

    private void stopCounting() {
        sensorManager.unregisterListener(this);
        isStepCounting = false;

        Toast.makeText(this, "Step counting stopped", Toast.LENGTH_SHORT).show();
        updateButtonLabel();
        tvDuration.removeCallbacks(durationUpdater);
    }

    private void updateButtonLabel() {
        btnStartStop.setText(isStepCounting ? "Stop" : "Start");
    }

    private final Runnable durationUpdater = new Runnable() {
        @Override
        public void run() {
            if (isStepCounting) {
                long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
                int seconds = (int) (elapsedMillis / 1000);
                int mins = seconds / 60;
                seconds %= 60;
                tvDuration.setText(String.format("Duration: %d min %d sec", mins, seconds));
                tvDuration.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isStepCounting || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float rawMagnitude = (float) Math.sqrt(x * x + y * y + z * z);
        float smoothed = ALPHA * lastMagnitude + (1 - ALPHA) * rawMagnitude;
        float delta = Math.abs(smoothed - lastMagnitude);

        if (delta > STEP_THRESHOLD) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastStepTime > STEP_INTERVAL_MS) {
                stepCount++;
                lastStepTime = currentTime;

                float distance = stepCount * STEP_LENGTH_METERS;
                float calories = stepCount * CALORIES_PER_STEP;

                tvStepCount.setText("Steps: " + stepCount);
                tvDistance.setText(String.format("Distance Walked: %.2f m", distance));
                tvCalories.setText(String.format("Calories Burned: %.2f kcal", calories));
            }
        }

        lastMagnitude = smoothed; // update for next cycle
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isStepCounting) stopCounting();
    }
}
