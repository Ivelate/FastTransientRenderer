package geometry;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

/**
 * Well it's not exactly a float buffer pool as it only has one float buffer. Let's call this class a "Float Buffer singleton"
 */
public class FloatBufferPool 
{
	private static final int INITIAL_BUFFER_CAPACITY=4096;
	
	//Well, it only has 1 float buffer hehe
	private static FloatBuffer myFloatBuff=BufferUtils.createFloatBuffer(INITIAL_BUFFER_CAPACITY);
	private static int buffSize=0;
	
	public static void generateBuffer(int size)
	{
		//myFloatBuff=BufferUtils.createFloatBuffer(size);
		int objSize=myFloatBuff.capacity();
		while(objSize<size) objSize*=2;
		
		myFloatBuff=BufferUtils.createFloatBuffer(objSize);
	}
	public static FloatBuffer requestBuffer(int size)
	{
		if(buffSize<size) generateBuffer(size);
		
		myFloatBuff.clear();
		return myFloatBuff;
	}
}
