package storage;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import core.ProgramParams;
import util.FFMpegUtil;
import util.ReadStream;

/**
 * Stores the transient storage image
 */
public abstract class TransientStorage
{
	protected int xres, yres, tres, channels;
	
	private float intensity_gammacorrection;
	private String imageFormat="";
	
	public TransientStorage(int xres,int yres,int tres,int channels,ProgramParams params)
	{
		this.xres=xres;
		this.yres=yres;
		this.tres=tres;
		this.channels=channels;
		
		this.intensity_gammacorrection=params.INTENSITY_GAMMA_CORRECTION;
		this.imageFormat=params.IMAGE_FORMAT;
	}
	
	/**
	 * Inserts the contents of a buffer fbuff from the coordinates xi,yi to the coordinates xf,yf. The temporal coordinates are inserted fully
	 */
	public void insertData(FloatBuffer fbuff,int xi,int yi,int xf,int yf)
	{
		for(int t=0;t<tres;t++)
		{
			for(int y=yi;y<yf;y++)
			{
				for(int x=xi;x<xf;x++)
				{
					for(int c=0;c<channels;c++)
					{
						float data=fbuff.get();
						insertIntoStorage(data,x,y,t,c);
					}
				}
			}
		}
	}
	
	protected abstract void insertIntoStorage(float data,int x,int y, int t,int c);
	
	protected abstract float getData(int x,int y,int t,int c);
	
	protected abstract float getImageData(int x,int y,int c); //Gets the data from the steady image
	
	/**
	 * Saves the steady image contained into the storage into a file
	 */
	public void saveStorageSteadyImage(File imageFile) throws IOException
	{		
		BufferedImage outImage = new BufferedImage(xres, yres,
				                    BufferedImage.TYPE_INT_RGB);
		float maxIntensity=0;
		//Find maximum intensity
		for(int x=0;x<xres;x++) for(int y=0;y<yres;y++) for(int c=0;c<3;c++)
		{
			float currentIntensity=getImageData(x,y,c);
			if(currentIntensity>maxIntensity) maxIntensity=currentIntensity;
		}

		float gammaIntensityCorrection=1/maxIntensity; //maxIntensity gamma will equal one.
		for(int x=0;x<xres;x++) for(int y=0;y<yres;y++)
		{
			int r,g,b;

			r=(int)(255*applyGamma(getImageData(x,y,0),gammaIntensityCorrection));
			g=(int)(255*applyGamma(getImageData(x,y,1),gammaIntensityCorrection));
			b=(int)(255*applyGamma(getImageData(x,y,2),gammaIntensityCorrection));

			int c=r<<16 | g << 8 | b;
			
			if(c<0) System.out.println(c);
			outImage.setRGB(x, yres-y-1, c);
		}
	

		System.out.println("Saving image "+imageFile.getName());
		ImageIO.write(outImage, this.imageFormat, imageFile);
	}
	
	/**
	 * Saves the contents of the storage into a folder of images
	 */
	public void saveStorageAsImageStreaks(File file) throws IOException
	{
		saveStorageAsImageStreaks(file,null);
	}
	
	/**
	 * Saves the contents of the storage into a stream, which will be piped to FFMPEG
	 */
	public void saveStorageAsImageStreaks(OutputStream stream) throws IOException
	{
		saveStorageAsImageStreaks(null,stream);
	}
	
	private void saveStorageAsImageStreaks(File directory,OutputStream stream) throws IOException
	{
		//BufferedOutputStream bstream=new BufferedOutputStream(stream);
		for(int t=0;t<tres;t++)
		{			
			BufferedImage outImage =
					  new BufferedImage(xres, yres,
					                    BufferedImage.TYPE_INT_RGB);

			for(int x=0;x<xres;x++) for(int y=0;y<yres;y++)
			{
				int r,g,b;
				if(imageGammaCorrected()){
					r=(int)(255*getData(x,y,t,0));
					g=(int)(255*getData(x,y,t,1));
					b=(int)(255*getData(x,y,t,2));
				}
				else{
					r=(int)(255*applyGamma(getData(x,y,t,0)));
					g=(int)(255*applyGamma(getData(x,y,t,1)));
					b=(int)(255*applyGamma(getData(x,y,t,2)));
				}
				int c=r<<16 | g << 8 | b;
				
				if(c<0) System.out.println(c);
				outImage.setRGB(x, yres-y-1, c);
			}
		
			if(directory!=null)
			{
				String ts=t+"";
				while(ts.length()<(tres+"").length()) ts="0"+ts;
				File f=new File(directory,"IMG_"+ts+"."+this.imageFormat);
				System.out.println("Saving image "+f.getName());
				ImageIO.write(outImage, this.imageFormat, f);
			}
			else if(stream!=null)
			{
				//To pipe!
				System.out.println("Piping image "+t+"/"+tres);
				ImageIO.write(outImage,this.imageFormat, stream); 
			}
		}
	}
	
	/**
	 * Creates a video commanding FFMPEG to get the images from a disk folder
	 */
	public void createVideo(File directory,File videoFile)
	{
		System.out.println("Creating video");
		Process p=null;
		 try
		 {
			 String cmd="ffmpeg -framerate 24 -i "+directory.getPath()+"/IMG_%0"+(tres+"").length()+"d."+this.imageFormat+" -pix_fmt yuv420p "+videoFile.getAbsolutePath()+" -y"; 
			 
			 p = Runtime.getRuntime().exec(cmd) ;  
			 ReadStream s1 = new ReadStream("stdin", p.getInputStream ());
			 ReadStream s2 = new ReadStream("stderr", p.getErrorStream ());
			 s1.start ();
			 s2.start ();
			 p.waitFor();     
			 System.out.println();
			 System.out.println("Video created succesfully at "+videoFile.getAbsolutePath());
		 } catch (Exception e) {  
			 e.printStackTrace();  
		 } finally {
		     if(p != null)
		         p.destroy();
		 }
	}
	
	/**
	 * Creates a video commanding FFMPEG to get the images using a memory pipe which will be written by us
	 */
	public Process createVideoAsync(File videoFile)
	{
		System.out.println("Creating video asynchronously (EXPERIMENTAL)");
		Process p=null;
		 try
		 {
			 String cmd= "ffmpeg -f image2pipe -vcodec "+FFMpegUtil.getCodecForFormat(this.imageFormat)+" -framerate 24 -i - -pix_fmt yuv420p "+videoFile.getAbsolutePath()+" -y"; 
			 
			 p = Runtime.getRuntime().exec(cmd) ;  
			 ReadStream s1 = new ReadStream("stdin", p.getInputStream ());
			 ReadStream s2 = new ReadStream("stderr", p.getErrorStream ());
			 s1.start ();
			 s2.start ();
		 } catch (Exception e) {  
			 e.printStackTrace();  
		 } 
		 
		 return p;
	}
	
	protected abstract boolean imageGammaCorrected();
	
	/**
	 * Applies a gamma correction over <val>
	 */
	protected float applyGamma(float val)
	{
		float gamm=(float)Math.pow(val * intensity_gammacorrection,1/2.2);
		if(gamm>1) gamm=1;
		return gamm;
	}
	protected float applyGamma(float val,float intensityCorrection)
	{
		float gamm=(float)Math.pow(val * intensityCorrection,1/2.2);
		if(gamm>1) gamm=1;
		return gamm;
	}
	
	private static void saveGrayscaleTextureToFile(float[] img,File file,int resx,int resy) throws IOException
	{
		float maxvalue=0;
		for(int i=0;i<img.length;i++) if(maxvalue<img[i]) maxvalue=img[i];
		System.out.println("Max value= "+maxvalue); //|TODO debug
		BufferedImage outImage =
				  new BufferedImage(resx, resy,
				                    BufferedImage.TYPE_INT_RGB);

		if(maxvalue>0){
			for(int x=0;x<resx;x++) for(int y=0;y<resy;y++) {
				
				int c=(int)((img[y*resx+x]*255) / maxvalue);
				
				if(c<0) System.out.println(c);
				outImage.setRGB(x, resy-y-1, c << 16 | c << 8 | c);
			}
		}
	
		ImageIO.write(outImage, "PNG", file);
	}
}
