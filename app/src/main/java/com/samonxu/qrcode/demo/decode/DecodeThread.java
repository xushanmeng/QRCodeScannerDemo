package com.samonxu.qrcode.demo.decode;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;

public class DecodeThread extends AsyncTask<Void, Void, Result> {
	private LuminanceSource luminanceSource;
	private DecodeListener listener;
	private Bitmap mBitmap;
	private boolean isStop = false;

	public DecodeThread(LuminanceSource luminanceSource, DecodeListener listener) {
		this.luminanceSource = luminanceSource;
		this.listener = listener;
	}

	@Override
	protected Result doInBackground(Void... params) {
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
		Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(3);
		hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, listener);
		MultiFormatReader multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		long start = System.currentTimeMillis();
		Result rawResult = null;
		try {
			rawResult = multiFormatReader.decodeWithState(bitmap);
			mBitmap = luminanceSource.renderCroppedGreyScaleBitmap();
			long end = System.currentTimeMillis();
			Log.d("DecodeThread", "Decode use " + (end - start) + "ms");
		} catch (ReaderException re) {
		} finally {
			multiFormatReader.reset();
		}
		return rawResult;
	}

	@Override
	protected void onPostExecute(Result result) {
		if (listener != null && !isStop) {
			if (result == null) {
				listener.onDecodeFailed(luminanceSource);
			} else {
				listener.onDecodeSuccess(result, luminanceSource, mBitmap);
			}
		}
	}

	public void cancel() {
		isStop = true;
		cancel(true);
	}
}
