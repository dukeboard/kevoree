package org.kevoree.boostrap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/11/2013
 * Time: 09:16
 */
public class Tester {

    public static void main(String[] args) {
        System.out.println(" ======== Kevoree Runner ======== ");

        Bootstrap bootstrap = new Bootstrap("node0");
        bootstrap.bootstrapFromKevScript(Tester.class.getClassLoader().getResourceAsStream("boot.kevs"));


    }

}
