package storage;

import java.nio.FloatBuffer;

import core.ProgramParams;

/**
 * Stores the transient storage image. Ignores the 4th channel
 */
public class TransientStorageRaw extends TransientStorage
{
	private float storage[][]; //I think this way is equally as fast than a big [] (preventing overflow) but i can be wrong tho
	public TransientStorageRaw(int xres,int yres,int tres,ProgramParams params)
	{
		super(xres,yres,tres,4,params);
		
		storage=new float[tres][xres*yres*3];
	}

	protected int getNormalizedIndex(int x,int y,int c)
	{
		return (y*xres + x)*3 + c;
		//return (t*yres*xres + y*xres + x)*3 + c;
		//return (c*yres*tres + t*yres + y)*xres + x;
	}
	
	@Override
	protected void insertIntoStorage(float data, int x, int y, int t,int c) 
	{
		if(c==3) return;
		storage[t][getNormalizedIndex(x,y,c)]=data;
	}

	@Override
	protected float getData(int x, int y, int t,int c) {
		if(c==3) return 0; //4th channel = alpha = 1
		return storage[t][getNormalizedIndex(x,y,c)];
	}

	@Override
	protected boolean imageGammaCorrected() {
		return false;
	}

	@Override
	protected float getImageData(int x, int y, int c) {
		float val=0;
		for(int t=0;t<this.tres;t++)
		{
			val+=storage[t][getNormalizedIndex(x,y,c)];
		}
		return val;
	}
	
	
}
