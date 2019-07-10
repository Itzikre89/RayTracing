package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class AxisAlignedBox extends Shape {
	private Point minPoint;
	private Point maxPoint;
	private String name = "";
	static private int CURR_IDX;

	/**
	 * Creates an axis aligned box with a specified minPoint and maxPoint.
	 */
	public AxisAlignedBox(Point minPoint, Point maxPoint) {
		this.minPoint = minPoint;
		this.maxPoint = maxPoint;
		name = new String("Box " + CURR_IDX);
		CURR_IDX += 1;
		fixBoundryPoints();
	}

	/**
	 * Creates a default axis aligned box with a specified minPoint and maxPoint.
	 */
	public AxisAlignedBox() {
		minPoint = new Point(-1.0, -1.0, -1.0);
		maxPoint = new Point(1.0, 1.0, 1.0);
	}

	/**
	 * This methods fixes the boundary points minPoint and maxPoint so that the
	 * values are consistent.
	 */
	private void fixBoundryPoints() {
		double min_x = Math.min(minPoint.x, maxPoint.x), max_x = Math.max(minPoint.x, maxPoint.x),
				min_y = Math.min(minPoint.y, maxPoint.y), max_y = Math.max(minPoint.y, maxPoint.y),
				min_z = Math.min(minPoint.z, maxPoint.z), max_z = Math.max(minPoint.z, maxPoint.z);
		minPoint = new Point(min_x, min_y, min_z);
		maxPoint = new Point(max_x, max_y, max_z);
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return name + endl + "Min Point: " + minPoint + endl + "Max Point: " + maxPoint + endl;
	}

	// Initializers
	public AxisAlignedBox initMinPoint(Point minPoint) {
		this.minPoint = minPoint;
		fixBoundryPoints();
		return this;
	}

	public AxisAlignedBox initMaxPoint(Point maxPoint) {
		this.maxPoint = maxPoint;
		fixBoundryPoints();
		return this;
	}

	@Override
	public Hit intersect(Ray ray) {
		double tNear = Integer.MIN_VALUE;
		double tFar = Integer.MAX_VALUE;
		Point midPoint = new Point((minPoint.x + maxPoint.x) / 2, (minPoint.y +
				  maxPoint.y) / 2, (minPoint.z + maxPoint.z) / 2);
		Point origin = ray.source();
		double x = Math.abs((minPoint.x - maxPoint.x) / 2);
		double y = Math.abs((minPoint.y - maxPoint.y) / 2);
		double z = Math.abs((minPoint.z - maxPoint.z) / 2);
		
		if(ray.direction().x == 0) {
			if(origin.x < minPoint.x || origin.x > maxPoint.x) {
				return null;
			}
		}
		
		double t1 = (double)(minPoint.x - origin.x) / (double)(ray.direction().x);
		double t2 = (double)(maxPoint.x - origin.x) / (double)(ray.direction().x);
		if(t1 > t2) {
			double temp = t1;
			t1 = t2;
			t2 = temp;
		}
		if(t1 > tNear) {
			tNear = t1;
		}
		if(t2 < tFar) {
			tFar = t2;
		}
		if(tNear > tFar) {
			return null;
		}
		if(tFar < Ops.epsilon) {
			return null;
		}
		
		if(ray.direction().y == 0) {
			if(origin.y < minPoint.y || origin.y > maxPoint.y) {
				return null;
			}
		}
		t1 = (double)(minPoint.y - origin.y) / (double)(ray.direction().y);
		t2 = (double)(maxPoint.y - origin.y) / (double)(ray.direction().y);
		if(t1 > t2) {
			double temp = t1;
			t1 = t2;
			t2 = temp;
		}
		if(t1 > tNear) {
			tNear = t1;
		}
		if(t2 < tFar) {
			tFar = t2;
		}
		if(tNear > tFar) {
			return null;
		}
		if(tFar < Ops.epsilon) {
			return null;
		}
			
		if(ray.direction().z == 0) {
			if(origin.z < minPoint.z || origin.z > maxPoint.z) {
				return null;
			}
		}
		t1 = (double)(minPoint.z - origin.z) / (double)(ray.direction().z);
		t2 = (double)(maxPoint.z - origin.z) / (double)(ray.direction().z);
		if(t1 > t2) {
			double temp = t1;
			t1 = t2;
			t2 = temp;
		}
		if(t1 > tNear) {
			tNear = t1;
		}
		if(t2 < tFar) {
			tFar = t2;
		}
		if(tNear > tFar) {
			return null;
		}
		if(tFar < Ops.epsilon) {
			return null;
		}
		
		if(tNear < Ops.epsilon) {
			return null;
		}
		Point hittingPoint = Ops.add(origin, tNear, ray.direction());
		Vec point = Ops.sub(hittingPoint, midPoint);
		Vec norm = new Vec(point.x / x, point.y / y, point.z / z).normalize();
		return new Hit(tNear, norm);
	}




}
