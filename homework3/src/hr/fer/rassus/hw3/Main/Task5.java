package hr.fer.rassus.hw3.Main;

import com.perfdynamics.pdq.Job;
import com.perfdynamics.pdq.Methods;
import com.perfdynamics.pdq.Node;
import com.perfdynamics.pdq.PDQ;
import com.perfdynamics.pdq.QDiscipline;

public class Task5 {

	public static void main(final String[] args) {

		double lambdaMax = 8.2d;
		double lambdaStep = 0.2d;

		double a = 0.3d;
		double b = 0.45d;
		double c = 0.3d;

		double v1 = (a + (c * (1d - a))) / (1d - c);
		double v2 = b;
		double v3 = 1d - a - b;
		double v4 = v1 + v2 + v3;
		double v5 = 1d;

		double[] v = new double[] { v1, v2, v3, v4, v5 };
		double[] S = new double[] { 0.01d, 0.05d, 0.5d, 0.2d, 0.1d };

		System.out.println(1d / ((v1) * S[0]));
		System.out.println(1d / ((v2) * S[1]));
		System.out.println(1d / ((v3) * S[2]));
		System.out.println(1d / ((v4) * S[3]));
		System.out.println(1d / ((v5) * S[4]));

		StringBuilder sb = new StringBuilder();
		sb.append("lambda;TanalytTOTAL");
		for (int i = 1; i < 6; ++i) {
			sb.append(";Tpdq" + i);
		}
		sb.append(";TpdqTOTAL");

		System.out.println(sb.toString());

		for (double lambda = 0.2d; lambda < (lambdaMax + 1e-6); lambda += lambdaStep) {
			double Ta = analyticCalculation(lambda, v, S);

			String Tpdq = PDQCalculation(lambda, v, S);

			String line = new String(lambda + ";" + Ta + ";" + Tpdq);
			line = line.replaceAll("\\.", ",");

			System.out.println(line);
		}

	}

	private static double analyticCalculation(final double lambda, final double[] v, final double[] S) {
		double[] N = new double[v.length];
		double Nsum = 0d;
		for (int i = 0; i < N.length; ++i) {
			double ro = S[i] * v[i] * lambda;
			N[i] = ro / (1 - ro);
			Nsum += N[i];
		}
		double T = Nsum / lambda;
		return T;
	}

	private static String PDQCalculation(final double lambda, final double[] v, final double[] S) {
		PDQ pdq = new PDQ();

		pdq.Init("Task5");

		pdq.CreateOpen("Requests", lambda);
		for (int i = 0; i < 5; ++i) {
			pdq.CreateNode("Channel" + i, Node.CEN, QDiscipline.FCFS);
			pdq.SetVisits("Channel" + i, "Requests", v[i], S[i]);
		}

		pdq.Solve(Methods.CANON);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; ++i) {
			sb.append(pdq.GetResidenceTime("Channel" + i, "Requests", Job.TRANS) + ";");
		}
		double T = pdq.GetResponse(Job.TRANS, "Requests");
		sb.append(T);
		return sb.toString();

//		ro = lambda * S
//				lambda * s mora bit 1, 0.25 * lambda * s mora biti 1
//				s = 0.2, lambda  =
	}
}
