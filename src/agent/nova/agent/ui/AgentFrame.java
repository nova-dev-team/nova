package nova.agent.ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.SwingUtilities;
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

    public TrayIcon trayIcon;// 托盘图标，但不是Image类型的
    public SystemTray Tray;// 系统托盘

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
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        for (Appliance app : apps.values())
            listModel.addElement(app.getName());
        JScrollPane softInfoPane = new JScrollPane(softInfo);
        softList.setBounds(10, 10, 200, 500);

        picture.setBounds(250, 10, 300, 300);
        softInfoPane.setBounds(250, 300, 300, 210);
        statusInfo.setBounds(10, 520, 100, 20);

        downProcess.setBounds(250, 520, 100, 20);
        install.setBounds(470, 520, 80, 20);

        this.add(softList);
        this.add(picture);
        this.add(softInfoPane);
        this.add(statusInfo);
        this.add(downProcess);
        this.add(install);
        picture.setVisible(true);
        downProcess.setVisible(false);
        this.setResizable(false);
        // 居中显示
        this.setSize(600, 600);
        double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        this.setLocation((int) (width - this.getWidth()) / 2,
                (int) (height - this.getHeight()) / 2);
        // show();

        this.setVisible(false);
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
                            .getString("agent.software.picture_path");
                    changeSize(Utils.pathJoin(Utils.NOVA_HOME,
                            relativeLocalPath, softName + ".jpg"), Utils
                            .pathJoin(Utils.NOVA_HOME, relativeLocalPath,
                                    softName + ".jpg"), 300, 250);
                    ImageIcon pic = new ImageIcon(Utils.pathJoin(
                            Utils.NOVA_HOME, relativeLocalPath, softName
                                    + ".jpg"));
                    picture.setIcon(pic);
                    picture.setVerticalAlignment(JLabel.TOP);
                    softInfo.setText(apps.get(softName).getInfo());
                }
            }
        });

        // Create system tray icon
        Tray = SystemTray.getSystemTray();// 获得系统托盘实例
        // 创建系统托盘的右键弹出菜单
        PopupMenu pm = new PopupMenu();
        MenuItem mi0 = new MenuItem("Open");
        MenuItem mi1 = new MenuItem("Close");
        pm.add(mi0);
        pm.add(mi1);

        String trayIconPath = Conf.getString("agent.software.trayicon_path");
        Image img = (new ImageIcon(
                Utils.pathJoin(Utils.NOVA_HOME, trayIconPath))).getImage();// 托盘图标，建议使用较小的图片

        trayIcon = new TrayIcon(img, "", pm);// 创建托盘图标实例
        trayIcon.setImageAutoSize(true);// 图标自动适应托盘，也就是说它自动改变大小
        trayIcon.setToolTip("SoftWare");// 设置提示语
        try {
            Tray.add(trayIcon);
        } catch (AWTException e1) {
            e1.printStackTrace();
        }

        this.addWindowListener(new WindowAdapter() {// 当“关闭”窗口时，同时关闭系统托盘图标
            public void windowClosing(WindowEvent e) {
                AgentFrame.instance.setVisible(false);
                Tray.remove(trayIcon);
            }
        });
        this.addWindowListener(new WindowAdapter() {// 最小化时
            public void windowIconified(WindowEvent e) {
                AgentFrame.instance.setVisible(false);
            }
        });

        mi0.addActionListener(new ActionListener() { // 右键弹出菜单的事件监听
            public void actionPerformed(ActionEvent e) {
                AgentFrame.instance.setExtendedState(JFrame.NORMAL);
                AgentFrame.instance.setVisible(true);
                AgentFrame.instance.setAlwaysOnTop(true);
            }
        });

        mi1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AgentFrame.instance.setVisible(false);
                Tray.remove(trayIcon);// 退出程序，移出系统托盘处的图标
            }
        });

        trayIcon.addMouseListener(new MouseAdapter() {// 单击鼠标左键，也是显示窗口
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {// 如果点击的是鼠标左键
                    AgentFrame.instance.setExtendedState(JFrame.NORMAL);
                    AgentFrame.instance.setVisible(true);
                    AgentFrame.instance.setAlwaysOnTop(true);
                    // Tray.remove(trayIcon);
                }
            }
        });

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

    private static AgentFrame instance = null;

    /**
     * Get the singleton of AgentFrame.
     * 
     * @return AgentFrame instance, singleton.
     */
    public static synchronized AgentFrame getInstance() {
        if (AgentFrame.instance == null) {
            AgentFrame.instance = new AgentFrame();
        }
        return AgentFrame.instance;
    }

    // TODO Add this to img and implement on per-user installation
    /**
     * Start AgentFrame and run in back.
     * 
     * @return
     */
    public void autoStart() {
        if (AgentFrame.instance == null) {
            AgentFrame.instance = new AgentFrame();
        }
        logger.info("AgentFrame has started and run in the back!");
    }

    /**
     * Start AgentFrame by user.
     * 
     * @return
     */
    public void userStart() {
        if (AgentFrame.instance == null) {
            AgentFrame.instance = new AgentFrame();
        }
        AgentFrame.instance.setVisible(true);
    }

    /**
     * Shutdown one AgentFrame
     */
    public void shutdown() {
        logger.info("Shutting down AgentFrame");
        if (AgentFrame.instance != null) {
            AgentFrame.instance.setVisible(false);
            Tray.remove(trayIcon);
        }
        AgentFrame.instance = null;
    }

}
