package io.zdp.wallet.desktop.ui.gui.action;

import java.awt.Window;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.zdp.wallet.api.db.domain.Wallet;
import io.zdp.wallet.api.service.WalletApiService;
import io.zdp.wallet.desktop.ui.common.Alert;
import io.zdp.wallet.desktop.ui.common.SwingHelper;
import io.zdp.wallet.desktop.ui.common.SynchronousJFXFileChooser;
import io.zdp.wallet.desktop.ui.common.model.BooleanWrapper;
import io.zdp.wallet.desktop.ui.common.model.StringWrapper;
import io.zdp.wallet.desktop.ui.gui.MainWindow;
import io.zdp.wallet.desktop.ui.gui.dialog.EnterPasswordPanel;
import io.zdp.wallet.desktop.ui.service.ConfigurationService;
import javafx.application.Platform;
import javafx.stage.FileChooser;

@Component
public class OpenWallet {

	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	@Autowired
	private MainWindow mainWindow;

	@Autowired
	private WalletApiService walletService;

	@Autowired
	private ConfigurationService configurationService;

	public void open ( Window parent, JDialog dialog ) {

		javafx.embed.swing.JFXPanel dummy = new javafx.embed.swing.JFXPanel();

		Platform.setImplicitExit( false );

		SynchronousJFXFileChooser chooser = new SynchronousJFXFileChooser( ( ) -> {

			FileChooser ch = new FileChooser();

			File initialFolderFile = configurationService.getConfiguration().getFileDialogFolder();

			if ( initialFolderFile != null ) {
				ch.setInitialDirectory( initialFolderFile );
			}

			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter( "ZDP Wallet (*.db)", "*.db" );
			ch.getExtensionFilters().add( extFilter );

			ch.setTitle( "Open wallet file" );
			return ch;
		} );

		File walletFile = chooser.showOpenDialog();

		if ( walletFile == null || false == walletFile.canRead() ) {
			return;
		}

		configurationService.getConfiguration().setFileDialogFolder( walletFile.getParent() );
		configurationService.saveConfiguration();

		log.debug( "Open wallet: " + walletFile );

		// Get password
		final String password = enterPassword( parent );

		// Looks like the operation was cancelled
		if ( password == null ) {
			log.info( "No password entered, cancel" );
			return;
		}

		SwingHelper.async( parent, "Opening wallet", ( ) -> {

			Wallet wallet = null;

			try {

				wallet = walletService.openWallet( walletFile, password );

				final Wallet fw = wallet;

				if ( wallet != null ) {

					SwingUtilities.invokeLater( ( ) -> {

						mainWindow.setWallet( fw, walletFile );

						if ( dialog != null ) {
							dialog.dispose();
						}
					} );

				} else {
					Alert.error( "Sorry, this wallet can not be loaded" );
				}
			} catch ( Exception e ) {
				log.error( "Error: ", e );
			}

		} );

	}

	private String enterPassword ( Window parent ) {

		StringWrapper password = new StringWrapper();

		final BooleanWrapper cancelled = new BooleanWrapper( false );

		while ( password.get() == null && cancelled.isFalse() ) {

			EnterPasswordPanel pp = new EnterPasswordPanel();

			JDialog eppDialog = SwingHelper.dialog( parent, pp );
			SwingHelper.installEscapeCloseOperation( eppDialog, cancelled );

			pp.btnOk.setEnabled( true );

			ActionListener al = e -> {

				password.set( new String( pp.password.getPassword() ) );

				eppDialog.dispose();

			};

			pp.btnOk.addActionListener( al );
			pp.password.addActionListener( al );

			eppDialog.setVisible( true );

		}

		return password.get();

	}

}
