package edu.cg.scene.camera;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public class PinholeCamera {
	private Point mCameraPosition;
	private Vec mTowardsVec;
	private Vec mUpVec;
	private double mDistanceToPlain;
	private int mHeight;
	private int mWidth;
	private double mViewPlainWidth;
	
	/**
	 * Initializes a pinhole camera model with default resolution 200X200 (RxXRy)
	 * and image width 2.
	 * 
	 * @param cameraPosition  - The position of the camera.
	 * @param towardsVec      - The towards vector of the camera (not necessarily
	 *                        normalized).
	 * @param upVec           - The up vector of the camera.
	 * @param distanceToPlain - The distance of the camera (position) to the center
	 *                        point of the image-plain.
	 * 
	 */
	public PinholeCamera(Point cameraPosition, Vec towardsVec, Vec upVec, double distanceToPlain) {
		initResolution(200, 200, 2);
		this.mCameraPosition = cameraPosition;
		this.mTowardsVec = towardsVec;
		this.mUpVec = upVec;
		this.mDistanceToPlain = distanceToPlain;
//		
//		initResolution(200, 200, 2);
//		this.p0 = cameraPosition;
//		this.f = distanceToPlain;
//		this.p1 = Ops.add(p0, f, towardsVec.normalize());
//		this.rightVec = Ops.cross(towardsVec.normalize(), upVec.normalize()).normalize();
//		this.upVec = Ops.cross(this.rightVec, towardsVec.normalize()).normalize();
	
		
	}

	/**
	 * Initializes the resolution and width of the image.
	 * 
	 * @param height         - the number of pixels in the y direction.
	 * @param width          - the number of pixels in the x direction.
	 * @param viewPlainWidth - the width of the image plain in world coordinates.
	 */
	public void initResolution(int height, int width, double viewPlainWidth) {
		this.mHeight = height;
		this.mWidth = width;
		this.mViewPlainWidth = viewPlainWidth;
	}

	/**
	 * Transforms from pixel coordinates to the center point of the corresponding
	 * pixel in model coordinates.
	 * 
	 * @param x - the index of the x direction of the pixel.
	 * @param y - the index of the y direction of the pixel.
	 * @return the middle point of the pixel (x,y) in the model coordinates.
	 */
	public Point transform(int x, int y) {
		double R = this.mViewPlainWidth / this.mWidth;
		Vec vTo = this.mTowardsVec.normalize();
		Vec vUp = this.mUpVec;
		double Rx = this.mWidth;
		double Ry = this.mHeight;
		Vec vRight = Ops.cross(vTo.normalize(), vUp.normalize()).normalize();
//		Vec vRight = (vTo.mult(vUp)).normalize();
		Vec vUpHat = Ops.cross(vRight, vTo.normalize()).normalize();
//		Vec vUpHat = (vRight.mult(vTo)).normalize();
		Point pc = getCameraPosition().add(vTo.normalize().mult(this.mDistanceToPlain));
		
		Vec leftSideEquation = vRight.mult(R * (x - Math.floor(Rx / 2d)));
		Vec rightSideEquation = vUpHat.mult(R * (y - Math.floor(Ry / 2d))).mult(-1);

		return pc.add(leftSideEquation).add(rightSideEquation);
	}

	/**
	 * Returns a copy of the camera position
	 * 
	 * @return a "new" point representing the camera position.
	 */
	public Point getCameraPosition() {
		return new Point(this.mCameraPosition.x, this.mCameraPosition.y, this.mCameraPosition.z);
	}
}
