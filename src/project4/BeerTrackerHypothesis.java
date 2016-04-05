package project4;

import enums.BoardElement;
import general.AbstractHypothesis;
import general.Values;

import java.util.ArrayList;

/**
 * Created by markus on 16.03.2016.
 */
public class BeerTrackerHypothesis extends AbstractHypothesis {

    public BeerTrackerHypothesis() {

        this.setGenotype(new int[Values.ANN.getNumberOfWeights()]);
        initiateRandomGenotype();
        generatePhenotype();

    }

    public BeerTrackerHypothesis(int[] newGenotype) {
        this.genotype = newGenotype;
        generatePhenotype();
    }

    @Override
    public void generatePhenotype() {
        this.phenotype = new double[this.genotype.length];
        for (int i = 0; i < this.phenotype.length; i++) {
            int tempGEnoVal = this.genotype[i];
            double tempPhenotypeValue = 1.0 * tempGEnoVal / Values.FLATLAND_GENOTYPE_RANGE;
            this.phenotype[i] = tempPhenotypeValue;
        }
    }

    @Override
    public void initiateRandomGenotype() {
        for (int i = 0; i < Values.ANN.getNumberOfWeights(); i++) {
            int randomGenotypeInt = random.nextInt(Values.FLATLAND_GENOTYPE_RANGE);
            getGenotype()[i] = randomGenotypeInt;
        }
    }

    @Override
    public void calculateFitness() {
        Values.ANN.setNetworkWeights(this.phenotype);

        for (int i = 0; i < Values.FLATLAND_ITERATIONS; i++) {
            ArrayList<Integer> sensorValues = Values.BEERWORLD.getSensors();

            Values.BEERWORLD.playTimestep(Values.CTRANN.getMove(sensorValues));
        }


        int beerWorldFallenObjects = Values.BEERWORLD.getFallenObjects();
        double fitness;
        if (beerWorldFallenObjects != 0)
            fitness = (1.0 * Values.BEERWORLD.getCaptured()
                    + Values.BEERWORLD.getAvoided()
                    - (Values.BEERWORLD.getFailedAvoid() * Values.BEERWORLD_FAILEDAVOID_PENALTY)
                    - (Values.BEERWORLD.getFailedCapture() * Values.BEERWORLD_FAILEDCAPTURE_PENALTY)) / beerWorldFallenObjects;
        else {
            fitness = 0;
        }
        this.setFitness(fitness);
    }


    @Override
    public AbstractHypothesis instantiateNewChileWithGenoType(int[] genotype) {
        return new BeerTrackerHypothesis(genotype);
    }

    @Override
    public AbstractHypothesis instantiateNewChild() {
        return new BeerTrackerHypothesis();
    }

    @Override
    public void mutate() {
        if (random.nextDouble() < Values.MUTATION_PROBABILITY) {
            for (int i = 0; i < this.getGenotype().length; i++) {
                this.getGenotype()[i] = random.nextInt(Values.FLATLAND_GENOTYPE_RANGE);
            }
        } else {
            for (int i = 0; i < this.getGenotype().length; i++) {
                if (random.nextDouble() < Values.MUTATION_PROBABILITY) {
                    this.getGenotype()[i] = random.nextInt(Values.FLATLAND_GENOTYPE_RANGE);
                }
            }
        }
    }
}
