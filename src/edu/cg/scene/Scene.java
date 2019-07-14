package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.camera.PinholeCamera;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Shape;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; // gets the values of 1, 2 and 3
	private boolean renderRefarctions = false;
	private boolean renderReflections = false;

	private PinholeCamera camera;
	private Vec ambient = new Vec(1, 1, 1); // white
	private Vec backgroundColor = new Vec(0, 0.5, 1); // blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();

	// MARK: initializers
	public Scene initCamera(Point eyePoistion, Vec towardsVec, Vec upVec, double distanceToPlain) {
		this.camera = new PinholeCamera(eyePoistion, towardsVec, upVec, distanceToPlain);
		return this;
	}

	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}

	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}

	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}

	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}

	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}

	public Scene initName(String name) {
		this.name = name;
		return this;
	}

	public Scene initRenderRefarctions(boolean renderRefarctions) {
		this.renderRefarctions = renderRefarctions;
		return this;
	}

	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}

	// MARK: getters
	public String getName() {
		return name;
	}

	public int getFactor() {
		return antiAliasingFactor;
	}

	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}

	public boolean getRenderRefarctions() {
		return renderRefarctions;
	}

	public boolean getRenderReflections() {
		return renderReflections;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Camera: " + camera + endl + "Ambient: " + ambient + endl + "Background Color: " + backgroundColor + endl
				+ "Max recursion level: " + maxRecursionLevel + endl + "Anti aliasing factor: " + antiAliasingFactor
				+ endl + "Light sources:" + endl + lightSources + endl + "Surfaces:" + endl + surfaces;
	}

	private transient ExecutorService executor = null;
	private transient Logger logger = null;

	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
	}

	public BufferedImage render(int imgWidth, int imgHeight, double viewPlainWidth, Logger logger)
			throws InterruptedException, ExecutionException {
		// This method is invoked each time Render Scene button is invoked.
		initSomeFields(imgWidth, imgHeight, logger);

		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		camera.initResolution(imgHeight, imgWidth, viewPlainWidth);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Intitialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);

		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][]) (new Future[imgHeight][imgWidth]);

		this.logger.log("Starting to shoot " + (imgHeight * imgWidth * antiAliasingFactor * antiAliasingFactor)
				+ " rays over " + name);

		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);

		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");

		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}

		executor.shutdown();

		this.logger.log("Ray tracing of " + name + " has been completed.");

		executor = null;
		this.logger = null;

		return img;
	}

	
	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			Point centerPoint = camera.transform(x, y);
			Ray ray = new Ray(camera.getCameraPosition(), centerPoint);
			Vec color = calcColor(ray, 0);
			return color.toColor();
		});
	}

	
	private Vec calcColor(Ray ray, int recusionLevel) {
		// white color when reached to max recursion
		if (this.maxRecursionLevel <= recusionLevel) {
			return new Vec();
		}

		Hit closestHit = this.closestHit(ray);
		if (closestHit == null) {
			return backgroundColor;
		}
		Vec ka = closestHit.getSurface().Ka();
		Vec res = ka.mult(this.ambient);
		Point hittingPoint = ray.getHittingPoint(closestHit);


		for (Light light : this.lightSources) {
			Ray toLight = light.rayToLight(hittingPoint);
			Vec Intensity = light.intensity(hittingPoint, toLight);
			if (!isBlockedBySurface(light, toLight)) {
				Vec color = this.calcDiffuseColor(closestHit, toLight);
				color = color.add(calcSpecularColor(closestHit, toLight, ray));
				res = res.add(color.mult(Intensity));
			}
		}

		if(this.renderReflections) {
			Vec reflectedVec = Ops.reflect(ray.direction(), closestHit.getNormalToSurface());
			Vec IntensityVec = closestHit.getSurface().Ks().mult(closestHit.getSurface().reflectionIntensity());
			Ray reflectedRay = new Ray(hittingPoint, reflectedVec);
			res = res.add(calcColor(reflectedRay, recusionLevel + 1).mult(IntensityVec));
					
		}
		
		if(this.renderRefarctions && closestHit.getSurface().isTransparent()) {
			double n1 = closestHit.getSurface().n1(closestHit);
			double n2 = closestHit.getSurface().n2(closestHit);
			Vec refractedVec = Ops.refract(ray.direction(), closestHit.getNormalToSurface(), n1, n2);
			Vec intensityVec = new Vec(closestHit.getSurface().refractionIntensity());
			Ray refractedRay = new Ray(hittingPoint, refractedVec);
			res = res.add(calcColor(refractedRay, recusionLevel + 1)).mult(intensityVec);
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param light
	 * @param rayToLight
	 * @return
	 */
	private boolean isBlockedBySurface(Light light, Ray rayToLight) {

		for (Surface surface : this.surfaces) {
			if (light.isOccludedBy(surface, rayToLight)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param ray
	 * @return
	 */
	private Hit closestHit(Ray ray) {
		Hit closest = null;
		Hit currHit;
		for (Surface surface : this.surfaces) {
			currHit = surface.intersect(ray);
			if (currHit != null && (closest == null || currHit.compareTo(closest) < 0)) {
				closest = currHit;
			}
		}
		return closest;
	}

	/**
	 * 
	 * @param hit
	 * @param ray
	 * @return
	 */
	private Vec calcDiffuseColor(Hit hit, Ray ray) {
		Vec kd = hit.getSurface().Kd();
		Vec n = hit.getNormalToSurface();
		Vec l = ray.direction();
		if(n.dot(l) < 0.0) {
			return kd.mult(0.0);
		}
		return kd.mult(n.dot(l));
	}

	/**
	 * 
	 * @param hit
	 * @param rayToLight
	 * @param origin
	 * @return
	 */
	private Vec calcSpecularColor(Hit hit, Ray rayToLight, Ray origin) {	
		Vec ks = hit.getSurface().Ks();
		Vec V = origin.direction().neg();
		Vec R = Ops.reflect(rayToLight.direction().neg(), hit.getNormalToSurface());
		double dotProduct = Ops.dot(V, R);
		
		return dotProduct < 0 ? new Vec() : ks.mult(Math.pow(dotProduct, hit.getSurface().shininess()));
	}

}
