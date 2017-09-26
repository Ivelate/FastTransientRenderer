package geometry;

import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import core.ProgramParams;
import material.Material;
import util.Camera;
import util.OBJContents;

/**
 * Loads geometry into the scene and renders it
 */
public class SceneManager 
{
	public enum ShadingType{DIFFUSE}; //Only diffuse shading supported per now
	
	private ProgramParams params;
	private List<ShaderGeometry> sceneElements=new ArrayList<ShaderGeometry>();
	
	public SceneManager(ProgramParams params)
	{
		this.params=params;
	}
	
	public void loadIntoGPU()
	{
		for(ShaderGeometry sg:sceneElements) sg.loadIntoGPU();
	}
	
	public void draw(Camera cam)
	{
		for(ShaderGeometry sg:sceneElements) sg.draw(cam,params);
	}
	
	
	public void loadNewGeometry(Material m,String route) throws FileNotFoundException
	{
		float[] vertexData=m.getVertexData(OBJContents.loadObjFile(route));
		//for(int i=0;i<vertexData.length;i++) System.out.println(vertexData[i]);
		sceneElements.add(new ShaderGeometry(m,vertexData));
	}
}
