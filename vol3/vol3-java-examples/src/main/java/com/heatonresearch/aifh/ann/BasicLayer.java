package com.heatonresearch.aifh.ann;

import com.heatonresearch.aifh.AIFHError;
import com.heatonresearch.aifh.ann.activation.ActivationFunction;
import com.heatonresearch.aifh.ann.train.GradientCalc;
import com.heatonresearch.aifh.randomize.GenerateRandom;

public class BasicLayer extends WeightedLayer {
    /**
     * The neuron count.
     */
    private int[] count;

    private boolean hasBias;


    /**
     * Do not use this constructor.  This was added to support serialization.
     */
    public BasicLayer() {

    }

    public BasicLayer(final ActivationFunction theActivation, boolean theHasBias, int[] theCount) {
        if( theCount.length!=1 && theCount.length!=3 ) {
            throw new AIFHError("The number of dimensions must be 1 or 3.");
        }
        setActivation(theActivation);
        this.hasBias = theHasBias;
        this.count = theCount;
    }

    public BasicLayer(final ActivationFunction theActivation, boolean theHasBias, int theCount) {
        this(theActivation,theHasBias,new int[] {theCount});
    }




    /**
     * @return the count
     */
    public int getCount() {
        int product = 1;
        for(int i=0;i<this.count.length;i++) {
            product*=this.count[i];
        }
        return product;
    }

    /**
     * @return The total number of neurons on this layer, includes context, bias
     *         and regular.
     */
    public int getTotalCount() {
        return getCount() + (hasBias() ? 1 : 0);
    }

    /**
     * @return the bias
     */
    public boolean hasBias() {
        return this.hasBias;
    }


    @Override
    public void computeLayer() {
        Layer prev = getOwner().getPreviousLayer(this);
        computeLayer(0,0, prev.getTotalCount(), getCount());
    }


    @Override
    public void computeGradient(GradientCalc calc) {
        final Layer prev = getOwner().getPreviousLayer(this);
        final int fromLayerSize = prev.getTotalCount();
        final int toLayerSize = getCount();
        this.computeGradient(calc,0,0,fromLayerSize,toLayerSize);
    }

    @Override
    public void trainingBatch(GenerateRandom rnd) {
        // Nothing needs to be done!
    }

    @Override
    public boolean isActive(int i) {
        return true;
    }

    @Override
    public int[] getDimensionCounts() {
        return this.count;
    }

    @Override
    public int getWeightDepthUnit() {
        Layer previousLayer = getOwner().getPreviousLayer(this);
        int prevCount;
        if( previousLayer instanceof Conv2DLayer ) {
            prevCount = (((Conv2DLayer)previousLayer).getFilterColumns() *
                    ((Conv2DLayer)previousLayer).getFilterRows());
        } else {
            if( previousLayer.getDimensionCounts().length==1) {
                prevCount = previousLayer.getCount();
            } else {
                prevCount = previousLayer.getDimensionCounts()[0] * previousLayer.getDimensionCounts()[1];
            }
        }
        if(previousLayer.hasBias()) {
            prevCount++;
        }


        return prevCount * getNeuronDepthUnit();
    }

    @Override
    public int getNeuronDepthUnit() {
        if( this.count.length==3) {
            return this.count[0] * this.count[1];
        } else {
            return this.count[0];
        }
    }
}