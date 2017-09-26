package shaders;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import java.io.InputStream;

import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Wrapper class for a OpenGL shader. Makes shaders way less verbose if used this way.
 * It uses a fragment and a vertex shader, nothing less, nothing more. 
 */
public abstract class SimpleShaderProgram
{
	private boolean VERBOSE=false;
	private int programIndex;

	public SimpleShaderProgram(String vertexR,String fragmentR,boolean verbose)
	{
		this.VERBOSE=verbose;
		
		final String FRAGMENT = readFile(fragmentR);
		final String VERTEX = readFile(vertexR);
		
		int vShader = createShader(GL_VERTEX_SHADER,VERTEX);
		int fShader = createShader(GL_FRAGMENT_SHADER,FRAGMENT);
		
		this.programIndex = glCreateProgram();
		glAttachShader(this.programIndex , vShader);
		glAttachShader(this.programIndex , fShader);
		
		glLinkProgram(this.programIndex );
		
		glDetachShader(this.programIndex , vShader);
		glDetachShader(this.programIndex , fShader);
		glDeleteShader(vShader);
		glDeleteShader(fShader);
		
		if(VERBOSE){
			String status =  glGetProgramInfoLog(this.programIndex , GL_LINK_STATUS); //Verbose
			System.out.println(status);
		}
	}
	public SimpleShaderProgram(String vertexR,String fragmentR)
	{
		this(vertexR,fragmentR,false);
	}
	private int createShader(int type,String cont)
	{
		int shader=glCreateShader(type);
		glShaderSource(shader, cont);
		glCompileShader(shader);
		
		if(VERBOSE){
			String status = glGetShaderInfoLog(shader, 1000);
			System.out.println(status);
		}
		
		return shader;
	}
	public abstract void setupAttributes();
	public void enable()
	{
		glUseProgram(this.programIndex);
	}
	public void disable()
	{
		glUseProgram(0);
	}
	public abstract int getSize();
	public int getID(){ return this.programIndex;}
	protected abstract void dispose();

	public final void fullClean()
	{
		this.disable();
		glDeleteProgram(this.programIndex);
		this.dispose();
	}
	
	
	
	/**
	 * Loads all text of the file with path <path> into a String, and returns it
	 */
	public static String readFile(String path)
	{
			InputStream is=SimpleShaderProgram.class.getResourceAsStream(path);
			java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
	}
}
