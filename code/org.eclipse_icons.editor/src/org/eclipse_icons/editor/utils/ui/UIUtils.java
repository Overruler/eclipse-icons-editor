package org.eclipse_icons.editor.utils.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse_icons.editor.Activator;
import org.osgi.framework.Bundle;

/**
 * @author Jabier Martinez
 */
public class UIUtils {

	public static final String[] IMAGE_EXTENSIONS = {"*.gif", "*.png", "*.bmp", "*.jpg"};
	
	public static void refreshWorkspace() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		try {
			root.refreshLocal(IResource.DEPTH_INFINITE,
							new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static void refreshWorkspace(String fullPath) {

		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			File file = new File(fullPath);
			IResource[] toRefresh = null;
			if (file.isDirectory()) {
				toRefresh = root.findContainersForLocationURI(file.toURI());
			} else if (file.isFile()) {
				toRefresh = root.findFilesForLocationURI(file.toURI());
			}
			for (IResource c : toRefresh) {
				c.refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getFileAbsolutePathFromPlugin(String relativePathToFile){
        Bundle bundle = Activator.getDefault().getBundle();
        IPath path = new Path(relativePathToFile);
        URL url = FileLocator.find(bundle, path, Collections.EMPTY_MAP);
        try {
			File file = new File(FileLocator.toFileURL(url).toURI());
			return file.getAbsolutePath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	public static String getEclipseAbsolutePath(){
		Location installLocation = Platform.getInstallLocation();
		File file = new File(installLocation.getURL().getFile());
		return file.getAbsolutePath();
	}
	
	public static IEditorPart openFile(String filePath){
		File fileToOpen = new File(filePath);

		if (fileToOpen.exists() && fileToOpen.isFile()) {
		   //IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
			IFileStore fileStore = null;
			try {
				fileStore = EFS.getStore(fileToOpen.toURI());
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		   IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		   try {
		       return IDE.openEditorOnFileStore( page, fileStore );
		   } catch ( PartInitException e ) {
			 //Put your exception handler here if you wish to
			e.printStackTrace();
		   }
		} else {
		   //Do something if the file does not exist
			System.out.println(filePath + " doesn't exist.");
		}
		return null;
	}
	
	public static IFile getIFileFromPlatformURI(String uri){
		if (!uri.startsWith("platform:/resource/"))
			return null;
		String locationString = uri.substring("platform:/resource/".length());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IFile iFile = (IFile) workspaceRoot.findMember(locationString);
		return iFile;
	}
	
	public static IFile getIFileFromFileURI(String path){
		String workspacePath = getWorkspacePath().toString();
		if (!path.startsWith("file:/"+workspacePath)){
			return null;
		}
		String relativePath = path.substring("file:/".length()+workspacePath.length());
		return getIFileFromPlatformURI("platform:/resource"+relativePath);
	}
	
	public static  IPath getWorkspacePath(){
		return Platform.getLocation();
	}
	
	public static void openExternalBrowser(URL url){
		try {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public static boolean isImageFile(String fileName) {
		int dot = fileName.lastIndexOf(".");
		if (dot==-1) {
			return false;
		} else {
			String fileExt = fileName.substring(dot,fileName.length());
			for (String extension : IMAGE_EXTENSIONS){
				// remove *
				extension = extension.substring(1);
				if (fileExt.equalsIgnoreCase(extension)){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isImageFile(IResource resource){
		return isImageFile(resource.getName());
	}
	
	public static boolean isImageFile(File file){
		return isImageFile(file.getName());
	}
	
	public static boolean isTransparentImageFile(IResource iResource){
		String extension = iResource.getFileExtension();
		if (extension!=null && extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif")){
			return true;
		}
		return false;
	}
	
	public static boolean folderContainsImages(IFolder folder){
		try {
			for (IResource resource: folder.members()){
				if (resource instanceof IFolder){
					if (folderContainsImages((IFolder)resource)){
						return true;
					}
				} else {
					if (isImageFile(resource)){
						return true;
					}
				}
			}
		} catch (CoreException e) {
			return false;
		}
		return false;
	}
	
	public static boolean isTheResourceContainedInAConcreteFolder(IResource resource, String folderName){
		IContainer container = resource.getParent();
		while (container!=null && !(container instanceof IProject)){
			if (container.getName()!=null && container.getName().equalsIgnoreCase(folderName)){
				return true;
			}
			container = container.getParent();
		}
		return false;
	}

	public static Image getImageFromResource(IResource resource) {
		return getImage(getIResourceAbsPath(resource));
	}
	
	public static Image getImage(String absolutePath) {
		try {
			if (absolutePath == null){
				return null;
			}
			return new Image(Display.getDefault(), new FileInputStream(absolutePath));
		} catch (Exception e) {
			// If any exception happens return null
			return null;
		}
	}
	
	// TODO use this in isImageFile also
	public static String getFileSuffix(final String path) {
	    String result = null;
	    if (path != null) {
	        result = "";
	        if (path.lastIndexOf('.') != -1) {
	            result = path.substring(path.lastIndexOf('.'));
	            if (result.startsWith(".")) {
	                result = result.substring(1);
	            }
	        }
	    }
	    return result;
	}

	public static String getIResourceAbsPath(IResource resource){
		if (resource.getLocation() == null){
			return null;
		}
		return resource.getLocation().toOSString();
	}
	
	public static boolean checkIfWritable(String fileAbsPath){
		// Check if read only
		File file = new File(fileAbsPath);
		if (file.exists() && !file.canWrite()){
			boolean result = MessageDialog.openQuestion(Display.getDefault()
					.getActiveShell(), "File cannot be written",
					"This file is read only. Do you want to make it writable?");
			if (result){
				file.setWritable(true);
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
	
}
