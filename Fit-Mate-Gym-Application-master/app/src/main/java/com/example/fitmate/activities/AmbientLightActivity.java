package com.example.fitmate.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;

public class AmbientLightActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private TextView tvLightSensorValue, tvLightDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambient_light);

        tvLightSensorValue = findViewById(R.id.tvLightSensorValue);
        tvLightDescription = findViewById(R.id.tvLightDescription);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        if (lightSensor == null) {
            tvLightSensorValue.setText("No Ambient Light Sensor found");
            tvLightDescription.setText("This device does not support Ambient Light detection.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0]; // lux value

            tvLightSensorValue.setText(String.format("Ambient Light Level: %.2f lux", lightLevel));

            String description = getLightLevelDescription(lightLevel);
            String recommendation = getLightLevelRecommendation(lightLevel);

            tvLightDescription.setText(description + "\nRecommendation: " + recommendation);
        }
    }

    private String getLightLevelDescription(float lux) {
        if (lux < 10) {
            return "Very dark environment";
        } else if (lux < 50) {
            return "Dim light";
        } else if (lux < 200) {
            return "Normal indoor lighting";
        } else if (lux < 1000) {
            return "Bright indoor lighting";
        } else {
            return "Very bright / outdoor lighting";
        }
    }

    private String getLightLevelRecommendation(float lux) {
        if (lux < 10) {
            return "Good for Sleep";
        } else if (lux < 50) {
            return "Good for Relaxing / Low strain activities";
        } else if (lux < 200) {
            return "Good for Study / Reading";
        } else if (lux < 1000) {
            return "Suitable for Active Work / Exercise";
        } else {
            return "Best for Outdoor Activities";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
