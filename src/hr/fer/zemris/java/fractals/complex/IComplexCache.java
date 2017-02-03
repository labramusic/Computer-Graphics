package hr.fer.zemris.java.fractals.complex;

public interface IComplexCache {

	Complex get();
	Complex get(double real, double imaginary);
	Complex get(Complex template);
	void release(Complex c);

}
