package edu.cg.scene.lightSources;

import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.objects.Surface;

public class PointLight extends Light {
	protected Point position;
	
	//Decay factors:
	protected double kq = 0.01;
	protected double kl = 0.1;
	protected double kc = 1;
	
	protected String description() {
		String endl = System.lineSeparator();
		return "Intensity: " + intensity + endl +
				"Position: " + position + endl +
				"Decay factors: kq = " + kq + ", kl = " + kl + ", kc = " + kc + endl;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Point Light:" + endl + description();
	}
	
	@Override
	public PointLight initIntensity(Vec intensity) {
		return (PointLight)super.initIntensity(intensity);
	}
	
	public PointLight initPosition(Point position) {
		this.position = position;
		return this;
	}
	
	public PointLight initDecayFactors(double kq, double kl, double kc) {
		this.kq = kq;
		this.kl = kl;
		this.kc = kc;
		return this;
	}

	@Override
	public Ray rayToLight(Point fromPoint) {
		Ray toLight = new Ray(fromPoint, this.position);
		return toLight;
	}

	@Override
	public boolean isOccludedBy(Surface surface, Ray rayToLight) {
		Hit t = surface.intersect(rayToLight);
		Point source = rayToLight.source();
		Point pointSource = this.position;
		if(t == null) {
			return false;
		}else {
			return source.distSqr(pointSource) > source.distSqr(rayToLight.getHittingPoint(t));
		}
	}

	@Override
	public Vec intensity(Point hittingPoint, Ray rayToLight) {
		double dist = Ops.dist(this.position, hittingPoint);
		double fatt = (kq * Math.pow(dist, 2)) + (kl * dist) + kc;		
		return this.intensity.mult(1 / fatt);
	}


}
