/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package com.mindquarry.desktop.client.widget.util;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

public class ImageAdaptor extends BufferedImage {
    public ImageAdaptor(int inWidth, int inHeight) {
        super(inWidth, inHeight, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public Image toSwtImage() {
        // This implementation is horribly unoptimized.
        int vnWidth = getWidth();
        int vnHeight = getHeight();
        int vnDepth = 32;

        PaletteData vpPalette = new PaletteData(0xff, 0xff00, 0xff0000);

        int vnScanlinePad = vnWidth * vnDepth / 8;
        WritableRaster vpRaster = getRaster();
        DataBufferByte vpBuffer = (DataBufferByte) vpRaster.getDataBuffer();

        byte[] vabData = vpBuffer.getData();
        ImageData vpImageData = new ImageData(vnWidth, vnHeight, vnDepth,
                vpPalette, vnScanlinePad, vabData);
        WritableRaster vpAlphaRaster = getAlphaRaster();
        DataBufferByte vpAlphaBuffer = (DataBufferByte) vpAlphaRaster
                .getDataBuffer();

        byte[] vabAlphaData = vpAlphaBuffer.getData();
        for (int vnX = 0; vnX < vnWidth; ++vnX) {
            for (int vnY = 0; vnY < vnHeight; ++vnY) {
                vpImageData.setAlpha(vnX, vnY, 0xff & vabAlphaData[(vnY
                        * vnWidth + vnX) * 4]);
            }
        }
        Image vpImage = new Image(Display.getDefault(), vpImageData);
        return vpImage;
    }
}
