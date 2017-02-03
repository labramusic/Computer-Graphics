package hr.fer.zemris.java.fractals;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import hr.fer.zemris.java.fractals.complex.Complex;
import hr.fer.zemris.java.fractals.complex.ComplexPolynomial;
import hr.fer.zemris.java.fractals.complex.ComplexRootedPolynomial;
import hr.fer.zemris.java.fractals.viewer.FractalViewer;
import hr.fer.zemris.java.fractals.viewer.IFractalProducer;
import hr.fer.zemris.java.fractals.viewer.IFractalResultObserver;

/**
 * Program which waits for user to input complex roots until "done" is typed.
 * Graphical representation of fractal derived from the Newton-Raphson method is
 * drawn on the screen using the input roots. For more information on the used
 * method, see "http://www.chiark.greenend.org.uk/~sgtatham/newton/". The
 * drawing is parallelized using FixedThreadPool and Callable objects.
 * 
 * @author labramusic
 *
 */
public class Newton {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		System.out.println("Welcome to Newton-Raphson iteration-based fractal viewer.");
		System.out.println("Please enter at least two roots, one root per line. Enter 'done' when done.");
		Scanner sc = new Scanner(System.in);
		List<Complex> roots = new ArrayList<>();
		int i = 0;
		Complex root;
		while (true) {
			++i;
			System.out.print("Root " + i + "> ");
			String line = sc.nextLine();
			if (line.equals("done")) {
				sc.close();
				break;
			}
			try {
				root = Complex.parse(line);
				roots.add(root);
			} catch (IllegalArgumentException e) {
				System.out.println("Couldn't parse root. Please try again.");
				--i;
			}
		}
		System.out.println("Image of fractal will appear shortly. Thank you.");
		Complex[] rootsArray = roots.toArray(new Complex[roots.size()]);
		ComplexRootedPolynomial crp = new ComplexRootedPolynomial(rootsArray);
		System.out.println(crp);
		// start fractal viewer and display the fractal
		FractalViewer.show(new FractalProducerImpl(crp));
	}

	/**
	 * A factory of daemon threads.
	 * 
	 * @author labramusic
	 *
	 */
	public static class DaemonicThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
	}

	/**
	 * Worker thread which draws a certain part of the image.
	 * 
	 * @author labramusic
	 *
	 */
	public static class WorkerThread implements Callable<Void> {

		/**
		 * Minimum real value.
		 */
		double reMin;

		/**
		 * Maximum real value.
		 */
		double reMax;

		/**
		 * Minimum imaginary value.
		 */
		double imMin;

		/**
		 * Maximum imaginary value.
		 */
		double imMax;

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
		 * Maximum number of iterations.
		 */
		int m = 16 * 16 * 16;

		/**
		 * The convergence threshold.
		 */
		double convergenceThreshold = 1e-3;

		/**
		 * The root value threshold.
		 */
		double rootThreshold = 2e-3;

		/**
		 * Graphical data.
		 */
		short[] data = new short[width * height];

		/**
		 * The polynomial used while drawing fractals.
		 */
		private ComplexRootedPolynomial polynomial;

		/**
		 * Initializes a new worker thread.
		 * 
		 * @param reMin
		 *            minimum real value
		 * @param reMax
		 *            maximum real value
		 * @param imMin
		 *            minimum imaginary value
		 * @param imMax
		 *            maximum imaginary value
		 * @param width
		 *            screen width
		 * @param height
		 *            screen height
		 * @param yMin
		 *            minimum y value
		 * @param yMax
		 *            maximum y value
		 * @param m
		 *            maximum number of iterations
		 * @param convergenceThreshold
		 *            convergence threshold
		 * @param rootThreshold
		 *            root threshold
		 * @param data
		 *            graphical data
		 * @param polynomial
		 *            polynomial used for drawing
		 */
		public WorkerThread(double reMin, double reMax, double imMin, double imMax, int width, int height, int yMin,
				int yMax, int m, double convergenceThreshold, double rootThreshold, short[] data,
				ComplexRootedPolynomial polynomial) {
			super();
			this.reMin = reMin;
			this.reMax = reMax;
			this.imMin = imMin;
			this.imMax = imMax;
			this.width = width;
			this.height = height;
			this.yMin = yMin;
			this.yMax = yMax;
			this.m = m;
			this.convergenceThreshold = convergenceThreshold;
			this.rootThreshold = rootThreshold;
			this.data = data;
			this.polynomial = polynomial;
		}

		@Override
		public Void call() throws Exception {
			ComplexPolynomial derived = polynomial.toComplexPolynom().derive();
			int offset = yMin * width;
			for (int y = yMin; y <= yMax; y++) {
				for (int x = 0; x < width; x++) {
					double cre = x / (width - 1.0) * (reMax - reMin) + reMin;
					double cim = (height - 1.0 - y) / (height - 1) * (imMax - imMin) + imMin;
					Complex zn = new Complex(cre, cim);
					Complex zn1;
					double module = 0;
					int iters = 0;
					do {
						Complex numerator = polynomial.apply(zn);
						Complex denominator = derived.apply(zn);
						Complex fraction = numerator.divide(denominator);
						zn1 = zn.sub(fraction);
						module = zn1.sub(zn).module();
						zn = zn1;
						++iters;
					} while (iters < m && module > convergenceThreshold);
					int index = polynomial.indexOfClosestRootFor(zn1, rootThreshold);
					if (index == -1) {
						data[offset++] = 0;
					} else {
						data[offset++] = (short) (index + 1);
					}
				}
			}
			return null;
		}
	}

	/**
	 * An implementation of {@link IFractalProducer} which draws fractals based
	 * on Newton-Raphson iteration.
	 * 
	 * @author labramusic
	 *
	 */
	public static class FractalProducerImpl implements IFractalProducer {

		/**
		 * The polynomial used while drawing fractals.
		 */
		private ComplexRootedPolynomial polynomial;

		/**
		 * Thread pool.
		 */
		private ExecutorService pool;

		/**
		 * The number of processors available.
		 */
		private int numberOfProcessors;

		/**
		 * Initializes a new fractal producer with the given
		 * {@link ComplexRootedPolynomial}.
		 * 
		 * @param polynomial
		 *            polynomial to be drawn
		 */
		public FractalProducerImpl(ComplexRootedPolynomial polynomial) {
			this.polynomial = polynomial;
			numberOfProcessors = Runtime.getRuntime().availableProcessors();
			ThreadFactory factory = new DaemonicThreadFactory();
			pool = Executors.newFixedThreadPool(numberOfProcessors, factory);
		}

		@Override
		public void produce(double reMin, double reMax, double imMin, double imMax, int width, int height,
				long requestNo, IFractalResultObserver observer) {
			System.out.println("Zapocinjem izracun...");
			int m = 16 * 16 * 16;
			double convergenceThreshold = 1e-3;
			double rootThreshold = 2e-3;
			short[] data = new short[width * height];
			final int numOfStrips = 8 * numberOfProcessors;
			int numOfYByStrip = height / numOfStrips;

			List<Future<Void>> results = new ArrayList<>();

			for (int i = 0; i < numOfStrips; i++) {
				int yMin = i * numOfYByStrip;
				int yMax = (i + 1) * numOfYByStrip - 1;
				if (i == numOfStrips - 1) {
					yMax = height - 1;
				}
				WorkerThread work = new WorkerThread(reMin, reMax, imMin, imMax, width, height, yMin, yMax, m,
						convergenceThreshold, rootThreshold, data, polynomial);
				results.add(pool.submit(work));
			}
			for (Future<Void> work : results) {
				try {
					work.get();
				} catch (InterruptedException | ExecutionException e) {
				}
			}

			System.out.println("Racunanje gotovo. Idem obavijestiti promatraca tj. GUI!");
			observer.acceptResult(data, (short) (polynomial.toComplexPolynom().order() + 1), requestNo);
		}
	}

}
