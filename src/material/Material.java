package material;

import org.lwjgl.util.vector.Vector3f;

import core.ProgramParams;
import shaders.ShaderLibrary;
import shaders.SimpleShaderProgram;
import util.OBJContents;

/**
 * Material abstract class. Per now only diffuse materials are supported
 */
public abstract class Material 
{
	public static final Vector3f WHITE_ALBEDO=new Vector3f(0.8f,0.8f,0.8f);
	public static final Vector3f RED_ALBEDO=new Vector3f(0.8f,0.2f,0.2f);
	public static final Vector3f GREEN_ALBEDO=new Vector3f(0.2f,0.8f,0.2f);
	public static final Vector3f BLUE_ALBEDO=new Vector3f(0.2f,0.2f,0.8f);
	
	public SimpleShaderProgram getAssociatedShader() {
		return ShaderLibrary.getShader(getShaderType());
	}
	protected abstract ShaderLibrary.ShaderType getShaderType();
	public abstract void setupShader(ProgramParams params);
	
	public abstract float[] getVertexData(OBJContents objContents);
	
	public static void vectorToBuffer(Vector3f vec,float[] buff,int offset){
		buff[offset]=vec.x;
		buff[offset+1]=vec.y;
		buff[offset+2]=vec.z;
	}
}
