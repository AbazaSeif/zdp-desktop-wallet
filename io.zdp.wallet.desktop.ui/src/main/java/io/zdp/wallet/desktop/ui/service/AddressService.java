package io.zdp.wallet.desktop.ui.service;

import java.awt.TrayIcon.MessageType;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.zdp.api.model.BalanceResponse;
import io.zdp.client.ZdpClient;
import io.zdp.wallet.api.domain.Wallet;
import io.zdp.wallet.desktop.ui.common.SwingHelper;
import io.zdp.wallet.desktop.ui.gui.MainWindow;

@Service
public class AddressService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${api.central.url}")
	private String apiCentralUrl;

	@Value("${api.url.address.new}")
	private String apiUrlAddressNew;

	@Autowired
	private MainWindow mainWindow;

	@Autowired
	private DesktopWalletService walletService;

	@Autowired
	private ZdpClient zdp;

	@PostConstruct
	public void init() {
	}

	public void sync(Runnable postSyncFunction) {

		SwingHelper.async(mainWindow.getFrame(), "Synchronizing wallet", () -> {

			try {

				Wallet wallet = walletService.getCurrentWallet();
				BalanceResponse balance = zdp.getAccountBalance(wallet.getPublicKey(), wallet.getPrivateKey());
				wallet.setBalance(balance.getBalance());
				
				walletService.saveCurrentWallet();

				mainWindow.showSystemTrayMessage(MessageType.INFO, "Wallet synchronized");

				if (postSyncFunction != null) {
					postSyncFunction.run();
				}

			} catch (Exception e) {
				log.error("Error: ", e);
			}
		});
	}

}
