import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * An OverlayIcon consists of a main icon and an overlay icon
 */
public class OverlayIcon extends CompositeImageDescriptor {
    // the size of the OverlayIcon
    private Point fSize = null;

    // the main image
    private ImageDescriptor fBase = null;

    // the additional image (a pin for example)
    private ImageDescriptor fOverlay = null;

    /**
     * @param base the main image
     * @param overlay the additional image (a pin for example)
     * @param size the size of the OverlayIcon
     */
    public OverlayIcon(ImageDescriptor base, ImageDescriptor overlay, Point size) {
        fBase = base;
        fOverlay = overlay;
        fSize = size;
    }

    /**
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int,
     *      int)
     */
    @Override
    protected void drawCompositeImage(int width, int height) {
        ImageData bg;
        if (fBase == null || (bg = fBase.getImageData()) == null) {
            bg = DEFAULT_IMAGE_DATA;
        }
        drawImage(bg, 0, 0);

        if (fOverlay != null) {
            if (fOverlay == null) {
                return;
            }
            int x = getSize().x;
            ImageData id = fOverlay.getImageData();
            x -= id.width;
            drawImage(id, x, 0);
        }
    }

    /**
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
     */
    @Override
    protected Point getSize() {
        return fSize;
    }
}
