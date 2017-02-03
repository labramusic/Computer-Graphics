package hr.fer.zemris.java.fractals.complex;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an immutable complex number.
 * 
 * @author labramusic
 *
 */
public class Complex {

	/**
	 * Complex number with value of 0.
	 */
	public static final Complex ZERO = new Complex(0, 0);

	/**
	 * Complex number with value of 1.
	 */
	public static final Complex ONE = new Complex(1, 0);

	/**
	 * Complex number with value of -1.
	 */
	public static final Complex ONE_NEG = new Complex(-1, 0);

	/**
	 * Complex number with value of i.
	 */
	public static final Complex IM = new Complex(0, 1);

	/**
	 * Complex number with value of -i.
	 */
	public static final Complex IM_NEG = new Complex(0, -1);

	/**
	 * Real part of the complex number.
	 */
	private double real;

	/**
	 * Imaginary part of the complex number.
	 */
	private double imaginary;

	/**
	 * Magnitude of the complex number.
	 */
	private double magnitude;

	/**
	 * Angle in radians, from 0 to 2 Pi.
	 */
	private double angle;

	/**
	 * Initializes a new complex number with the default value of zero.
	 */
	public Complex() {
		this(0, 0);
	}

	/**
	 * Initializes a new complex number out of its real and imaginary parts. The
	 * magnitude and angle are automatically calculated.
	 * 
	 * @param re
	 *            real part of complex number
	 * @param im
	 *            imaginary part of complex number
	 */
	public Complex(double re, double im) {
		this.real = re;
		this.imaginary = im;
		magnitude = Math.sqrt(real * real + imaginary * imaginary);
		// PI is added so the angle is between 0 and 2 PI
		angle = Math.atan2(imaginary, real) + Math.PI;
	}

	/**
	 * Gets the real part of the complex number.
	 *
	 * @return the real part of the complex number
	 */
	public double getReal() {
		return real;
	}

	/**
	 * Gets the imaginary part of the complex number.
	 *
	 * @return the imaginary part of the complex number
	 */
	public double getImaginary() {
		return imaginary;
	}

	/**
	 * Calculates a new complex number from the given magnitude and angle.
	 * 
	 * @param magnitude
	 *            magnitude of the complex number
	 * @param angle
	 *            angle of the complex number
	 * @return complex number with the given magnitude and angle
	 */
	public static Complex fromMagnitudeAndAngle(double magnitude, double angle) {
		double real = magnitude * Math.cos(angle);
		double imaginary = magnitude * Math.sin(angle);
		return new Complex(real, imaginary);
	}

	/**
	 * Returns module of complex number.
	 * 
	 * @return module of complex number
	 */
	public double module() {
		return Math.sqrt(Math.pow(real, 2) + Math.pow(imaginary, 2));
	}

	/**
	 * Multiplies two complex numbers. Throws {@link IllegalArgumentException}
	 * if given number is null.
	 * 
	 * @param c
	 *            complex number to be multiplied with the first
	 * @return result of multiplication as a new complex number
	 */
	public Complex multiply(Complex c) {
		if (c == null) {
			throw new IllegalArgumentException("Multiplicand cannot be null.");
		}
		double real = this.real * c.real - this.imaginary * c.imaginary;
		double imaginary = this.real * c.imaginary + this.imaginary * c.real;
		return new Complex(real, imaginary);
	}

	/**
	 * Divides the first complex number with the given complex number. Throws
	 * {@link ArithmeticException} in case of division by zero. Throws
	 * {@link IllegalArgumentException} if given number is null.
	 * 
	 * @param c
	 *            complex number which divides the first
	 * @return result of division as a new complex number
	 */
	public Complex divide(Complex c) {
		if (c == null) {
			throw new IllegalArgumentException("Divisor cannot be null.");
		}
		double denominator = c.real * c.real + c.imaginary * c.imaginary;
		if (denominator == 0) {
			throw new ArithmeticException("Cannot divide by zero.");
		}
		double real = (this.real * c.real + this.imaginary * c.imaginary) / denominator;
		double imaginary = (this.imaginary * c.real - this.real * c.imaginary) / denominator;
		return new Complex(real, imaginary);
	}

	/**
	 * Adds two complex numbers. Throws {@link IllegalArgumentException} if
	 * given number is null.
	 * 
	 * @param c
	 *            complex number being added to the first
	 * @return result of addition as new complex number
	 */
	public Complex add(Complex c) {
		if (c == null) {
			throw new IllegalArgumentException("Sumand cannot be null.");
		}
		double real = this.real + c.real;
		double imaginary = this.imaginary + c.imaginary;
		return new Complex(real, imaginary);
	}

	/**
	 * Subtracts the given complex number from the first. Throws
	 * {@link IllegalArgumentException} if given number is null.
	 * 
	 * @param c
	 *            complex number being subtracted from the first
	 * @return result of subtraction as a new complex number
	 */
	public Complex sub(Complex c) {
		if (c == null) {
			throw new IllegalArgumentException("Subtrahend cannot be null.");
		}
		double real = this.real - c.real;
		double imaginary = this.imaginary - c.imaginary;
		return new Complex(real, imaginary);
	}

	/**
	 * Negates the complex number.
	 * 
	 * @return negated complex number
	 */
	public Complex negate() {
		return new Complex(-real, -imaginary);
	}

	/**
	 * Calculates the nth power of a complex number. Throws
	 * IllegalArgumentException if n is less than zero.
	 * 
	 * @param n
	 *            the power to which the number should be raised
	 * @return result of the nth power as a new complex number
	 */
	public Complex power(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("The exponent n must be a number equal to or higher than zero.");
		}
		Complex result = Complex.ONE;
		for (int i = 0; i < n; ++i) {
			result = result.multiply(this);
		}
		return result;
	}

	/**
	 * Calculates the nth roots of a complex number. Throws
	 * IllegalArgumentException if n is less than zero.
	 * 
	 * @param n
	 *            number of roots to be calculated
	 * @return nth roots of the given number as an array of complex numbers
	 */
	public List<Complex> root(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("The exponent n must be a number equal to or higher than zero.");
		}
		List<Complex> roots = new ArrayList<>();
		double magnitude = Math.pow(this.magnitude, 1.0 / n);
		for (int k = 0; k < n; ++k) {
			double angle = (this.angle + 2.0 * k * Math.PI) / n;
			roots.add(fromMagnitudeAndAngle(magnitude, angle));
		}
		return roots;
	}

	/**
	 * Parses the given string for a complex number. Accepts strings such as:
	 * "3.51", "-3.17", "-i2.71", "i", "1", "-2.71 - i3.15", "2 + i".
	 * 
	 * @param s
	 *            string representation of the complex number
	 * @return complex number parsed from the given string
	 */
	public static Complex parse(String s) {
		Pattern pattern = Pattern
				.compile("([+-]?\\d*\\.?\\d+) ?([+-] ?i(\\d*\\.?\\d+)?)|" + "([+-]?\\d*\\.?\\d+)|(-?i(\\d*\\.?\\d+)?)");
		Matcher matcher = pattern.matcher(s);
		if (!matcher.matches()) {
			throw new IllegalArgumentException();

		} else {
			double real = 0;
			double imaginary = 0;

			if (matcher.group(1) != null) {
				// real and imaginary part are not zero
				real = Double.parseDouble(matcher.group(1));

			} else if (matcher.group(4) != null) {
				// real part is not zero, imaginary is
				real = Double.parseDouble(matcher.group(4));
			}

			if (matcher.group(2) != null) {
				// real and imaginary part are not zero
				String expression = matcher.group(2).replaceFirst("i", "");
				expression = expression.replaceAll(" ", "").trim();
				if (expression.equals("+")) {
					// i
					imaginary = 1;
				} else if (expression.equals("-")) {
					// -i
					imaginary = -1;
				} else {
					imaginary = Double.parseDouble(expression);
				}

			} else if (matcher.group(5) != null) {
				// imaginary part is not zero, real is
				String expression = matcher.group(5).replaceFirst("i", "");
				expression = expression.replaceAll(" ", "").trim();
				if (expression.isEmpty()) {
					// i
					imaginary = 1;
				} else if (expression.equals("-")) {
					// -i
					imaginary = -1;
				} else {
					imaginary = Double.parseDouble(expression);
				}
			}

			return new Complex(real, imaginary);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		DecimalFormat formatter = new DecimalFormat("#.##");
		if (real == 0 && imaginary == 0) {
			sb.append(0);
		}
		if (real != 0) {
			sb.append(formatter.format(real));
		}
		if (imaginary != 0) {
			if (imaginary > 0 && real != 0) {
				sb.append(" + ");
			} else if (imaginary < 0 && real != 0) {
				sb.append(" - ");
			} else if (imaginary < 0 && real == 0) {
				sb.append("-");
			}
			sb.append("i");
			if (imaginary != 1 && imaginary != -1) {
				if (imaginary < 0) {
					imaginary *= -1;
				}
				sb.append(formatter.format(imaginary));
			}
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(imaginary);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(real);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Complex other = (Complex) obj;
		if (Double.doubleToLongBits(imaginary) != Double.doubleToLongBits(other.imaginary))
			return false;
		if (Double.doubleToLongBits(real) != Double.doubleToLongBits(other.real))
			return false;
		return true;
	}

}
