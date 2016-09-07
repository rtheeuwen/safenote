package safenote.client.app;

import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

class LoadingScreen {

    private final JFrame frame;

    public LoadingScreen() throws IOException{
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setBackground(new Color(0,0,0,0));

        frame.setPreferredSize(new Dimension(256, 256));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-128, dim.height/2-128);
        frame.setVisible(true);

        JLabel label = new JLabel(new ImageIcon(new ClassPathResource("/WEB-INF/img/logo.png").getURL()));

        frame.add(label);
        frame.pack();
    }

    public void done(){
        frame.dispose();
    }
}
