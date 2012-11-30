import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Andy Fang
 * April 28, 2009
 * Description:
 *      This class implements a feed-forward multilayer neural network that
 *      implements the backpropagation algorithm. This class has two different 
 *      constructors: one for training the perceptron and one for configuring
 *      the perceptron to predetermined weights. The constructor that takes in
 *      training sets the perceptron takes in a set of input vectors
 *      [trainingInputs] (vectors are represented as arrays of doubles in this
 *      class) and a set of target output vectors [targetOutputs], and the
 *      weights [weights]. The other constructor only takes in the weights
 *      [weights]. The input layer takes in a set of input values and alters
 *      each value with weights and biases. In the hidden layer, each neuron
 *      takes in a weighted sum of all the values directed to it and performs a
 *      nonlinear function on it. The output layer is simply the last hidden
 *      layer. The weighted sum that takes place in between layers is performed
 *      in the outputActivation method. The nonlinear function performed, also
 *      called the threshold function, is seen in the thresholdFunction method
 *      in this class. The training algorithm for the backpropagation perceptron
 *      is explained in the updateWeights method.
 *      
 *      WARNING: If you are going to change the thresholdFunction, you may have
 *      to change the derivativeOfFunction as well.
 */




public class BackPropPerceptron
{
    // the first index of the trainingInputs and the index of the targetOutputs
    // match the same training set
    
    private double[][] trainingInputs;
        // array of input vectors from the training sets
    private double[][] targetOutputs;
        // array of output vectors from the training sets
    
    private int numLayers;
    private double[][][] weights; 
        // indexing based on the input layer (e.g. weights[0] refers to the
        // weight layer between the input layer and the 1st hidden layer)
    
    private double[][][] weightChanges;
    private double[][] psiFunc;                   
        // saves psi function values for each node in the perceptron
    private double[][] nodes;                     
        // saves the values of the perceptron's nodes
    
    public static final double TRAINING_OFFSET = .7;
    public static final double MAX_ALLOWED_ERROR = .01;
    public static final int MAX_PEL_VAL = 16777215;
    
    /*
     * This main class creates the training sets and weight sets and initializes
     * a perceptron.
     * 
     * There are two ways to run the main program:
     * 1) If you do not have weights initialized and want to train a weight set
     * and then run the perceptron on a test. The weights will be outputted to 
     * file (weights).
     * To do this, comment out sections that are labeled TRAIN-AND-RUN.
     * 2) You already have weights and simply want to run the perceptron. To do
     * this, comment out sections that are labeled JUST-RUN.
     */
    public static void main(String[] args) throws IOException
    {
        FileInputStream fstream = new FileInputStream("weights");
        FileOutputStream fstream1 = new FileOutputStream("weights");
        
        /* begin JUST-RUN */
        DataInputStream in = new DataInputStream(fstream);
        /* end JUST-RUN */
        
        /* begin TRAIN-AND-RUN */
        DataOutputStream out = new DataOutputStream(fstream1);
        /* end TRAIN-AND-RUN */
        
        
        //initializing a letter-recognizing perceptron
        double[][] trainingInputs   = new double[26][64];
        double[][] trainingOutputs  = new double[26][26];
        ImageProcessor imgProcessor = new ImageProcessor();
        
        for(int i = (int)'A'; i <= (int)'Z'; i++)
        {
            BitmapProcessor bmpProcessor = new BitmapProcessor(
                    "letter" + (char)i + ".bmp");

            int[] flatImg = imgProcessor.flattenImage(bmpProcessor.getImage());
            trainingInputs[i - (int)'A'] = new double[flatImg.length];
            
            for(int j = 0; j < flatImg.length; j++)
            {
                trainingInputs[i - (int)'A'][j] = (double)((double)flatImg[j] / 
                        (double)MAX_PEL_VAL);
            }        
            
            trainingOutputs[i - (int)'A'][i - (int)'A'] = 1.0;
        }
        
        
        /* begin TRAIN-AND-RUN */
        double[][][] weights = new double[2][][];
        weights[0] = new double[64][35];
        weights[1] = new double[35][26];
        /* end TRAIN-AND-RUN */

        /* begin JUST-RUN */
//        weights = new double[in.readInt()][][];
//        
//        for(int i = 0; i < weights.length; i++)
//        {
//            weights[i] = new double[in.readInt()][in.readInt()];
//        }
//        
//        for(int i = 0; i < weights.length; i++)
//        {
//            for(int j = 0; j < weights[i].length; j++)
//            {
//                for(int k = 0; k < weights[i][j].length; k++)
//                {
//                    weights[i][j][k] = in.readDouble();
//                }
//            }
//        }
        /* end JUST-RUN */
        
        BackPropPerceptron p = new BackPropPerceptron(
                trainingInputs, trainingOutputs, weights);
        
        /* begin TRAIN-AND-RUN */
        p.train();
        /* end TRAIN-AND-RUN */
        
        /* Writing out the weights into an output file */
        weights = p.getWeights();
        out.writeInt(weights.length);
        for(int i = 0; i < weights.length; i++)
        {
            out.writeInt(weights[i].length);
            out.writeInt(weights[i][0].length);
        }
        for(int i = 0; i < weights.length; i++)
        {
            for(int j = 0; j < weights[i].length; j++)
            {
                for(int k = 0; k < weights[i][j].length; k++)
                {
                    out.writeDouble(weights[i][j][k]);
                }
            }
        }
        /* end of writing weights to file */
        
        
        BitmapProcessor bmpProcessor = new BitmapProcessor("letterA.bmp");
        int[] flatImg = imgProcessor.flattenImage(bmpProcessor.getImage());
        double[] input = new double[flatImg.length];
        
        for(int j = 0; j < flatImg.length; j++)
        {
            input[j] = (double)((double)flatImg[j]/(double)MAX_PEL_VAL);
        } 
        double[] output = p.evaluate(input);
        
        for(int i = 0; i < output.length; i++)
        {
            System.out.println(output[i]);
        }
        
        in.close();
        out.close();
        
        
        
        
        //initializing an XOR perceptron
        /*
        double[][] trainingInputs = new double[4][2];
        trainingInputs[0][0] = 0;
        trainingInputs[0][1] = 0;
        trainingInputs[1][0] = 0;
        trainingInputs[1][1] = 1;
        trainingInputs[2][0] = 1;
        trainingInputs[2][1] = 0;
        trainingInputs[3][0] = 1;
        trainingInputs[3][1] = 1;
        
        double[][] trainingOutputs = new double[4][1];
        trainingOutputs[0][0] = 0;
        trainingOutputs[1][0] = 1;
        trainingOutputs[2][0] = 1;
        trainingOutputs[3][0] = 0;
        
        double[][][] weights = new double[2][][];
        weights[0] = new double[2][2];
        weights[1] = new double[2][1];
        
        BackPropPerceptron p = new BackPropPerceptron(trainingInputs, trainingOutputs, weights);
        
        p.updateWeights();
        System.out.println(p.evaluate(trainingInputs[0])[0]);
        System.out.println(p.evaluate(trainingInputs[1])[0]);
        System.out.println(p.evaluate(trainingInputs[2])[0]);
        System.out.println(p.evaluate(trainingInputs[3])[0]);
        */
    }
    
    /*
     * Parameters: training inputs, training outputs, and a weight set
     * Function:   The constructor takes in the training sets and the perceptron
     *             while initializing the weights via initializeWeights.
     */
    public BackPropPerceptron(double[][] trainingIn, double[][] trainingOut, double[][][] w)
    {
        trainingInputs = trainingIn;
        targetOutputs  = trainingOut;
        weights = w;
        numLayers = weights.length + 1;
            // number of layers is the number of weights between layers + 1
        psiFunc = new double[numLayers][];
        nodes = new double[numLayers][];
        
        for(int i = 0; i < numLayers-1; i++)
        {
            psiFunc[i] = new double[weights[i].length];
            // number of nodes in input layer going into weights[i] weight layer
            
            nodes[i] = new double[weights[i].length];
        }
        psiFunc[numLayers-1] = new double[weights[weights.length-1][0].length];
        nodes[numLayers-1] = new double[weights[weights.length-1][0].length];
            // # of nodes in last layer equals the # of output nodes in last 
            // weight layer
    
        initializeWeights();
    }

    /*
     * Parameters: a weight set
     * Function:   The constructor takes in a weight set, which internally
     *             contains the perceptron dimensions. This constructor should
     *             only be used if the weights are pre-calculated since the
     *             weights are not initialized and the training sets are null.
     *           
     */
    public BackPropPerceptron(double[][][] w)
    {
        weights = w;
        numLayers = weights.length + 1;
            //number of layers is the number of weights between layers + 1
        
        psiFunc = new double[numLayers][];
        nodes = new double[numLayers][];
        
        for(int i = 0; i < numLayers-1; i++)
        {
            psiFunc[i] = new double[weights[i].length]; 
                // # of nodes in input layer going into weights[i] weight layer
            
            nodes[i] = new double[weights[i].length];
        }
        psiFunc[numLayers-1] = new double[weights[weights.length-1][0].length];
        nodes[numLayers-1] = new double[weights[weights.length-1][0].length];
    }
    
    /*
     * Parameters: n/a
     * Function: Initializes the weights to random values between 0 and 1.
     *           Also initializes the weightChanges array
     */
    private void initializeWeights()
    {
        weightChanges = new double[weights.length][][];
        
        for(int i = 0; i < weights.length; i++)
        {
            weightChanges[i] = new double[weights[i].length][];
            for(int j = 0; j < weights[i].length; j++)
            {
                weightChanges[i][j] = new double[weights[i][j].length];
                for(int k = 0; k < weights[i][j].length; k++)
                {
                    weightChanges[i][j][k] = 0;
                    weights[i][j][k] = Math.random();
                }
            }
        }
    }
    
    /*
     * Parameters: none
     * Function: returns the set of weights the perceptron currently uses
     */
    public double[][][] getWeights()
    {
        return weights;
    }
     
    /*
     * Parameters: the training inputs, the training outputs, and weight set
     * Function:   This method trains the perceptron by applying the error
     *             function to it and adjusting the weights accordingly until
     *             the perceptron converges. For this function, the definition
     *             of converging is determined by the constant
     *             MAX_ALLOWED_ERROR; if the error falls below this constant,
     *             then the perceptron is considered to have converged.
     */
    public void train()
    {
        double err = error();
        while(err > MAX_ALLOWED_ERROR)
        {
            System.out.println("error = " + err);
            
            updateWeights();
            err = error();
        }
    }
    
    /*
     * Information: This method essentially implements entire backpropagation
     *              algorithm. First, the method iterates through each input
     *              training set and evaluates it using the weights that the
     *              perceptron already has. Then, it calculates the psi function
     *              (which is stored in the psiFunc array) for each node in the
     *              perceptron; the purpose of the psi function will be
     *              discussed later. The formula for the output layer of the
     *              perceptron is, where i iterates through the output layer and
     *              j iterates through the layer before the output layer and h_j
     *              means the hidden layer node indexed j in the layer before
     *              the output layer and w_ji means the weight coming from node
     *              j in the hidden layer to node i in the output layer:
     *              
     *              psi_i = (targetOutput_i - calculatedOutput_i) *
     *                  derivativeOfThreshold(summation_j(h_j*w_ji)).
     *              
     *              The psi function for the rest of the layers is defined
     *              slightly differently, given that k iterates through the
     *              layer before the current layer and j iterates through the
     *              current layer and i iterates through the layer after the
     *              current layer and a_k is a node in the previous layer:
     *              
     *              psi_j = derivativeOfThreshold(summation_k(a_k*w_kj)) *
     *                  summation_i(psi_i*w_ji).
     *              
     *              Now, after all the node's respective psi functions have been
     *              calculated, the delta weights for every weight going from
     *              node j in one layer to node i in the next layer can be
     *              calculated (which are stored in the weightChanges array):
     *              
     *              deltaW_ji = trainingOffset * node_j * psi_i.
     *              
     *              After all the delta weights are calculated for one training
     *              input, then they are applied to the weights, and the method
     *              goes through the next training input.
     *              
     * Parameters:  none
     * Function:    Calculates the values in the weightChanges array and the
     *              psiFunc array using the formulae in the back-prop algorithm.
     *              Then, the weightChanges are applied to the weights.
     */
    public void updateWeights()
    {
        double[][] tempNodes;
        
        for(int i = 0; i < trainingInputs.length; i++)
        {
            tempNodes = evaluateNodes(trainingInputs[i]);
            
          // calculating psi function for last layer
          for(int out = 0; out < tempNodes[tempNodes.length-1].length; out++)
          { // iterating through last layer
              double weightedSum = 0;
              for(int in = 0; in < tempNodes[tempNodes.length-2].length; in++)
              { // iterating through layer before last layer
                  weightedSum += tempNodes[tempNodes.length-2][in] * 
                          weights[weights.length-1][in][out];
                  // calculating weighted sum from layer k going into node j
                  // in output layer
              }
              psiFunc[numLayers-1][out] = (targetOutputs[i][out] - 
                      tempNodes[tempNodes.length-1][out]) * 
                      derivativeOfThreshold(weightedSum);
          }
          
          // calculating psi function for rest of perceptron
          for(int layer = numLayers-2; layer > 0; layer--)
          {
              for(int curr = 0; curr < tempNodes[layer].length; curr++)
              {
                  double weightedSum = 0;
                  double psiSum = 0;
                  
                  for(int next = 0; next < tempNodes[layer+1].length; next++)
                  {
                      psiSum += psiFunc[layer+1][next] *
                              weights[layer][curr][next];
                  }
                  
                  for(int prev = 0; prev < tempNodes[layer-1].length; prev++)
                  {
                      weightedSum += tempNodes[layer-1][prev] *
                              weights[layer-1][prev][curr];
                  }
                  psiFunc[layer][curr] = derivativeOfThreshold(weightedSum) * 
                          psiSum;
              }
          }

          
          // calculating weightChanges for the entire perceptron
          for(int layer = 0; layer < weights.length; layer++)
          {
              for(int inp = 0; inp < weights[layer].length; inp++)
              {
                  for(int out = 0; out < weights[layer][inp].length; out++)
                  {
                      weightChanges[layer][inp][out] = TRAINING_OFFSET * 
                              tempNodes[layer][inp] * psiFunc[layer+1][out];
                  }
              }
          }
          
          // applying weightChanges to the perceptron
          for(int layer = 0; layer < weights.length; layer++)
          {
              for(int inp = 0; inp < weights[layer].length; inp++)
              {
                  for(int out = 0; out < weights[layer][inp].length; out++)
                  {
                      int l = layer;
                      weights[l][inp][out] += weightChanges[l][inp][out];
                  }
              }
          }
       } // end of looping through i - the index for each training vector
        
    } // end of updateWeights

    
    /*
     * Parameters: one input vector
     * Function: Given an input layer, this method uses the outputActivation
     * method to calculate each hidden layer and the output layer. The last call
     * of the outputActivation method returns the output of the perceptron.
     */
     public double[] evaluate(double[] inputVector)
     {
         // initialization
         double[] inLayer = inputVector;
         double[] outLayer = null;
         
         for(int i = 0; i < weights.length; i++)
         {
             outLayer = outputActivation(inLayer, weights[i]);
             inLayer = outLayer;
         }
        
         return outLayer;
     }
    
    /*
     * Parameters: one input vector
     * Function: Given an input layer, this method uses the outputActivation
     * method to calculate each hidden layer and the output layer. The last call
     * of the outputActivation method returns the output of the perceptron.
     * However, this method returns all the nodes of the perceptron. This method
     * is used to calculate elements in the psiFunc and weightChanges arrays in
     * the updateWeights method.
     */
    private double[][] evaluateNodes(double[] inputVector)
    {
        // initialization
        double[][] output = new2DDoubleArray(nodes);
        double[]  inLayer = inputVector;
        double[] outLayer = output[output.length-1];
        
        for(int i = 0; i < weights.length; i++)
        {
            output[i] = inLayer;
            outLayer = outputActivation(inLayer, weights[i]);
            inLayer = outLayer;
            output[i+1] = outLayer;
        }
        
        return output;
    }
    
    
    /*
     * Parameters: 1d double array of input values and a 2d array of weights
     * Function: Calculates weighted sums of input nodes and weights to output
     *           the activation values. The function then applies the Perceptron
     *           class' threshold function to the weighted sum. This function
     *           only evaluates one layer of the perceptron. This method is used
     *           by the evaluate method to evaluate the output of the entire
     *           percepton.
     *           
     */
    public double[] outputActivation(double[] inVals, double[][] weights)
    {
        int inCount = inVals.length;
            // number of input nodes into the given layer
        
        int outCount = weights[0].length;
            //number of nodes in given layer
        
        double functionInput;
        double[] outVals = new double[outCount];
        
        for(int i = 0; i < outCount; i++)
        { // iterates through the output layer nodes
            functionInput = 0.0;
            for(int j = 0; j < inCount; j++)
            { // iterates through the input layer nodes
                functionInput += (double)((double)(inVals[j]) * 
                        (double)(weights[j][i]));
                    //weighted sum being calculated
            }
                        
            outVals[i] = thresholdFunction(functionInput);
        }
        
        return outVals;
    }
    
    /*
     * Parameters: 1 double
     * Function:   Performs the perceptron's particular sigmoid function on the
     *             given input and returns the output of the function. This
     *             function acts as the general threshold function.
     */
    public double thresholdFunction(double x)
    {
        return (1.0)/(1.0 + (Math.exp(x*-1.0)));
    }
    
    /*
     * Parameters: 1 double
     * Function: Performs the derivative of the perceptron's particular sigmoid
     *           function on the given input and returns the output of the
     *           function. This function only works because of the simple
     *           derivative of a sigmoid function; if the function is not a
     *           sigmoid, then this method needs to be altered.
     */
    public double derivativeOfThreshold(double x)
    {
        double func = thresholdFunction(x);
        return (1 - func) * func;
    }
     
    /*
     * Parameters: none
     * Function: Calculates the error function of the perceptron given the set
     *           of weights associated with it. E = .5*Sum((Ti-Oi)^2), where Ti
     *           is the target output of a training set and Oi is the calculated
     *           output of that training set.
     */
    public double error()
    {
        int numOutputVals = targetOutputs.length; 
            // number of target outputs and corresponding calculated outputs
        double error = 0.0; // the output of the error function
        double[][] calculatedOutputs = new double[numOutputVals][];
        
        for(int i = 0; i < numOutputVals; i++)
        {
            double[][] temp = evaluateNodes(trainingInputs[i]);
            
            calculatedOutputs[i] = temp[temp.length-1];
            for(int j = 0; j < calculatedOutputs[i].length; j++)
            { // iterates through each node of the output layer
                error += Math.pow(
                        targetOutputs[i][j] - calculatedOutputs[i][j], 2.0);
            }
        }
        
        return .5*error;
            // adjust by 1/2 factor after squaring according to formula
     }

    /*
     * Parameters: a 2D double array
     * Function: Creates a new 2D double array object that has the same elements
     *           as the inputed array.
     */
    private double[][] new2DDoubleArray(double[][] a)
    {
        double[][] output = new double[a.length][];
        
        for(int i = 0; i < a.length; i++)
        {
            output[i] = new double[a[i].length];
            for(int j = 0; j < a[i].length; j++)
            {
                output[i][j] = a[i][j];
            }        
        }
        
        return output;
    }
} // end of class definition of BackPropPerceptron