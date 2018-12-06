package java8updates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Some of the important Java 8 features are;

 forEach() method in Iterable interface
 default and static methods in Interfaces
 Functional Interfaces and Lambda Expressions
 Java Stream API for Bulk Data Operations on Collections
 Java Time API
 Collection API improvements
 Concurrency API improvements
 Java IO improvements
 Miscellaneous Core API improvements

 */





public class Java8ForEachExample {


    public static void main(String args[]){
        List<Integer> myList = new ArrayList<Integer>();
        //traversing through forEach method of Iterable with anonymous class
        myList.forEach(new Consumer<Integer>() {

            @Override
            public void accept(Integer o) {
                System.out.println("forEach anonymous class Value::"+0);

            }

        });

        //traversing with Consumer interface implementation
        Consumer action = new Consumer<Integer>() {
            @Override
            public void accept(Integer o) {

            }
        };
        myList.forEach(action);


    }
}
