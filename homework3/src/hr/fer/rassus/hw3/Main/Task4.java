package hr.fer.rassus.hw3.Main;

import com.perfdynamics.pdq.Job;
import com.perfdynamics.pdq.Methods;
import com.perfdynamics.pdq.Node;
import com.perfdynamics.pdq.PDQ;
import com.perfdynamics.pdq.QDiscipline;

public class Task4 {

	public static void main(final String[] args) {

		double lambdaMax = 8.2d;
		double lambdaStep = 0.2d;

		double[] v = new double[] { 1d, 2.5d, 0.5d, 0.75d, 1.25d, 0.72d / 0.94d, 0.52d / 0.94d };
		double[] S = new double[] { 0.003d, 0.001d, 0.01d, 0.04d, 0.1d, 0.13d, 0.15d };

		StringBuilder sb = new StringBuilder();
		sb.append("lambda;TanalytTOTAL");
		for (int i = 1; i < 8; ++i) {
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

		pdq.Init("Task4");

		pdq.CreateOpen("Requests", lambda);
		for (int i = 0; i < 7; ++i) {
			pdq.CreateNode("Channel" + i, Node.CEN, QDiscipline.FCFS);
			pdq.SetVisits("Channel" + i, "Requests", v[i], S[i]);
		}

		pdq.Solve(Methods.CANON);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 7; ++i) {
			sb.append(pdq.GetResidenceTime("Channel" + i, "Requests", Job.TRANS) + ";");
		}
		double T = pdq.GetResponse(Job.TRANS, "Requests");
		sb.append(T);
		return sb.toString();
	}
}
