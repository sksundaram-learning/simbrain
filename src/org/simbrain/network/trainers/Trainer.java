/*
 * Part of Simbrain--a java-based neural network kit Copyright (C) 2005,2007 The
 * Authors. See http://www.simbrain.net/credits This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.simbrain.network.trainers;

import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.simbrain.network.interfaces.BiasedNeuron;
import org.simbrain.network.interfaces.Network;
import org.simbrain.network.interfaces.Neuron;
import org.simbrain.util.ClassDescriptionPair;
import org.simbrain.util.Utils;

/**
 * Superclass for all forms of supervised training algorithms. A trainer
 * combines input data, training data, and error information in a single
 * package, which can be visualized in a special GUI dialog. Network, input, and
 * output layers are immutable. To change these you must create a new Trainer
 * object. I tried having those be mutable and many headaches ensued.
 *
 * @author jyoshimi
 */
public class Trainer {

    /**
     * Reference to the network being trained.
     */
    private final Network network;

    /** Input layer. */
    private final List<Neuron> inputLayer;

    /** Output layer. */
    private final List<Neuron> outputLayer;

    /** Listener list. */
    private List<EventListener> listeners = new ArrayList<EventListener>();
    
    /**
     * A reference to the training method, which actually computes the weight
     * updates.  This can be changed as needed.
     */
    private TrainingMethod trainingMethod;

    /**
     * Same number of columns as input network. Same number of rows as training
     * data.
     */
    private double[][] inputData;

    /**
     * Same number of columns as output network. Same number of rows as training
     * data.
     */
    private double[][] trainingData;
    
    /** Flag used for iterative training methods. */
    private boolean updateCompleted = true;
    
    /** Flag used if methods' inputs are the result of state harvesting. */ 
    private boolean stateHarvester;


    /** List of Trainer types. */
    private static final ClassDescriptionPair[] RULE_LIST = {
            new ClassDescriptionPair(Backprop.class, "Backprop"),
            new ClassDescriptionPair(LMSIterative.class, "LMS-Iterative"),
            new ClassDescriptionPair(LMSOffline.class, "LMS-Offline") };

    /**
     * Construct a trainer from a network, input, and output layer.
     *
     * @param network parent network
     * @param inputLayer input layer
     * @param outputLayer output layer
     * @param method the training method
     */
    public Trainer(Network network, List<Neuron> inputLayer,
            List<Neuron> outputLayer, TrainingMethod method) {
        this.network = network;
        this.inputLayer = inputLayer;
        this.outputLayer = outputLayer;
        
        // TODO: Allow for vertical sorting... Or just document this.
//        Collections.sort(inputLayer, Comparators.X_ORDER);
//        Collections.sort(outputLayer, Comparators.X_ORDER);

        setTrainingMethod(method);

        // SimnetUtils.printLayers(SimnetUtils.getIntermedateLayers(network,
        // inputLayer, outputLayer));
    }
    
    /**
     * Construct a trainer from a network, input, and output layer.
     *
     * @param network parent network
     * @param inputLayer input layer
     * @param outputLayer output layer
     * @param method the training method name as a string.
     */
    public Trainer(Network network, List<Neuron> inputLayer,
            List<Neuron> outputLayer, String method) {
        this.network = network;
        this.inputLayer = inputLayer;
        this.outputLayer = outputLayer;
        
//        // TODO: Allow for vertical sorting... Or just document this.
//        Collections.sort(inputLayer, Comparators.X_ORDER);
//        Collections.sort(outputLayer, Comparators.X_ORDER);

        setTrainingMethod(method);
    }

    /**
     * @return the inputData
     */
    public final double[][] getInputData() {
        return inputData;
    }

    /**
     * @param newData the inputData to set
     * @throws InvalidDataException
     */
    public final void setInputData(double[][] newData) {
        if (newData != null) {
        	// TODO: Causes probelms with state harvesting temporarily commented out, but some sort of check is needed
//            if (newData[0].length != inputLayer.size()) {
//                throw new InvalidDataException("Data mismatch: selected data has "
//                        + newData[0].length + " columns; input layer has "
//                        + inputLayer.size() + " neurons");
//            }
            // System.out.println("Input Data: \n" +
            // Utils.doubleMatrixToString(newData));
            this.inputData = newData;
            trainingMethod.init(this);
            fireInputDataChanged(inputData);
        }
    }

    /**
     * Use a csv file to load input data.
     *
     * @param inputFile the inputData as a csv file
     */
    public final void setInputData(final File inputFile) {
        setInputData(Utils.getDoubleMatrix(inputFile));
    }

    /**
     * @return the trainingData
     */
    public final double[][] getTrainingData() {
        return trainingData;
    }

    /**
     * Set training data.
     *
     * @param newData the trainingData to set
     * @throws InvalidDataException
     */
    public void setTrainingData(double[][] newData) {
        if (newData != null) {
            if (newData[0].length != outputLayer.size()) {
                throw new InvalidDataException(
                        "Data mismatch: selected data has " + newData[0].length
                                + " columns; output layer has "
                                + outputLayer.size() + " neurons");
            }
            this.trainingData = newData;
            trainingMethod.init(this);
            fireTrainingDataChanged(trainingData);
            // System.out.println("Training Data: \n" +
            // Utils.doubleMatrixToString(newData));
        }
    }

    /**
     * Use a csv file to load training data.
     *
     * @param trainingDataFile the training data as a .csv file
     */
    public void setTrainingData(final File trainingDataFile) {
        setTrainingData(Utils.getDoubleMatrix(trainingDataFile));
    }

    /**
     * @return the inputLayer
     */
    public List<Neuron> getInputLayer() {
        return inputLayer;
    }

    /**
     * @return the outputLayer
     */
    public List<Neuron> getOutputLayer() {
        return outputLayer;
    }

    /**
     * @return the network
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Notify listeners that the error value has been updated. Only makes sense
     * for iterable methods.
     */
    public void fireErrorUpdated() {
        for (EventListener listener : listeners) {
            if (listener instanceof TrainerListener) {
            		((TrainerListener) listener).errorUpdated();
            }
        }
    }

    /**
     * Notify listeners that the input data has changed
     */
    private void fireInputDataChanged(double[][] inputData) {
        for (EventListener listener : listeners) {
        	if(listener instanceof TrainerListener) {
            ((TrainerListener) listener).inputDataChanged(inputData);
        	}
        }
    }

    /**
     * Notify listeners that the training data was changed.
     */
    private void fireTrainingDataChanged(double[][] trainingData) {
        for (EventListener listener : listeners) {
            if(listener instanceof TrainerListener) {
            	((TrainerListener) listener).trainingDataChanged(inputData);
            }
        }
    }

    /**
     * Randomize the output layer weights and biases.
     */
    public void randomizeOutputWeightsAndBiases() {
        for (Neuron neuron : getOutputLayer()) {
            neuron.randomizeFanIn();
            if (neuron.getUpdateRule() instanceof BiasedNeuron) {
                ((BiasedNeuron) neuron.getUpdateRule()).setBias(Math.random());
            }
        }
    }

    /**
     * Add a listener.
     *
     * @param trainerListener the listener to add
     */
    public void addListener(EventListener eventListener) {
        if (listeners == null) {
            listeners = new ArrayList<EventListener>();
        }
        listeners.add(eventListener);
    }

    /**
     * Returns a description of the network topology.
     *
     * @return the description
     */
    public String getTopologyDescription() {
        String retString = inputLayer.size() + " > " + outputLayer.size();
        return retString;
    }

    @Override
    public String toString() {
        String retString = "";
        if (network != null) {
            retString += "Network: " + network.getId() + "\n";
        }
        if (inputLayer != null) {
            retString += "Input Layer: " + inputLayer.size() + " neuron(s) \n";
        }
        if (outputLayer != null) {
            retString += "Output Layer: " + outputLayer.size()
                    + " neuron(s) \n";
        }
        if (inputData != null) {
            retString += "Input Data: " + inputData.length + "x"
                    + inputData[0].length + " table \n";
        }
        if (trainingData != null) {
            retString += "Training Data: " + trainingData.length + "x"
                    + trainingData[0].length + "table \n";
        }
        return retString;
    }

    /**
     * @return the ruleList
     */
    public static ClassDescriptionPair[] getRuleList() {
        return RULE_LIST;
    }

    /**
     * @return the listeners
     */
    public List<EventListener> getListeners() {
        return listeners;
    }

    /**
     * Apply the training method using the current data.
     */
    public void update() {
        trainingMethod.apply(this);
    }

    /**
     * @return the trainingMethod
     */
    public TrainingMethod getTrainingMethod() {
        return trainingMethod;
    }

    /**
     * @param trainingMethod the trainingMethod to set
     */
    public void setTrainingMethod(TrainingMethod trainingMethod) {
        this.trainingMethod = trainingMethod;
        trainingMethod.init(this);
    }

    /**
     * Sets the training method using a String description. The provided
     * description must match the class name. E.g. "Backprop" for
     * "Backprop.java".
     * 
     * @param name the "simple name" of the class associated with the training
     *            method rule to set.
     */
    public void setTrainingMethod(String name) {
        try {
            TrainingMethod newMethod = (TrainingMethod) Class.forName(
                    "org.simbrain.network.trainers." + name).newInstance();
            setTrainingMethod(newMethod);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "The provided training method name, \"" + name
                            + "\", does not correspond to a known method name."
                            + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Randomize the trainer.
     */
    public void randomize() {
        trainingMethod.randomize(this);        
    }
    

    /**
     * @return boolean updated completed.
     */
    public boolean isUpdateCompleted() {
        return updateCompleted;
    }

    /**
     * Sets updated completed value.
     *
     * @param updateCompleted Updated completed value to be set
     */
    public void setUpdateCompleted(final boolean updateCompleted) {
        this.updateCompleted = updateCompleted;
    }

    /**
     * @return whether or not the trainer uses harvested data.
     */
	public boolean isStateHarvester() {
		return stateHarvester;
	}

	/**
	 * @param stateHarvester flags this as a trainer that harvests states.
	 */
	public void setStateHarvester(boolean stateHarvester) {
		this.stateHarvester = stateHarvester;
	}


}
