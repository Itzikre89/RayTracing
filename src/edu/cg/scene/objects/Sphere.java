package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Sphere extends Shape {
	private Point center;
	private double radius;

	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl + "Center: " + center + endl + "Radius: " + radius + endl;
	}

	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}

	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}

	@Override
	public Hit intersect(Ray ray) {
		double B = ray.direction().mult(2.0).dot(ray.source().sub(this.center));
		double C = ray.source().distSqr(this.center) - Math.pow(this.radius, 2);
		double delta = Math.pow(B, 2) - 4 * C;
		double hitTowards;
		boolean isWithin = false;
		Vec normalizedHit;
		if(delta < 0) {
			return null;
		} else {
			double t0 = (-B + Math.sqrt(delta)) / 2.0;
			double t1 = (-B - Math.sqrt(delta)) / 2.0;
			
			if(t0 < Ops.epsilon && t1 < Ops.epsilon) {
				return null;
			} 
			if(t0 > Ops.epsilon && t1 > Ops.epsilon) {
				hitTowards = Math.min(t0, t1);
				normalizedHit = ray.add(hitTowards).sub(this.center).normalize();
				Hit hit = new Hit (hitTowards, isWithin ? normalizedHit.neg() : normalizedHit);
				hit.setIsWithin(isWithin);
				return hit;
			} else {
				return null;
			}			
		}
	}
}
