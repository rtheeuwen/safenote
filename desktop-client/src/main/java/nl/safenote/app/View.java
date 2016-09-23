package nl.safenote.app;

import nl.safenote.controllers.AuthenticationController;
import nl.safenote.controllers.NoteController;
import nl.safenote.model.Header;
import nl.safenote.model.Note;
import nl.safenote.utils.FileIO;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.TimeZone;

@Component
public class View {

    private Shell shell;
    private Font font;
    private Font tableFont;
    private static Table table;
    private StyledText styledText;
    private Text searchText;
    private Color backGroundColor;
    private static boolean searching;
    private static String text;

    private static AuthenticationController authenticationController;
    private static NoteController noteController;

    private static Note activeNote;

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
        if(activeNote!=null&&!Objects.equals(activeNote.getContent(), text)){
            activeNote.setContent(text);
            noteController.updateNote(activeNote);
        }
    }

    private void createContents(final Display display, final boolean generate){
        shell = new Shell();
        shell.setSize(1080, 695);
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
                            getHeaders();
                    }
                    else
                        wrongPassLabel.setVisible(true);
                } else {
                    if(View.authenticationController.authenticate(passphraseText.getText())){
                        layout.topControl = workbench;
                        login.dispose();
                        shell.layout();
                        getHeaders();
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
                            getHeaders();
                        }
                        else
                            wrongPassLabel.setVisible(true);
                    } else {
                        if(View.authenticationController.authenticate(passphraseText.getText())){
                            layout.topControl = workbench;
                            login.dispose();
                            shell.layout();
                            getHeaders();
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
                        getHeaders();
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
        workbench.setLayout(new GridLayout(10, false));

        backGroundColor = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
//        backGroundColor = display.getSystemColor(SWT.BACKGROUND);

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
        newButton.setText("   new note    ");

        Image deleteIcon = getImage("/delete.gif");
        Button deleteButton = new Button(workbench, SWT.PUSH);
        deleteButton.setBackground(backGroundColor);
        deleteButton.setImage(deleteIcon);
        deleteButton.setText("  delete note  ");

        Image infoIcon = getImage("/info.gif");
        Button infoButton = new Button(workbench, SWT.PUSH);
        infoButton.setBackground(backGroundColor);
        infoButton.setImage(infoIcon);
        infoButton.setText("   note info   ");

        Image syncIcon = getImage("/sync.gif");
        Button syncButton = new Button(workbench, SWT.PUSH);
        syncButton.setBackground(backGroundColor);
        syncButton.setImage(syncIcon);
        syncButton.setText("  synchronize  ");

        Image exportIcon = getImage("/lock.gif");

        Button exportButton = new Button(workbench, SWT.PUSH);
        exportButton.setBackground(backGroundColor);
        exportButton.setImage(exportIcon);
        exportButton.setText("  export key   ");

        Composite topRightFiller = new Composite(workbench, SWT.NONE);
        GridData gd_topRightFiller = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_topRightFiller.minimumWidth = 120;
        gd_topRightFiller.widthHint = 120;
        topRightFiller.setLayoutData(gd_topRightFiller);

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


        styledText = new StyledText(workbench, SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
        GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1);
        gd_styledText.widthHint = 600;
        gd_styledText.minimumWidth=600;
        styledText.setLayoutData(gd_styledText);
        int margin = 50;
        styledText.setMargins(margin, margin, margin, 10);
        styledText.setWordWrap(true);
        styledText.setFont(font);
        styledText.setLineSpacing(12);

        //POPULATE VIEWER
        styledText.setText("Welcome to SafeNote!");
        styledText.setSelection(styledText.getText().length());

        Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);

        MenuItem mntmNewItem = new MenuItem(menu, SWT.NONE);
        mntmNewItem.setText("File");
        display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        MenuItem mntmNewItem_1 = new MenuItem(menu, SWT.NONE);
        mntmNewItem_1.setText("Edit");

        MenuItem help = new MenuItem(menu, SWT.NONE);
        help.setText("help");

        /******************************************WORKBENCH METHODS***************************************************/

        searchText.addListener( SWT.KeyDown, event -> {
            if(event.keyCode==13){
                String query = searchText.getText();
                if(query!=""){
                    View.searching = true;
                    searchLabel.setImage(clearIcon);
                    table.clearAll();
                    java.util.List<Header> headers = noteController.search(query);
                    for(int i=0; i<headers.size(); i++){
                        Header header = headers.get(i);
                        TableItem item = new TableItem(table, SWT.NONE);
                        item.setText(header.getHeader());
                        item.setData(header.getId());
                        if((i&1)==1)
                            item.setBackground(backGroundColor);
                    }

                    table.getColumn(0).pack();

                }
            }
        });

        searchLabel.addListener( SWT.MouseUp, event -> {
            if(View.searching){
                View.searching = false;
                searchLabel.setImage(searchIcon);
                searchText.setText("");
            } else {
                String query = searchText.getText();
                if(query.length()!=0&&query!=null){
                    View.searching = true;
                    searchLabel.setImage(clearIcon);
                    table.removeAll();
                    noteController.search(query).stream().forEachOrdered(header -> {TableItem item = new TableItem(table, SWT.NONE);
                        item.setText(header.getHeader()); item.setData(header.getId());});
                    table.getColumn(0).pack();
                }
            }
        });

        newButton.addListener( SWT.MouseUp, event -> {
            String id = noteController.createNote();
            openNote(id);
            getHeaders();
            table.select(0);
        });

        deleteButton.addListener( SWT.MouseUp, event -> {
            if(activeNote!=null&&MessageDialog.openConfirm(shell, "confirm", "Delete "+ activeNote.getHeader()+"?")) {
                noteController.deleteNote(activeNote.getId());
                activeNote = null;
                styledText.setText("");
                getHeaders();
                openNote(table.getItem(0).getData().toString());
                table.select(0);
            }
        });

        infoButton.addListener( SWT.MouseUp, event -> {
            if(activeNote!=null) {
                StringBuilder message = new StringBuilder();
                String header = activeNote.getHeader();
                message.append(Objects.equals("", header)?"New note": header);
                message.append("\n\nCreated on: " + formatDateTime(activeNote.getCreated()));
                message.append("\nLast modified on: " + formatDateTime(activeNote.getModified()));
                message.append("\nRevision: " + activeNote.getVersion());
                MessageDialog.openInformation(shell, "note information", message.toString());
            }
        });

        syncButton.addListener( SWT.MouseUp, event -> {
            String message;
            if(noteController.synchronize())
                message = "Synchronized successfully.";
            else
                message = "Could not contact server";
            MessageDialog.openInformation(shell, "synchronization", message);
        });

        exportButton.addListener( SWT.MouseUp, event -> {
            new KeyDisplay(shell, SWT.NONE).open();
        });


        table.addListener( SWT.MouseUp, event -> {
            TableItem[] active = table.getSelection();
            int index = table.getSelectionIndex();
            if(active.length!=0){
                openNote(active[0].getData().toString());
                getHeaders();
                table.select(index);
            }
        });

        styledText.addListener(SWT.Modify, e -> {
            text = styledText.getText();
            int currentIndex = styledText.getLineAtOffset(styledText.getCaretOffset());
            if(!(styledText.getTopIndex()-20<currentIndex))
                styledText.setTopIndex(styledText.getLineAtOffset(styledText.getCaretOffset()) - 1);
        });

        styledText.addListener(SWT.KeyUp, e -> {
            if(e.keyCode==13&&styledText.getLineAtOffset(styledText.getCaretOffset())==1){
                if(activeNote!=null&&!Objects.equals(activeNote.getContent(), styledText.getText())){
                    activeNote.setContent(styledText.getText());
                    noteController.updateNote(activeNote);
                    activeNote.setHeader(noteController.getNote(activeNote.getId()).getHeader());
                    TableItem[] active = table.getSelection();
                    int index = table.getSelectionIndex();
                    if(active.length!=0){
                        getHeaders();
                        table.select(index);
                    }
                }
            }
        });

    }

    private void getHeaders(){
        table.removeAll();
        java.util.List<Header> headers = noteController.getHeaders();
        if(headers.size()==0){
            String id = noteController.createNote();
            openNote(id);
            getHeaders();
            table.select(0);
            styledText.forceFocus();
        } else {
            headers.stream().forEachOrdered(header -> {
                TableItem item = new TableItem(table, SWT.NONE);
                item.setText("\n      " + header.getHeader() + "\n");
                item.setData(header.getId());
            });

            table.getColumn(0).pack();
        }
    }

    private void openNote(String id){
        if(activeNote!=null&&!Objects.equals(activeNote.getContent(), styledText.getText())){
            activeNote.setContent(styledText.getText());
            noteController.updateNote(activeNote);
        }
        activeNote = noteController.getNote(id);
        styledText.setText(activeNote.getContent());
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

    private static String formatDateTime(long dateTime){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), TimeZone
                .getDefault().toZoneId()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

    }

    /****************************************** KEY EXPORT DIALOG ***************************************************/


    static class KeyDisplay extends Dialog {

        protected Shell shell;

        /**
         * Create the dialog.
         * @param parent
         * @param style
         */
        public KeyDisplay(Shell parent, int style) {
            super(parent, style);
            setText("Key");
        }

        /**
         * Open the dialog.
         * @return the result
         */
        public void open() {
            createContents();
            shell.open();
            shell.layout();
            Display display = getParent().getDisplay();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        }

        /**
         * Create contents of the dialog.
         */
        private void createContents() {
            shell = new Shell(getParent(), getStyle());
            Display display = getParent().getDisplay();
            Rectangle screen = display.getPrimaryMonitor().getClientArea();
            shell.setSize(screen.width, screen.height);
            shell.setFullScreen(true);
            shell.setText(getText());
            shell.setLayout(new FillLayout(SWT.HORIZONTAL));

            Composite composite = new Composite(shell, SWT.NONE);
            composite.setLayout(new GridLayout(1, false));
            composite.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

            Color white = display.getSystemColor(SWT.COLOR_WHITE);
            Color black = display.getSystemColor(SWT.COLOR_BLACK);
            PaletteData palette = new PaletteData(new RGB[] { white.getRGB(), black.getRGB() });
            ImageData sourceData = new ImageData(16, 16, 1, palette);
            sourceData.transparentPixel = 0;
            Cursor cursor = new Cursor(display, sourceData, 0, 0);
            composite.setCursor(cursor);

            Image key = new Image(getParent().getDisplay(), FileIO.getKeyDir());
            Label keyLabel = new Label(composite, SWT.CENTER);
            GridData gd_keyLabel = new GridData(SWT.FILL, SWT.FILL, true, true);
            gd_keyLabel.widthHint = 1000;
            gd_keyLabel.heightHint = 1000;
            keyLabel.setLayoutData(gd_keyLabel);
            keyLabel.setImage(key);
            keyLabel.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

            keyLabel.addListener(SWT.MouseUp, e -> {
                shell.dispose();
            });

            shell.addListener(SWT.KeyUp, e-> {
                shell.dispose();
            });
        }
    }
}
