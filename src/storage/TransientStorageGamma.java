package storage;

import java.nio.FloatBuffer;

import core.ProgramParams;

/**
 * Stores the transient storage image compressing it using its gamma. Ignores the 4th channel
 */
public class TransientStorageGamma extends TransientStorage
{
	private byte storage[];
	private float finalImage[]=null;
	public TransientStorageGamma(int xres,int yres,int tres,ProgramParams params,boolean saveFinalImage)
	{
		super(xres,yres,tres,4,params);
		
		storage=new byte[xres*yres*tres*3];
		if(saveFinalImage) finalImage=new float[xres*yres*3];
	}

	protected int getNormalizedIndex(int x,int y,int t,int c)
	{
		return (t*yres*xres + y*xres + x)*3 + c;
		//return (c*yres*tres + t*yres + y)*xres + x;
	}
	
	@Override
	protected void insertIntoStorage(float data, int x, int y, int t,int c) 
	{
		if(c==3) return;
		float gammaData=applyGamma(data);
		
		storage[getNormalizedIndex(x,y,t,c)]=(byte)(gammaData*255);
		
		if(finalImage!=null) finalImage[(y*xres + x)*3 + c]+=data; //Assuming only one insertion!
	}

	@Override
	public float getData(int x, int y, int t,int c) {
		if(c==3) return 0; //4th channel = alpha = 1
		return (storage[getNormalizedIndex(x,y,t,c)]&0xFF)/255f;
	}

	@Override
	protected boolean imageGammaCorrected() {
		return true;
	}

	@Override
	protected float getImageData(int x, int y, int c) {
		return finalImage!=null?finalImage[(y*xres + x)*3 + c]:0;
	}
	
	
}
