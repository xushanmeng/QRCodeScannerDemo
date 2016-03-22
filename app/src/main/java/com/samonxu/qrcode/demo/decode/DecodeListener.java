package com.samonxu.qrcode.demo.decode;

import android.graphics.Bitmap;

import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;

public interface DecodeListener extends ResultPointCallback {
	public void onDecodeSuccess(Result result, LuminanceSource source, Bitmap bitmap);

	public void onDecodeFailed(LuminanceSource source);
}
