
package herramientas;

import java.awt.Dimension;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;

/**
 *
 * @author Alvaro Monsalve
 */
public class Explode {

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Dimension d;
    private int count = 0;
    private JPanel container;
    private JPanel content;
    private int px = 0;
    private int py = 0;
    private int ancho_min = 10;
    private int alto_min = 10;
    private int velocidad = 20;

    public Explode(JPanel container, JPanel content) {
        this.container = container;
        this.content = content;
        this.container.removeAll();
        d = new Dimension(10, 10);
        this.content.setSize(d);
        px = this.container.getSize().width / 2 - this.ancho_min / 2;
        py = this.container.getSize().height / 2 - this.alto_min / 2;
        content.setLocation(px, py);
        content.setVisible(true);
        this.container.add(content);
        count = 10;
    }
    
    public void play() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        //nuevo tamaño 
                        d = new Dimension(container.getSize().width * count / 100, container.getSize().height * count / 100);
                        count = count + 10;
                        //se recalcula la posicion mientras el jpanel crece
                        px = container.getSize().width / 2 - d.width / 2;
                        py = container.getSize().height / 2 - d.height / 2;
                        content.setLocation(px, py);
                        if (count > 100) {
                            close();
                        }
                        content.setSize(d);
                        container.updateUI();
                    }
                }, 100, velocidad, TimeUnit.MILLISECONDS);
    }
    
    public void close() {
        scheduler.shutdownNow();
    }
}
