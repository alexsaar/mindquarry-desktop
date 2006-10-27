import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TeamlistSample {

    public static void main(String[] args) {
        Shell shell = new Shell();
        ApplicationWindow window = new ApplicationWindow(shell) {
            /**
             * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
             */
            protected Control createContents(Composite parent) {
                GridLayout layout = new GridLayout(1, true);
                parent.setLayout(layout);

                // ScrolledComposite scroller = new ScrolledComposite(parent,
                // SWT.BORDER | SWT.V_SCROLL);
                // scroller.setLayout(layout);
                // scroller.setLayoutData(new GridData(GridData.FILL_BOTH));

                layout = new GridLayout(3, true);

                Composite group = new Composite(parent, SWT.BORDER);
                group.setBackground(getShell().getDisplay().getSystemColor(
                        SWT.COLOR_WHITE));
                group.setLayout(layout);
                group.setLayoutData(new GridData(GridData.FILL_BOTH));

                final Image img = new Image(Display.getCurrent(), getClass()
                        .getResourceAsStream("/images/options.png")); //$NON-NLS-1$

                Canvas canvas = new Canvas(group, SWT.NONE);
                canvas.setBackground(getShell().getDisplay().getSystemColor(
                        SWT.COLOR_WHITE));
                canvas.addPaintListener(new PaintListener() {
                    public void paintControl(PaintEvent e) {
                        e.gc.drawImage(img, 0, 0, 60, 50, 0, 0, 60, 50);
                    }
                });
                canvas = new Canvas(group, SWT.NONE);
                canvas.setBackground(getShell().getDisplay().getSystemColor(
                        SWT.COLOR_WHITE));
                canvas.addPaintListener(new PaintListener() {
                    public void paintControl(PaintEvent e) {
                        e.gc.drawImage(img, 0, 0, 60, 50, 0, 0, 60, 50);
                    }
                });
                canvas = new Canvas(group, SWT.NONE);
                canvas.setBackground(getShell().getDisplay().getSystemColor(
                        SWT.COLOR_WHITE));
                canvas.addPaintListener(new PaintListener() {
                    public void paintControl(PaintEvent e) {
                        e.gc.drawImage(img, 0, 0, 60, 50, 0, 0, 60, 50);
                    }
                });
                canvas = new Canvas(group, SWT.NONE);
                canvas.setBackground(getShell().getDisplay().getSystemColor(
                        SWT.COLOR_WHITE));
                canvas.addPaintListener(new PaintListener() {
                    public void paintControl(PaintEvent e) {
                        e.gc.drawImage(img, 0, 0, 60, 50, 0, 0, 60, 50);
                    }
                });
                
                Button button = new Button(group, SWT.TOGGLE);
                button.setBackground(group.getBackground());
                button.setText("Synchronize");
                button.setImage(img);
                return parent;
            }
        };
        window.setBlockOnOpen(true);
        window.open();
    }
}
