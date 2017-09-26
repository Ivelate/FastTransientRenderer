package core;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.GL_TEXTURE3;
import static org.lwjgl.opengl.GL13.GL_TEXTURE4;
import static org.lwjgl.opengl.GL13.GL_TEXTURE5;
import static org.lwjgl.opengl.GL13.GL_TEXTURE6;
import static org.lwjgl.opengl.GL13.GL_TEXTURE7;
import static org.lwjgl.opengl.GL13.GL_TEXTURE8;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT2;
import static org.lwjgl.opengl.GL30.GL_COMPARE_REF_TO_TEXTURE;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.GL_RGB32F;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenRenderbuffers;
import static org.lwjgl.opengl.GL30.glRenderbufferStorage;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL44;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import geometry.Quad;
import geometry.SceneManager;
import material.DiffuseMaterial;
import material.Material;
import shaders.FullscreenShader;
import shaders.ShaderLibrary;
import shaders.SolidAnglePrecomputationShader;
import shaders.VideoPlayShader;
import storage.TransientStorage;
import storage.TransientStorageGamma;
import storage.TransientStorageGammaBig;
import storage.TransientStorageRaw;
import util.Camera;
import util.IncorrectArgumentsException;
import util.MatrixHelper;

public class FastTransientRenderer 
{
	private final static String NATIVE_FOLDER_NAME="fastTransientRendererLibs";
	private ProgramParams params;
	
	private SceneManager sceneManager;
	private Camera mainCamera;
	private Camera lightCamera;
	
	private int lightFbo;
	private int camFbo;
	private int solidAngleFbo;
	
	private int voxelStorageTexture;
	private int solidAngleTexture;
	
	private TransientStorage transientStorage;
	
	private Quad quad;
	private FullscreenShader fullscreenShader;
	private VideoPlayShader videoPlayShader;
	private SolidAnglePrecomputationShader solidAnglePrecompShader;
	
	public FastTransientRenderer(ProgramParams params) throws LWJGLException
	{
		params.validateParams();
		
		this.params=params;
		
		//if(params.SAVE_HDR)
		this.transientStorage=params.SAVE_HDR?new TransientStorageRaw(params.RES_X,params.RES_Y,params.RES_T,params):
			new TransientStorageGammaBig(params.RES_X,params.RES_Y,params.RES_T,params,params.SAVE_STEADY_IMAGE);
		
		//Creating the display
		Display.setTitle("Fast Transient Renderer");
		Display.setResizable(false);
		Display.setDisplayMode(new DisplayMode(params.RES_X/params.BATCHING,params.RES_Y/params.BATCHING));
		Display.setVSyncEnabled(false);
		Display.setFullscreen(false);
		
		Display.create();

		initResources();
		
		long ti=System.currentTimeMillis();
		render(); //We render the transient video and exit

		GL11.glFinish();
		
		System.out.println("Rendered t="+(System.currentTimeMillis()-ti)+"ms");
		
		System.out.println(GL11.glGetError());
		cleanup();
		
		//Save steady state image if specified
		if(params.SAVE_STEADY_IMAGE) saveSteadyImage(params.STEADY_IMAGE_NAME);
		//Save video
		long tii=System.currentTimeMillis();
		
		if(params.SAVE_IMAGES)saveVideo(params.VIDEO_NAME);
		else saveVideoAsync(params.VIDEO_NAME);
		
		System.out.println("Time to save video: "+(System.currentTimeMillis()-tii)+"ms");
	}
	
	/**
	 * Saves the steady image resulting of this rendering into disk
	 */
	private void saveSteadyImage(String imageName)
	{
		try 
		{
			File imageFile=new File(imageName+"."+params.IMAGE_FORMAT);
			int repeatedNames=0;
			while(imageFile.exists()){
				repeatedNames++;
				imageFile=new File(imageName+"_"+repeatedNames+"."+params.IMAGE_FORMAT);
			}
			this.transientStorage.saveStorageSteadyImage(imageFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves all transient images to a folder and creates a video using those
	 */
	private void saveVideo(String videoName)
	{
		try 
		{
			File imgFolder=new File("imgs");	
			File videoFile=new File(videoName+".mp4");
			int repeatedNames=0;
			while(videoFile.exists()){
				repeatedNames++;
				videoFile=new File(videoName+"_"+repeatedNames+".mp4");
			}
			this.transientStorage.saveStorageAsImageStreaks(imgFolder);
			this.transientStorage.createVideo(imgFolder,videoFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a video without saving the transient images to disk. The images are created and piped to FFMPEG directly via STDOUT / STDIN.
	 */
	private void saveVideoAsync(String videoName)
	{
		try 
		{
			File videoFile=new File(videoName+".mp4");
			int repeatedNames=0;
			while(videoFile.exists()){
				repeatedNames++;
				videoFile=new File(videoName+"_"+repeatedNames+".mp4");
			}
			
			Process p=this.transientStorage.createVideoAsync(videoFile);
			this.transientStorage.saveStorageAsImageStreaks(p.getOutputStream());
			p.getOutputStream().close();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				p.destroy();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Inits OpenGL resources
	 */
	public void initResources()
	{
		GL11.glClearColor(0,0,0,0);
		GL11.glViewport(0, 0, params.RES_X, params.RES_Y);
		GL11.glEnable(GL11.GL_CULL_FACE); //cull face
		GL11.glEnable(GL11.GL_DEPTH_TEST); //Depth test
		GL11.glDisable( GL11.GL_BLEND ); //No hay cosas transparentes
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		this.lightFbo=GL30.glGenFramebuffers();
		this.camFbo=GL30.glGenFramebuffers();
		this.solidAngleFbo=GL30.glGenFramebuffers();
		
		//****************************** SOLID ANGLE PRECOMP FRAMEBUFFER ****************************************/
		//TEXTURE4 contains the solid angle data
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.solidAngleFbo);
		int anglesTex=glGenTextures();
		glActiveTexture(GL13.GL_TEXTURE4);
		glBindTexture(GL_TEXTURE_2D, anglesTex);
		glTexImage2D(GL_TEXTURE_2D, 0,GL30.GL_R32F, params.LIGHT_RES_X/2, params.LIGHT_RES_Y/2, 0,GL11.GL_RED, GL_UNSIGNED_BYTE, (FloatBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, anglesTex, 0);
		IntBuffer drawBuffers = BufferUtils.createIntBuffer(1); 
		
		drawBuffers.put(GL_COLOR_ATTACHMENT0);
		
		drawBuffers.flip();
		GL20.glDrawBuffers(drawBuffers);
		this.solidAngleTexture=anglesTex;
		System.out.println("ERR"+GL11.glGetError());

		
		//****************************** CAM FRAMEBUFFER ****************************************/
		//TEXTURE0 contains the albedo texture from camera view
		//TEXTURE1 contains the position texture (not normalized) from camera view
		//TEXTURE7 contains the normals texture from camera view
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.camFbo);
		int colorTexture=glGenTextures();
		
		//Creates and inits the base color texture as a RGB texture
		glActiveTexture(GL13.GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, colorTexture);
		glTexImage2D(GL_TEXTURE_2D, 0,GL_RGB, params.RES_X, params.RES_Y, 0,GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTexture, 0);
		glActiveTexture(GL13.GL_TEXTURE1);
		
		int positionTexture=glGenTextures();
		glBindTexture(GL_TEXTURE_2D, positionTexture);
		glTexImage2D(GL_TEXTURE_2D, 0,GL30.GL_RGB16F, params.RES_X, params.RES_Y, 0,GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, positionTexture, 0);

		glActiveTexture(GL13.GL_TEXTURE7);
		int normalTexture=glGenTextures();
		glBindTexture(GL_TEXTURE_2D, normalTexture);
		glTexImage2D(GL_TEXTURE_2D, 0,GL11.GL_RGB8, params.RES_X, params.RES_Y, 0,GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, normalTexture, 0);
			
		//The depth buffer of this FBO will be a RENDER BUFFER as we don't need to access it later
		int depthRenderBuffer=GL30.glGenRenderbuffers();
		glBindRenderbuffer( GL_RENDERBUFFER, depthRenderBuffer );
		glRenderbufferStorage( GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, params.RES_X, params.RES_Y);
		GL30.glFramebufferRenderbuffer( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderBuffer);
		/*int baseFboDepth=glGenTextures();

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, baseFboDepth);
		glTexImage2D(GL_TEXTURE_2D, 0,GL14.GL_DEPTH_COMPONENT24, params.RES_X, params.RES_Y, 0,GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, (FloatBuffer)null); 
		System.out.println("ERR"+GL11.glGetError());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, baseFboDepth, 0); //Set the depth texture as the default render depth target
		*/
		drawBuffers = BufferUtils.createIntBuffer(3); //Drawing to 3 textures
		
		drawBuffers.put(GL_COLOR_ATTACHMENT0);
		drawBuffers.put(GL_COLOR_ATTACHMENT1);
		drawBuffers.put(GL_COLOR_ATTACHMENT2);
		
		drawBuffers.flip();
		GL20.glDrawBuffers(drawBuffers);
		
		//****************************** LIGHT FRAMEBUFFER ****************************************/
		//TEXTURE2 contains the albedo texture from light view
		//TEXTURE3 contains the position texture (not normalized) from light view
		//TEXTURE6 contains the normals texture from light view
		//TEXTURE7 contains the depth texture of this FBO to perform shadow mapping
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.lightFbo);
		int colorTextureLight=glGenTextures();
		
		//Creates and inits the base color texture as a RGB texture
		glActiveTexture(GL13.GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, colorTextureLight);
		glTexImage2D(GL_TEXTURE_2D, 0,GL_RGB, params.LIGHT_RES_X, params.LIGHT_RES_Y, 0,GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureLight, 0);
		int positionTextureLight=glGenTextures();
		//Creates and inits the base color texture as a RGB texture
		glActiveTexture(GL13.GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, positionTextureLight);
		glTexImage2D(GL_TEXTURE_2D, 0,GL30.GL_RGB16F, params.LIGHT_RES_X, params.LIGHT_RES_Y, 0,GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, positionTextureLight, 0);
		int normalTextureLight=glGenTextures();
		glActiveTexture(GL13.GL_TEXTURE6);
		glBindTexture(GL_TEXTURE_2D, normalTextureLight);
		glTexImage2D(GL_TEXTURE_2D, 0,GL11.GL_RGB8, params.LIGHT_RES_X, params.LIGHT_RES_Y, 0,GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, normalTextureLight, 0);
			
		//The depth buffer of this FBO will be a texture, too. This will make depth sorting slower but we will be able to access depth values later.
		/*int depthRenderBufferLight=GL30.glGenRenderbuffers();
		glBindRenderbuffer( GL_RENDERBUFFER, depthRenderBufferLight );
		glRenderbufferStorage( GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, params.LIGHT_RES_X, params.LIGHT_RES_Y);
		GL30.glFramebufferRenderbuffer( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderBufferLight);*/
		int baseFboDepthLight=glGenTextures();

		glActiveTexture(GL_TEXTURE5);
		glBindTexture(GL_TEXTURE_2D, baseFboDepthLight);
		glTexImage2D(GL_TEXTURE_2D, 0,GL14.GL_DEPTH_COMPONENT24, params.LIGHT_RES_X, params.LIGHT_RES_Y, 0,GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, (FloatBuffer)null); 
		System.out.println("ERR"+GL11.glGetError());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		//glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, baseFboDepthLight, 0); //Set the depth texture as the default render depth target
		
		IntBuffer drawBuffersLight = BufferUtils.createIntBuffer(3); //Drawing to 2 textures
		
		drawBuffersLight.put(GL_COLOR_ATTACHMENT0);
		drawBuffersLight.put(GL_COLOR_ATTACHMENT1);
		drawBuffersLight.put(GL_COLOR_ATTACHMENT2);
		
		drawBuffersLight.flip();
		GL20.glDrawBuffers(drawBuffersLight);
		
		
		//****************************************** FRAMEBUFFER END ***************************************************/
		
		//Creating 3D storage texture
		this.voxelStorageTexture=glGenTextures();
		System.out.println(GL11.glGetError());

		glActiveTexture(GL13.GL_TEXTURE0); //Texture 0 is the 3d objective texture 
		glBindTexture(GL_TEXTURE_3D, voxelStorageTexture); 
		glTexParameteri(GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST); 
		glTexParameteri(GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		//Using a float value, so no concurrency for now (32F,32F,32F). One 32F is empty because GLSL dont support RGB YET |TODO
		GL12.glTexImage3D(GL_TEXTURE_3D,0,GL30.GL_RGBA32F,params.RES_X/params.BATCHING,params.RES_Y/params.BATCHING,params.RES_T,0, GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,(FloatBuffer)null);
		System.out.println(GL11.glGetError());

		//****************************************** FULLSCREEN BUFFER *************************************************/
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		this.mainCamera=new Camera(params.camznear,params.camzfar,params.camfov,((float)params.RES_X)/params.RES_Y);
		this.mainCamera.moveTo(params.camPos);
		this.mainCamera.lookTo(params.camLookTo);
		
		this.lightCamera=new Camera(params.lightznear,params.lightzfar,params.lightfov,((float)params.LIGHT_RES_X)/params.LIGHT_RES_Y);
		this.lightCamera.moveTo(params.lightPos);
		this.lightCamera.lookTo(params.lightLookTo);

		ShaderLibrary.init();
		this.sceneManager=new SceneManager(params);
		try {
			for(int i=0;i<params.routesUsed.size();i++){
				this.sceneManager.loadNewGeometry(params.materialsUsed.get(i), params.routesUsed.get(i));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.sceneManager.loadIntoGPU();
		
		this.quad=new Quad();
		this.fullscreenShader=new FullscreenShader();
		this.videoPlayShader=new VideoPlayShader();
		this.solidAnglePrecompShader=new SolidAnglePrecomputationShader();
		
		System.out.println(GL11.glGetError());
	}
	
	/**
	 * Performs all render steps resulting in the transient video stored in the 3D texture
	 */
	public void render()
	{
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		//Calculating solid angle correction texture
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.solidAngleFbo);
		GL11.glViewport(0, 0, params.LIGHT_RES_X/2, params.LIGHT_RES_Y/2);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.quad.getVbo());
		this.solidAnglePrecompShader.enable();
		this.solidAnglePrecompShader.setupAttributes();
		GL20.glUniform2f(GL20.glGetUniformLocation(this.solidAnglePrecompShader.getID(),"screenRes"),params.LIGHT_RES_X/2,params.LIGHT_RES_Y/2);
		MatrixHelper.uploadMatrix(Matrix4f.invert(this.lightCamera.getProjectionMatrix(),null), GL20.glGetUniformLocation(this.solidAnglePrecompShader.getID(), "invProjMat"));
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
		this.solidAnglePrecompShader.disable();
		
		//Drawing scene from camera and saving normals and those things
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.camFbo);		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT); //Depth must be cleared manually in some GPUs it seems |TODO ATTENTION HERE
		GL11.glViewport(0, 0, params.RES_X, params.RES_Y);
		this.sceneManager.draw(this.mainCamera);
		
		//Drawing scene from light and saving normals and those things
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.lightFbo);		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT); //Depth must be cleared manually in some GPUs it seems |TODO ATTENTION HERE
		GL11.glViewport(0, 0, params.LIGHT_RES_X, params.LIGHT_RES_Y);
		this.sceneManager.draw(this.lightCamera);

		//This buffer will be used to transfer data from GPU to CPU. As rendering is done in batches, not all data will be transfered at once
		FloatBuffer buff=BufferUtils.createFloatBuffer((int)(4L*params.RES_X*params.RES_Y*params.RES_T/(params.BATCHING*params.BATCHING)));
		
		//This buffer is used to fill the 3D storage with 0s again
		FloatBuffer zeroBuffer=BufferUtils.createFloatBuffer(4);
		zeroBuffer.put(0);zeroBuffer.put(0);zeroBuffer.put(0);zeroBuffer.put(0);
		zeroBuffer.flip();
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		//Perform rendering in batches, dividing the X and Y coordinate (the Z is fully rendered in each pass)
		for(int x=0;x<params.BATCHING;x++) for(int y=0;y<params.BATCHING;y++)
		{ 
			System.out.println("Batch "+(x*params.BATCHING+y)+"/"+params.BATCHING*params.BATCHING);
			//GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glViewport(0, 0, params.RES_X/params.BATCHING, params.RES_Y/params.BATCHING);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.quad.getVbo());
			this.fullscreenShader.enable();
			this.fullscreenShader.setupAttributes();
			
			GL20.glUniform1f(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"scale"),1f/params.BATCHING);
			GL20.glUniform2f(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"offset"),(1f/params.BATCHING)*x,(1f/params.BATCHING)*y);
			
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"camColor"),0);
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"camPosition"),1);
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"lightColor"),2);
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"lightPosition"),3);
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"pxAngles"),4);
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"lightDepth"),5);
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"camNormal"),6);
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"lightNormal"),7);
	
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"lightRes_x"),params.LIGHT_RES_X);
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"lightRes_y"),params.LIGHT_RES_Y);
			
			int lightUniform=GL20.glGetUniformLocation(this.fullscreenShader.getID(), "lightPosUniform");
			GL20.glUniform3f(lightUniform, params.lightPos.x, params.lightPos.y, params.lightPos.z);
			
			glUniform1i(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"tres"),params.RES_T);
			GL20.glUniform1f(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"t0"),params.T0);
			GL20.glUniform1f(GL20.glGetUniformLocation(this.fullscreenShader.getID(),"tdelta"),params.TDELTA);
			
			GL42.glBindImageTexture(0, this.voxelStorageTexture, 0, true, 0, GL15.GL_WRITE_ONLY, GL30.GL_RGBA32F);
			
			MatrixHelper.uploadMatrix(this.lightCamera.getProjectionViewMatrix(), GL20.glGetUniformLocation(this.fullscreenShader.getID(), "lightVP"));
	
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
			this.fullscreenShader.disable();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			GL11.glFinish();
			
			GL11.glBindTexture(GL_TEXTURE_3D, this.voxelStorageTexture);
			GL11.glGetTexImage(GL_TEXTURE_3D, 0,GL11.GL_RGBA, GL11.GL_FLOAT, buff);
			this.transientStorage.insertData(buff,params.RES_X*x/params.BATCHING, params.RES_Y*y/params.BATCHING, params.RES_X*(x+1)/params.BATCHING, params.RES_Y*(y+1)/params.BATCHING);
			buff.clear();
			//I dont kinda like this method and only works on OpenGL4.4 but it works
			GL44.glClearTexImage(this.voxelStorageTexture, 0, GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE, zeroBuffer);
			GL11.glFinish();
		}
	}
	
	/**
	 * Renders the video on real time. Unused! The video is dumped to disk instead
	 */
	@Deprecated
	public void renderVideo(int frame)
	{
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);	
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		glDisable(GL11.GL_DEPTH_TEST);
		GL11.glViewport(0, 0, params.RES_X, params.RES_Y);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.quad.getVbo());
		this.videoPlayShader.enable();
		this.videoPlayShader.setupAttributes();
		
		GL42.glBindImageTexture(0, this.voxelStorageTexture, 0, true, 0, GL15.GL_WRITE_ONLY, GL30.GL_RGBA32F);
		GL20.glUniform1i(GL20.glGetUniformLocation(this.videoPlayShader.getID(), "frameToPlay"),frame);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
		
		this.videoPlayShader.disable();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	public void cleanup()
	{
		Display.destroy();
	}
	
	
	
	/********************************************   MAIN   *************************************************/
	public static void main(String[] args) throws Exception
	{
		ProgramParams params=new ProgramParams();

		for(int i=0;i<args.length;i++)
		{
			switch(args[i])
			{
			case "-model":
				params.routesUsed.add(args[++i]);
				params.materialsUsed.add(new DiffuseMaterial(new Vector3f(Float.parseFloat(args[++i]),
						Float.parseFloat(args[++i]),Float.parseFloat(args[++i]))));
				break;
			case "-resx":
				params.RES_X=Integer.parseInt(args[++i]);
				break;
			case "-resy":
				params.RES_Y=Integer.parseInt(args[++i]);
				break;
			case "-rest":
				params.RES_T=Integer.parseInt(args[++i]);
				break;
			case "-res":
				params.RES_X=Integer.parseInt(args[++i]);
				params.RES_Y=Integer.parseInt(args[++i]);
				params.RES_T=Integer.parseInt(args[++i]);
				break;
			case "-lightresx":
				params.LIGHT_RES_X=Integer.parseInt(args[++i]);
				break;
			case "-lightresy":
				params.LIGHT_RES_Y=Integer.parseInt(args[++i]);
				break;
			case "-lightres":
				params.LIGHT_RES_X=Integer.parseInt(args[++i]);
				params.LIGHT_RES_Y=Integer.parseInt(args[++i]);
				break;
			case "-intensityCorrection":
				params.INTENSITY_GAMMA_CORRECTION=Integer.parseInt(args[++i]);
				break;
			case "-batches":
				params.BATCHING=Integer.parseInt(args[++i]);
				break;
			case "-tdelta":
				params.TDELTA=Integer.parseInt(args[++i]);
				break;
			case "-t0":
				params.T0=Integer.parseInt(args[++i]);
				break;
			case "-saveImages":
				params.SAVE_IMAGES=true;
				break;
			case "-saveSteadyImage":
				params.SAVE_STEADY_IMAGE=true;
				break;
			case "-steadyImageName":
				params.STEADY_IMAGE_NAME=args[++i];
				break;
			case "-imageFormat":
				params.SAVE_IMAGES=true;
				String format=args[++i].toUpperCase();
				switch(format)
				{
				case "PNG":
					params.IMAGE_FORMAT="PNG";
					break;
				case "JPEG":
				case "JPG":
					params.IMAGE_FORMAT="JPEG";
					break;
				default:
					throw new IncorrectArgumentsException("Unsupported image format - only PNG and JPEG supported by now");
				}
				break;
			case "-videoName":
				params.VIDEO_NAME=args[++i];
				break;
				
			case "-cam":
				params.camPos=new Vector3f(Float.parseFloat(args[++i]),Float.parseFloat(args[++i]),Float.parseFloat(args[++i]));
				params.camLookTo=new Vector3f(Float.parseFloat(args[++i]),Float.parseFloat(args[++i]),Float.parseFloat(args[++i]));
				break;
			case "-camfov":
				params.camfov=Float.parseFloat(args[++i]);
				break;
			case "-camznearfar":
				params.camznear=Float.parseFloat(args[++i]);
				params.camzfar=Float.parseFloat(args[++i]);
				break;
			case "-lightcam":
				params.lightPos=new Vector3f(Float.parseFloat(args[++i]),Float.parseFloat(args[++i]),Float.parseFloat(args[++i]));
				params.lightLookTo=new Vector3f(Float.parseFloat(args[++i]),Float.parseFloat(args[++i]),Float.parseFloat(args[++i]));
				break;
			case "-lightcamfov":
				params.lightfov=Float.parseFloat(args[++i]);
				break;
			case "-lightcamznearfar":
				params.lightznear=Float.parseFloat(args[++i]);
				params.lightzfar=Float.parseFloat(args[++i]);
				break;	
				
			}
		}
		
		if(params.materialsUsed.size()==0) {
			System.err.println("Error: No rendering model specified. Please specify a .obj to render by using -model %ROUTE% %R% %G% %B%");
			return;
		}
		
		try
		{
			try{
				FastTransientRenderer.loadNatives();
			}
			catch(Exception ex){
				System.err.println("Error loading native libraries. Program will try to keep executing, but something can fail.");
				ex.printStackTrace();
			}
			new FastTransientRenderer(params);
		}
		catch(Exception e){
			System.err.println("Exception catched! ");
			e.printStackTrace();
			throw e;
		}
		
		System.out.println("End");
	}
	
	/**
	 * Load native libraries needed for LWJGL to work
	 */
	public static final void loadNatives() throws IOException
	{
		System.out.println("NATIVE LOAD");
		File nativesFolder=new File(NATIVE_FOLDER_NAME);
		if(!nativesFolder.exists()||!nativesFolder.isDirectory()||!nativesFolder.canRead()) throw new IOException("Can't read native libraries folder");
		System.setProperty("org.lwjgl.librarypath", nativesFolder.getAbsolutePath());
	}
}
