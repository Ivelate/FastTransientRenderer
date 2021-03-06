package storage;

import java.nio.FloatBuffer;

import core.ProgramParams;

/**
 * Stores the transient storage image compressing it using its gamma. Ignores the 4th channel. The storage is bidimensional in order to
 * be able to contain a higher amount of data entries than Integer.MAX_VALUE
 */
public class TransientStorageGammaBig extends TransientStorage
{
	private byte storage[][];
	private float finalImage[]=null;
	public TransientStorageGammaBig(int xres,int yres,int tres,ProgramParams params,boolean saveFinalImage)
	{
		super(xres,yres,tres,4,params);
		
		storage=new byte[tres][yres*xres*3];
		if(saveFinalImage) finalImage=new float[xres*yres*3];
	}

	protected int getNormalizedIndex(int x,int y,int c)
	{
		return (y*xres + x)*3 + c;
		//return (c*yres*tres + t*yres + y)*xres + x;
	}
	
	@Override
	protected void insertIntoStorage(float data, int x, int y, int t,int c) 
	{
		if(c==3) return;
		float gammaData=applyGamma(data);
		
		storage[t][getNormalizedIndex(x,y,c)]=(byte)(gammaData*255);
		
		if(finalImage!=null) finalImage[(y*xres + x)*3 + c]+=data; //Assuming only one insertion!
	}

	@Override
	public float getData(int x, int y, int t,int c) {
		if(c==3) return 0; //4th channel = alpha = 1
		return (storage[t][getNormalizedIndex(x,y,c)]&0xFF)/255f;
	}
	
	@Override
	protected float getImageData(int x, int y, int c) {
		return finalImage!=null?finalImage[(y*xres + x)*3 + c]:0;
	}

	@Override
	protected boolean imageGammaCorrected() {
		return true;
	}
	
	
}
