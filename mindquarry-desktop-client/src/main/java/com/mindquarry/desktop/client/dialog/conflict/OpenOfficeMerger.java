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
package com.mindquarry.desktop.client.dialog.conflict;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.connection.ConnectionSetupException;
import com.sun.star.connection.NoConnectException;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class that starts OpenOffice and that lets the user merge
 * two documents manually.
 * 
 * @author Daniel Naber
 */
public class OpenOfficeMerger implements Merger {
  
  private static final Log log = LogFactory.getLog(OpenOfficeMerger.class);
  
  private static final String UNO_URL =
    "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";

  private static final long WAIT_AMOUNT = 250;    // milliseconds
  private static final int MAX_TRIES = 100;

  private static final String SOFFICE_CMD = "soffice";
  private static final String SOFFICE_PARAM1 = "-accept=socket,port=8100;urp;";
  // this makes the soffice window invisible, the swriter window will still
  // be visible:
  private static final String SOFFICE_PARAM2 = "-invisible";  

  public OpenOfficeMerger() {
  }
  
  /**
   * Start OpenOffice in merge mode.
   * NOTE: this method returns immediately, the return value thus
   * isn't valid yet. Ask the user to click a button when he's finished.
   * @throws IOException 
   */
  public File merge(File file1, File file2) throws IOException {
    // TODO: better way to build file URLs?
    String url1 = "file://" + file1.getAbsolutePath();
    String url2 = "file://" + file2.getAbsolutePath();

    try {
      // 1. this version does all the work for us, it starts OOo if it isn't running
      // yet. However, we need to reference the original JARs (juh.jar etc) in the 
      // OOo installation directory (not the ones we provide) so this seems useless
      // as OOo might be installed anywhere
      //XComponentContext xRemoteContext = com.sun.star.comp.helper.Bootstrap.bootstrap();

      // 2. access a running version, if no one is running we need to start
      // it ourselfes
      XComponentContext xLocalContext = com.sun.star.comp.helper.Bootstrap.createInitialComponentContext(null); 
      XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager(); 
      Object urlResolver  = xLocalServiceManager.createInstanceWithContext("com.sun.star.bridge.UnoUrlResolver", xLocalContext); 
      XUnoUrlResolver xUnoUrlResolver = (XUnoUrlResolver) UnoRuntime.queryInterface(XUnoUrlResolver.class, urlResolver); 
      Object initialObject = getConnection(xUnoUrlResolver); 
      XPropertySet xPropertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, initialObject); 
      Object remoteContext = xPropertySet.getPropertyValue("DefaultContext");
      XComponentContext xRemoteContext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, remoteContext); 
      
      XMultiComponentFactory xRemoteServiceManager = 
        xRemoteContext.getServiceManager(); 

     if (xRemoteServiceManager == null) {
       throw new NullPointerException("xRemoteServiceManager not available");
     }
 
     // get the Desktop, we need its XComponentLoader interface to load a new document 
     Object desktop = xRemoteServiceManager.createInstanceWithContext( 
         "com.sun.star.frame.Desktop", xRemoteContext); 

     // query the XComponentLoader interface from the desktop 
     XComponentLoader xComponentLoader = (XComponentLoader)UnoRuntime.queryInterface( 
         XComponentLoader.class, desktop); 

     // create empty array of PropertyValue structs, needed for loadComponentFromURL 
     PropertyValue[] loadProps = new PropertyValue[0]; 

     // load text file 
     xComponentLoader.loadComponentFromURL( 
         url1, "_blank", 0, loadProps);
     
     XDesktop myDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
     XFrame frame = myDesktop.getCurrentFrame();
     XDispatchProvider dispatchProvider = (XDispatchProvider) 
       UnoRuntime.queryInterface(XDispatchProvider.class, frame);
     
     PropertyValue[] compareProps = new PropertyValue[1];
     compareProps[0] = new PropertyValue();
     compareProps[0].Name = "URL";
     compareProps[0].Value = url2;

     Object dispatchHelper = xRemoteServiceManager.createInstanceWithContext(
         "com.sun.star.frame.DispatchHelper", xRemoteContext );
     XDispatchHelper xDispatchHelper =
       (XDispatchHelper) UnoRuntime.queryInterface(XDispatchHelper.class, dispatchHelper);

     // warning: returns immediately
     xDispatchHelper.executeDispatch(dispatchProvider, ".uno:CompareDocuments", "", 
       0, compareProps);
    } catch (NoConnectException e) {
      IOException ioe = new IOException(e.toString());
      ioe.initCause(e);
      throw ioe;
    } catch (Exception e) {
      throw new RuntimeException(e.toString(), e);
    }

    // the user is supposed to save (not "save as..."), so just
    // return the original file:
    return file1;
  }

  /**
   * Start OpenOffice with parameters that allow connecting it
   * and wait until the connection is possible or until the maxmim
   * number of retries has been reached. 
   */
  private Object getConnection(XUnoUrlResolver xUnoUrlResolver) throws 
      ConnectionSetupException, IllegalArgumentException, IOException, NoConnectException {
    Object initialObject = null;
    try {
      initialObject = xUnoUrlResolver.resolve(UNO_URL); 
      log.info("Succesfully connected to OpenOffice (already running)");
    } catch (NoConnectException e) {
      String[]  cmdArray = new String[] {SOFFICE_CMD, SOFFICE_PARAM1, SOFFICE_PARAM2};
      log.info("Could no connect to OpenOffice, trying to start '" +
          Arrays.asList(cmdArray) + "'");
      Runtime.getRuntime().exec(cmdArray);
      int tryCount = 1;
      while (true) {
        log.info("Trying to connect to OpenOffice (" +tryCount+ " of " +MAX_TRIES+ " attempts)");
        initialObject = tryGettingConnection(xUnoUrlResolver);
        if (initialObject != null) {
          log.info("Succesfully connected to OpenOffice");
          break;
        }
        try {
          Thread.sleep(WAIT_AMOUNT);
        } catch (InterruptedException e1) {
          throw new RuntimeException(e1);
        }
        tryCount++;
        if (tryCount > MAX_TRIES) {
          throw new NoConnectException("Couldn't connect OpenOffice even after " + 
              MAX_TRIES + " tries, giving up");
        }
      }
    }
    return initialObject;
  }

  /**
   * Try to connect a running instance of OpenOffice. Return null
   * if we cannot connect.
   */
  private Object tryGettingConnection(XUnoUrlResolver xUnoUrlResolver) throws ConnectionSetupException, IllegalArgumentException {
    Object initialObject = null;
    try {
      initialObject = xUnoUrlResolver.resolve(UNO_URL); 
    } catch (NoConnectException e) {
      return null;
    }
    return initialObject;
  }

  /* test only:
  public static void main(String[] args) throws IOException {    
    File file1 = new File("/home/dnaber/foo1.odt");
    File file2 = new File("/home/dnaber/foo2.odt");
    OpenOfficeMerger prg = new OpenOfficeMerger();
    prg.merge(file1, file2);
  }*/

}
