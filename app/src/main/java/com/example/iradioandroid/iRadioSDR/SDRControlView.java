package com.example.iradioandroid.iRadioSDR;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.iradioandroid.R;

import android.widget.Button;
import android.widget.TextView;

public class SDRControlView extends ConstraintLayout implements View.OnClickListener {
    private static final String TAG = "SDRControlView";
    private iRadioSDRPlayer iRadioSDRPlayerService = null;  // Service for controlling the SDR (WebSDR,Kiwi) .. (startPlayer,stopPlayer,...)
    ConstraintLayout controlPanel = null; // overlay panel for frequency, modulation ... input
    iRadioSDRPlayer.Modulation modulation = iRadioSDRPlayer.Modulation.AM; // default AM
    TextView textview_frequency = null; // frequency display in panel
    // Buttons for the numpad
    Button btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9;
    Button btn_Back, btn_Tune;
    Button btn_AM, btn_LSB, btn_USB;

    public SDRControlView(Context context) {
        super(context);
        initView();
    }

    public SDRControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SDRControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }


    private void initView() {
        // this is the xml layout for overlay control panel
        inflate(getContext(), R.layout.sdrcontrolview, this);

        controlPanel = findViewById(R.id.controlPanel);
        controlPanel.setOnClickListener(this);

        textview_frequency = findViewById(R.id.tv_frequency);
        textview_frequency.setOnClickListener(this);

        btn_0 = findViewById(R.id.button0);
        btn_0.setOnClickListener(this);

        btn_1 = findViewById(R.id.button1);
        btn_1.setOnClickListener(this);

        btn_2 = findViewById(R.id.button2);
        btn_2.setOnClickListener(this);

        btn_3 = findViewById(R.id.button3);
        btn_3.setOnClickListener(this);

        btn_4 = findViewById(R.id.button4);
        btn_4.setOnClickListener(this);

        btn_5 = findViewById(R.id.button5);
        btn_5.setOnClickListener(this);

        btn_6 = findViewById(R.id.button6);
        btn_6.setOnClickListener(this);

        btn_7 = findViewById(R.id.button7);
        btn_7.setOnClickListener(this);

        btn_8 = findViewById(R.id.button8);
        btn_8.setOnClickListener(this);

        btn_9 = findViewById(R.id.button9);
        btn_9.setOnClickListener(this);

        btn_Back = findViewById(R.id.buttonBack);
        btn_Back.setOnClickListener(this);

        btn_Tune = findViewById(R.id.buttonTune);
        btn_Tune.setOnClickListener(this);

        btn_AM = findViewById(R.id.buttonAM);
        btn_AM.setOnClickListener(this);

        btn_USB = findViewById(R.id.buttonUSB);
        btn_USB.setOnClickListener(this);

        btn_LSB = findViewById(R.id.buttonLSB);
        btn_LSB.setOnClickListener(this);
    }

    // onClick handling for all buttons on numpad/control panel
    @Override
    public void onClick(View view) {
        // toggle control panel view (done via alpha)
        if (controlPanel.getId() == view.getId()) {
            if (controlPanel.getAlpha() == 0f) {
                controlPanel.setAlpha(1f);
                btn_Tune.setEnabled(true);
                if (iRadioSDRPlayerService != null) {
                    textview_frequency.setText(String.valueOf(iRadioSDRPlayerService.getFrequency()));
                }
            } else {
                controlPanel.setAlpha(0f);
                btn_Tune.setEnabled(false);  // simulate a locked keyboard
            }
        }

        if (btn_0.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(0);
        }
        if (btn_1.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(1);
        }
        if (btn_2.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(2);
        }
        if (btn_3.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(3);
        }
        if (btn_4.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(4);
        }
        if (btn_5.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(5);
        }
        if (btn_6.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(6);
        }
        if (btn_7.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(7);
        }
        if (btn_8.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(8);
        }
        if (btn_9.getId() == view.getId()) {
            addOneDigitOnFrequencyDisplay(9);
        }

        if (btn_Back.getId() == view.getId()) {
            backOneDigitOnFrequencyDisplay();
        }

        if (textview_frequency.getId() == view.getId()) {
            textview_frequency.setText("");
        }

        // Send new parameters to SDR service
        if (btn_Tune.getId() == view.getId()) {
            if (iRadioSDRPlayerService != null) {
                Log.i(TAG, "set WebSDR to new frequency, mode, ...");
                if (textview_frequency.length()!=0) {
                    iRadioSDRPlayerService.setFrequency(Integer.parseInt(textview_frequency.getText().toString()));
                    iRadioSDRPlayerService.setModulation(modulation);
                    iRadioSDRPlayerService.startPlayer();
                }
            }
        }

        if (btn_AM.getId() == view.getId()) {
            Log.i(TAG, "set WebSDR to AM modulation");
            modulation = iRadioSDRPlayer.Modulation.AM;
            btn_AM.setBackground(getResources().getDrawable(R.drawable.rft_ekd_key_yellow));
            btn_USB.setBackground(getResources().getDrawable(R.drawable.rft_ekd_key));
            btn_LSB.setBackground(getResources().getDrawable(R.drawable.rft_ekd_key));
        }

        if (btn_USB.getId() == view.getId()) {
            Log.i(TAG, "set WebSDR to USB modulation");
            modulation = iRadioSDRPlayer.Modulation.USB;
            btn_AM.setBackground(getResources().getDrawable(R.drawable.rft_ekd_key));
            btn_USB.setBackground(getResources().getDrawable(R.drawable.rft_ekd_key_yellow));
            btn_LSB.setBackground(getResources().getDrawable(R.drawable.rft_ekd_key));
        }

        if (btn_LSB.getId() == view.getId()) {
            Log.i(TAG, "set WebSDR to LSB modulation");
            modulation = iRadioSDRPlayer.Modulation.LSB;
            btn_AM.setBackground(getResources().getDrawable(R.drawable.rft_ekd_key));
            btn_USB.setBackground(getResources().getDrawable(R.drawable.rft_ekd_key));
            btn_LSB.setBackground(getResources().getDrawable(R.drawable.rft_ekd_key_yellow));
        }
    }

    private void addOneDigitOnFrequencyDisplay(int digit) {
        String str = textview_frequency.getText().toString();
        if (str.length() < 5) {
            textview_frequency.setText(str + digit);
            textview_frequency.invalidate();
        }
    }

    private void backOneDigitOnFrequencyDisplay() {
        String str = textview_frequency.getText().toString();
        if (str.length() > 0) {
            textview_frequency.setText(str.substring(0, str.length() - 1));
            textview_frequency.invalidate();
        }
    }


    public void setSDRPlayerService(iRadioSDRPlayer iRadioSDRPlayerService) {
        this.iRadioSDRPlayerService = iRadioSDRPlayerService;
    }
}
