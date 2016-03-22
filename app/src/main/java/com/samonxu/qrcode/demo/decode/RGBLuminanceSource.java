package com.samonxu.qrcode.demo.decode;

import android.graphics.Bitmap;

import com.samonxu.qrcode.demo.camera.Size;


public class RGBLuminanceSource extends LuminanceSource {

	private byte[] luminances;

	public RGBLuminanceSource(int[] rgbPixels, Size imageSize) {
		super(imageSize.width, imageSize.height);
		int width = imageSize.width;
		int height = imageSize.height;
		luminances = new byte[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				int pixel = rgbPixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if (r == g && g == b) {
					// Image is already greyscale, so pick any channel.
					luminances[offset + x] = (byte) r;
				} else {
					// Calculate luminance cheaply, favoring green.
					luminances[offset + x] = (byte) ((r + g + g + b) >> 2);
				}
			}
		}
	}

	@Override
	public byte[] getMatrix() {
		return luminances;
	}

	@Override
	public byte[] getRow(int y, byte[] row) {
		if (y < 0 || y >= getHeight()) {
			throw new IllegalArgumentException("Requested row is outside the image: " + y);
		}
		int width = getWidth();
		if (row == null || row.length < width) {
			row = new byte[width];
		}
		System.arraycopy(luminances, y * width, row, 0, width);
		return row;
	}

	public Bitmap renderCroppedGreyScaleBitmap() {
		int width = getWidth();
		int height = getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int grey = luminances[y * width + x] & 0xff;
				pixels[y * width + x] = 0xFF000000 | (grey * 0x00010101);
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

}
