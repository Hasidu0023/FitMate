package com.example.fitmate.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fitmate.R;

public class HealthTipsActivity extends AppCompatActivity {

    private LinearLayout containerTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_tips);

        containerTips = findViewById(R.id.containerTips);

        float bmi = getIntent().getFloatExtra("BMI_VALUE", 0f);

        addBMICard(bmi);
        addTipsCards(bmi);
    }

    private void addBMICard(float bmi) {
        CardView card = createCardView();

        TextView tv = new TextView(this);
        tv.setText(String.format("📊 Your BMI: %.2f", bmi));
        tv.setTextSize(22f);
        tv.setTextColor(Color.parseColor("#333333"));
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(24, 24, 24, 24);

        card.addView(tv);
        containerTips.addView(card);
    }

    private void addTipsCards(float bmi) {
        String[] tips;
        String title;

        if (bmi == 0f) {
            title = "⚠️ BMI not available";
            tips = new String[] {
                    "Please calculate your BMI first."
            };
        } else if (bmi < 18.5f) {
            title = "📉 Underweight";
            tips = new String[] {
                    "🍽️ Eat nutrient-rich foods like proteins and healthy fats.",
                    "🕐 Have small frequent meals throughout the day.",
                    "🚫 Avoid empty calories such as sugary snacks.",
                    "👨‍⚕️ Consult a nutritionist for personalized advice."
            };
        } else if (bmi < 25f) {
            title = "✅ Normal weight";
            tips = new String[] {
                    "🥗 Maintain a balanced diet rich in fruits and vegetables.",
                    "🏃 Exercise regularly to stay fit.",
                    "💧 Stay hydrated and get enough sleep.",
                    "🩺 Have regular health checkups."
            };
        } else if (bmi < 30f) {
            title = "⚠️ Overweight";
            tips = new String[] {
                    "📉 Reduce calorie intake, especially processed foods.",
                    "🌽 Include more fiber-rich vegetables and fruits.",
                    "🚶 Increase physical activity such as walking or swimming.",
                    "🥤 Avoid sugary drinks and junk food."
            };
        } else {
            title = "🚨 Obese";
            tips = new String[] {
                    "👨‍⚕️ Consult a healthcare professional immediately.",
                    "📋 Follow a supervised diet and exercise plan.",
                    "🏃‍♂️ Avoid a sedentary lifestyle — be active!",
                    "🩺 Monitor your health regularly."
            };
        }

        addTitleCard(title);

        for (String tip : tips) {
            CardView card = createCardView();

            TextView tv = new TextView(this);
            tv.setText(tip);
            tv.setTextSize(16f);
            tv.setTextColor(Color.parseColor("#444444"));
            tv.setPadding(24, 24, 24, 24);

            card.addView(tv);
            containerTips.addView(card);
        }
    }

    private void addTitleCard(String title) {
        CardView card = createCardView();

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(20f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(Color.parseColor("#222222"));
        tv.setPadding(24, 24, 24, 24);

        card.addView(tv);
        containerTips.addView(card);
    }

    private CardView createCardView() {
        CardView card = new CardView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);

        card.setRadius(24f);
        card.setCardElevation(8f);
        card.setUseCompatPadding(true);
        card.setContentPadding(12, 12, 12, 12);
        card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

        return card;
    }
}
