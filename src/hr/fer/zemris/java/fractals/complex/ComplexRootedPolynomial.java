package hr.fer.zemris.java.fractals.complex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an immutable model of root-based complex polynomial.
 * 
 * @author labramusic
 *
 */
public class ComplexRootedPolynomial {

	/**
	 * List of complex roots.
	 */
	private List<Complex> roots;

	/**
	 * Initializes a new ComplexRootedPolynomial based on its roots. Throws
	 * {@link IllegalArgumentException} if any of the given roots is null.
	 * 
	 * @param roots
	 *            complex roots
	 */
	public ComplexRootedPolynomial(Complex... roots) {
		List<Complex> list = new ArrayList<>();
		for (Complex root : roots) {
			if (root == null) {
				throw new IllegalArgumentException("Complex root cannot be null.");
			}
			list.add(root);
		}
		this.roots = Collections.unmodifiableList(list);
	}

	/**
	 * Computes polynomial value at given point z. Throws
	 * {@link IllegalArgumentException} if given complex number is null.
	 * 
	 * @param z
	 *            complex point
	 * @return polynomial value at z
	 */
	public Complex apply(Complex z) {
		if (z == null) {
			throw new IllegalArgumentException("Complex number cannot be null.");
		}
		Complex value = Complex.ONE;
		for (Complex root : roots) {
			value = value.multiply(z.sub(root));
		}
		return value;
	}

	/**
	 * Converts this representation to {@link ComplexPolynomial} type.
	 * 
	 * @return ComplexPolynomial representation of this ComplexRootedPolynomial
	 */
	public ComplexPolynomial toComplexPolynom() {
		ComplexPolynomial result = new ComplexPolynomial(Complex.ONE);
		for (Complex root : roots) {
			result = result.multiply(new ComplexPolynomial(root.negate(), Complex.ONE));
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("f(z) = ");
		for (Complex root : roots) {
			sb.append("(z");
			double real = root.getReal();
			double im = root.getImaginary();
			if ((real == 0 && im < 0) || (im == 0 && real < 0)) {
				sb.append("+" + root.negate());
			} else if ((real == 0) || (im == 0)) {
				sb.append("-" + root);
			} else {
				sb.append("-(" + root + ")");
			}
			sb.append(")");
		}
		return sb.toString();
	}

	/**
	 * Finds index of closest root for given complex number z that is within the
	 * given threshold. Returns -1 if such root doesn't exist. Throws
	 * {@link IllegalArgumentException} if given complex number is null.
	 * 
	 * @param z
	 *            complex number
	 * @param threshold
	 *            number threshold
	 * @return index of closest root for given z, or -1 if such root doesn't
	 *         exist
	 */
	public int indexOfClosestRootFor(Complex z, double threshold) {
		if (z == null) {
			throw new IllegalArgumentException("Complex number cannot be null.");
		}
		int index = -1;
		for (int i = 0; i < roots.size(); ++i) {
			Complex root = roots.get(i);
			double diff = z.sub(root).module();
			if (diff <= threshold) {
				threshold = diff;
				index = i;
			}
		}
		return index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roots == null) ? 0 : roots.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComplexRootedPolynomial other = (ComplexRootedPolynomial) obj;
		if (roots == null) {
			if (other.roots != null)
				return false;
		} else if (!roots.equals(other.roots))
			return false;
		return true;
	}

}
