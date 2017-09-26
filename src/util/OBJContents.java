package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.lwjgl.util.vector.Vector3f;

public class OBJContents 
{
	private List<Vector3f> normals;
	private List<Vector3f> vertexes;
	
	private List<OBJTriangle> tris;
	
	public OBJContents()
	{
		normals=new ArrayList<Vector3f>();
		vertexes=new ArrayList<Vector3f>();
		tris=new ArrayList<OBJTriangle>();
	}
	
	public List<Vector3f> getNormals()
	{
		return normals;
	}
	public Vector3f getNormal(int loc)
	{
		return normals.get(loc);
	}
	public List<Vector3f> getVertexes()
	{
		return vertexes;
	}
	public Vector3f getVertex(int loc)
	{
		return vertexes.get(loc);
	}
	public List<OBJTriangle> getTris()
	{
		return tris;
	}
	
	public static OBJContents loadObjFile(String objpath) throws FileNotFoundException
	{
		OBJContents contents=new OBJContents();
		
		Scanner s=new Scanner(new File(objpath));
		while(s.hasNextLine())
		{
			String line=s.nextLine();
			String[] lc=line.split(" ");

			switch(lc[0])
			{
			case "v":
				contents.vertexes.add(new Vector3f(Float.parseFloat(lc[1]),Float.parseFloat(lc[2]),Float.parseFloat(lc[3])));
				break;
			case "vn":
				contents.normals.add(new Vector3f(Float.parseFloat(lc[1]),Float.parseFloat(lc[2]),Float.parseFloat(lc[3])));
				break;
			case "f":
				//|TODO support non-specified normals
				OBJTriangle tri=new OBJTriangle();
				for(int i=0;i<3;i++){
					String[] fc=lc[i+1].split("/");
					tri.v[i]=Integer.parseInt(fc[0])-1;
					tri.n[i]=Integer.parseInt(fc[2])-1;
				}
				contents.tris.add(tri);
				break;
			}
		}
		s.close();
		
		return contents;
	}
	
}
