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
package com.mindquarry.desktop.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

/**
 * Guess the MIME type of a File. Based on
 * com.mindquarry.search.cocoon.TextFilterGenerator.
 * 
 * TODO: Move class to mindquarry-common and refactor TextFilterGenerator to use
 * this utility class.
 * 
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com"> Lars
 *         Trieloff</a>
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 * @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">
 *         Christian Richardt</a>
 * 
 */
public class MimeTypeUtilities {

    /**
     * "Stupid" mime type which only says that the content is a binary stream.
     * Often used when the actual mime type is unknown.
     */
    private static final String GENERIC_BINARY_MIMETYPE = "application/octet-stream";
    
    private static MimetypesFileTypeMap fileTypeMap = null;

    private static void initFileTypeMap() {
        fileTypeMap = new MimetypesFileTypeMap();

        // Additional MIME types
        // (http://www.forensicinnovations.com/formats-mime.html, 14 Sep 2007)
        fileTypeMap.addMimeTypes("application/atom+xml atom");
        fileTypeMap.addMimeTypes("application/msword doc dot");
        fileTypeMap.addMimeTypes("application/mspowerpoint ppt pot");
        fileTypeMap.addMimeTypes("application/msexcel xls");
        fileTypeMap.addMimeTypes("application/pdf pdf");
        fileTypeMap.addMimeTypes("application/rdf+xml rdf rss");
        fileTypeMap.addMimeTypes("application/x-vnd.openxmlformat docx docm dotx dotm");
        fileTypeMap.addMimeTypes("application/x-vnd.openxmlformat xlsx xlsm");
        fileTypeMap.addMimeTypes("application/x-vnd.openxmlformat pptx pptm potx");
        fileTypeMap.addMimeTypes("application/x-javascript js");
        fileTypeMap.addMimeTypes("application/x-rar-compressed rar");
        fileTypeMap.addMimeTypes("application/x-textedit bat cmd");
        fileTypeMap.addMimeTypes("application/zip zip");
        fileTypeMap.addMimeTypes("audio/mpeg mp3");
        fileTypeMap.addMimeTypes("image/bmp bmp");
        fileTypeMap.addMimeTypes("image/gif gif");
        fileTypeMap.addMimeTypes("image/jpeg jpg jpeg jpe");
        fileTypeMap.addMimeTypes("image/png png");
        fileTypeMap.addMimeTypes("text/css css");
        fileTypeMap.addMimeTypes("text/csv csv");
        fileTypeMap.addMimeTypes("text/html htm html");
        fileTypeMap.addMimeTypes("text/xml xml");

        FileTypeMap.setDefaultFileTypeMap(fileTypeMap);
    }
    
    /**
     * The "application/octet-stream" mime type is generic and does not give any
     * information just like the null String.
     */
    public static boolean isUndefined(String mimeType) {
        return (mimeType == null || GENERIC_BINARY_MIMETYPE.equals(mimeType));
    }

    /**
     * Convenience method that calls returns guessMimetype(new File(filename)).
     */
    public static String guessMimetype(String filename) {
        return guessMimetype(new File(filename));
    }

    /**
     * Guess the MIME type of a file. Firstly, it looks up the file extension in
     * a table. If the extension is not in the table, the content of the file is
     * examined using jmimemagic, in fashion similar to the unix 'file' command.
     * 
     * @param file
     *            The file of which the MIME type is to be determined.
     * @return Best guess of the MIME type, GENERIC_BINARY_MIMETYPE if unknown.
     */
    public static String guessMimetype(File file) {
        String mimeType = GENERIC_BINARY_MIMETYPE;

        // check for the file types table with the File object
        if (isUndefined(mimeType)) {
            mimeType = guessMimetypeUsingMap(file);
        }

        // check for the file types table with the File object
        if (isUndefined(mimeType) && file.exists()) {
            mimeType = guessMimetypeUsingJmimemagic(file);
        }

        return mimeType;
    }

    /**
     * Guess the MIME type of a file by looking up the file extension. Uses the
     * information from the MimetypesFileTypeMap provided by Java plus some
     * additionally added MIME types for common files.
     * 
     * @param file
     *            The file of which the MIME type is to be determined.
     * @return Recognised MIME type, GENERIC_BINARY_MIMETYPE if unknown.
     */
    public static String guessMimetypeUsingMap(File file) {
        if(fileTypeMap == null) {
            initFileTypeMap();
        }

        // check for the file types table with the File object
        return MimetypesFileTypeMap.getDefaultFileTypeMap()
                .getContentType(file);
    }

    /**
     * Guess the MIME type of a file using the jmimemagic parser which uses the
     * magic bytes data stored at the beginning of most files to obtain the
     * actual mime type.by looking up the file extension.
     * 
     * @param file
     *            The file of which the MIME type is to be determined.
     * @return Recognised MIME type, GENERIC_BINARY_MIMETYPE if unknown.
     */
    public static String guessMimetypeUsingJmimemagic(File file) {
        // call the jmimemagic parser with the File object
        try {
            String mimeType = Magic.getMagicMatch(file, true).getMimeType();
            if (!mimeType.equals("???")) {
                return mimeType;
            } else {
                return GENERIC_BINARY_MIMETYPE;
            }
        } catch (MagicParseException e) {
        } catch (MagicMatchNotFoundException e) {
        } catch (MagicException e) {
            // ignore all exceptions since we are guessing only
        }

        return GENERIC_BINARY_MIMETYPE;
    }
    
    /**
     * For debugging. Guesses the MIME types of a collection of file extensions.
     * 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // 1. Test guessMimetypeUsingMap(...) using a list of extensions
        String[] testCases = new String[] { "atom", "avi", "bat", "bmp", "css",
                "csv", "doc", "docx", "gif", "htm", "html", "jpg", "jpeg",
                "js", "mov", "mp3", "mpg", "mpeg", "pdf", "png", "ppt", "pptx",
                "rar", "rss", "rtf", "tiff", "txt", "wav", "xls", "xlsx",
                "xml", "zip",

                "exe", "ics", "mp4", "php", "pl", "py", // TODO: check these
        };

        for (String testCase : testCases) {
            System.out.println(testCase
                    + " => "
                    + MimeTypeUtilities.guessMimetypeUsingMap(new File("test."
                            + testCase)));
        }

        // ---------------------------------------------------------------------
        
        // 2. Test guessMimetype using random files in temp dir
        File tempFile = File.createTempFile("prefix", "suffix");
        File tempDirFile = tempFile.getParentFile();
        tempFile.delete();
        
        List<String> results = new ArrayList<String>();

        for (String child : tempDirFile.list()) {
            File childFile = new File(tempDirFile, child);
            if (childFile.isFile()) {
                results.add(child + " => "
                        + MimeTypeUtilities.guessMimetypeUsingMap(childFile)
                        + " vs " + MimeTypeUtilities
                                .guessMimetypeUsingJmimemagic(childFile)
                        + " => " + MimeTypeUtilities.guessMimetype(childFile));
            }
        }

        System.out.println("==============================================");
        for(String line : results) {
            System.out.println(line);
        }
    }
}
