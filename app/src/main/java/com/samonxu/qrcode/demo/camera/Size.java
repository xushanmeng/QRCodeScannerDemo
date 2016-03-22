package com.samonxu.qrcode.demo.camera;

public class Size {
	public Size(int w, int h) {
		width = w;
		height = h;
	}

	public Size(Size src) {
		width = src.width;
		height = src.height;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Size)) {
			return false;
		}
		Size s = (Size) obj;
		return width == s.width && height == s.height;
	}

	public int size() {
		return width * height;
	}

	public int width;
	public int height;

	@Override
	public String toString() {
		return width + "x" + height;
	}
};