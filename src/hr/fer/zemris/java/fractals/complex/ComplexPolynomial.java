package hr.fer.zemris.java.fractals.complex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an immutable model of coefficient-based complex polynomial.
 * 
 * @author labramusic
 *
 */
public class ComplexPolynomial {

	/**
	 * List of complex factors.
	 */
	private List<Complex> factors;

	/**
	 * Initializes a new ComplexPolynomial with the given complex factors. The
	 * factors are given as coefficients for order from 0 to n. Throws
	 * {@link IllegalArgumentException} if any of the given factors is null.
	 * 
	 * @param factors
	 *            complex factors
	 */
	public ComplexPolynomial(Complex... factors) {
		List<Complex> list = new ArrayList<>();
		for (Complex factor : factors) {
			if (factor == null) {
				throw new IllegalArgumentException("Complex factor cannot be null.");
			}
			list.add(factor);
		}

		while (list.size() > 1 && list.get(list.size() - 1).equals(Complex.ZERO)) {
			list.remove(list.size() - 1);
		}
		this.factors = Collections.unmodifiableList(list);
	}

	/**
	 * Returns order of this polynomial.
	 * 
	 * @return order of this polynomial
	 */
	public short order() {
		return (short) (factors.size() - 1);
	}

	/**
	 * Multiplies this polynomial with the given polynomial and returns the
	 * result. Throws {@link IllegalArgumentException} if given polynomial is
	 * null.
	 * 
	 * @param p
	 *            polynomial to multiply with
	 * @return polynomial as result of multiplying two polynomials
	 */
	public ComplexPolynomial multiply(ComplexPolynomial p) {
		if (p == null) {
			throw new IllegalArgumentException("Polynomial cannot be null.");
		}
		int length = factors.size() + p.factors.size() - 1;
		Complex[] newFactors = new Complex[length];
		for (int i = 0; i < newFactors.length; ++i) {
			newFactors[i] = Complex.ZERO;
		}
		for (int i = 0; i < factors.size(); ++i) {
			for (int j = 0; j < p.factors.size(); ++j) {
				int index = i + j;
				Complex f = factors.get(i).multiply(p.factors.get(j));
				newFactors[index] = newFactors[index].add(f);
			}
		}
		return new ComplexPolynomial(newFactors);
	}

	/**
	 * Computes first derivative of this polynomial.
	 * 
	 * @return first derivative
	 */
	public ComplexPolynomial derive() {
		int size = factors.size() - 1;
		if (size == 0) {
			size = 1;
		}
		Complex[] newFactors = new Complex[size];
		for (int i = 0; i < size; ++i) {
			newFactors[i] = Complex.ZERO;
		}
		for (int i = 0; i < size; ++i) {
			if (factors.size() == 1)
				break;
			newFactors[i] = factors.get(i + 1).multiply(new Complex(i + 1, 0));
		}
		return new ComplexPolynomial(newFactors);
	}

	/**
	 * Computes polynomial value at given point z. Throws
	 * {@link IllegalArgumentException} if given complex number is null.
	 * 
	 * @param z
	 *            complex point
	 * @return value of polynomial at given complex point
	 */
	public Complex apply(Complex z) {
		if (z == null) {
			throw new IllegalArgumentException("Complex number cannot be null.");
		}
		Complex value = Complex.ZERO;
		for (int i = 0; i < factors.size(); ++i) {
			Complex factor = factors.get(i);
			if (factor.equals(Complex.ZERO))
				continue;
			Complex power = z.power(i);
			value = value.add(factor.multiply(power));
		}
		return value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("f(z) = ");

		int first = factors.size() - 1;
		for (int i = first; i >= 0; --i) {
			Complex factor = factors.get(i);
			if (factor.equals(Complex.ZERO) && factors.size() > 1)
				continue;

			double real = factor.getReal();
			double im = factor.getImaginary();
			if (real == 0 || im == 0) {
				if ((real < 0 || im < 0) && i != first) {
					sb.append("  -  ");
					factor = factor.negate();
					real = factor.getReal();
					im = factor.getImaginary();
				} else if (i != first) {
					sb.append(" + ");
				}
				if (real == -1 || im == -1) {
					sb.append("-");
				} else if ((real != 1 && im != 1) || i == 0) {
					sb.append(factor);
				}
			} else {
				if (i != first) {
					sb.append(" + ");
				}
				sb.append("(" + factor + ")");
			}

			if (i == 1) {
				sb.append("z");
			} else if (i != 0) {
				sb.append("z^" + i);
			}
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((factors == null) ? 0 : factors.hashCode());
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
		ComplexPolynomial other = (ComplexPolynomial) obj;
		if (factors == null) {
			if (other.factors != null)
				return false;
		} else if (!factors.equals(other.factors))
			return false;
		return true;
	}

}
