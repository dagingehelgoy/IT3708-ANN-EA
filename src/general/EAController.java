package general;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class EAController {

    protected Random random = new Random();
    protected List<AbstractHypothesis> population;
    protected ArrayList<AbstractHypothesis> adults;
    protected ArrayList<Pair> parentPairs;

    public EAController(){
        adults = new ArrayList<>();
        parentPairs = new ArrayList<>();

    }

    public void generateInitialPopulation(AbstractHypothesis initialObject, int sizeOfGeneration) {
        List<AbstractHypothesis> initialPopulation = new ArrayList<>();
        for (int i = 0; i < sizeOfGeneration; i++) {
            AbstractHypothesis hypothesis = initialObject.instantiateNewChild();
            initialPopulation.add(hypothesis);
        }
        this.population = initialPopulation;
    }

    public void generatePhenotypes() {
        population.forEach(AbstractHypothesis::generatePhenotype);
    }

    public void testAndUpdateFitnessOfPhenotypes() {
        for (AbstractHypothesis hypothesis : population) {
            hypothesis.calculateFitness();
        }
    }

    public void adultSelection() {
        List<AbstractHypothesis> tempPopulation = new ArrayList<>();
        tempPopulation.addAll(population);
        switch (Values.ADULT_SELECTION) {
            case FULL_GENERATION_REPLACEMENT:
                adults.clear();
                adults.addAll(tempPopulation);
                break;
            case OVER_PRODUCTION:
                adults.clear();
                while (adults.size() < Values.MAX_ADULT_SIZE) {
                    AbstractHypothesis hyp = fitnessRoulette(tempPopulation);
                    adults.add(hyp);
                    tempPopulation.remove(hyp);
                }
                break;
            case GENERATION_MIXING:
                List<AbstractHypothesis> allHypothesis = new ArrayList<>();
                allHypothesis.addAll(adults);
                allHypothesis.addAll(population);
                adults.clear();
                while (adults.size() < Values.MAX_ADULT_SIZE) {
                    AbstractHypothesis hyp = fitnessRoulette(allHypothesis);
                    adults.add(hyp);
                    allHypothesis.remove(hyp);
                }
                break;
            case PICK_BEST_ADULTS:
                List<AbstractHypothesis> allHypothesis2 = new ArrayList<>();
                allHypothesis2.addAll(adults);
                allHypothesis2.addAll(population);
                adults.clear();
                while (adults.size() < Values.MAX_ADULT_SIZE) {
                    AbstractHypothesis hyp = getBestHypothesis(allHypothesis2);
                    allHypothesis2.remove(hyp);
                    adults.add(hyp);
                }
                break;
        }
    }

    public void parentSelection() {
        parentPairs.clear();
        List<AbstractHypothesis> tempAdults = new ArrayList<>();
        tempAdults.addAll(adults);
        AbstractHypothesis parent1;
        AbstractHypothesis parent2;

        switch (Values.PARENT_SELECTION) {
            case FITNESS_PROPORTIONATE:
                while (parentPairs.size() < Values.MAX_PARENT_SIZE) {
                    parent1 = fitnessRoulette(tempAdults);
                    tempAdults.remove(parent1);
                    parent2 = fitnessRoulette(tempAdults);
                    tempAdults.add(parent1);
                    parentPairs.add(new Pair<>(parent1, parent2));
                }
                break;
            case SIGMA_SCALING:
                while (parentPairs.size() < Values.MAX_PARENT_SIZE) {
                    updateExpectedValues(tempAdults);
                    parent1 = expectedValueRoulette(tempAdults);
                    tempAdults.remove(parent1);
                    updateExpectedValues(tempAdults);
                    parent2 = expectedValueRoulette(tempAdults);
                    tempAdults.add(parent1);
                    parentPairs.add(new Pair<>(parent1, parent2));
                }
                break;
            case TOURNAMENT_SELECTION:
                while (parentPairs.size() < Values.MAX_PARENT_SIZE) {
                    tempAdults.clear();
                    tempAdults.addAll(adults);

                    List<AbstractHypothesis> tournamentGroup = new ArrayList<>();
                    for (int i = 0; i < Values.TOURNAMENT_SELECTION_GROUP_SIZE; i++) {
                        AbstractHypothesis tournamentAttendor = tempAdults.get(random.nextInt(tempAdults.size()));
                        tournamentGroup.add(tournamentAttendor);
                        tempAdults.remove(tournamentAttendor);
                    }

                    if (random.nextDouble() >= 1 - Values.TOURNAMENT_SELECTION_EPSILON) {
                        AbstractHypothesis bestAttendor = tournamentGroup.get(0);
                        double bestFitness = bestAttendor.getFitness();
                        AbstractHypothesis secondBestAttendor = tournamentGroup.get(1);
                        for (AbstractHypothesis tempAttendor : tournamentGroup) {
                            if (tempAttendor.getFitness() > bestFitness) {
                                secondBestAttendor = bestAttendor;
                                bestAttendor = tempAttendor;
                                bestFitness = tempAttendor.getFitness();
                            }
                        }
                        parentPairs.add(new Pair<>(bestAttendor, secondBestAttendor));
                    } else {
                        AbstractHypothesis rand1 = tournamentGroup.get(random.nextInt(tournamentGroup.size()));
                        tournamentGroup.remove(rand1);
                        AbstractHypothesis rand2 = tournamentGroup.get(random.nextInt(tournamentGroup.size()));
                        parentPairs.add(new Pair<>(rand1, rand2));
                    }
                }
                break;
            case UNIFORM_SELECTION:
                while (parentPairs.size() < Values.MAX_PARENT_SIZE) {
                    tempAdults.clear();
                    tempAdults.addAll(adults);
                    AbstractHypothesis rand1 = tempAdults.get(random.nextInt(tempAdults.size()));
                    tempAdults.remove(rand1);
                    AbstractHypothesis rand2 = tempAdults.get(random.nextInt(tempAdults.size()));
                    parentPairs.add(new Pair<>(rand1, rand2));
                }
                break;
        }
    }

    public void generateNewPopulation() {
        List<AbstractHypothesis> newPopulation = new ArrayList<>();


        for (Pair<AbstractHypothesis, AbstractHypothesis> pair : parentPairs) {
            newPopulation.addAll(generateNewChildren(pair.getElement1(), pair.getElement2()));
        }

        for (int i = 0; i < Values.NUMBER_OF_ELITES; i++) {
            AbstractHypothesis elite = findHypothesisWithBestFitness(population);
            population.remove(elite);
            newPopulation.add(elite);
        }

        population.clear();
        population.addAll(newPopulation);
    }






    public double calculateStandardDeviation(List<AbstractHypothesis> hyps, double avarageFitness) {
        double sumOfSquaredDistance = 0.0;
        for (AbstractHypothesis tempHyp : hyps) {
            sumOfSquaredDistance += Math.pow(tempHyp.getFitness() - avarageFitness, 2);
        }

        return Math.sqrt(sumOfSquaredDistance / hyps.size());
    }

    public double calculateAvarageFitness(List<AbstractHypothesis> hypothesises) {
        double totalFitness = getTotalFitnessFromIHypothesis(hypothesises);
        return totalFitness / hypothesises.size();
    }


    public AbstractHypothesis getBestHypothesis(List<AbstractHypothesis> hypothesises) {
        AbstractHypothesis bestHyp = hypothesises.get(0);
        for (AbstractHypothesis hyp : hypothesises) {
            if (hyp.getFitness() > bestHyp.getFitness()) {
                bestHyp = hyp;
            }
        }
        return bestHyp;
    }

    public List<AbstractHypothesis> getPopulation() {
        return population;
    }

    protected AbstractHypothesis fitnessRoulette(List<AbstractHypothesis> hypothesises) {
        double totalFitness = getTotalFitnessFromIHypothesis(hypothesises);
        double x = totalFitness * random.nextDouble();
        for (AbstractHypothesis hyp : hypothesises) {
            x -= hyp.getFitness();
            if (x <= 0) {
                return hyp;
            }
        }
        return null;
    }

    private List<AbstractHypothesis> generateNewChildren(AbstractHypothesis parent1, AbstractHypothesis parent2) {
        List<AbstractHypothesis> children = new ArrayList<>();
        if (random.nextDouble() <= Values.CROSSOVER_PROBABILITY) {
            children.addAll(parent1.crossover(parent1, parent2));
            children.forEach((hyp) -> hyp.mutate());
        } else {
            AbstractHypothesis child1 = parent1.instantiateNewChileWithGenoType(parent1.getGenotype());
            AbstractHypothesis child2 = parent2.instantiateNewChileWithGenoType(parent2.getGenotype());
            children.add(child1);
            children.add(child2);
        }

        return children;
    }

    private AbstractHypothesis findHypothesisWithBestFitness(List<AbstractHypothesis> hypothesises) {
        AbstractHypothesis best = hypothesises.get(0);
        for (AbstractHypothesis hyp : hypothesises) {
            if (hyp.getFitness() > best.getFitness()) {
                best = hyp;
            }
        }
        return best;
    }

    private double getTotalFitnessFromIHypothesis(List<AbstractHypothesis> hypothesises) {
        double sum = 0;
        for (AbstractHypothesis hyp : hypothesises) {
            sum += hyp.getFitness();
        }
        return sum;
    }

    private double getTotalExpectedValueFromIHypothesis(List<AbstractHypothesis> hypothesises) {
        double sum = 0;
        for (AbstractHypothesis hyp : hypothesises) {
            sum += hyp.getExpectedValue();
        }
        return sum;
    }

    private AbstractHypothesis expectedValueRoulette(List<AbstractHypothesis> hypothesises) {
        double totalExpectedValue = getTotalExpectedValueFromIHypothesis(hypothesises);
        double x = totalExpectedValue * random.nextDouble();
        for (AbstractHypothesis hyp : hypothesises) {
            x -= hyp.getExpectedValue();
            if (x <= 0) {
                return hyp;
            }
        }
        return null;
    }



    private void updateExpectedValues(List<AbstractHypothesis> hypothesises) {
        for (AbstractHypothesis adult : hypothesises) {
            calculateExpectedValue(adult, hypothesises);
        }
    }

    private void calculateExpectedValue(AbstractHypothesis hyp, List<AbstractHypothesis> hyps) {
        hyp.setExpectedValue(0);
        double avarageFitness = calculateAvarageFitness(hyps);

        double standardDeviation = calculateStandardDeviation(hyps, avarageFitness);

        if (standardDeviation == 0) {
            hyp.setExpectedValue(hyp.getFitness());
        } else {
            hyp.setExpectedValue(1 + (hyp.getFitness() - avarageFitness) / (2 * standardDeviation));
        }
    }



}
