package util;

public class FFMpegUtil 
{
	public static String getCodecForFormat(String format)
	{
		if(format.equals("JPEG")) return "mjpeg";
		if(format.equals("PNG")) return "png";
		
		return null;
	}
}
