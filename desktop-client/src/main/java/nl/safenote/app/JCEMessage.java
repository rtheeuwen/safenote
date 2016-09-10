package nl.safenote.app;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides an error message in case the JCE dependency is not satisfied.
 * The Java Cryptography Extension is required to be installed to the user's Oracle JRE
 * The JCE is not required when using the OpenJDK JRE
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */
class JCEMessage {

    void initializeApplication() {

        Display display = new Display();

        Shell shell = new Shell(display);
        shell.setText("Missing JCE");
        shell.setSize(600, 100);
        shell.setLayout(new GridLayout());
        shell.setBackground(new Color(display, 242, 242, 242));

        Label label = new Label(shell, SWT.BORDER| SWT.LEFT);
        label.setSize(300,30);
        label.setLocation(100, 100);
        label.setText("You need to install the Java cryptography extension in order to run SafeNote. " +
                "\nGet the latest version and install it in  $JAVA_HOME/jre/lib/security." +
                "\nhttp://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html");



        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.close();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        finally {
            System.exit(1);
        }
    }
}
