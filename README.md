This is a multilayer perceptron that implements backpropagation that detects from a given image, what English letter (capitialized) it represents the most.

The main program is in BackPropPerceptron.java, you can just run it if you import this project into Eclipse.

The program runs in two modes (1) if you have weights, which are populated in the "weights" file or (2) if you do not have weights and want to train the perceptron from scratch (this is if you want to tweak with training rate constant, etc.)


The weights file is saved in the following order (saved in raw bits form via DataOutputStream):

Keep in mind that a "weight layer" is just an intermediate between two layers.
A weight set of dimension *m* x *n* just represents the weights that transition a layer of *m* nodes to a layer of *n* nodes.

1. (int) how many layers of weights there are (x)
2. for each i in x
 2. (int) how many nodes are in the layer inputting into this weight layer (j)
 2. (int) how many nodes are in the layer outputted by this weight layer (k)
3. for all i, for all j, for all k (triple nested for loop)
 3. (double) the weight @ weights[i][j][k]
