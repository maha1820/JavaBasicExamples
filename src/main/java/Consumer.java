package java8updates;


/**
 * The Consumer interface is a functional interface (an interface with a single abstract method). It accepts an input and returns no result.
 *
 */

public class Consumer<Integer> implements IConsumer{

    @Override
    public void accept(java.lang.Integer t) {
        System.out.println("Consumer impl Value::" + t);

    }
}
