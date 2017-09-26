package shaders;

/**
 * Stores all shaders for different materials. For now, only diffuse shaders are supported
 */
public class ShaderLibrary 
{
	public enum ShaderType{
		DIFFUSE;
		public static int length(){return 1;}
	}
	
	private static SimpleShaderProgram[] shaders;
	public static boolean inited=false;
	
	public static void init()
	{
		inited=true;
		shaders=new SimpleShaderProgram[ShaderType.length()];
		
		shaders[ShaderType.DIFFUSE.ordinal()]=new DiffuseShader();
	}
	public static SimpleShaderProgram getShader(ShaderType type)
	{
		if(!inited) return null;
		
		return shaders[type.ordinal()];
	}
}
