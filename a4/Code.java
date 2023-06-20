package a4;

import java.nio.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.Math;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;
import org.joml.*;

public class Code extends JFrame implements GLEventListener, KeyListener, MouseWheelListener, MouseMotionListener
{	
	// Canvas and Rendering Programs
	private GLCanvas myCanvas;
	private int renderingProgram1, renderingProgram2;
	private int cubeRenderingProgram;
	private int axesProgram;
	private int renderingProgramCubeMap;
	private int floorRenderingProgram;
	private int torusGeomProgram, noiseCatProgram;

	// VAO and VBO
	private int vao[] = new int[1];
	private int vbo[] = new int[27];
	
	// Toggles 
	private boolean toggle = true;
	private boolean lightToggle = true;
	
	// Location values
	private float catOneLocX, catOneLocY, catOneLocZ;
	private float catTwoLocX, catTwoLocY, catTwoLocZ;
	private float floorLocX, floorLocY, floorLocZ;
	private float campfireX, campfireY, campfireZ;
	private float torusX, torusY, torusZ;
	private float noiseX, noiseY, noiseZ;
	private float cafeX, cafeY, cafeZ;
	private float lightX, lightY, lightZ;
	private float uValue, vValue, nValue;
	
	// Camera and Model Handling
	private int catOneVertices, catTwoVertices, campfireVertices, buildingVertices, cafeVertices, floorVertices;
	private int catOneTexture, catTwoTexture, cubeTexture, floorTexture, skyboxTexture, campfireTexture, buildingTexture, cafeTexture;
	private int normalMapTexture, heightMapTexture;
	private ImportedModel catOne, catTwo;
	private ImportedModel campfire;
	private ImportedModel building, cafe;
	private ImportedModel floor;
	private Torus myTorus = new Torus(0.5f, 0.3f, 36);
	private int numTorusVertices, numTorusIndices;
	private Camera camera;
	
	//Elapsed Time for Movement 
	private double tf;
	private double startTime;
	private double elapsedTime;

	// shadow stuff
	private int scSizeX, scSizeY;
	private int sLoc;
	private int [] shadowTex = new int[1];
	private int [] shadowBuffer = new int[1];
	private Matrix4f lightVmat = new Matrix4f();
	private Matrix4f lightPmat = new Matrix4f();
	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();
	private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
	private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

	// VR stuff
	private float IOD = 0.01f;  // tunable interocular distance � we arrived at 0.01 for this scene by trial-and-error
	private float near = 0.01f;
	private float far = 100.0f;

	// 3D texture stuff
	private int noiseTexture;
	private int noiseWidth = 256;
	private int noiseHeight= 256;
	private int noiseDepth = 256;
	private double[][][] noise = new double[noiseWidth][noiseHeight][noiseDepth];
	private java.util.Random random = new java.util.Random();
	
	// Variables needed for Vertex and Fragment Shader
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f();
	private Matrix4f vMat = new Matrix4f();
	private Matrix4f mMat = new Matrix4f();
	private Matrix4f invTrMat = new Matrix4f();
	private Matrix4fStack modelStack = new Matrix4fStack(4);
	private int mLoc, pLoc, vLoc, nLoc, pAxesLoc;
	private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
	private int alphaLoc, flipLoc;
	private Vector3f initialLightLoc = new Vector3f();
	private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];

	// Variables for Lighting ADS
	float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
	float[] lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
	float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

	// Base Material ADS and Shi
	float[] matAmb;
	float[] matDif;
	float[] matSpe;
	float matShi;

	//Bronze material for the Orange Cat (from Utils.java)
	float[] bronzAmb = Utils.bronzeAmbient();
	float[] bronzDif = Utils.bronzeDiffuse();
	float[] bronzSpe = Utils.bronzeSpecular();
	float bronzShi = Utils.bronzeShininess();
	
	//Copper material for the Plane http://www.barradeau.com/nicoptere/dump/materials.html
	float[] coppAmb = {0.19125f, 0.0735f, 0.0225f, 1.0f};
	float[] coppDif = {0.7038f, 0.27048f, 0.0828f, 1.0f};
	float[] coppSpe = {0.256777f, 0.137622f, 0.086014f, 1.0f};
	float coppShi = 12.8f;

	// Ruby material for the campfire http://www.barradeau.com/nicoptere/dump/materials.html
	float[] rubyAmb = {0.1745f, 0.01175f, 0.01175f, 0.55f};
	float[] rubyDif = {0.61424f, 0.04136f, 0.04136f, 0.55f};
	float[] rubySpe = {0.726811f, 0.626959f, 0.626059f, 0.55f};
	float rubyShi = 76.8f;

	// Black Plastic material for the Black cat http://www.barradeau.com/nicoptere/dump/materials.html
	float[] blackAmb = {0.0f, 0.0f, 0.0f, 1.0f};
	float[] blackDif = {0.01f, 0.01f, 0.01f, 1.0f};
	float[] blackSpe = {0.5f, 0.5f, 0.5f, 1.0f};
	float blackShi = 32.0f;

	// Material ADS for the Buildings and Cafe 
	float[] noAmb = {0.2f, 0.2f, 0.2f, 1.0f};
	float[] noDif = {0.8f, 0.8f, 0.8f, 1.0f};
	float[] noSpe = {0.0f, 0.0f, 0.0f, 1.0f};
	float noShi = 10.0f;

	// Aspect Ratio
	private float aspect;

	public Code()
	{
		setTitle("Lab 4 Assignment");
		setSize(1000, 1000);
		myCanvas = new GLCanvas();
		myCanvas.setFocusable(false);
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		addKeyListener(this); 
		myCanvas.addMouseMotionListener(this);
		myCanvas.addMouseWheelListener(this); 
		
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glColorMask(true, true, true, true);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
     	// Elapsed Time for Movement
		elapsedTime = System.currentTimeMillis() - startTime;
		tf = elapsedTime/1000.0;
				
		gl.glColorMask(true, false, false, false);
		scene(-10.0f);
				
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glColorMask(false, true, true, false);
		scene(10.0f);
		
	}

	public void scene(float leftRight)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		computePerspectiveMatrix(leftRight);

		// Camera Handling
		vMat.identity().setTranslation(-(camera.getCameraX() + leftRight * IOD/2.0f ),-camera.getCameraY(),-camera.getCameraZ());
		vMat.rotateLocalX(camera.getUValue());
		vMat.rotateLocalY(camera.getVValue());
		vMat.rotateLocalZ(camera.getNValue());   

		// Skybox Rendering Program
		gl.glUseProgram(renderingProgramCubeMap);

		vLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "v_matrix");
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		pLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "p_matrix");
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
				
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	    
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_POLYGON_OFFSET_FILL);	//  for reducing
		gl.glPolygonOffset(3.0f, 5.0f);		//  shadow artifacts    
		
		passOne(leftRight);

		gl.glDisable(GL_POLYGON_OFFSET_FILL); // artifact reduction, continued

    	gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    	gl.glActiveTexture(GL_TEXTURE1);
    	gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);

    	gl.glDrawBuffer(GL_FRONT);

		passTwo(leftRight);
	
		if(toggle)
        	createAxes(); 	
	}
		
	private void computePerspectiveMatrix(float leftRight)
	{	float top = (float)Math.tan(1.0472f / 2.0f) * (float)near;
		float bottom = -top;
		float frustumshift = (IOD / 2.0f) * near / far;
		float left = -aspect * top - frustumshift * leftRight;
		float right = aspect * top - frustumshift * leftRight;
		pMat.setFrustum(left, right, bottom, top, near, far);
		lightPmat.setFrustum(left, right, bottom, top, near, far);
	}

	public void init(GLAutoDrawable drawable)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();

		startTime = System.currentTimeMillis();

		renderingProgram1 = Utils.createShaderProgram("a4/shaders/vert1Shader.glsl", "a4/shaders/frag1Shader.glsl");
		renderingProgram2 = Utils.createShaderProgram("a4/shaders/vert2Shader.glsl", "a4/shaders/frag2Shader.glsl");
        axesProgram = Utils.createShaderProgram("a4/shaders/axesVertShader.glsl", "a4/shaders/axesFragShader.glsl");
		renderingProgramCubeMap = Utils.createShaderProgram("a4/shaders/vertCShader.glsl", "a4/shaders/fragCShader.glsl");
		cubeRenderingProgram = Utils.createShaderProgram("a4/shaders/vertCubeShader.glsl", "a4/shaders/fragCubeShader.glsl");
		floorRenderingProgram = Utils.createShaderProgram("a4/shaders/vertFloorShader.glsl", "a4/shaders/fragFloorShader.glsl");
		torusGeomProgram = Utils.createShaderProgram("a4/shaders/vertTorusShader.glsl", "a4/shaders/geomShader.glsl", "a4/shaders/fragTorusShader.glsl");
		noiseCatProgram = Utils.createShaderProgram("a4/shaders/vertNoiseShader.glsl", "a4/shaders/fragNoiseShader.glsl");

		catOne = new ImportedModel("obj/cat1.obj");
		catTwo = new ImportedModel("obj/cat2.obj");
		campfire = new ImportedModel("obj/campfire.obj");
		building = new ImportedModel("obj/building3.obj");
		cafe = new ImportedModel("obj/shopcafe.obj");
		floor = new ImportedModel("obj/floor.obj");

		camera = new Camera();
		
		setupVertices();
        setupShadowBuffers();

		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);
		
		catOneLocX = 0.0f;	catOneLocY = 0.3f; catOneLocZ = 0.0f;
		catTwoLocX = -2.0f; catTwoLocY = -0.1f; catTwoLocZ = 0.0f;
		floorLocX = 0.0f; floorLocY = - 0.1f; floorLocZ = 0.0f;
		lightX = 1.0f; lightY = 3.0f; lightZ = 0.0f;
		campfireX = 0.0f; campfireY = 0.0f; campfireZ = 0.0f;
		cafeX = 0.0f; cafeY = 0.0f; cafeZ = -12.0f;
		torusX = 0.0f; torusY = 2.0f; torusZ = 0.0f;
		noiseX = 0.0f; noiseY = 3.0f; noiseZ = 0.0f;


		catOneTexture = Utils.loadTexture("textures/cat1.jpg");
		catTwoTexture = Utils.loadTexture("textures/cat2.jpg");
		cubeTexture = Utils.loadTexture("textures/cubetex.jpg");
		floorTexture = Utils.loadTexture("textures/plane.png");
		campfireTexture = Utils.loadTexture("textures/campfire.jpg");
		buildingTexture = Utils.loadTexture("textures/9stbuildingtex.png");
		cafeTexture = Utils.loadTexture("textures/roadsideshop.png");
		normalMapTexture = Utils.loadTexture("textures/castleroofNORMAL.jpg");
		heightMapTexture = Utils.loadTexture("textures/height.jpg");

		generateNoise();
		noiseTexture = buildNoiseTexture();

		skyboxTexture = Utils.loadCubeMap("cubeMap");
		
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
	}
   	public void passOne(float leftRight)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(renderingProgram1);
		
		initialLightLoc = new Vector3f(lightX + leftRight * IOD/2.0f, lightY, lightZ);

		if(!lightToggle || (lightX == 0 && lightZ == 0))
		{
			initialLightLoc = new Vector3f(0, 0, 0);
		}


		currentLightPos.set(initialLightLoc);
		
        lightVmat.identity().setLookAt(currentLightPos, origin, up);	// vector from light to origin
		
        // Draw Matrix Stack: Cat One
		modelStack.pushMatrix();

		modelStack.translate(catOneLocX + (5 * (float)(Math.sin(tf))),catOneLocY, catOneLocZ  + (4 * (float)(Math.cos(tf))));
		modelStack.rotateY((float)tf);
		modelStack.rotateX((float)Math.sin(tf  * 10)/4);
		modelStack.scale(1.5f, 1.5f, 1.5f);

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(modelStack);

		modelStack.pushMatrix();
	    sLoc = gl.glGetUniformLocation(renderingProgram1, "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, catOne.getNumVertices());
		modelStack.popMatrix();

		//Draw Matrix Stack: Cat Two  
		modelStack.pushMatrix();

		modelStack.translate(catTwoLocX, catTwoLocY + ((float)Math.sin(tf*10)/5), catTwoLocZ);
		modelStack.rotateX(-(float)Math.sin(tf  * 10)/4);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(modelStack);

		modelStack.pushMatrix();

		sLoc = gl.glGetUniformLocation(renderingProgram1, "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);


		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, catTwo.getNumVertices());
		modelStack.popMatrix(); modelStack.popMatrix(); modelStack.popMatrix();

		// Creates Campfire in Pass One
       	mMat.identity();
		mMat.translate(campfireX, campfireY, campfireZ);
		mMat.scale(2.0f, 2.0f, 2.0f);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);

		sLoc = gl.glGetUniformLocation(renderingProgram1, "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, campfireVertices); 

		// Creates Cafe in Pass One 
		mMat.identity();

		mMat.scale(1.5f, 1.5f, 1.5f);
		mMat.translateLocal(cafeX, cafeY, cafeZ);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);

		sLoc = gl.glGetUniformLocation(renderingProgram1, "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, cafeVertices);

		// Creates Buildings in Pass One

		createBuildingPassOne(20.0f, 0.0f, -15.0f, -30.0f, 5.0f);
		createBuildingPassOne(-4.0f, 0.0f, -25.0f, 15.0f, 0.0f);
		createBuildingPassOne(-15.0f, 0.0f, 4.0f, 0.0f, -5.0f);
		createBuildingPassOne(-13.0f, 0.0f, -8.0f, 0.0f, 5.0f);
		createBuildingPassOne(11.0f, 0.0f, 12.0f, 0.0f, -5.0f);
		createBuildingPassOne(6.0f, 0.0f, -23.0f, -36.0f, 6.0f);
	}
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo(float leftRight)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(renderingProgram2);

		initialLightLoc = new Vector3f(lightX + leftRight * IOD/2.0f, lightY, lightZ);

		if(!lightToggle || (lightX == 0 && lightZ == 0))
		{
			initialLightLoc = new Vector3f(0, 0, 0);
		}


		mLoc = gl.glGetUniformLocation(renderingProgram2, "m_matrix");
		vLoc = gl.glGetUniformLocation(renderingProgram2, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram2, "p_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram2, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgram2, "shadowMVP");

		
		// Matrix Stack: Cat One
		modelStack.pushMatrix();

		modelStack.translate(catOneLocX + (5 * (float)(Math.sin(tf))),catOneLocY, catOneLocZ  + (4 * (float)(Math.cos(tf))));
		modelStack.rotateY((float)tf);
		modelStack.rotateX((float)Math.sin(tf  * 10)/4);
		modelStack.scale(1.5f, 1.5f, 1.5f);
		

		matAmb = bronzAmb;
		matDif = bronzDif;
		matSpe = bronzSpe;
		matShi = bronzShi;
				
		currentLightPos.set(initialLightLoc);
		installLights(renderingProgram2);
		

		modelStack.invert(invTrMat);	
		invTrMat.transpose(invTrMat);

    	shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(modelStack);    

		modelStack.pushMatrix();
		gl.glUniformMatrix4fv(mLoc, 1, false, modelStack.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));	
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, catOneTexture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, catOne.getNumVertices());
		modelStack.popMatrix();

		// Matrix Stack: Cat Two  
		modelStack.pushMatrix();

		modelStack.translate(catTwoLocX, catTwoLocY + ((float)Math.sin(tf*10)/5), catTwoLocZ);
		modelStack.rotateX(-(float)Math.sin(tf  * 10)/4);
		
		matAmb = blackAmb;
		matDif = blackDif;
		matSpe = blackSpe;
		matShi = blackShi;

		currentLightPos.set(initialLightLoc);
		installLights(renderingProgram2);

		modelStack.invert(invTrMat);	
		invTrMat.transpose(invTrMat);

    	shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(modelStack);    

		modelStack.pushMatrix();
		gl.glUniformMatrix4fv(mLoc, 1, false, modelStack.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, catTwoTexture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, catTwo.getNumVertices());
		modelStack.popMatrix(); modelStack.popMatrix(); modelStack.popMatrix();	

	    // Creates Campfire in Pass Two
       	mMat.identity();
		mMat.translate(campfireX, campfireY, campfireZ);
		mMat.scale(2.0f, 2.0f, 2.0f);
		
		currentLightPos.set(initialLightLoc);
		installLights(renderingProgram2);

		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, campfireTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, campfireVertices); 

		spawnEnvironment();	

		createTorusGeom();

		createNoiseCat();

		createYellowCube();

		createFloor();

	}

	public static void main(String[] args){	new Code();	}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) 
    {
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.setPerspective((float) Math.toRadians(50.0f), aspect, 0.1f, 1000.0f);
    	
		setupShadowBuffers();
	}
	public void dispose(GLAutoDrawable drawable) {}
	
	private void setupVertices()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();

		catOneVertices = catOne.getNumVertices();
		Vector3f[] vertices = catOne.getVertices();
		Vector2f[] texCoords = catOne.getTexCoords();
		Vector3f[] normals = catOne.getNormals();		
		
		float[] pvalues = new float[catOneVertices*3];
		float[] tvalues = new float[catOneVertices*2];
		float[] nvalues = new float[catOneVertices*3];

		catTwoVertices = catTwo.getNumVertices();
		Vector3f[] verticesTwo = catTwo.getVertices();
		Vector2f[] texCoordsTwo = catTwo.getTexCoords();
		Vector3f[] normalsTwo = catTwo.getNormals();		
		
		float[] pTwovalues = new float[catTwoVertices*3];
		float[] tTwovalues = new float[catTwoVertices*2];
		float[] nTwovalues = new float[catTwoVertices*3];

		campfireVertices = campfire.getNumVertices();
		Vector3f[] tVertices = campfire.getVertices();
		Vector2f[] tTexCoords = campfire.getTexCoords();
		Vector3f[] tNormals = campfire.getNormals();
		
		float[] campfirePvalues = new float[campfireVertices*3];
		float[] campfireTvalues = new float[campfireVertices*2];
		float[] campfireNvalues = new float[campfireVertices*3];
		
		buildingVertices = building.getNumVertices();
		Vector3f[] buildVertices = building.getVertices();
		Vector2f[] buildingTexCoords = building.getTexCoords();
		Vector3f[] buildNormals = building.getNormals();

		float[] buildingPvalues = new float[buildingVertices*3];
		float[] buildingTvalues = new float[buildingVertices*2];
		float[] buildingNvalues = new float[buildingVertices*3];

		cafeVertices = cafe.getNumVertices();
		Vector3f[] cVertices = cafe.getVertices();
		Vector2f[] cTexCoords = cafe.getTexCoords();
		Vector3f[] cNormals = cafe.getNormals();

		float[] cafePvalues = new float[cafeVertices*3];
		float[] cafeTvalues = new float[cafeVertices*2];
		float[] cafeNvalues = new float[cafeVertices*3];

		floorVertices = floor.getNumVertices();
		Vector3f[] fVertices = floor.getVertices();
		Vector2f[] fTexCoords = floor.getTexCoords();
		Vector3f[] fNormals = floor.getNormals();
	
		float[] floorPvalues = new float[floorVertices*3];
		float[] floorTvalues = new float[floorVertices*2];
		float[] floorNvalues = new float[floorVertices*3];

		numTorusVertices = myTorus.getNumVertices();
		numTorusIndices = myTorus.getNumIndices();
	
		Vector3f[] torVertices = myTorus.getVertices();
		Vector2f[] torTexCoords = myTorus.getTexCoords();
		Vector3f[] torNormals = myTorus.getNormals();
		int[] torIndices = myTorus.getIndices();
		
		float[] torPvalues = new float[torVertices.length*3];
		float[] torTvalues = new float[torTexCoords.length*2];
		float[] torNvalues = new float[torNormals.length*3];

		float[] cubeVertexPositions =
		{	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};

		float[] cubePositions =
		{
			-0.25f,  0.25f, -0.25f, -0.25f, -0.25f, -0.25f, 0.25f, -0.25f, -0.25f,
			0.25f, -0.25f, -0.25f, 0.25f,  0.25f, -0.25f, -0.25f,  0.25f, -0.25f,
			0.25f, -0.25f, -0.25f, 0.25f, -0.25f,  0.25f, 0.25f,  0.25f, -0.25f,
			0.25f, -0.25f,  0.25f, 0.25f,  0.25f,  0.25f, 0.25f,  0.25f, -0.25f,
			0.25f, -0.25f,  0.25f, -0.25f, -0.25f,  0.25f, 0.25f,  0.25f,  0.25f,
			-0.25f, -0.25f,  0.25f, -0.25f,  0.25f,  0.25f, 0.25f,  0.25f,  0.25f,
			-0.25f, -0.25f,  0.25f, -0.25f, -0.25f, -0.25f, -0.25f,  0.25f,  0.25f,
			-0.25f, -0.25f, -0.25f, -0.25f,  0.25f, -0.25f, -0.25f,  0.25f,  0.25f,
			-0.25f, -0.25f,  0.25f,  0.25f, -0.25f,  0.25f,  0.25f, -0.25f, -0.25f,
			0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f,  0.25f,
			-0.25f,  0.25f, -0.25f, 0.25f,  0.25f, -0.25f, 0.25f,  0.25f,  0.25f,
			0.25f,  0.25f,  0.25f, -0.25f,  0.25f,  0.25f, -0.25f,  0.25f, -0.25f
		};

		float[] cubeTextureCoordinates =
		{
			0.0f, 0.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f

		};

		for (int i=0; i < catOneVertices; i++)
		{	
			pvalues[i*3]   = (float) (vertices[i]).x();
			pvalues[i*3+1] = (float) (vertices[i]).y();
			pvalues[i*3+2] = (float) (vertices[i]).z();
			tvalues[i*2]   = (float) (texCoords[i]).x();
			tvalues[i*2+1] = (float) (texCoords[i]).y();
			nvalues[i*3]   = (float) (normals[i]).x();
			nvalues[i*3+1] = (float) (normals[i]).y();
			nvalues[i*3+2] = (float) (normals[i]).z();
		}

		for (int i=0; i < catTwoVertices; i++)
		{	
			pTwovalues[i*3]   = (float) (verticesTwo[i]).x();
			pTwovalues[i*3+1] = (float) (verticesTwo[i]).y();
			pTwovalues[i*3+2] = (float) (verticesTwo[i]).z();
			tTwovalues[i*2]   = (float) (texCoordsTwo[i]).x();
			tTwovalues[i*2+1] = (float) (texCoordsTwo[i]).y();
			nTwovalues[i*3]   = (float) (normalsTwo[i]).x();
			nTwovalues[i*3+1] = (float) (normalsTwo[i]).y();
			nTwovalues[i*3+2] = (float) (normalsTwo[i]).z();
		}

		for (int i=0; i < campfireVertices; i++)
		{	
			campfirePvalues[i*3]   = (float) (tVertices[i]).x();
			campfirePvalues[i*3+1] = (float) (tVertices[i]).y();
			campfirePvalues[i*3+2] = (float) (tVertices[i]).z();
			campfireTvalues[i*2]   = (float) (tTexCoords[i]).x();
			campfireTvalues[i*2+1] = (float) (tTexCoords[i]).y();
			campfireNvalues[i*3]   = (float) (tNormals[i]).x();
			campfireNvalues[i*3+1] = (float) (tNormals[i]).y();
			campfireNvalues[i*3+2] = (float) (tNormals[i]).z();
		}

		for (int i=0; i < buildingVertices; i++)
		{	
			buildingPvalues[i*3]   = (float) (buildVertices[i]).x();
			buildingPvalues[i*3+1] = (float) (buildVertices[i]).y();
			buildingPvalues[i*3+2] = (float) (buildVertices[i]).z();
			buildingTvalues[i*2]   = (float) (buildingTexCoords[i]).x();
			buildingTvalues[i*2+1] = (float) (buildingTexCoords[i]).y();
			buildingNvalues[i*3]   = (float) (buildNormals[i]).x();
			buildingNvalues[i*3+1] = (float) (buildNormals[i]).y();
			buildingNvalues[i*3+2] = (float) (buildNormals[i]).z();
		}

		for (int i=0; i < cafeVertices; i++)
		{	
			cafePvalues[i*3]   = (float) (cVertices[i]).x();
			cafePvalues[i*3+1] = (float) (cVertices[i]).y();
			cafePvalues[i*3+2] = (float) (cVertices[i]).z();
			cafeTvalues[i*2]   = (float) (cTexCoords[i]).x();
			cafeTvalues[i*2+1] = (float) (cTexCoords[i]).y();
			cafeNvalues[i*3]   = (float) (cNormals[i]).x();
			cafeNvalues[i*3+1] = (float) (cNormals[i]).y();
			cafeNvalues[i*3+2] = (float) (cNormals[i]).z();
		}

		for (int i=0; i < floorVertices; i++)
		{	
			floorPvalues[i*3]   = (float) (fVertices[i]).x();
			floorPvalues[i*3+1] = (float) (fVertices[i]).y();
			floorPvalues[i*3+2] = (float) (fVertices[i]).z();
			floorTvalues[i*2]   = (float) (fTexCoords[i]).x();
			floorTvalues[i*2+1] = (float) (fTexCoords[i]).y();
			floorNvalues[i*3]   = (float) (fNormals[i]).x();
			floorNvalues[i*3+1] = (float) (fNormals[i]).y();
			floorNvalues[i*3+2] = (float) (fNormals[i]).z();
			
		}

		for (int i=0; i<numTorusVertices; i++)
		{	torPvalues[i*3]   = (float) torVertices[i].x();
			torPvalues[i*3+1] = (float) torVertices[i].y();
			torPvalues[i*3+2] = (float) torVertices[i].z();
			torTvalues[i*2]   = (float) torTexCoords[i].x();
			torTvalues[i*2+1] = (float) torTexCoords[i].y();
			torNvalues[i*3]   = (float) torNormals[i].x();
			torNvalues[i*3+1] = (float) torNormals[i].y();
			torNvalues[i*3+2] = (float) torNormals[i].z();
		}

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cvertBuf = Buffers.newDirectFloatBuffer(cubeVertexPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cvertBuf.limit()*4, cvertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer floorBuf = Buffers.newDirectFloatBuffer(floorPvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, floorBuf.limit()*4, floorBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer floorTextBuff = Buffers.newDirectFloatBuffer(floorTvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, floorTextBuff.limit()*4, floorTextBuff, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer campfireVertBuf = Buffers.newDirectFloatBuffer(campfirePvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, campfireVertBuf.limit()*4, campfireVertBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer campfireTexBuf = Buffers.newDirectFloatBuffer(campfireTvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, campfireTexBuf.limit()*4, campfireTexBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer campfireNorBuf = Buffers.newDirectFloatBuffer(campfireNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, campfireNorBuf.limit()*4, campfireNorBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer vertTwoBuf = Buffers.newDirectFloatBuffer(pTwovalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertTwoBuf.limit()*4, vertTwoBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer texTwoBuf = Buffers.newDirectFloatBuffer(tTwovalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texTwoBuf.limit()*4, texTwoBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer norTwoBuf = Buffers.newDirectFloatBuffer(nTwovalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norTwoBuf.limit()*4,norTwoBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cubePositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit()*4, cubeBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer cubeTextBuf = Buffers.newDirectFloatBuffer(cubeTextureCoordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeTextBuf.limit()*4, cubeTextBuf, GL_STATIC_DRAW);
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		FloatBuffer buildingVertBuf = Buffers.newDirectFloatBuffer(buildingPvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, buildingVertBuf.limit()*4, buildingVertBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		FloatBuffer buildingTexBuf = Buffers.newDirectFloatBuffer(buildingTvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, buildingTexBuf.limit()*4, buildingTexBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
		FloatBuffer buildingNorBuf = Buffers.newDirectFloatBuffer(buildingNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, buildingNorBuf.limit()*4, buildingNorBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		FloatBuffer cafeVertBuf = Buffers.newDirectFloatBuffer(cafePvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, cafeVertBuf.limit()*4, cafeVertBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
		FloatBuffer cafeTexBuf = Buffers.newDirectFloatBuffer(cafeTvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, cafeTexBuf.limit()*4, cafeTexBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		FloatBuffer cafeNorBuf = Buffers.newDirectFloatBuffer(cafeNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, cafeNorBuf.limit()*4, cafeNorBuf, GL_STATIC_DRAW);				
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		FloatBuffer floorNormBuff = Buffers.newDirectFloatBuffer(floorNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, floorNormBuff.limit()*4, floorNormBuff, GL_STATIC_DRAW);
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
		FloatBuffer torVertBuf = Buffers.newDirectFloatBuffer(torPvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torVertBuf.limit()*4, torVertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
		FloatBuffer torTexBuf = Buffers.newDirectFloatBuffer(torTvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torTexBuf.limit()*4, torTexBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[23]);
		FloatBuffer torNorBuf = Buffers.newDirectFloatBuffer(torNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torNorBuf.limit()*4, torNorBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[24]);
		IntBuffer torIdxBuf = Buffers.newDirectIntBuffer(torIndices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, torIdxBuf.limit()*4, torIdxBuf, GL_STATIC_DRAW);
	}

	// Creates the Floor Object (utilized plane.png I created with FireAlpaca64)
	public void createFloor()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glUseProgram(floorRenderingProgram);
		
		mLoc = gl.glGetUniformLocation(floorRenderingProgram, "m_matrix");
		vLoc = gl.glGetUniformLocation(floorRenderingProgram, "v_matrix");
		pLoc = gl.glGetUniformLocation(floorRenderingProgram, "p_matrix");
		nLoc = gl.glGetUniformLocation(floorRenderingProgram, "norm_matrix");
	
		mMat.identity();
		mMat.translate(floorLocX, floorLocY, floorLocZ);
		mMat.scale(70.0f, 70.0f, 70.0f);
		
		matAmb = coppAmb;
		matDif = coppDif;
		matSpe = coppSpe;
		matShi = coppShi;
		
		currentLightPos.set(initialLightLoc);
		installLights(floorRenderingProgram);

		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
	
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
	
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, floorTexture);

		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, normalMapTexture);

		gl.glActiveTexture(GL_TEXTURE3);
		gl.glBindTexture(GL_TEXTURE_2D, heightMapTexture); 

		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
   		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    	gl.glGenerateMipmap(GL_TEXTURE_2D);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, floorVertices);
	}	

	// Creates a Campfire that the Cats will Prance Around
	public void createCampFire()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		mMat.identity();
		mMat.translate(campfireX, campfireY, campfireZ);
		mMat.scale(2.0f, 2.0f, 2.0f);
		
		matAmb = rubyAmb;
		matDif = rubyDif;
		matSpe = rubySpe;
		matShi = rubyShi;

		currentLightPos.set(initialLightLoc);
		installLights(renderingProgram2);

		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, campfireTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, campfireVertices);
		
	}

	// Creates a Building for Pass One
	public void createBuildingPassOne(float x, float y, float z, float yRotate, float zRotate)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		mMat.identity();
		mMat.scale(0.25f, 0.25f, 0.25f);
		mMat.rotateLocalY(yRotate);
		mMat.rotateLocalZ(zRotate);
		mMat.translateLocal(x, y, z);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);

		sLoc = gl.glGetUniformLocation(renderingProgram1, "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, buildingVertices);
		
	}
	// Creates a Building for Pass Two
	public void createBuildingPassTwo(float x, float y, float z, float yRotate, float zRotate)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		mMat.identity();
		mMat.scale(0.25f, 0.25f, 0.25f);
		mMat.rotateLocalY(yRotate);
		mMat.rotateLocalZ(zRotate);
		mMat.translateLocal(x, y, z);
		
		matAmb = noAmb;
		matDif = noDif;
		matSpe = noSpe;
		matShi = noShi;

		currentLightPos.set(initialLightLoc);
		installLights(renderingProgram2);

		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, buildingTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, buildingVertices);
		
	}

	public void createCafe()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		mMat.identity();

		mMat.scale(1.5f, 1.5f, 1.5f);
		mMat.translateLocal(cafeX, cafeY, cafeZ);

			
		matAmb = noAmb;
		matDif = noDif;
		matSpe = noSpe;
		matShi = noShi;

		currentLightPos.set(initialLightLoc);
		installLights(renderingProgram2);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, cafeTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, cafeVertices);
		
	}
	// Hard-coded function to create and control placements of cafe and buildings
	public void spawnEnvironment()
	{

		createCafe();

		createBuildingPassTwo(20.0f, 0.0f, -15.0f, -30.0f, 5.0f);
		createBuildingPassTwo(-4.0f, 0.0f, -25.0f, 15.0f, 0.0f);
		createBuildingPassTwo(-15.0f, 0.0f, 4.0f, 0.0f, -5.0f);
		createBuildingPassTwo(-13.0f, 0.0f, -8.0f, 0.0f, 5.0f);
		createBuildingPassTwo(11.0f, 0.0f, 12.0f, 0.0f, -5.0f);
		createBuildingPassTwo(6.0f, 0.0f, -23.0f, -36.0f, 6.0f);

	}

	// Object that shows where the positional lighting currently is 
	public void createYellowCube()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glUseProgram(cubeRenderingProgram);
				
		mLoc = gl.glGetUniformLocation(cubeRenderingProgram, "m_matrix");
		vLoc = gl.glGetUniformLocation(cubeRenderingProgram, "v_matrix");
		pLoc = gl.glGetUniformLocation(cubeRenderingProgram, "p_matrix");

		mMat.identity();
		mMat.translate(lightX, lightY, lightZ);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, cubeTexture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
	}

	public void createTorusGeom()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glUseProgram(torusGeomProgram);

		mLoc = gl.glGetUniformLocation(torusGeomProgram, "m_matrix");
		vLoc = gl.glGetUniformLocation(torusGeomProgram, "v_matrix");
		pLoc = gl.glGetUniformLocation(torusGeomProgram, "p_matrix");
		nLoc = gl.glGetUniformLocation(torusGeomProgram, "norm_matrix");
			
		mMat.identity();
		mMat.translate(torusX, torusY, torusZ);
		mMat.rotateY((float)tf);
		mMat.scale(1.5f, 0.75f, 1.5f);

		matAmb = rubyAmb;
		matDif = rubyDif;
		matSpe = rubySpe;
		matShi = rubyShi;
		
		currentLightPos.set(initialLightLoc);
		installLights(torusGeomProgram);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[23]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[24]);
		gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);
	}

	public void createNoiseCat()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glUseProgram(noiseCatProgram);

		mLoc = gl.glGetUniformLocation(noiseCatProgram, "m_matrix");
		vLoc = gl.glGetUniformLocation(noiseCatProgram, "v_matrix");
		pLoc = gl.glGetUniformLocation(noiseCatProgram, "p_matrix");
		nLoc = gl.glGetUniformLocation(noiseCatProgram, "norm_matrix");
		alphaLoc = gl.glGetUniformLocation(noiseCatProgram, "alpha");
		flipLoc = gl.glGetUniformLocation(noiseCatProgram, "flipNormal");
			
		mMat.identity();
		mMat.translate(noiseX, noiseY, noiseZ);
		mMat.rotateY(30.0f);
		mMat.scale(2.0f, 2.0f, 2.0f);

		matAmb = bronzAmb;
		matDif = bronzDif;
		matSpe = bronzSpe;
		matShi = bronzShi;
		
		currentLightPos.set(initialLightLoc);
		installLights(noiseCatProgram);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glProgramUniform1f(noiseCatProgram, alphaLoc, 1.0f);
		gl.glProgramUniform1f(noiseCatProgram, flipLoc, 1.0f);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
	
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_3D, noiseTexture);

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glBlendEquation(GL_FUNC_ADD);

		gl.glEnable(GL_CULL_FACE);

		gl.glCullFace(GL_FRONT);
		gl.glProgramUniform1f(noiseCatProgram, alphaLoc, 0.3f);
		gl.glProgramUniform1f(noiseCatProgram, flipLoc, -1.0f);
		gl.glDrawArrays(GL_TRIANGLES, 0, catOne.getNumVertices());

		gl.glCullFace(GL_BACK);
		gl.glProgramUniform1f(noiseCatProgram, alphaLoc, 0.3f);
		gl.glProgramUniform1f(noiseCatProgram, flipLoc, -1.0f);
		gl.glDrawArrays(GL_TRIANGLES, 0, catOne.getNumVertices());

		gl.glDisable(GL_BLEND);
	}
	
	public void createAxes()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glUseProgram(axesProgram);
		vLoc = gl.glGetUniformLocation(axesProgram, "v_matrix");
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));

		pAxesLoc = gl.glGetUniformLocation(axesProgram, "p_matrix");
		gl.glUniformMatrix4fv(pAxesLoc, 1, false, pMat.get(vals));
		
		gl.glDrawArrays(GL_LINES, 0, 6);
	}
	
  // Installs Lighting 
	private void installLights(int renderingProgram)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		lightPos[0]=currentLightPos.x(); lightPos[1]=currentLightPos.y(); lightPos[2]=currentLightPos.z();
		
		if(lightToggle == true)
		{
			lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
			lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
			lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		}
		else if(lightToggle == false)
		{
			lightAmbient = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
			lightDiffuse = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
			lightSpecular = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
		}

		globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
		mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");
	
		gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
		gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
		gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
		gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
	}

	// Setups Shadow Buffers
    private void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadowBuffer, 0);
	
		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

	}
    // 3D Texture Setup
	private void fillDataArray(byte data[])
	{ double veinFrequency = 2.0;
	  double turbPower = 3.0;
	  double maxZoom =  32.0;
	  for (int i=0; i<noiseWidth; i++)
	  { for (int j=0; j<noiseHeight; j++)
	    { for (int k=0; k<noiseDepth; k++)
	      {	double xyzValue = (float)i/noiseWidth + (float)j/noiseHeight + (float)k/noiseDepth
							+ turbPower * turbulence(i,j,k,maxZoom)/256.0;

		double sineValue = logistic(Math.abs(Math.sin(xyzValue * 3.14159 * veinFrequency)));
		sineValue = Math.max(-1.0, Math.min(sineValue*1.25-0.20, 1.0));

		Color c = new Color((float)sineValue,
				(float)Math.min(sineValue*1.5-0.25, 1.0),
				(float)sineValue);

	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+0] = (byte) c.getRed();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+1] = (byte) c.getGreen();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+2] = (byte) c.getBlue();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+3] = (byte) 255;
	} } } }

	private int buildNoiseTexture()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		byte[] data = new byte[noiseWidth*noiseHeight*noiseDepth*4];
		
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseWidth, noiseHeight, noiseDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				noiseWidth, noiseHeight, noiseDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}

	void generateNoise()
	{	for (int x=0; x<noiseWidth; x++)
		{	for (int y=0; y<noiseHeight; y++)
			{	for (int z=0; z<noiseDepth; z++)
				{	noise[x][y][z] = random.nextDouble();
	}	}	}	}
	
	double smoothNoise(double zoom, double x1, double y1, double z1)
	{	//get fractional part of x, y, and z
		double fractX = x1 - (int) x1;
		double fractY = y1 - (int) y1;
		double fractZ = z1 - (int) z1;

		//neighbor values that wrap
		double x2 = x1 - 1; if (x2<0) x2 = (Math.round(noiseWidth / zoom)) - 1;
		double y2 = y1 - 1; if (y2<0) y2 = (Math.round(noiseHeight / zoom)) - 1;
		double z2 = z1 - 1; if (z2<0) z2 = (Math.round(noiseDepth / zoom)) - 1;

		//smooth the noise by interpolating
		double value = 0.0;
		value += fractX       * fractY       * fractZ       * noise[(int)x1][(int)y1][(int)z1];
		value += (1.0-fractX) * fractY       * fractZ       * noise[(int)x2][(int)y1][(int)z1];
		value += fractX       * (1.0-fractY) * fractZ       * noise[(int)x1][(int)y2][(int)z1];	
		value += (1.0-fractX) * (1.0-fractY) * fractZ       * noise[(int)x2][(int)y2][(int)z1];
				
		value += fractX       * fractY       * (1.0-fractZ) * noise[(int)x1][(int)y1][(int)z2];
		value += (1.0-fractX) * fractY       * (1.0-fractZ) * noise[(int)x2][(int)y1][(int)z2];
		value += fractX       * (1.0-fractY) * (1.0-fractZ) * noise[(int)x1][(int)y2][(int)z2];
		value += (1.0-fractX) * (1.0-fractY) * (1.0-fractZ) * noise[(int)x2][(int)y2][(int)z2];
		
		return value;
	}

	private double turbulence(double x, double y, double z, double maxZoom)
	{	double sum = 0.0, zoom = maxZoom;
		while(zoom >= 0.9)
		{	sum = sum + smoothNoise(zoom, x/zoom, y/zoom, z/zoom) * zoom;
			zoom = zoom / 2.0;
		}
		sum = 128.0 * sum / maxZoom;
		return sum;
	}

	private double logistic(double x)
	{	double k = 3.0;
		return (1.0/(1.0+Math.pow(2.718,-k*x)));
	}
		
	// Keyboard and Mouse Controls
	public void keyPressed(KeyEvent e)
	{
		char charPressed = e.getKeyChar();
		int codePressed = e.getKeyCode();
		float speed = 0.1f;
		float value = 0.0f;
		
		// Camera move forward
		if(charPressed == 'w')
		{
			value = camera.getCameraZ();
			value -= speed;
			camera.setCameraZ(value);
		}
		// Camera strafe left
		if(charPressed == 'a')
		{
			value = camera.getCameraX();
			value -= speed;
			camera.setCameraX(value);
		}
		// Camera move back
		if(charPressed == 's')
		{
			value = camera.getCameraZ();
			value += speed;
			camera.setCameraZ(value);
		}
		// Camera strafe right
		if(charPressed == 'd')
		{
			value = camera.getCameraX();
			value += speed;
			camera.setCameraX(value);
		}	
		// Camera Down
		if(charPressed == 'e')
		{
			value = camera.getCameraY();
			value -= speed;
			camera.setCameraY(value);
		}
		// Camera Up
		if(charPressed == 'q')
		{
			value = camera.getCameraY();
			value += speed;
			camera.setCameraY(value);
		}

		// Pitch Up
		if(codePressed == KeyEvent.VK_UP)
		{
			value = camera.getUValue();
			value -= speed;
			camera.setUValue(value);
		}
		// Pitch Down
		if(codePressed == KeyEvent.VK_DOWN)
		{
			value = camera.getUValue();
			value += speed;
			camera.setUValue(value);
		}
		// Pan Left
		if(codePressed == KeyEvent.VK_LEFT)
		{
			value = camera.getVValue();
			value -= speed;
			camera.setVValue(value);
			
		}
		// Pan Right
		if(codePressed == KeyEvent.VK_RIGHT)
		{
			value = camera.getVValue();
			value += speed;
			camera.setVValue(value);
		}
		// Roll Left
		if(charPressed == 'j')
		{
			value = camera.getNValue();
			value -= speed;
			camera.setNValue(value);
		}
		// Roll Right
		if(charPressed == 'k')
		{
			value = camera.getNValue();
			value += speed;
			camera.setNValue(value);
		}
		// Toggle Axes
		if(codePressed == KeyEvent.VK_SPACE)
			toggle = !toggle;

		// Toggle Positional Lights
		if(charPressed == 't')
			lightToggle = !lightToggle;
		
		// Exit the program
        if (codePressed == KeyEvent.VK_ESCAPE) 
             System.exit(0);
        					
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		int rotation = e.getWheelRotation();

		if(rotation > 0)
		{
			lightZ += 1.5f;
		}

		if(rotation < 0)
		{
			lightZ -= 1.5f;
		}

		System.out.println("light Z: " + lightZ);
	}

	@Override
    public void mouseMoved(MouseEvent e) 
	{
 		float x = 50 * (((float) e.getX() - myCanvas.getWidth() / 2) / myCanvas.getWidth() * 2);
        float y = 50 * -(((float) e.getY() - myCanvas.getHeight() / 2) / myCanvas.getHeight() * 2);

		y = Math.min(Math.max(y, 0), 50);
		
		lightX = x;
		System.out.println("light X: " + lightX);
		
		lightY = y;
		System.out.println("light Y: " + lightY);

    }

	@Override
    public void mouseDragged(MouseEvent e) 
	{
   
    }
    
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

}
