/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samonxu.qrcode.demo.decode;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.samonxu.qrcode.demo.camera.Size;

/**
 * <p>
 * Android摄像机获取的图像默认采用YCbCr_420_SP（即YUV420SP或NV21）存储格式，为YUV图像的一种。
 * </p>
 * <p>
 * <b>[采样比例]</b><br>
 * YUV图像采样比例以下几种：<br>
 * 444采样中，Y:U:V=4:4:4，每一个Y对应一个UV<br>
 * 422采样中，Y:U:V=4:2:2，每两个Y共用一个UV<br>
 * 411采样中，Y:U:V=4:1:1，每四个Y共用一个UV<br>
 * 420采样中，Y:UV=4:2或Y:U:V=4:1:1，每四个Y共用一个UV<br>
 * 其中420采样并不代表只有U没有V，而是在采样的时候，如果这一行用4:2:0采样，下一行就用4:0:2采样，总体比率还是Y:UV=4:2或Y:U:V=4:1:1的。
 * </p>
 * <p>
 * <b>[存储格式]</b><br>
 * YUV图像有两大类型，Planar类型和Packed类型。<br>
 * Planar存储格式为：Y...U...V... 或 Y...V...U... 或 Y...(UV)... 等；<br>
 * Packed存储格式为： (YUV)... 或 (YUVY)... 或 (UYVY)... 等
 * </p>
 * <p>
 * <b>[数据大小]</b><br>
 * YUV420图像为Planar类型，先是连续存储的Y（明度/灰度信息），然后是色彩信息U(Cb，蓝色色度)和V(Cr，红色色度)。<br>
 * YUV420图像中的比例为Y:UV=4:2，其中Y、U、V各占一个byte。 <br>
 * 也就是说2/3的数据为Y信息，1/3为色彩信息，所以YUV数据长度为图像长宽乘积的1.5倍。<br>
 * YUV420有两种，YUV420P和YUV420SP。<br>
 * YUV420P为Y...U...V...类型；<br>
 * YUV420SP为Y...(UV)...类型，其中的U、V为交错存储。<br>
 * </p>
 * <p>
 * <b>[二维码处理]</b><br>
 * 在二维码处理过程中，只需要用到Y信息（前2/3的数据），不必考虑UV数据具体存储规则。<br>
 * 本类兼容所有Planar格式的YUV图像。
 * </p>
 */
public class PlanarYUVLuminanceSource extends LuminanceSource {
	private byte[] yuvData;
	private Size dataSize;
	private Rect previewRect;

	/**
	 * @param yuvData
	 *                  YUV数据，包含Y信息和UV信息
	 * @param dataSize
	 *                  图像大小
	 * @param previewRect
	 *                  要处理的图像区域
	 */
	public PlanarYUVLuminanceSource(byte[] yuvData, Size dataSize, Rect previewRect) {
		super(previewRect.width(), previewRect.height());

		if (previewRect.left + previewRect.width() > dataSize.width || previewRect.top + previewRect.height() > dataSize.height) {
			throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
		}

		this.yuvData = yuvData;
		this.dataSize = dataSize;
		this.previewRect = previewRect;
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
		int offset = (y + previewRect.top) * dataSize.width + previewRect.left;
		System.arraycopy(yuvData, offset, row, 0, width);
		return row;
	}

	@Override
	public byte[] getMatrix() {
		int width = getWidth();
		int height = getHeight();
		if (width == dataSize.width && height == dataSize.height) {
			return yuvData;
		}
		int area = width * height;
		byte[] matrix = new byte[area];
		int inputOffset = previewRect.top * dataSize.width + previewRect.left;

		if (width == dataSize.width) {
			System.arraycopy(yuvData, inputOffset, matrix, 0, area);
			return matrix;
		}

		byte[] yuv = yuvData;
		for (int y = 0; y < height; y++) {
			int outputOffset = y * width;
			System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
			inputOffset += dataSize.width;
		}
		return matrix;
	}

	@Override
	public boolean isCropSupported() {
		return true;
	}

	public int getDataWidth() {
		return dataSize.width;
	}

	public int getDataHeight() {
		return dataSize.height;
	}

	/**
	 * 根据扫描结果，生成一个灰度图像
	 * 
	 * @return
	 */
	public Bitmap renderCroppedGreyScaleBitmap() {
		int width = getWidth();
		int height = getHeight();
		int[] pixels = new int[width * height];
		byte[] yuv = yuvData;
		int inputOffset = previewRect.top * dataSize.width + previewRect.left;
		for (int y = 0; y < height; y++) {
			int outputOffset = y * width;
			for (int x = 0; x < width; x++) {
				int grey = yuv[inputOffset + x] & 0xff;
				pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
			}
			inputOffset += dataSize.width;
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
}
