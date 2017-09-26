package material;

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import core.ProgramParams;
import shaders.DiffuseShader;
import shaders.ShaderLibrary;
import shaders.SimpleShaderProgram;
import util.OBJContents;

/**
 * Diffuse material with customizable RGB colors
 */
public class DiffuseMaterial extends Material
{
	private static final ShaderLibrary.ShaderType associatedShader=ShaderLibrary.ShaderType.DIFFUSE;
	
	private Vector3f albedo;
	public DiffuseMaterial(Vector3f albedo)
	{
		this.albedo=albedo;
	}
	
	@Override
	protected ShaderLibrary.ShaderType getShaderType() {
		return associatedShader;
	}

	@Override
	public void setupShader(ProgramParams params) {
		int colorUniform=GL20.glGetUniformLocation(this.getAssociatedShader().getID(), "colorUniform");
		GL20.glUniform3f(colorUniform, albedo.x, albedo.y, albedo.z);
		
		/*int lightUniform=GL20.glGetUniformLocation(this.getAssociatedShader().getID(), "lightPosUniform");
		GL20.glUniform3f(lightUniform, params.lightPos.x, params.lightPos.y, params.lightPos.z);*/
	}

	@Override
	public float[] getVertexData(OBJContents objContents) {
		int vertsize=6;
		float[] data=new float[vertsize*objContents.getTris().size()*3];
		Vector3f aux1=new Vector3f();
		Vector3f aux2=new Vector3f();

		for(int i=0;i<objContents.getTris().size();i++)
		{
			//Align triangles normals with the screen (flipping the points in order for the normal to be recognized correcly by openGL)
			Vector3f.sub(objContents.getVertex(objContents.getTris().get(i).v[1]), objContents.getVertex(objContents.getTris().get(i).v[0]), aux1);
			Vector3f.sub(objContents.getVertex(objContents.getTris().get(i).v[2]), objContents.getVertex(objContents.getTris().get(i).v[0]), aux2);
			Vector3f.cross(aux1, aux2, aux1);
			boolean swap=Vector3f.dot(aux1, objContents.getNormal(objContents.getTris().get(i).n[0]))<0;
			for(int t=0;t<3;t++)
			{
				int insertt=swap?2-t:t;
				vectorToBuffer(objContents.getVertex(objContents.getTris().get(i).v[t]),data,(i*3 + insertt)*vertsize);
				vectorToBuffer(objContents.getNormal(objContents.getTris().get(i).n[t]),data,(i*3 + insertt)*vertsize + 3);
			}
		}
		
		return data;
	}
	
	
}
