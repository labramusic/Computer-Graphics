package hr.fer.zemris.java.raytracer;

import java.util.List;

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
 * with virtual objects. The tracing is done sequentially.
 * 
 * @author labramusic
 *
 */
public class RayCaster {

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

				short[] rgb = new short[3];
				int offset = 0;
				for (int y = 0; y < height; y++) {
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

				System.out.println("Izračuni gotovi...");
				observer.acceptResult(red, green, blue, requestNo);
				System.out.println("Dojava gotova...");
			}

			/**
			 * Calculates the intersection between the view ray and the closest
			 * object in its view, if intersection exists, its color is
			 * determined using Phong's lighting algorithm.
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
			 * Determines if the observed point is visible or shadowed and
			 * colors it appropriately.
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
			 * Colors the visible intersection point using the light intensity
			 * from the given light source.
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

		};
	}

}
