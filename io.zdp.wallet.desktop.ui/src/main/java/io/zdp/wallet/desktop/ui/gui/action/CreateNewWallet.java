package io.zdp.wallet.desktop.ui.gui.action;

import java.awt.FileDialog;
import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.swing.JDialog;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.zdp.common.crypto.CryptoUtils;
import io.zdp.common.utils.Mnemonics;
import io.zdp.common.utils.Mnemonics.Language;
import io.zdp.wallet.api.domain.Wallet;
import io.zdp.wallet.api.service.WalletService;
import io.zdp.wallet.desktop.ui.common.Alert;
import io.zdp.wallet.desktop.ui.common.I18n;
import io.zdp.wallet.desktop.ui.common.QTextComponentContextMenu;
import io.zdp.wallet.desktop.ui.common.SwingHelper;
import io.zdp.wallet.desktop.ui.common.TextComponentFocuser;
import io.zdp.wallet.desktop.ui.gui.MainWindow;
import io.zdp.wallet.desktop.ui.gui.dialog.WalletCreationPanel;
import io.zdp.wallet.desktop.ui.service.DesktopWalletService;

@Component
public class CreateNewWallet {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private I18n i18n;

	@Autowired
	private DesktopWalletService walletService;

	@Autowired
	private MainWindow mainWindow;

	public void create(Window parent) {

		WalletCreationPanel panel = new WalletCreationPanel();

		new QTextComponentContextMenu(panel.txtSeed);
		new QTextComponentContextMenu(panel.txtMnemonics);

		panel.txtSeed.addFocusListener(new TextComponentFocuser());
		panel.txtMnemonics.addFocusListener(new TextComponentFocuser());

		JDialog passwordDialog = SwingHelper.dialog(parent, panel);
		passwordDialog.setTitle("New wallet");

		generateWalletInfo(panel);

		panel.languageSelector.addItemListener(i -> {
			generateWalletInfo(panel);
		});

		panel.btnCreatWallet.addActionListener(ev -> {

			if (false == Alert.confirm("Did you write down the wallet password or list of words?")) {
				return;
			}

			String pass = panel.txtSeed.getText();

			FileDialog fileDialog = new FileDialog(mainWindow.getFrame(), "Save Wallet", FileDialog.SAVE);
			fileDialog.setFilenameFilter(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".wallet");
				}
			});
			fileDialog.setFile("my_new.wallet");
			fileDialog.setVisible(true);

			File walletFile = new File(fileDialog.getDirectory() + "" + fileDialog.getFile());

			if (fileDialog.getDirectory() == null || fileDialog.getFile() == null) {
				return;
			}

			passwordDialog.dispose();

			log.debug("Save new wallet: " + walletFile);

			try {

				Wallet w = WalletService.create(pass, walletFile);

				walletService.setCurrentWallet(w, walletFile, pass);

				mainWindow.setWallet(w, walletFile, pass);

				if (parent != mainWindow.getFrame()) {
					parent.dispose();
				}

				Alert.info("New wallet was created!");

			} catch (Exception e) {
				log.error("Error: ", e);
			}
		});
		
		panel.btnCancel.addActionListener(e->{
			passwordDialog.dispose();
		});

		SwingHelper.installEscapeCloseOperation(passwordDialog);

		passwordDialog.setVisible(true);
	}

	private void generateWalletInfo(WalletCreationPanel panel) {

		try {
			String seed = CryptoUtils.generateRandomNumber256bits();
			panel.txtSeed.setText(seed);

			Language l = Language.ENGLISH;

			if (panel.languageSelector.getSelectedItem().equals("English")) {
				l = Language.ENGLISH;
			} else if (panel.languageSelector.getSelectedItem().equals("French")) {
				l = Language.FRENCH;
			} else if (panel.languageSelector.getSelectedItem().equals("Italian")) {
				l = Language.ITALIAN;
			} else if (panel.languageSelector.getSelectedItem().equals("Japanese")) {
				l = Language.JAPANESE;
			} else if (panel.languageSelector.getSelectedItem().equals("Korean")) {
				l = Language.KOREAN;
			} else if (panel.languageSelector.getSelectedItem().equals("Spanish")) {
				l = Language.SPANISH;
			} else if (panel.languageSelector.getSelectedItem().equals("Chinese Simplified")) {
				l = Language.CHINESE_SIMPLIFIED;
			} else if (panel.languageSelector.getSelectedItem().equals("Chinese Traditional")) {
				l = Language.CHINESE_TRADITIONAL;
			}

			List<String> generateWords = Mnemonics.generateWords(l, seed);
			String words = StringUtils.join(generateWords, IOUtils.LINE_SEPARATOR);
			panel.txtMnemonics.setText(words);
			panel.txtMnemonics.setCaretPosition(0);

		} catch (NoSuchAlgorithmException e) {
			log.error("Error: ", e);
		}

	}

}
