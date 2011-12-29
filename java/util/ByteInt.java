package util;

public class ByteInt {

	public static byte[] intToByteArray(long value) {
		return new byte[]{
					(byte) (value >>> 24),
					(byte) (value >>> 16),
					(byte) (value >>> 8),
					(byte) value};
	}

	public static long byteArrayToInt(byte[] b) {
		return (b[0] << 24)
				+ ((b[1] & 0xFF) << 16)
				+ ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}
}
