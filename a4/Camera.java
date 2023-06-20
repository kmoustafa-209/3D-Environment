package a4;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.Math;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;
import org.joml.*;

	

public class Camera
{
	private float cameraX;
	private float cameraY;
	private float cameraZ;
	private float uValue;
	private float vValue;
	private float nValue;

	public Camera()
	{	
		initCamera();
	}	
	
	private void initCamera()
	{

		cameraX = 0.0f; cameraY = 3.0f; cameraZ = 20.0f; 
		uValue = 0.0f; vValue = 0.0f; nValue = 0.0f;

	}
		
	public float getCameraX()
	{
		return cameraX;
	}
	
	public float getCameraY()
	{
		return cameraY;
	}
	
	public float getCameraZ()
	{
		return cameraZ;
	}
	
	public void setCameraX(float value)
	{
		cameraX = value;
	}
	public void setCameraY(float value)
	{
		cameraY = value;
	}

	public void setCameraZ(float value)
	{
		cameraZ = value;
	}
	
	public float getUValue()
	{
		return uValue;
	}
	
	public float getVValue()
	{
		return vValue;
	}
	
	public float getNValue()
	{
		return nValue;
	}
	
	public void setUValue(float value)
	{
		uValue = value;
	}
	
	public void setVValue(float value)
	{
		vValue = value;
	}
	
	public void setNValue(float value)
	{
		nValue = value;
	}
	
}
