package nl.safenote.app;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;

import org.eclipse.swt.widgets.Shell;

/**
 * Provides a native window inside which the front end is displayed in a browser panel
 * @Author Roel Theeuwen
 * @Verion 1.0
 * @Since 2016-09-04
 */

class Window {

    private static final int APP_HEIGHT = 5000;
    private static final int APP_WIDTH = 5000;
    private static final String APP_TITLE = "SafeNote - Client";


    public void initializeApplication(String url) {
        Display display = new Display();

        Shell shell = new Shell(display);
        shell.setText(APP_TITLE);
        shell.setSize(APP_WIDTH, APP_HEIGHT);
        shell.setLayout(new GridLayout());
        shell.setBackground(new Color(display, 242, 242, 242));

        Browser browser = new Browser(shell, SWT.NONE);
        GridData browserLayout = new GridData(GridData.FILL_BOTH);
        browserLayout.grabExcessHorizontalSpace = true;
        browserLayout.grabExcessVerticalSpace = true;
        browser.setLayoutData(browserLayout);
        browser.setUrl(url);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.close();

        new Thread(()->{
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }).run();

    }
}