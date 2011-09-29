package nova.test.agent;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nova.common.util.Conf;
import nova.common.util.Utils;

public class MinimumAgentFrame {
	TrayIcon trayIcon;// 托盘图标，但不是Image类型的 哦
	SystemTray Tray;// 系统托盘
	String trayIconPath = Conf.getString("agent.software.trayicon_path");
	Image img = (new ImageIcon(Utils.pathJoin(Utils.NOVA_HOME, trayIconPath)))
			.getImage();// 托盘图标，建议使用较小的图片

	public MinimumAgentFrame() {
		final JFrame frame = new JFrame();
		JPanel jp = new JPanel();
		frame.add(jp);
		frame.setSize(320, 240);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setIconImage(img);// 设置窗口左上角的图标
		frame.setVisible(false); // 初始不可见
		Tray = SystemTray.getSystemTray();// 获得系统托盘实例
		// 创建系统托盘的右键弹出菜单
		PopupMenu pm = new PopupMenu();
		MenuItem mi0 = new MenuItem("Open");
		MenuItem mi1 = new MenuItem("Close");
		pm.add(mi0);
		pm.add(mi1);

		trayIcon = new TrayIcon(img, "", pm);// 创建托盘图标实例
		trayIcon.setImageAutoSize(true);// 图标自动适应托盘，也就是说它自动改变大小
		trayIcon.setToolTip("SoftWare");// 设置提示语
		try {
			Tray.add(trayIcon);
		} catch (AWTException e1) {
			e1.printStackTrace();
		}

		frame.addWindowListener(new WindowAdapter() {// 当“关闭”窗口时，同时关闭系统托盘图标
			public void windowClosing(WindowEvent e) {
				System.exit(0);
				Tray.remove(trayIcon);
			}
		});
		frame.addWindowListener(new WindowAdapter() {// 当“关闭”窗口时，同时关闭系统托盘图标
			public void windowIconified(WindowEvent e) {
				frame.setVisible(false);
			}
		});

		mi0.addActionListener(new ActionListener() { // 右键弹出菜单的事件监听
			public void actionPerformed(ActionEvent e) {
				frame.setExtendedState(JFrame.NORMAL);
				frame.setVisible(true);
			}
		});

		mi1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
				Tray.remove(trayIcon);// 退出程序，移出系统托盘处的图标

			}
		});

		trayIcon.addMouseListener(new MouseAdapter() {// 单击鼠标左键，也是显示窗口
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {// 如果点击的是鼠标左键
					frame.setExtendedState(JFrame.NORMAL);
					frame.setVisible(true);
					// Tray.remove(trayIcon);
				}
			}
		});

	}

	public static void main(String[] args) {
		if (SystemTray.isSupported()) { // 如果操作系统支持托盘，那么就创建MyTray的实例
			new MinimumAgentFrame();
		}
	}
}