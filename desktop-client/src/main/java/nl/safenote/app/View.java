package nl.safenote.app;

import nl.safenote.api.AuthenticationController;
import nl.safenote.api.NoteController;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class View {

    private Shell shell;
    private Font font;
    private Font tableFont;
    private Table table;
    private Text searchText;
    private Color backGroundColor;
    private static boolean searching;

    private static AuthenticationController authenticationController;
    private static NoteController noteController;

    @Autowired
    public View(AuthenticationController authenticationController, NoteController noteController) {
        View.authenticationController = authenticationController;
        View.noteController = noteController;
    }


    public void open(boolean generate){
        Display display = Display.getDefault();
        createContents(display, generate);
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    private void createContents(final Display display, final boolean generate){
        shell = new Shell();
        shell.setSize(1200, 800);
        shell.setText("SafeNote");
        shell.setImage(getImage("/logo.png"));


        final StackLayout layout = new StackLayout();
        shell.setLayout(layout);

        final Composite login = new Composite(shell, SWT.NONE);
        final Composite workbench = new Composite(shell, SWT.NONE);

        layout.topControl = login;

        /******************************************LOGIN COMPOSITE***************************************************/

        Button loginButton;
        Composite topFiller;
        login.setLayout(new GridLayout(1, false));

        topFiller = new Composite(login, SWT.NONE);
        topFiller.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));

        final Text passphraseText = new Text(login, SWT.BORDER | SWT.PASSWORD | SWT.LEFT);
        GridData gd_passphraseText = new GridData(SWT.CENTER, SWT.LEFT, false, false, 1, 1);
        gd_passphraseText.minimumWidth = 190;
        gd_passphraseText.widthHint = 190;
        passphraseText.setLayoutData(gd_passphraseText);

        final Text gen_confirmText = new Text(login, SWT.BORDER | SWT.PASSWORD | SWT.LEFT);

        GridData gen_gd_confirmText = new GridData(SWT.CENTER, SWT.LEFT, false, false, 1, 1);
        gen_gd_confirmText.minimumWidth = 190;
        gen_gd_confirmText.widthHint = 190;
        gen_confirmText.setLayoutData(gen_gd_confirmText);
        gen_confirmText.setVisible(generate);

        final StyledText wrongPassLabel = new StyledText(login, SWT.CENTER);
        wrongPassLabel.setEnabled(false);
        wrongPassLabel.setVisible(false);
        wrongPassLabel.setDoubleClickEnabled(false);
        wrongPassLabel.setEditable(false);
        wrongPassLabel.setForeground(display.getSystemColor(SWT.COLOR_MAGENTA));
        wrongPassLabel.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        wrongPassLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        if(generate)
            wrongPassLabel.setText("passphrase does not match");
        else
            wrongPassLabel.setText("wrong passphrase");


        Composite middleFiller = new Composite(login, SWT.NONE);
        GridData gd_middleFiller = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_middleFiller.minimumHeight = 1;
        gd_middleFiller.heightHint = 1;
        middleFiller.setLayoutData(gd_middleFiller);

        Image loginIcon = getImage("/lock.gif");

        loginButton = new Button(login, SWT.NONE | SWT.CENTER);
        loginButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        loginButton.setImage(loginIcon);
        if(generate)
            loginButton.setText("  confirm    ");
        else
            loginButton.setText("  authenticate    ");

        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                if(generate){
                    String passphrase = passphraseText.getText();
                    String confirm = gen_confirmText.getText();
                    if(passphrase!=""&&passphrase.equals(confirm)&&View.authenticationController.authenticate(passphrase)){
                            layout.topControl = workbench;
                            login.dispose();
                            shell.layout();
                        //TODO authenticantioncontroller generate
                    }
                    else
                        wrongPassLabel.setVisible(true);
                } else {
                    if(View.authenticationController.authenticate(passphraseText.getText())){
                        layout.topControl = workbench;
                        login.dispose();
                        shell.layout();
                        //TODO authenticantioncontroller authenticate
                    } else {
                        wrongPassLabel.setVisible(true);
                    }
                }
            }
        });
        passphraseText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.keyCode==13){
                    if(generate){
                        String passphrase = passphraseText.getText();
                        String confirm = gen_confirmText.getText();
                        if(passphrase!=""&&passphrase.equals(confirm)&&View.authenticationController.authenticate(passphrase)){
                            layout.topControl = workbench;
                            login.dispose();
                            shell.layout();
                        }
                        else
                            wrongPassLabel.setVisible(true);
                    } else {
                        if(View.authenticationController.authenticate(passphraseText.getText())){
                            layout.topControl = workbench;
                            login.dispose();
                            shell.layout();
                            //TODO authenticantioncontroller authenticate
                        } else {
                            wrongPassLabel.setVisible(true);
                        }
                    }
                }
            }
        });

        gen_confirmText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.keyCode==13){
                    String passphrase = passphraseText.getText();
                    String confirm = gen_confirmText.getText();
                    if(passphrase!=""&&passphrase.equals(confirm)){
                        View.authenticationController.authenticate(passphrase);
                        layout.topControl = workbench;
                        login.dispose();
                        //TODO authenticantioncontroller generate
                    }
                    else
                        wrongPassLabel.setVisible(true);
                }
            }
        });

        Image loadingIcon = getImage("/loading.gif");

        Label loadingLabel = new Label(login, SWT.CENTER);
        loadingLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        loadingLabel.setImage(loadingIcon);
        loadingLabel.setVisible(false);


        Composite bottomFiller = new Composite(login, SWT.NONE);
        bottomFiller.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));



        /******************************************WORKBENCH COMPOSITE***************************************************/


        font = new Font(display, "monospaced", 12, SWT.NONE);
        tableFont = new Font(display, "monospaced", 11, SWT.NONE);
        workbench.setLayout(new GridLayout(9, false));

        backGroundColor = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

        Composite topLeftFiller = new Composite(workbench, SWT.NONE);
        GridData gd_topLeftFiller = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_topLeftFiller.minimumWidth = 30;
        gd_topLeftFiller.widthHint = 30;
        topLeftFiller.setLayoutData(gd_topLeftFiller);

        searchText = new Text(workbench, SWT.BORDER);
        final Image searchIcon = getImage("/search.gif");
        final Image clearIcon = getImage("/clear.gif");
        final Label searchLabel = new Label(workbench, SWT.NONE);
        searchLabel.setImage(searchIcon);


        GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_text.minimumWidth = 190;
        gd_text.widthHint = 190;
        Point location = searchText.getLocation();
        searchText.setLocation(new Point(location.x+80, location.y));
        searchText.setLayoutData(gd_text);

        Image newIcon = getImage("/new.gif");

        Button newButton = new Button(workbench, SWT.PUSH);
        newButton.setBackground(backGroundColor);
        newButton.setImage(newIcon);
        newButton.setText("  new note   ");

        Image deleteIcon = getImage("/delete.gif");
        Button deleteButton = new Button(workbench, SWT.PUSH);
        deleteButton.setBackground(backGroundColor);
        deleteButton.setImage(deleteIcon);
        deleteButton.setText(" delete note ");

        Image syncIcon = getImage("/sync.gif");
        Button syncButton = new Button(workbench, SWT.PUSH);
        syncButton.setBackground(backGroundColor);
        syncButton.setImage(syncIcon);
        syncButton.setText(" synchronize ");

        Image exportIcon = getImage("/lock.gif");

        Button exportButton = new Button(workbench, SWT.PUSH);
        exportButton.setBackground(backGroundColor);
        exportButton.setImage(exportIcon);
        exportButton.setText(" export key  ");

        Composite topRightFiller = new Composite(workbench, SWT.NONE);
        GridData gd_topRightFiller = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_topRightFiller.minimumWidth = 120;
        gd_topRightFiller.widthHint = 120;
        topRightFiller.setLayoutData(gd_topRightFiller);
        new Label(workbench, SWT.NONE);

        table = new Table(workbench, SWT.FULL_SELECTION);

        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gd_table.widthHint = 300;
        gd_table.minimumWidth = 300;
        gd_table.grabExcessHorizontalSpace = false;
        table.setLayoutData(gd_table);
        table.setHeaderVisible(false);
        table.setLinesVisible(true);
        table.setFont(tableFont);
        table.setBackground(backGroundColor);
        TableColumn column = new TableColumn(table, SWT.NONE);

        //populate table initially
        noteController.getHeaders().stream().forEachOrdered(header -> {TableItem item = new TableItem(table, SWT.NONE);
            item.setText(header.getHeader()); item.setData(header.getId());});
        table.getColumn(0).pack();


        StyledText styledText = new StyledText(workbench, SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
        GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        gd_styledText.widthHint = 600;
        gd_styledText.minimumWidth=600;
        styledText.setLayoutData(gd_styledText);
        int margin = 90;
        styledText.setMargins(margin, margin, margin, margin);
        styledText.setWordWrap(true);
        styledText.setFont(font);
        styledText.setLineSpacing(12);
        new Label(workbench, SWT.NONE);
        styledText.forceFocus();

        //POPULATE VIEWER
        styledText.setText("test");
        styledText.setSelection(styledText.getText().length());
        new Label(workbench, SWT.NONE);


        Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);

        MenuItem mntmNewItem = new MenuItem(menu, SWT.NONE);
        mntmNewItem.setText("File");
        display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        MenuItem mntmNewItem_1 = new MenuItem(menu, SWT.NONE);
        mntmNewItem_1.setText("Edit");

        MenuItem help = new MenuItem(menu, SWT.NONE);
        help.setText("help");

        searchText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.keyCode==13){
                    String query = searchText.getText();
                    if(query!=""){
                        View.searching = true;
                        searchLabel.setImage(clearIcon);
                        table.clearAll();
                        noteController.search(query).stream().forEachOrdered(header -> {TableItem item = new TableItem(table, SWT.NONE);
                        item.setText(header.getHeader()); item.setData(header.getId());});
                        table.getColumn(0).pack();
                    }
                }
            }
        });

        searchLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                if(View.searching){
                    View.searching = false;
                    searchLabel.setImage(searchIcon);
                    searchText.setText("");
                } else {
                    String query = searchText.getText();
                    if(query!=""){
                        View.searching = true;
                        searchLabel.setImage(clearIcon);
                        table.clearAll();
                        noteController.search(query).stream().forEachOrdered(header -> {TableItem item = new TableItem(table, SWT.NONE);
                            item.setText(header.getHeader()); item.setData(header.getId());});
                        table.getColumn(0).pack();
                    }
                }

            }
        });

        newButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                //click new note
                throw new UnsupportedOperationException();

            }
        });

        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                //click delete
                throw new UnsupportedOperationException();
            }
        });

        syncButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                //click sync
                throw new UnsupportedOperationException();
            }
        });

        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                //click export
                throw new UnsupportedOperationException();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                //click table row
                System.out.println(table.getSelection()[0].getData());
                throw new UnsupportedOperationException();
            }
        });

    }

    private static Image getImage(String path) {
        try {
            Display display = Display.getCurrent();
            ImageData data = new ImageData(new ClassPathResource(path).getInputStream());
            if (data.transparentPixel > 0)
                return new Image(display, data, data.getTransparencyMask());
            return new Image(display, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
