package com.samonxu.qrcode.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Samon Xu on 2016/03/22 0022.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private TextView resultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTv = (TextView) findViewById(R.id.tv_result);
    }

    @Override
    public void onClick(View v) {
        startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            resultTv.setText(data.getStringExtra(CaptureActivity.EXTRA_RESULT));
        } else {
            resultTv.setText("");
        }
    }
}
