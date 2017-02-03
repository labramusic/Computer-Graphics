package hr.fer.zemris.java.raytracer.model;

/**
 * Represents a sphere used as a graphical object in scenes. The sphere is defined by its center point,
 * its radius and coefficients which determine its color.
 * @author labramusic
 *
 */
public class Sphere extends GraphicalObject {

	/** The center. */
	private Point3D center;

	/** The radius. */
	private double radius;

	/** The kdr. */
	private double kdr;

	/** The kdg. */
	private double kdg;

	/** The kdb. */
	private double kdb;

	/** The krr. */
	private double krr;

	/** The krg. */
	private double krg;

	/** The krb. */
	private double krb;

	/** The krn. */
	private double krn;

	/**
	 * Instantiates a new sphere.
	 *
	 * @param center the center
	 * @param radius the radius
	 * @param kdr the kdr
	 * @param kdg the kdg
	 * @param kdb the kdb
	 * @param krr the krr
	 * @param krg the krg
	 * @param krb the krb
	 * @param krn the krn
	 */
	public Sphere(Point3D center, double radius, double kdr, double kdg, double kdb, double krr, double krg, double krb,
			double krn) {
		super();
		this.center = center;
		this.radius = radius;
		this.kdr = kdr;
		this.kdg = kdg;
		this.kdb = kdb;
		this.krr = krr;
		this.krg = krg;
		this.krb = krb;
		this.krn = krn;
	}

	@Override
	public RayIntersection findClosestRayIntersection(Ray ray) {
		Point3D start = ray.start;
		Point3D dir = ray.direction;
		double b = dir.scalarMultiply(2).scalarProduct(start.sub(center));
		double c = start.sub(center).scalarProduct(start.sub(center)) - radius*radius;
		double l1 = (-b - Math.sqrt(b*b - 4*c))/2;
		double l2 = (-b + Math.sqrt(b*b - 4*c))/2;
		double l;
		boolean outer = true;

		if (l1!=l2 && l1>0 && l2>0) {
			l = l1;
		} else if(l1!=l2 && l1<=0 && l2>0) {
			l = l2;
			outer = false;
		} else if(l1==l2 && l1!=Double.NaN) {
			l = l1;
		} else {
			// no intersection or the intersection is behind the observer
			return null;
		}
		Point3D point = start.add(dir.scalarMultiply(l));
		double distance = start.sub(point).norm();

		Point3D normal = point.sub(center).normalize();

		return new RayIntersection(point, distance, outer) {

			@Override
			public Point3D getNormal() {
				return normal;
			}

			@Override
			public double getKrr() {
				return krr;
			}

			@Override
			public double getKrn() {
				return krn;
			}

			@Override
			public double getKrg() {
				return krg;
			}

			@Override
			public double getKrb() {
				return krb;
			}

			@Override
			public double getKdr() {
				return kdr;
			}

			@Override
			public double getKdg() {
				return kdg;
			}

			@Override
			public double getKdb() {
				return kdb;
			}
		};
	}

}


