import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
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
            @Override
            protected Control createContents(Composite parent) {
                parent.setLayout(new FillLayout());

                // Create the ScrolledComposite to scroll horizontally and
                // vertically
                ScrolledComposite sc = new ScrolledComposite(parent,
                        SWT.H_SCROLL | SWT.V_SCROLL);

                // Create a child composite to hold the controls
                Composite child = new Composite(sc, SWT.NONE);
                child.setLayout(new FillLayout());

                Composite group = new Composite(child, SWT.BORDER);
                group.setBackground(getShell().getDisplay().getSystemColor(
                        SWT.COLOR_WHITE));
                group.setLayout(new GridLayout(3, true));

                Image img = new Image(Display.getCurrent(), getClass()
                        .getResourceAsStream("/images/options.png")); //$NON-NLS-1$

                final ImageDescriptor imgDesc = ImageDescriptor
                        .createFromImage(img);

                new TeamspaceItem(group, "test", "first test item", imgDesc);
                new TeamspaceItem(group, "test2", "second test item", imgDesc);
                new TeamspaceItem(group, "test3", "thirt test item", imgDesc);
                new TeamspaceItem(group, "test4", "fourth test item", imgDesc);
                
                sc.setContent(child);
                sc.setMinSize(200, 200);
                sc.setExpandHorizontal(true);
                sc.setExpandVertical(true);
                return parent;
            }
        };
        window.setBlockOnOpen(true);
        window.open();
    }
}
