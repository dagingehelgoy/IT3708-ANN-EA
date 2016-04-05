package project4;

import enums.ENodeType;
import general.Pair;
import general.Values;

import java.util.*;

/**
 * Created by markus on 01.04.2016.
 */
public class CTRNN {

    private LinkedHashMap<Pair<Node, Node>, Double> networkWeights;


    private int numberOfBiasNodeWeights = 0;
    private List<Node> inputLayerNodes;
    private List<List<Node>> hiddenLayers;
    private List<Node> outputLayerNodes;

    public CTRNN() {
        initializeAnn();
    }

    private void initializeAnn() {
        List<Node> inputLayerNodes = new ArrayList<>();
        List<List<Node>> hiddenLayers = new ArrayList<>();
        List<Node> outputLayerNodes = new ArrayList<>();

        Node biasNode = new Node(ENodeType.BIAS);

        networkWeights = new LinkedHashMap<>();

        //Init topology

        //Initiates input layer with nodes
        for (int i = 0; i < Values.ANN_INPUT_NODES; i++) {
            inputLayerNodes.add(new Node(ENodeType.INPUT));
        }

        //Initiates hidden layers with nodes
        for (int i = 0; i < Values.ANN_NODES_IN_HIDDEN_LAYERS.length; i++) {
            List<Node> tempHiddenLayerNodes = new ArrayList<>();
            for (int j = 0; j < Values.ANN_NODES_IN_HIDDEN_LAYERS[i]; j++) {
                tempHiddenLayerNodes.add(new Node(ENodeType.HIDDEN));
            }
            hiddenLayers.add(tempHiddenLayerNodes);
        }

        //Initiates out layer with nodes
        for (int i = 0; i < Values.ANN_OUTPUT_NODES; i++) {
            outputLayerNodes.add(new Node(ENodeType.OUTPUT));
        }

        //Initiate all node weights

        //Input - first hidden
        for (Node inputNode : inputLayerNodes) {
            for (Node hiddenNode : hiddenLayers.get(0)) {
                networkWeights.put(new Pair<>(inputNode, hiddenNode), 0.0);
            }
        }

        //second hidden - n hidden
        for (int layerCounter = 0; layerCounter < hiddenLayers.size(); layerCounter++) {

            List<Node> topHiddenLayer = hiddenLayers.get(layerCounter);

            //Add all weights within each layer
            for (int firstNodeCounter = 0; firstNodeCounter < topHiddenLayer.size(); firstNodeCounter++) {
                Node firstNode = topHiddenLayer.get(firstNodeCounter);

                //Add all reflexive weights
                networkWeights.put(new Pair<>(firstNode, firstNode), 0.0);

                //Add all weights within each node in layer
                if (firstNodeCounter < topHiddenLayer.size() - 1) {
                    for (int secondNodeCounter = firstNodeCounter + 1; secondNodeCounter < topHiddenLayer.size(); secondNodeCounter++) {
                        Node secondNode = topHiddenLayer.get(secondNodeCounter);
                        networkWeights.put(new Pair<>(firstNode, secondNode), 0.0);
                        networkWeights.put(new Pair<>(secondNode, firstNode), 0.0);
                    }
                }
            }

            //If topHiddenLayer is not last layer, add weights between all nodes in topHiddenLayer and bottomHiddenLayer
            if (layerCounter < hiddenLayers.size() - 1) {
                List<Node> bottomHiddenLayer = hiddenLayers.get(layerCounter + 1);
                for (Node topLayerNode : topHiddenLayer) {
                    for (Node bottomLayerNode : bottomHiddenLayer) {
                        networkWeights.put(new Pair<>(topLayerNode, bottomLayerNode), 0.0);
                    }
                }

            }

        }

        //last hidden layer - output
        List<Node> lastHiddenLayer = hiddenLayers.get(hiddenLayers.size() - 1);
        for (Node lastHiddenLayerNode : lastHiddenLayer) {
            for (Node outputLayerNode : outputLayerNodes) {
                networkWeights.put(new Pair<>(lastHiddenLayerNode, outputLayerNode), 0.0);
            }
        }

        //Reflexive and within output layer weights
        for (int i = 0; i < outputLayerNodes.size(); i++) {
            Node firstNode = outputLayerNodes.get(i);

            //Add reflexive
            networkWeights.put(new Pair<>(firstNode, firstNode), 0.0);

            if (i < outputLayerNodes.size() - 1) {
                for (int j = i + 1; j < outputLayerNodes.size(); j++) {
                    Node secondNode = outputLayerNodes.get(j);
                    networkWeights.put(new Pair<>(firstNode, secondNode), 0.0);
                    networkWeights.put(new Pair<>(secondNode, firstNode), 0.0);
                }
            }
        }


        //Add Bias node weights
        for (List<Node> hiddenLayer : hiddenLayers) {
            for (Node hiddenNode : hiddenLayer) {
                networkWeights.put(new Pair<>(biasNode, hiddenNode), 0.0);
                numberOfBiasNodeWeights++;
            }
        }

        for (Node outputNode : outputLayerNodes) {
            networkWeights.put(new Pair<>(biasNode, outputNode), 0.0);
            numberOfBiasNodeWeights++;
        }
    }


    public void setNetworValues(double[] phenotype){
        //TODO: Set weights, gains and time constants
    }

    public int getNumberOfWeights() {
        return networkWeights.size();
    }

    public int getNumberOfOutputAndHiddenNodes() {
        int numGeins = 0;

        for (List<Node> hiddenLayer : hiddenLayers) {
            numGeins += hiddenLayer.size();
        }

        numGeins += outputLayerNodes.size();

        return numGeins;
    }

    public void setNetworkWeights(double[] weights) {
        int counter = 0;
        for (Map.Entry<Pair<Node, Node>, Double> entry : networkWeights.entrySet()) {
            Pair<Node, Node> key = entry.getKey();
            networkWeights.put(key, weights[counter++]);
        }
    }

    public void setNodeGeins(double[] gains) {
        int counter = 0;
        for (List<Node> hiddenLayer : hiddenLayers) {
            for (Node hiddenNode : hiddenLayer) {
                hiddenNode.setGain(gains[counter++]);
            }
        }

        for (Node outputNode : outputLayerNodes) {
            outputNode.setGain(gains[counter++]);
        }
    }

    public void setNodeTimeConstants(double[] timeConstants) {
        int counter = 0;
        for (List<Node> hiddenLayer : hiddenLayers) {
            for (Node hiddenNode : hiddenLayer) {
                hiddenNode.setGain(timeConstants[counter++]);
            }
        }

        for (Node outputNode : outputLayerNodes) {
            outputNode.setGain(timeConstants[counter++]);
        }
    }


    public int getMove(double[] inputValues) {

        //updates input layer with input values
        setValuesOfInputLayerNodes(inputValues);

        updateAllNodeValues();

//        int highestIndex = findHighestIndex(outputLayerNodes);
//        return highestIndex;
        return 0;
    }

    private void updateAllNodeValues() {
        for (List<Node> hiddenLayer : hiddenLayers) {
            updateNodeValuesInLayer(hiddenLayer);
        }

        updateNodeValuesInLayer(outputLayerNodes);
    }

    private void updateNodeValuesInLayer(List<Node> layer) {
        for (Node node : layer) {
            calculateNewSValue(node);
            double delta_y = (node.getsValue() - node.getyValue()) / node.getTimeConstant();
            double newY = node.getyValue() + delta_y;
            node.setyValue(newY);
            double newOutputValue = 1 / (1 + Math.exp(-node.getGain() * node.getyValue()));
            node.setOutputValue(newOutputValue);
        }
    }

    private void calculateNewSValue(Node nodeToBeUpdated) {
        nodeToBeUpdated.setsValue(0.0);
        for (Map.Entry<Pair<Node, Node>, Double> entry : networkWeights.entrySet()) {
            Pair<Node, Node> nodePairKey = entry.getKey();
            if (nodePairKey.getElement2().equals(nodeToBeUpdated)) {
                Node parentNode = nodePairKey.getElement1();
                double weight = entry.getValue();

                nodeToBeUpdated.setsValue(nodeToBeUpdated.getsValue() + parentNode.getOutputValue() * weight);
            }
        }
    }


    private void setValuesOfInputLayerNodes(double[] inputValues) {
        for (int i = 0; i < inputLayerNodes.size(); i++) {
            Node node = inputLayerNodes.get(i);
            node.setOutputValue(inputValues[i]);
        }
    }

    private int findHighestIndex(double[] outputLayer) {
        int highestIndex = 0;
        double highestValue = outputLayer[0];
        for (int i = 1; i < outputLayer.length; i++) {
            if (outputLayer[i] > highestValue) {
                highestIndex = i;
                highestValue = outputLayer[i];
            }
        }
        return highestIndex;
    }
}
