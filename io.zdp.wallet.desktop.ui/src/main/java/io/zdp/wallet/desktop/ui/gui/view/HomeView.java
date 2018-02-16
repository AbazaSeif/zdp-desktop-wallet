package io.zdp.wallet.desktop.ui.gui.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.zdp.wallet.desktop.ui.gui.dialog.HomePanel;
import io.zdp.wallet.desktop.ui.service.DesktopWalletService;

@Component
public class HomeView {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DesktopWalletService walletService;

	@Value("${app.url.online.faq}")
	private String appUrlOnlineFaq;

	@Value("${app.url.online.help}")
	private String appUrlOnlineHelp;

	public JPanel get() {

		JPanel panel = new JPanel(new BorderLayout());

		HomePanel homePanel = new HomePanel();

		String balance = walletService.getCurrentWallet().getBalance().toPlainString();
		homePanel.txtBalance.setText(balance);

		panel.add(new JScrollPane(homePanel), BorderLayout.CENTER);

		return panel;

	}

}
