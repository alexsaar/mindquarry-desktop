import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class TeamspaceItem extends Composite {
    private Canvas image;

    private CLabel label;

    public TeamspaceItem(Composite parent, String name, String description,
            final ImageDescriptor icon) {
        super(parent, SWT.NONE);

        // configure teamspace item
        setBackground(getParent().getBackground());

        // layout the teamspace item
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        setLayout(layout);

        Menu menu = new Menu(this);
        MenuItem synchItem = new MenuItem(menu, SWT.PUSH);
        synchItem.setText("Synchronize...");

        image = new Canvas(this, SWT.NONE);
        image.setLayoutData(new GridData(GridData.FILL_BOTH));

        initChild(image, description, menu);

        image.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                e.gc.drawImage(icon.createImage(), 0, 0,
                        icon.getImageData().width, icon.getImageData().height,
                        0, 0, icon.getImageData().width,
                        icon.getImageData().height);
            }
        });
        label = new CLabel(this, SWT.CENTER);
        label.setText(name);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setFont(new Font(getDisplay(), "Arial", 10, SWT.NORMAL)); //$NON-NLS-1$
        initChild(label, description, menu);
    }

    private void initChild(Control child, String description, Menu menu) {
        child.setBackground(getBackground());
        child.setMenu(menu);
        child.setToolTipText(description);
        child.addMouseTrackListener(new MouseOverListener());
        child.setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
        child.addMouseListener(new MouseClickListener());
    }

    class MouseClickListener implements MouseListener {
        /**
         * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseDoubleClick(MouseEvent e) {
            System.out.println("double clicked");
        }

        /**
         * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseDown(MouseEvent e) {
            System.out.println("down");
        }

        /**
         * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseUp(MouseEvent e) {
            System.out.println("up");
        }
    }

    class MouseOverListener implements MouseTrackListener {
        /**
         * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseEnter(MouseEvent e) {
            image.setBackground(getShell().getDisplay().getSystemColor(
                    SWT.COLOR_LIST_SELECTION));
            label.setBackground(getShell().getDisplay().getSystemColor(
                    SWT.COLOR_LIST_SELECTION));
        }

        /**
         * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseExit(MouseEvent e) {
            image.setBackground(getBackground());
            label.setBackground(getBackground());
        }

        /**
         * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseHover(MouseEvent e) {
            // nothing to do here
        }
    }
}
