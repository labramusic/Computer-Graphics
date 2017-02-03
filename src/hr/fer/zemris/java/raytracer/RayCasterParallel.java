package hr.fer.zemris.java.raytracer;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import hr.fer.zemris.java.raytracer.model.GraphicalObject;
import hr.fer.zemris.java.raytracer.model.IRayTracerProducer;
import hr.fer.zemris.java.raytracer.model.IRayTracerResultObserver;
import hr.fer.zemris.java.raytracer.model.LightSource;
import hr.fer.zemris.java.raytracer.model.Point3D;
import hr.fer.zemris.java.raytracer.model.Ray;
import hr.fer.zemris.java.raytracer.model.RayIntersection;
import hr.fer.zemris.java.raytracer.model.Scene;
import hr.fer.zemris.java.raytracer.viewer.RayTracerViewer;

/**
 * Program which draws a predefined scene of graphical objects using a simple
 * ray tracer. The ray tracer generates an image by tracing the path of light
 * through pixels in an image plane and simulating the effects of its encounters
 * with virtual objects. The tracing is done parallelized, using the Fork-Join
 * framework and {@link RecursiveAction}.
 * 
 * @author labramusic
 *
 */
public class RayCasterParallel {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		RayTracerViewer.show(getIRayTracerProducer(), new Point3D(10, 0, 0), new Point3D(0, 0, 0),
				new Point3D(0, 0, 10), 20, 20);
	}

	/**
	 * A recursive action which traces a part of the image on the screen,
	 * defined by a set threshold.
	 * 
	 * @author labramusic
	 *
	 */
	public static class TraceAction extends RecursiveAction {

		/**
		 * Default serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Position of human observer.
		 */
		Point3D eye;

		/**
		 * Horizontal width of observed space.
		 */
		double horizontal;

		/**
		 * Vertical height of observed space.
		 */
		double vertical;

		/**
		 * Screen width.
		 */
		int width;

		/**
		 * Screen height.
		 */
		int height;

		/**
		 * Minimum y value.
		 */
		int yMin;

		/**
		 * Maximum y value.
		 */
		int yMax;

		/**
		 * Screen x axis.
		 */
		Point3D xAxis;

		/**
		 * Screen y axis.
		 */
		Point3D yAxis;

		/**
		 * Upper left screen corner.
		 */
		Point3D screenCorner;

		/**
		 * Scene containing graphical objects.
		 */
		Scene scene;

		/**
		 * Red color intensity.
		 */
		short[] red;

		/**
		 * Green color intensity.
		 */
		short[] green;

		/**
		 * Blue color intensity.
		 */
		short[] blue;

		/**
		 * Defines max number of rows a thread will draw at a time.
		 */
		static final int threshold = 16;

		/**
		 * Initializes a new TraceAction.
		 * 
		 * @param eye
		 *            position of human observer
		 * @param horizontal
		 *            horizontal width of observed space
		 * @param vertical
		 *            vertical height of observed space
		 * @param width
		 *            screen width
		 * @param height
		 *            screen height
		 * @param yMin
		 *            minimum y value
		 * @param yMax
		 *            maximum y value
		 * @param xAxis
		 *            x axis
		 * @param yAxis
		 *            y axis
		 * @param screenCorner
		 *            upper left screen corner
		 * @param scene
		 *            scene containing graphical objects
		 * @param red
		 *            red color intensity
		 * @param green
		 *            green color intensity
		 * @param blue
		 *            blue color intensity
		 */
		public TraceAction(Point3D eye, double horizontal, double vertical, int width, int height, int yMin, int yMax,
				Point3D xAxis, Point3D yAxis, Point3D screenCorner, Scene scene, short[] red, short[] green,
				short[] blue) {
			super();
			this.eye = eye;
			this.horizontal = horizontal;
			this.vertical = vertical;
			this.width = width;
			this.height = height;
			this.yMin = yMin;
			this.yMax = yMax;
			this.xAxis = xAxis;
			this.yAxis = yAxis;
			this.screenCorner = screenCorner;
			this.scene = scene;
			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		@Override
		public void compute() {
			if (yMax - yMin + 1 <= threshold) {
				computeDirect();
				return;
			}
			invokeAll(
					new TraceAction(eye, horizontal, vertical, width, height, yMin, yMin + (yMax - yMin) / 2, xAxis,
							yAxis, screenCorner, scene, red, green, blue),
					new TraceAction(eye, horizontal, vertical, width, height, yMin + (yMax - yMin) / 2 + 1, yMax, xAxis,
							yAxis, screenCorner, scene, red, green, blue));
		}

		/**
		 * Computes the intensity of each color for a certain strip of the
		 * image.
		 */
		public void computeDirect() {
			int offset = yMin * width;
			short rgb[] = new short[3];
			for (int y = yMin; y <= yMax; y++) {
				for (int x = 0; x < width; x++) {
					Point3D screenPoint = screenCorner.add(xAxis.scalarMultiply(horizontal * x / (width - 1)))
							.sub(yAxis.scalarMultiply(vertical * y / (height - 1)));
					Ray ray = Ray.fromPoints(eye, screenPoint);

					tracer(scene, ray, rgb);

					red[offset] = rgb[0] > 255 ? 255 : rgb[0];
					green[offset] = rgb[1] > 255 ? 255 : rgb[1];
					blue[offset] = rgb[2] > 255 ? 255 : rgb[2];

					offset++;
				}
			}
		}

		/**
		 * Calculates the intersection between the view ray and the closest
		 * object in its view, if intersection exists, its color is determined
		 * using Phong's lighting algorithm.
		 * 
		 * @param scene
		 *            scene containing graphical objects
		 * @param viewRay
		 *            ray between eye and screen point
		 * @param rgb
		 *            color intensities
		 */
		private void tracer(Scene scene, Ray viewRay, short[] rgb) {
			List<GraphicalObject> objects = scene.getObjects();
			RayIntersection intersection = null;
			double distance = Double.MAX_VALUE;
			for (GraphicalObject object : objects) {
				RayIntersection newIntersection = object.findClosestRayIntersection(viewRay);
				if (newIntersection != null && newIntersection.getDistance() < distance) {
					intersection = newIntersection;
					distance = intersection.getDistance();
				}
			}
			if (intersection != null && intersection.isOuter()) {
				determineColorFor(scene, viewRay, intersection, rgb);
			} else {
				rgb[0] = 0;
				rgb[1] = 0;
				rgb[2] = 0;
			}
		}

		/**
		 * Determines if the observed point is visible or shadowed and colors it
		 * appropriately.
		 * 
		 * @param scene
		 *            scene containing graphical objects
		 * @param viewRay
		 *            ray between eye and screen point
		 * @param intersection
		 *            intersection between view ray and observed object
		 * @param rgb
		 *            color intensities
		 */
		private void determineColorFor(Scene scene, Ray viewRay, RayIntersection intersection, short[] rgb) {
			rgb[0] = 15;
			rgb[1] = 15;
			rgb[2] = 15;

			final double eps = 0.01;
			List<GraphicalObject> objects = scene.getObjects();
			List<LightSource> lightSources = scene.getLights();

			for (LightSource lightSource : lightSources) {
				Ray ray = Ray.fromPoints(lightSource.getPoint(), intersection.getPoint());
				boolean isShadowed = false;
				double distance = lightSource.getPoint().sub(intersection.getPoint()).norm();
				for (GraphicalObject object : objects) {
					RayIntersection lightIntersection = object.findClosestRayIntersection(ray);
					if (lightIntersection != null) {
						double lightDistance = lightSource.getPoint().sub(lightIntersection.getPoint()).norm();
						if (Double.compare(lightDistance + eps, distance) < 0) {
							isShadowed = true;
							break;
						}
					}
				}

				if (isShadowed) {
					isShadowed = false;
					continue;
				} else {
					color(lightSource, viewRay, intersection, rgb);
				}
			}

		}

		/**
		 * Colors the visible intersection point using the light intensity from
		 * the given light source.
		 * 
		 * @param lightSource
		 *            the light source
		 * @param viewRay
		 *            ray between eye and screen point
		 * @param intersection
		 *            intersection between view ray and observed object
		 * @param rgb
		 *            color intensities
		 */
		private void color(LightSource lightSource, Ray viewRay, RayIntersection intersection, short rgb[]) {
			Point3D l = lightSource.getPoint().sub(intersection.getPoint()).normalize();
			Point3D n = intersection.getNormal();
			Point3D r = n.scalarMultiply(2).scalarMultiply(l.scalarProduct(n)).sub(l).normalize();
			Point3D v = viewRay.start.sub(intersection.getPoint()).normalize();
			int iR = lightSource.getR();
			int iG = lightSource.getG();
			int iB = lightSource.getB();
			double krn = intersection.getKrn();
			double kdr = intersection.getKdr();
			double krr = intersection.getKrr();
			double kdg = intersection.getKdg();
			double krg = intersection.getKrg();
			double kdb = intersection.getKdb();
			double krb = intersection.getKrb();
			double lnScalar = l.scalarProduct(n);
			if (lnScalar < 0) {
				lnScalar = 0;
			}
			double rvScalar = r.scalarProduct(v);
			if (rvScalar < 0) {
				rvScalar = 0;
			}
			rgb[0] += (iR * (kdr * lnScalar + krr * Math.pow(rvScalar, krn)));
			rgb[1] += (iG * (kdg * lnScalar + krg * Math.pow(rvScalar, krn)));
			rgb[2] += (iB * (kdb * lnScalar + krb * Math.pow(rvScalar, krn)));
		}
	}

	/**
	 * Returns a new ray tracer producer which then produces a ray tracer. The
	 * ray tracer calculates the color of each pixel and draws it on the screen.
	 * 
	 * @return ray tracer producer
	 */
	private static IRayTracerProducer getIRayTracerProducer() {
		return new IRayTracerProducer() {
			@Override
			public void produce(Point3D eye, Point3D view, Point3D viewUp, double horizontal, double vertical,
					int width, int height, long requestNo, IRayTracerResultObserver observer) {

				System.out.println("Započinjem izračune...");
				short[] red = new short[width * height];
				short[] green = new short[width * height];
				short[] blue = new short[width * height];

				Point3D zAxis = view.sub(eye).normalize();
				Point3D yAxis = viewUp.normalize().sub(zAxis.scalarMultiply(zAxis.scalarProduct(viewUp.normalize())))
						.normalize();
				Point3D xAxis = zAxis.vectorProduct(yAxis).normalize();

				Point3D screenCorner = view.sub(xAxis.scalarMultiply(horizontal / 2))
						.add(yAxis.scalarMultiply(vertical / 2));

				Scene scene = RayTracerViewer.createPredefinedScene();

				ForkJoinPool pool = new ForkJoinPool();
				pool.invoke(new TraceAction(eye, horizontal, vertical, width, height, 0, height - 1, xAxis, yAxis,
						screenCorner, scene, red, green, blue));
				pool.shutdown();

				System.out.println("Izračuni gotovi...");
				observer.acceptResult(red, green, blue, requestNo);
				System.out.println("Dojava gotova...");
			}

		};
	}

}
