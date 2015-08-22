package com.example.root.ailight;

import android.bluetooth.BluetoothSocket;
import android.widget.SeekBar;

/**
 * Created by root on 15-8-22.
 */
public abstract class LightSeakBarListener implements   SeekBar.OnSeekBarChangeListener {

    SeekBar excludeSeakBar;
    int type;
    public LightSeakBarListener(SeekBar excludeSeakBar, int i){
        this.excludeSeakBar = excludeSeakBar;
        this.type =i;

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(getSocket() != null &&  getSocket().isConnected()) {
            excludeSeakBar.setProgress(0);
            LightComdSenderHelper.sendCmd(seekBar.getProgress(), getSocket(),type);
        }else{
            seekBar.setProgress(0);
        }
    }

    public abstract BluetoothSocket getSocket();
}
