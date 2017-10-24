Fast Transient Renderer input Arguments
===========================

Arguments format
----------------

The renderer accepts arguments of the kind '-argument value value...'.

A value can be of the type:

* |value: float|: a floating point number separated by a '.' (e.g. 1.5).

* |value: int|: a integer number (e.g. 10).

* |value: string|: a text string, with properly string limiters (e.g. 'name' or
'a long name').


Camera parameters
-----------------

* -cam |px: float| |py: float| |pz: float| |lx: float| |ly: float| |lz: float|
   px py pz coords. of the camera position and lx ly lz coordinates of the point at which the camera is looking.

* -camfov |fov: float|
   fov of the camera, on degrees. By default, 90
   
* -camznearfar |znear: float| |zfar: float|
   Camera projection matrix znear and zfar. By default, 0.1 and 10
   
* -res |x: int| |y: int| |t: int|
   width, height and temporal resolution of the camera image, in pixels

   
* -lightcam |px: float| |py: float| |pz: float| |lx: float| |ly: float| |lz: float|
   px py pz coords. of the light camera position and lx ly lz coordinates of the point at which the light camera is looking.
   
* -lightcamfov |fov: float|
   fov of the light camera, on degrees. By default, 90
   
* -lightcamznearfar |znear: float| |zfar: float|
   Light camera projection matrix znear and zfar. By default, 0.1 and 10

* -lightres |x: int| |y: int| 
   width and height of the light camera image, in pixels. Note that the light camera doesn't have a temporal resolution   
  
  
Video parameters
---------------

By default the images are saved in PNG format (.png), and the videos are saved in MPEG4 format (.mp4)

* -intensityCorrection |i: float|
   Intensity correction applied to the gamma corrected final images. The higher the value, the brighter the final images.
   Is better to find this arg by trial and error for each individual scene. The default value is 4000

* -videoName |name: string|
   Name of the output video. If it already exists, an identifier will be added at its end (_1, _2 etc.)

* -saveImages 
   Each individual image will be saved into an imgs/ folder, overriding current images if needed. Please note that this option is slower than
   storing directly the video.
   
* -saveImagesAsStreaks
   Each individual image will be saved into an imgs/ folder, but altering the normal image order so the x image coordinate is the temporal dimension, the y image coordinate is the x coordinate
   and each stored image correspond to a single y coordinate.

* -imageName |name: string|
   Initial name of each individual frame image stored in imgs/ . After this initial name a modifier will be added in function of the current frame,
   which will look like <name>_<frame>

* -imageFormat |format: string|
   Accepts either PNG or JPG. Currently HDR is unsupported.

* -saveSteadyImage 
   Saves a image which accumulates all temporal intensity (an steady image) into disk
   
* -steadyImageName |name: string|
   Name of the output steady image. If it already exists, an identifier will be added at its end (_1, _2 etc.)
     

Transient parameters
---------------------

* -tdelta |t: float|
   Specifies how much time is ellapsed in each video frame

* -t0 |t: float|
   Specifies the initial video time
   
* -disableDirectLight
   Stores only the indirect light in the video and images. Considers all direct light as 0
   
* -considerCameraDist
   By default, distance from the world to the camera is considered as 0. With this arg, that distance is accounted too
   

   
Others
---------------------
* -model |obj_route: string| |r: float| |g: float| |b: float|
   Obj model to render, with a RGB albedo. Only diffuse rendering is supported by now. More than one model can be specified.
   
* -batches |b: int|
   Perform the rendering in b different batches. Increase this number if you run out of VRAM or if your GPU crashes while rendering (too much workload).
   Rising the number of batches will negatively affect the computation speed.
