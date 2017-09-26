package core;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import material.Material;

/**
 * Check INPUT.md file for more info
 */
public class ProgramParams 
{
	public int RES_X=600;
	public int RES_Y=400;
	public int RES_T=250;
	
	public int BATCHING=2;
	public boolean SAVE_HDR=false; //No utility to save HDR exists in java yet, so its better to leave this arg as false.
	public String VIDEO_NAME="output_video";
	public boolean SAVE_IMAGES=false; //If true, images will be saved into /img folder. If false, they would be memory-piped to FFMPEG
	public String IMAGE_FORMAT="PNG"; //PNG: Lossless, big. JPEG: Lossy, fast. HDR: Unsupported for now
	public boolean SAVE_STEADY_IMAGE=false; //If true, an image will be saved containing all temporal information (steady state)
	public String STEADY_IMAGE_NAME="output_image";
	
	public float T0=0;
	public float TDELTA=0.05f;
	
	public float INTENSITY_GAMMA_CORRECTION=4000;
	
	public int LIGHT_RES_X=600;
	public int LIGHT_RES_Y=400;
	
	
	public Vector3f camPos=new Vector3f(2,1,0);
	public Vector3f camLookTo=new Vector3f(0,1.2f,0);
	public float camznear=0.1f;
	public float camzfar=10f;
	public float camfov=90;
	
	
	//public Vector3f lightPos=new Vector3f(0,3.5f,0);
	//public Vector3f lightLookTo=new Vector3f(0,0,0);
	public Vector3f lightPos=camPos;
	public Vector3f lightLookTo=camLookTo;
	public float lightznear=0.1f;
	public float lightzfar=10f;
	public float lightfov=90;

	
	public List<Material> materialsUsed=new ArrayList<Material>();
	public List<String> routesUsed=new ArrayList<String>();
	
	
	
	
	//************** VALIDATOR *************//
	public void validateParams()
	{
		if(!(IMAGE_FORMAT.equals("PNG")||IMAGE_FORMAT.equals("JPEG"))){
			System.err.println("Incorrect image format used. Using PNG");
			IMAGE_FORMAT="PNG";
		}
		
		if(RES_X%BATCHING!=0){
			RES_X=((RES_X/BATCHING)+1)*BATCHING;
			System.err.println("BATCHING must be a divisor of RES_X. New RES_X="+RES_X);
		}
		if(RES_Y%BATCHING!=0){
			RES_Y=((RES_Y/BATCHING)+1)*BATCHING;	
			System.err.println("BATCHING must be a divisor of RES_Y. New RES_Y="+RES_Y);
		}
		if(RES_T%BATCHING!=0){
			RES_T=((RES_T/BATCHING)+1)*BATCHING;
			System.err.println("BATCHING must be a divisor of RES_T. New RES_T="+RES_T);
		}
	}
}
