package net.capps.word.heroku;

/**
 * This class launches the web application in an embedded Jetty container. This is the entry point to your application. The Java
 * command that is used for launching should fire this main method.
 */
public class Main {

    private final static SetupHelper setupHelper = SetupHelper.getInstance();

    public static void main(String[] args) throws Exception {
        setupHelper.initDatabase();
        setupHelper.createInitialUser();
        setupHelper.initJetty();
    }

}
