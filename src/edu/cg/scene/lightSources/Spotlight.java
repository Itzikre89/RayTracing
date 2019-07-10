package edu.cg.scene.lightSources;

import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.objects.Surface;

public class Spotlight extends PointLight {
	private Vec direction;
	
	public Spotlight initDirection(Vec direction) {
		this.direction = direction;
		return this;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Spotlight: " + endl +
				description() + 
				"Direction: " + direction + endl;
	}
	
	@Override
	public Spotlight initPosition(Point position) {
		return (Spotlight)super.initPosition(position);
	}
	
	@Override
	public Spotlight initIntensity(Vec intensity) {
		return (Spotlight)super.initIntensity(intensity);
	}
	
	@Override
	public Spotlight initDecayFactors(double q, double l, double c) {
		return (Spotlight)super.initDecayFactors(q, l, c);
	}
	
	@Override
	public boolean isOccludedBy(Surface surface, Ray rayToLight) {
		Vec L = rayToLight.direction().neg();
		Vec D = this.direction.normalize();
		double dotProduct = L.dot(D);
		if(dotProduct < Ops.epsilon || super.isOccludedBy(surface, rayToLight)) {
			return true;
		}
		return false;
	}
	
	@Override
	public Vec intensity(Point hittingPoint, Ray rayToLight) {
		double vDotProduct = this.direction.normalize().neg().dot(rayToLight.direction());
		double dist = Ops.dist(this.position, hittingPoint);		
		double fatt = (kq * Math.pow(dist, 2)) + (kl * dist) + kc;	
		
		return super.intensity(hittingPoint,rayToLight).mult(vDotProduct / fatt);
	}

}
