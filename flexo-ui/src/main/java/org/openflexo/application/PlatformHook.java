// License: GPL. For details, see LICENSE file.
package org.openflexo.application;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.PrintFilesEvent;
import java.awt.desktop.PrintFilesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.AboutDialog;
import org.openflexo.foundation.utils.OperationCancelledException;
import org.openflexo.module.ModuleLoader;
import org.openflexo.toolbox.StringUtils;

/**
 * This interface allows platform (operating system) dependent code to be bundled into self-contained classes.
 * 
 * Note that this code was originally inspired from JOSM software, released with GPL licence.<br>
 * see git://github.com/openstreetmap/josm.git
 * 
 * @since 1023
 */
public abstract class PlatformHook implements QuitHandler, AboutHandler, PreferencesHandler, OpenFilesHandler, PrintFilesHandler {

	protected static final Logger logger = Logger.getLogger(PlatformHook.class.getPackage().getName());

	private ApplicationContext applicationContext;

	/**
	 * Visitor to construct a PlatformHook from a given {@link Platform} object.
	 */
	public static PlatformVisitor<PlatformHook> CONSTRUCT_FROM_PLATFORM = new PlatformVisitor<PlatformHook>() {
		@Override
		public PlatformHook visitUnixoid() {
			return new PlatformHookUnixoid();
		}

		@Override
		public PlatformHook visitWindows() {
			return new PlatformHookWindows();
		}

		@Override
		public PlatformHook visitOsx() {
			return new PlatformHookOsx();
		}
	};

	/**
	 * Get the platform corresponding to this platform hook.
	 * 
	 * @return the platform corresponding to this platform hook
	 */
	public abstract Platform getPlatform();

	/**
	 * The preStartupHook will be called extremly early.<br>
	 * It is guaranteed to be called before the GUI setup has started.
	 */
	public void preStartupHook() {
		// Do nothing by default
	}

	/**
	 * The afterPrefStartupHook will be called early, but after the preferences have been loaded and basic processing of command line
	 * arguments is finished. It is guaranteed to be called before the GUI setup has started.
	 */
	public void afterPrefStartupHook() {
		// Do nothing by default
	}

	/**
	 * The startupHook will be called early, but after the GUI setup has started.
	 *
	 * Reason: On OSX we need to register some callbacks with the OS, so we'll receive events from the system menu.
	 * 
	 * @param callback
	 *            Java expiration callback, providing GUI feedback
	 * @since 12270 (signature)
	 */
	public void startupHook() {
		// Do nothing by default
	}

	/**
	 * The openURL hook will be used to open an URL in the default web browser.
	 * 
	 * @param url
	 *            The URL to open
	 * @throws IOException
	 *             if any I/O error occurs
	 * @throws URISyntaxException
	 */
	public boolean openUrl(String url) throws IOException, URISyntaxException {
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI(url));
			return true;
		}
		return false;
	}

	/**
	 * The initSystemShortcuts hook will be called by the Shortcut class after the modifier groups have been read from the config, but
	 * before any shortcuts are read from it or registered from within the application.
	 *
	 * Please note that you are not allowed to register any shortuts from this hook, but only "systemCuts"!
	 *
	 * BTW: SystemCuts should be named "system:&lt;whatever&gt;", and it'd be best if sou'd recycle the names already used by the Windows
	 * and OSX hooks. Especially the later has really many of them.
	 *
	 * You should also register any and all shortcuts that the operation system handles itself to block Openflexo from trying to use
	 * them---as that would just not work. Call setAutomatic on them to prevent the keyboard preferences from allowing the user to change
	 * them.
	 */
	public abstract void initSystemShortcuts();

	/**
	 * The makeTooltip hook will be called whenever a tooltip for a menu or button is created.
	 *
	 * Tooltips are usually not system dependent, unless the JVM is too dumb to provide correct names for all the keys.
	 *
	 * Some LAFs don't understand HTML, such as the OSX LAFs.
	 *
	 * @param name
	 *            Tooltip text to display
	 * @param sc
	 *            Shortcut associated (to display accelerator between parenthesis)
	 * @return Full tooltip text (name + accelerator)
	 */
	public String makeTooltip(String name, Shortcut sc) {
		StringBuilder result = new StringBuilder();
		result.append("<html>").append(name);
		if (sc != null && !sc.getKeyText().isEmpty()) {
			result.append(" <font size='-2'>(").append(sc.getKeyText()).append(")</font>");
		}
		return result.append("&nbsp;</html>").toString();
	}

	/**
	 * Returns the default LAF to be used on this platform to look almost as a native application.
	 * 
	 * @return The default native LAF for this platform
	 */
	public abstract String getDefaultStyle();

	/**
	 * Determines if the platform allows full-screen.
	 * 
	 * @return {@code true} if full screen is allowed, {@code false} otherwise
	 */
	public boolean canFullscreen() {
		return !GraphicsEnvironment.isHeadless()
				&& GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported();
	}

	/**
	 * Renames a file.
	 * 
	 * @param from
	 *            Source file
	 * @param to
	 *            Target file
	 * @return {@code true} if the file has been renamed, {@code false} otherwise
	 */
	public boolean rename(File from, File to) {
		return from.renameTo(to);
	}

	/**
	 * Returns a detailed OS description (at least family + version).
	 * 
	 * @return A detailed OS description.
	 * @since 5850
	 */
	public abstract String getOSDescription();

	/**
	 * Returns OS build number.
	 * 
	 * @return OS build number.
	 * @since 12217
	 */
	public String getOSBuildNumber() {
		return "";
	}

	/**
	 * Setup system keystore to add Openflexo HTTPS certificate (for remote control).
	 * 
	 * @param entryAlias
	 *            The entry alias to use
	 * @param trustedCert
	 *            the Openflexo certificate for localhost
	 * @return {@code true} if something has changed as a result of the call (certificate installation, etc.)
	 * @throws KeyStoreException
	 *             in case of error
	 * @throws IOException
	 *             in case of error
	 * @throws CertificateException
	 *             in case of error
	 * @throws NoSuchAlgorithmException
	 *             in case of error
	 * @since 7343
	 */
	public boolean setupHttpsCertificate(String entryAlias, KeyStore.TrustedCertificateEntry trustedCert)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		// TODO setup HTTPS certificate on Unix and OS X systems
		return false;
	}

	/**
	 * Executes a native command and returns the first line of standard output.
	 * 
	 * @param command
	 *            array containing the command to call and its arguments.
	 * @return first stripped line of standard output
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 12217
	 */
	public String exec(String... command) throws IOException {
		Process p = Runtime.getRuntime().exec(command);
		try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
			return StringUtils.strip(input.readLine());
		}
	}

	/**
	 * Returns the platform-dependent default cache directory.
	 * 
	 * @return the platform-dependent default cache directory
	 * @since 7829
	 */
	public abstract File getDefaultCacheDirectory();

	/**
	 * Returns the platform-dependent default preferences directory.
	 * 
	 * @return the platform-dependent default preferences directory
	 * @since 7831
	 */
	public abstract File getDefaultPrefDirectory();

	/**
	 * Returns the platform-dependent default user data directory.
	 * 
	 * @return the platform-dependent default user data directory
	 * @since 7834
	 */
	public abstract File getDefaultUserDataDirectory();

	/**
	 * Determines if the JVM is OpenJDK-based.
	 * 
	 * @return {@code true} if {@code java.home} contains "openjdk", {@code false} otherwise
	 * @since 12219
	 */
	public boolean isOpenJDK() {
		String javaHome = System.getProperty("java.home");
		return javaHome != null && javaHome.contains("openjdk");
	}

	/**
	 * Returns extended modifier key used as the appropriate accelerator key for menu shortcuts. It is advised everywhere to use
	 * {@link Toolkit#getMenuShortcutKeyMask()} to get the cross-platform modifier, but:
	 * <ul>
	 * <li>it returns KeyEvent.CTRL_MASK instead of KeyEvent.CTRL_DOWN_MASK. We used the extended modifier for years, and Oracle recommends
	 * to use it instead, so it's best to keep it</li>
	 * <li>the method throws a HeadlessException ! So we would need to handle it for unit tests anyway</li>
	 * </ul>
	 * 
	 * @return extended modifier key used as the appropriate accelerator key for menu shortcuts
	 * @since 12748 (as a replacement to {@code GuiHelper.getMenuShortcutKeyMaskEx()})
	 */
	public int getMenuShortcutKeyMaskEx() {
		return KeyEvent.CTRL_DOWN_MASK;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void registerApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ModuleLoader getModuleLoader() {
		return applicationContext.getModuleLoader();
	}

	/**
	 * Invoked when the application is asked to quit.
	 *
	 * @param e
	 *            the request to quit this application
	 * @param response
	 *            the one-shot response object used to cancel or proceed with the quit action
	 */
	@Override
	public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
		try {
			getModuleLoader().quit(true);
			response.performQuit();
		} catch (OperationCancelledException exception) {
			// return false;
			logger.info("Quit cancelled");
			response.cancelQuit();
		}
	}

	/**
	 * Called when the application is asked to show its about dialog.
	 *
	 * @param e
	 *            the request to show the about dialog
	 */
	@Override
	public void handleAbout(AboutEvent e) {
		logger.info("handleAbout()");
		new AboutDialog();
	}

	/**
	 * Called when the app is asked to show its preferences UI.
	 *
	 * @param e
	 *            the request to show preferences
	 */
	@Override
	public void handlePreferences(PreferencesEvent e) {
		logger.info("handlePreferences()");
		getApplicationContext().getPreferencesService().showPreferences();
	}

	/**
	 * Called when the application is asked to open a list of files.
	 *
	 * @param e
	 *            the request to open a list of files, and the search term used to find them, if any
	 */
	@Override
	public void openFiles(OpenFilesEvent e) {
		// Not implemented yet
		logger.warning("openFiles not implemented");
		/*if (Desktop.isDesktopSupported()) {
			for (File file : e.getFiles()) {
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}*/
	}

	/**
	 * Called when the application is asked to print a list of files.
	 *
	 * @param e
	 *            the request to print a list of files
	 */
	@Override
	public void printFiles(PrintFilesEvent e) {
		// Not implemented yet
		logger.warning("printFiles not implemented");
	}

}
