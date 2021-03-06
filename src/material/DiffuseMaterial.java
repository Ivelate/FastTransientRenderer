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

	/*public float[] getVertexData(float[] onlyTris)
	{
		float[] data=new float[onlyTris.length*2];
		
		Vector3f aux1=new Vector3f();
		Vector3f aux2=new Vector3f();
		Vector3f aux3=new Vector3f();
				
		for(int i=0;i<onlyTris.length;i+=9)
		{
			aux1.x=onlyTris[i];aux1.y=onlyTris[i+1];aux1.z=onlyTris[i+2];
			aux2.x=onlyTris[i+3];aux2.y=onlyTris[i+4];aux2.z=onlyTris[i+5];
			
			Vector3f.sub(aux2, aux1, aux3);
			aux2.x=onlyTris[i+6];aux2.y=onlyTris[i+7];aux2.z=onlyTris[i+8];
			Vector3f.sub(aux2, aux1, aux2);
			
			Vector3f.cross(aux3, aux2, aux1);
			aux1.normalise();
			
			for(int j=0;j<3;j++) {
				data[i*2 + j*6]=onlyTris[i + j*3]; data[i*2 + j*6 + 1]=onlyTris[i + j*3 + 1]; data[i*2 + j*6 + 2]=onlyTris[i + j*3 + 2];
				data[i*2 + j*6 + 3]=aux1.x; data[i*2 + j*6 + 4]=aux1.y; data[i*2 + j*6 + 5]=aux1.z;
			}
		}
		
		return data;
	}*/
	
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
