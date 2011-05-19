package nova.agent.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nova.agent.NovaAgent;
import nova.agent.appliance.Appliance;
import nova.common.util.Conf;
import nova.common.util.Utils;

import org.apache.log4j.Logger;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Simple on demand software install user interface
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class AgentFrame extends JFrame {

	Logger logger = Logger.getLogger(AgentFrame.class);

	private static final long serialVersionUID = 1L;
	private DefaultListModel listModel = new DefaultListModel();
	private JList softList = new JList(listModel); // 软件列表
	private JTextArea softInfo = new JTextArea("info of soft"); // 软件信息
	private JLabel picture = new JLabel(""); // 软件图片
	private JButton install = new JButton("install"); // 安装按钮
	private ConcurrentHashMap<String, Appliance> apps = NovaAgent.getInstance()
			.getAppliances();
	public static JLabel statusInfo = new JLabel("Download process"); // 安装状态条
	public static JProgressBar downProcess = new JProgressBar(); // 安装进度条

	/**
	 * Change the percent of down
	 * 
	 * @param currentValue
	 * @param totalValue
	 */
	public static void setDownProcessValue(double currentValue,
			double totalValue) {
		downProcess.setValue((int) ((currentValue / totalValue) * 100));
	}

	/**
	 * Display something when download start
	 * 
	 * @param info
	 *            The info you want to display
	 */
	public static void setInfoDisplayWhenDown(String info) {
		downProcess.setBorderPainted(true);
		downProcess.setBackground(Color.pink);
		downProcess.setStringPainted(true);
		downProcess.setVisible(true);
		downProcess.setValue(0);
		downProcess.setMaximum(100);
		statusInfo.setText(info);
	}

	/**
	 * Get rid of something after install
	 * 
	 * @param info
	 */
	public static void setInfoDisplayAfterInstall(String info) {
		statusInfo.setText(info);
		downProcess.setVisible(false);
		NovaAgent.getInstance().saveAppliances();
	}

	public AgentFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridBagLayout());
		GridBagConstraints gbConstraints = new GridBagConstraints();

		gbConstraints.fill = GridBagConstraints.BOTH;
		gbConstraints.anchor = GridBagConstraints.CENTER;

		Container container = getContentPane();

		gbConstraints.insets = new Insets(10, 10, 10, 10);
		for (Appliance app : apps.values())
			listModel.addElement(app.getName());

		JScrollPane softListPane = new JScrollPane(softList);
		softListPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		addComp(softListPane, container, gbConstraints, 0, 0, 6, 3, 1, 1);

		gbConstraints.insets = new Insets(10, 10, 10, 10);
		addComp(picture, container, gbConstraints, 0, 3, 3, 3, 1, 1);

		gbConstraints.insets = new Insets(10, 10, 10, 10);
		JScrollPane softInfoPane = new JScrollPane(softInfo);
		softInfoPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		softInfoPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		addComp(softInfoPane, container, gbConstraints, 3, 3, 3, 3, 1, 1);
		softInfo.setEditable(false);

		gbConstraints.insets = new Insets(10, 10, 10, 10);
		addComp(statusInfo, container, gbConstraints, 6, 0, 1, 1, 0, 0);

		gbConstraints.insets = new Insets(10, 10, 10, 10);
		addComp(downProcess, container, gbConstraints, 6, 1, 1, 3, 0, 0);
		downProcess.setVisible(false);

		gbConstraints.insets = new Insets(10, 10, 10, 10);
		gbConstraints.fill = GridBagConstraints.NONE;
		gbConstraints.anchor = GridBagConstraints.EAST;
		addComp(install, container, gbConstraints, 6, 5, 1, 1, 0, 0);
		// 居中显示
		setSize(600, 600);
		double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		setLocation((int) (width - this.getWidth()) / 2,
				(int) (height - this.getHeight()) / 2);
		// show();

		setVisible(true);
		// setResizable(false);

		// 下载并安装一个所选软件
		install.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (softList.getSelectedIndex() == -1) // 确定是否选择一个安装项
				{
					JOptionPane.showMessageDialog(null,
							"Please select one soft");
				} else if (!apps.get(softList.getSelectedValue()).getStatus()
						.equals(Appliance.Status.NOT_INSTALLED)) {
					JOptionPane
							.showMessageDialog(null,
									"You have already install this software, please select another one.");
				} else {
					// 再次确定是否安装所选软件
					int value = JOptionPane.showOptionDialog(
							null,
							"Do you want to install "
									+ softList.getSelectedValue() + "?",
							"Option Dialog", JOptionPane.DEFAULT_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, new Object[] {
									"Install", "Cancel" }, "Install");
					if (value == 0) {
						Appliance app = apps.get(softList.getSelectedValue());
						app.setStatus(Appliance.Status.DOWNLOAD_PENDING);
					}
				}

			}
		});

		// 所选软件信息显示
		softList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (softList.getSelectedIndex() != -1) {
					String softName = softList.getSelectedValue().toString();
					String relativeLocalPath = Conf
							.getString("agent.software.image_path");
					changeSize(Utils.pathJoin(Utils.NOVA_HOME,
							relativeLocalPath, softName + ".jpg"), Utils
							.pathJoin(Utils.NOVA_HOME, relativeLocalPath,
									softName + ".jpg"),
							picture.getBounds().width,
							picture.getBounds().height);
					ImageIcon pic = new ImageIcon(Utils.pathJoin(
							Utils.NOVA_HOME, relativeLocalPath, softName
									+ ".jpg"));
					picture.setIcon(pic);
					softInfo.setText(apps.get(softName).getInfo());
				}
			}
		});
	}

	private void addComp(Component com, Container con,
			GridBagConstraints gbConstraints, int row, int column,
			int numberOfRow, int numberOfColumn, int weightx, int weighty) {
		gbConstraints.gridx = column;
		gbConstraints.gridy = row;
		gbConstraints.gridwidth = numberOfColumn;
		gbConstraints.gridheight = numberOfRow;
		gbConstraints.weightx = weightx;
		gbConstraints.weighty = weighty;

		con.add(com, gbConstraints);
	}

	/**
	 * 
	 * @param absolutePath
	 *            Origin image path
	 * @param localSavePath
	 *            New image path
	 * @param wid
	 * @param hei
	 */
	public void changeSize(String absolutePath, String localSavePath, int wid,
			int hei) {// 控制图片大小
		File _file = new File(absolutePath); // 读入文件
		try {
			Image src = ImageIO.read(_file); // 构造对象

			BufferedImage tag = new BufferedImage(wid, hei,
					BufferedImage.TYPE_INT_RGB);

			tag.getGraphics().drawImage(src, 0, 0, wid, hei, null);
			FileOutputStream out = new FileOutputStream(localSavePath);// 输出到文件流

			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);

			encoder.encode(tag);
			out.close();
		} catch (IOException e) {
			logger.error("Change image size failed!", e);
		}
	}
}
