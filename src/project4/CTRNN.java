package project4;

import enums.ENodeType;
import general.Pair;
import general.Values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public void resetNetwork(){
        hiddenLayers.forEach(this::resetNodesInLayer);
        resetNodesInLayer(outputLayerNodes);
    }

    private void resetNodesInLayer(List<Node> layer) {
        for (Node node : layer){
            node.resetValues();
        }
    }

    private void initializeAnn() {
        inputLayerNodes = new ArrayList<>();
        hiddenLayers = new ArrayList<>();
        outputLayerNodes = new ArrayList<>();

        Node biasNode = new Node(ENodeType.BIAS);

        networkWeights = new LinkedHashMap<>();

        //Init topology

        //Initiates input layer with nodes
        for (int i = 0; i < Values.CTRNN_INPUT_NODES; i++) {
            inputLayerNodes.add(new Node(ENodeType.INPUT));
        }

        //Initiates hidden layers with nodes
        for (int i = 0; i < Values.CTRNN_NODES_IN_HIDDEN_LAYERS.length; i++) {
            List<Node> tempHiddenLayerNodes = new ArrayList<>();
            for (int j = 0; j < Values.CTRNN_NODES_IN_HIDDEN_LAYERS[i]; j++) {
                tempHiddenLayerNodes.add(new Node(ENodeType.HIDDEN, i));
            }
            hiddenLayers.add(tempHiddenLayerNodes);
        }

        //Initiates out layer with nodes
        for (int i = 0; i < Values.CTRNN_OUTPUT_NODES; i++) {
            outputLayerNodes.add(new Node(ENodeType.OUTPUT));
        }

        //Initiate all node weights

        //Input - first hidden
        for (Node inputNode : inputLayerNodes) {
            for (Node hiddenNode : hiddenLayers.get(0)) {
                networkWeights.put(new Pair<>(inputNode, hiddenNode), 0.0);
            }
        }

        //first hidden - n hidden
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


    public void setNetworkValues(double[] phenotype){
        int numNodeWeights = getNumberOfNormalNodeWeights();
        int numBiasWeights = getNumberOfBiasNodeWeights();
        int numGains = getNumberOfBiasNodeWeights();
        int numTimeConstants = getNumberOfTimeConstantValues();

        double[] nodeWeights = Arrays.copyOfRange(phenotype, 0, numNodeWeights+numBiasWeights);
        double[] gains = Arrays.copyOfRange(phenotype, numNodeWeights+numBiasWeights, numNodeWeights+numBiasWeights+numGains);
        double[] timeConstants = Arrays.copyOfRange(phenotype, numNodeWeights+numBiasWeights+numGains, numNodeWeights+numBiasWeights+numGains+numTimeConstants);

        setNetworkWeights(nodeWeights);
        setNodeGains(gains);
        setNodeTimeConstants(timeConstants);
    }

    public int getPhenotypeSize(){
        return getNumberOfNormalNodeWeights() + getNumberOfBiasNodeWeights() + getNumberOfGainValues() + getNumberOfTimeConstantValues();
    }


    public double[] getMove(int[] inputValues){
        //updates input layer with input values
        setValuesOfInputLayerNodes(inputValues);

        updateAllNodeValues();

        double[] outputArray = new double[outputLayerNodes.size()];
        for (int i = 0; i < outputArray.length; i++) {
            outputArray[i] = outputLayerNodes.get(i).getOutputValue();
        }

        return outputArray;
    }

    public int getNumberOfNormalNodeWeights(){
        return getNumberOfWeights() - numberOfBiasNodeWeights;
    }

    public int getNumberOfBiasNodeWeights(){
        return numberOfBiasNodeWeights;
    }

    public int getNumberOfGainValues(){
        return getNumberOfOutputAndHiddenNodes();
    }

    public int getNumberOfTimeConstantValues(){
        return getNumberOfGainValues();
    }

    private int getNumberOfWeights() {
        return networkWeights.size();
    }

    private int getNumberOfOutputAndHiddenNodes() {
        int numGeins = 0;

        for (List<Node> hiddenLayer : hiddenLayers) {
            numGeins += hiddenLayer.size();
        }

        numGeins += outputLayerNodes.size();

        return numGeins;
    }

    private void setNetworkWeights(double[] weights) {
        int counter = 0;
        for (Map.Entry<Pair<Node, Node>, Double> entry : networkWeights.entrySet()) {
            Pair<Node, Node> key = entry.getKey();
            networkWeights.put(key, weights[counter++]);
        }
    }

    private void setNodeGains(double[] gains) {
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

    private void setNodeTimeConstants(double[] timeConstants) {
        int counter = 0;
        for (List<Node> hiddenLayer : hiddenLayers) {
            for (Node hiddenNode : hiddenLayer) {
                hiddenNode.setTimeConstant(timeConstants[counter++]);
            }
        }

        for (Node outputNode : outputLayerNodes) {
            outputNode.setTimeConstant(timeConstants[counter++]);
        }
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

            double delta_y = (node.getsValue() - node.getYValue()) / node.getTimeConstant();

            double newY = node.getYValue() + delta_y;
            node.setyValue(newY);

            double newOutputValue = 1 / (1 + Math.exp(-node.getGain() * node.getYValue()));
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

                //Test om der er noe forskjell om man bruker gammel eller ny.
                //nodeToBeUpdated.setsValue(nodeToBeUpdated.getsValue() + parentNode.getOldOutputValue() * weight);
                nodeToBeUpdated.setsValue(nodeToBeUpdated.getsValue() + parentNode.getOutputValue() * weight);
            }
        }
    }


    private void setValuesOfInputLayerNodes(int[] inputValues) {
        for (int i = 0; i < inputLayerNodes.size(); i++) {
            Node node = inputLayerNodes.get(i);
            node.setOutputValue(inputValues[i]);
        }
    }

    public void printWeights(){
        for (Map.Entry<Pair<Node, Node>, Double> entry : networkWeights.entrySet()) {
            Pair<Node, Node> nodePairKey = entry.getKey();
            double weight = entry.getValue();

            System.out.println(nodePairKey.getElement1() + " " + weight + " " + nodePairKey.getElement2());
        }
    }

    public static void main(String[] args) throws IOException {
        CTRNN network = new CTRNN();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String goodPhenotype = "4.0 -2.5 -1.5 -3.5 -1.0 -5.0 -2.0 -5.0 3.0 -4.5 -0.5 2.0 2.0 -1.0 1.5 -2.0 4.0 -2.0 -5.0 -4.0 -2.5 1.0 -9.5 -5.5 -2.0 -8.5 2.6 4.0 1.4 3.6 1.15 1.2 1.65 1.25";
        boolean continueLoop = true;
        while (continueLoop){
            System.out.println("");
            System.out.println("1 - get output from sensor input, 2 - Enter new phenotype, 3 - use existing phenotype, 0 - Exit: ");
            String command = br.readLine();
            if (command.equals("0")){
                continueLoop = false;
            }
            else if(command.equals("1")){
                System.out.println("Enter sensorinput: ");
                String sensorString = br.readLine();
//                System.out.println(sensorString);
                String[] stringArray = sensorString.split("");
                int[] sensorArray = Arrays.stream(stringArray).mapToInt(Integer::parseInt).toArray();

                double[] moveArray = network.getMove(sensorArray);
                double moveValue = moveArray[1] - moveArray[0];
                int move = BeerTrackerHypothesis.getMove(moveValue);

                printCtrnnOutput(sensorArray, moveArray, move);
            }
            else if (command.equals("2")) {
                System.out.println("Enter phenotype: ");
                String phenotypeString = br.readLine();
//                System.out.println(phenotypeString);
//                System.out.println("4.0 -2.5 -1.5 -3.5 -1.0 -5.0 -2.0 -5.0 3.0 -4.5 -0.5 2.0 2.0 -1.0 1.5 -2.0 4.0 -2.0 -5.0 -4.0 -2.5 1.0 -9.5 -5.5 -2.0 -8.5 2.6 4.0 1.4 3.6 1.15 1.2 1.65 1.25");
                String[] stringArray = phenotypeString.split(" ");
                double[] phenotypeArray = Arrays.stream(stringArray).mapToDouble(Double::parseDouble).toArray();
//                System.out.println(Arrays.toString(phenotypeArray));
                network.setNetworkValues(phenotypeArray);
                System.out.println("Network updated");
                System.out.println("");

            }
            else if (command.equals("3")) {
                String[] stringArray = goodPhenotype.split(" ");
                double[] phenotypeArray = Arrays.stream(stringArray).mapToDouble(Double::parseDouble).toArray();
                network.setNetworkValues(phenotypeArray);
                System.out.println("Network updated");
                System.out.println("");
            }else{
                try{
                    String[] stringArray = command.split("");
                    int[] sensorArray = Arrays.stream(stringArray).mapToInt(Integer::parseInt).toArray();

                    double[] moveArray = network.getMove(sensorArray);
                    double moveValue = moveArray[1] - moveArray[0];
                    int move = BeerTrackerHypothesis.getMove(moveValue);

                    printCtrnnOutput(sensorArray, moveArray, move);

                }catch (Exception ignored){
                    System.out.println("Unknown command!");
                }
            }
        }


    }

    private static void printCtrnnOutput(int[] sensorArray, double[] moveArray, int move) {
        System.out.println("Move: " + move + "\t\tOutput layer: " + Arrays.toString(moveArray) + "\tSensor input: " + Arrays.toString(sensorArray));
    }

//    private int findHighestIndex(List<Node> nodeLayer) {
//        int highestIndex = 0;
//        double highestValue = nodeLayer.get(0).getOutputValue();
//        for (int i = 1; i < nodeLayer.size(); i++) {
//            double tempValue = nodeLayer.get(i).getOutputValue();
//            if (tempValue > highestValue) {
//                highestIndex = i;
//                highestValue = tempValue;
//            }
//        }
//        return highestIndex;
//    }
}
