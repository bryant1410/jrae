package classify;

import math.*;

import org.jblas.*;

import util.*;

public class SoftmaxCost implements DifferentiableFunction
{
	DoubleMatrix Features, Labels;
	DifferentiableMatrixFunction Activation;
	ClassifierTheta Gradient;
	double Lambda;
	int CatSize, FeatureLength;
	
	public SoftmaxCost(DoubleMatrix Features ,int[] Labels, int CatSize, double Lambda)
	{
		this.Labels = DoubleMatrix.zeros(Labels.length, CatSize);
		for(int i=0; i<Labels.length; i++)
		{
			if( Labels[i] < 0 || Labels[i] > CatSize )
				System.err.println("Bad Data : " + Labels[i] + " | " + i);
			if( Labels[i] > 0 && Labels[i] <= CatSize )
				this.Labels.put(i,Labels[i]-1,1);
		}
		this.Features = Features;
		this.Lambda = Lambda;
		this.CatSize = CatSize;
		this.FeatureLength = Features.rows;
		if( CatSize > 1 )
			Activation = new Softmax();
		else
			Activation = new Sigmoid();
		Gradient = null;
	}
	
	public SoftmaxCost(DoubleMatrix Features ,DoubleMatrix Labels,double Lambda)
	{
		this.Features = Features;
		this.Lambda = Lambda;
		this.Labels = Labels;
		this.CatSize = Features.columns;
		this.FeatureLength = Features.rows;
		if( CatSize > 1 )
			Activation = new Softmax();
		else
			Activation = new Sigmoid();
		Gradient = null;
	}
	
	@Override
	public int dimension() 
	{
		return CatSize * FeatureLength + CatSize;
	}

	@Override
	public double valueAt(double[] x) 
	{
		ClassifierTheta Theta = new ClassifierTheta(x,FeatureLength,CatSize);
		
		DoubleMatrix Sigmoid = Activation.valueAt(((Theta.W.transpose()).mmul(Features)).addColumnVector(Theta.b));
		DoubleMatrix Diff = Sigmoid.sub(Labels.transpose());
		
		double MeanTerm = 1.0/ Features.columns;
		double Cost = 0.5 * DoubleMatrixFunctions.SquaredNorm(Diff) * MeanTerm; 
		double RegularisationTerm = 0.5 * Lambda * DoubleMatrixFunctions.SquaredNorm(Theta.W);
		
	    DoubleMatrix Delta = Diff.mul( Activation.derivativeAt(Sigmoid) );
	    
	    DoubleMatrix gradW = ((Delta.mmul( Features.transpose() )).mul(MeanTerm)).transpose();
	    gradW = gradW.addi(Theta.W.mul(Lambda));
	    DoubleMatrix gradb = (Delta.rowSums()).mul(MeanTerm);
		
	    //System.out.println(Delta.columnSums());
	    
	    Gradient = new ClassifierTheta(gradW,gradb);
	    
	    //System.out.println("SoftmaxCost : " + (Cost + RegularisationTerm));
		return Cost + RegularisationTerm;
	}

	@Override
	public double[] derivativeAt(double[] x) 
	{
		if(Gradient == null)
			System.err.println("Derivation query before value query");
		double[] theta = Gradient.Theta;
		Gradient = null;
		return theta;
	}
}