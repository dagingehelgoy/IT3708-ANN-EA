package project5;

import enums.EProblemSelection;
import general.AbstractHypothesis;
import general.EAController;
import general.Values;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import project4.BeerWorld;
import project4.CTRNN;

public class P5Main extends Application {


    private int generation;

    private P5GUIController p5GuiController;
    private EAController eaController;
    private boolean shouldRestart;
    boolean simulationPaused;


    public static void main(String[] args) {
        MTSPHypothesis mstHyp = new MTSPHypothesis();
        mstHyp.initiateRandomGenotype();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

//        XLSXReader xlsxReader = new XLSXReader();-
//        int[][] distance = xlsxReader.read("src/project5/Distance.xlsx", 48);
//        int[][] cost = xlsxReader.read("src/project5/Cost.xlsx", 48);

        Values.SELECTED_PROBLEM = EProblemSelection.MTSP;

        p5GuiController = new P5GUIController();
//
        Pane pane = p5GuiController.generateGUI(this);
//
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
//
//         run generation loop
        startEvolutionaryAlgorithmLoop();
    }

    private void startEvolutionaryAlgorithmLoop() {

        eaController = new EAController();
        generation = 0;

        AnimationTimer mainLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!simulationPaused) {

                    if (shouldRestart) {
                        generation = 0;
                        p5GuiController.clearGUI();
                        shouldRestart = false;

                        eaController = new EAController();
                    }

                    generation += 1;

                    eaController.generatePhenotypes();

                    eaController.testAndUpdateFitnessOfPhenotypes();

                    if (generation % Values.GENERATION_PRINT_THROTTLE == 0) {
                        updateGUI();
                    }

                    eaController.adultSelection();
                    eaController.parentSelection();
                    eaController.generateNewPopulation();
                }
            }
        };
        mainLoop.start();
    }

    private void updateGUI() {

        double avgFitness = eaController.calculateAvarageFitness(eaController.getPopulation());
        AbstractHypothesis bestHypothesis = eaController.getBestHypothesis(eaController.getPopulation());

        p5GuiController.updateLineCharts(eaController.getPopulation(), bestHypothesis.getFitness(),
                avgFitness,
                eaController.calculateStandardDeviation(eaController.getPopulation(), avgFitness),
                generation, bestHypothesis.getPhenotypeString());
    }

    void restartAlgorithm() {
        shouldRestart = true;
    }
}