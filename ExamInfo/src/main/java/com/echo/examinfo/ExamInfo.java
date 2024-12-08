package com.echo.examinfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2023/12/1 20:58
 * @description 获取考试资讯
 */
public class ExamInfo {
    final static int ITEM_MAX_NUM = 8;//最多查找项目数
    final static int MAX_PAGES = 3;//最高查找页数
    final static ScheduledExecutorService[] timer = new ScheduledExecutorService[1];//线程池
    final static Runnable[] autoUpdate = new Runnable[1];//线程任务
    private final static boolean[] resizeAble = {false};//缩放开关(默认,右下角)
    private final static boolean[] resizeAbleOnLeft = {false};//缩放开关(左上角)
    private final static boolean[] isDragging = {false};//是否拖拽
    static JFrame win;//窗口
    public static MouseAdapter HOVER_EVENT = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            JComponent co = (JComponent) e.getComponent();
            if (isDragging[0] || !co.isEnabled()) return;
            //System.out.println("hover in");
            co.setBackground(Config.GRAY);
            co.setOpaque(true);
            win.repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            JComponent co = (JComponent) e.getComponent();
            if (isDragging[0] || !co.isEnabled()) return;
            //System.out.println("hover out");
            co.setBackground(Config.TRANSPARENT);
            co.setOpaque(false);
            win.repaint();
        }
    };
    private static JPanel winPane;//窗口面板
    private static JPanel head;//标题栏
    private static JScrollPane scroll = new JScrollPane();//滚动面板
    private static JScrollBar bar = scroll.getVerticalScrollBar();//滚动条
    private static Box left, center;
    private static Box home;//viewpoint, 主页
    private static JPanel settings;//viewpoint, 配置页
    private static JLabel practice;//实践信息
    private static JLabel setting;//实践信息
    private static JButton update, query, close;
    private static boolean allowUpdate = true;
    private static boolean allowQuery = false;
    private static boolean allowSetting = false;
    private static boolean allowCenter = false;//实践信息栏
    private static Box top, middle, bottom, headLine, infoLine;
    private static JLabel title, author;
    private static JButton save;
    private static JButton goBack;
    private static Map<String, JTextField> settingsMap = new HashMap<>();
    private static SystemTray systemTray = SystemTray.getSystemTray();
    private static TrayIcon trayIcon;
    private static JPopupMenu jMenu;
    private static JCheckBoxMenuItem showItem, onTopItem, autoStart;
    private static JMenuItem settingItem, exitItem;
    private static JFrame trayCover = new JFrame();
    private static String get = "https://www.jseea.cn/webfile/index/index_zkxx/index.html";//成绩
    private static String get2 = "https://www.jseea.cn/webfile/index/index_zkxx/index_";
    private static String getPolicy = "https://www.jseea.cn/webfile/index/index_zcwj/";//消息
    private static String getPolicy2 = "https://www.jseea.cn/webfile/index/index_zcwj/index_";
    private static String post = "https://sdata.jseea.cn/tpl_front/score/allScoreList.html";
    private static String getPractice = "https://sdata.jseea.cn/tpl_front/shzk/sjlwbk/sjlwKCListShzkQuery.html";//实践
    private static String practiceUrl = getPractice;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());//使用系统L&F
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");//更简洁好看..
        } catch (Exception e) {
            e.printStackTrace();
        }
        //生成界面
        win = new JFrame();
        win.setTitle("江苏自考资讯");
        winPane = new JPanel(new BorderLayout());
        settings = new JPanel(new BorderLayout());
        head = new JPanel(new BorderLayout());
        update = new JButton("更新资讯");
        query = new JButton("查询成绩");
        close = new JButton("━");//╳
        practice = new JLabel("暂无实践信息, 请先完成");
        setting = new JLabel("配置");
        title = new JLabel("设置");
        author = new JLabel("作者邮箱: Echoforwork2021@qq.com");
        save = new JButton("保 存");
        goBack = new JButton("返 回");
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        home = Box.createVerticalBox();
        scroll.setViewportView(home);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bar.setUI(new MyScrollBarUI());
        bar.setUnitIncrement(Config.SCROLL_UNIT_INCREMENT);//滚动步长
        bar.setBlockIncrement(Config.SCROLL_BLOCK_INCREMENT);//点击步长
        left = Box.createHorizontalBox();
        center = Box.createHorizontalBox();
        top = Box.createVerticalBox();
        middle = Box.createVerticalBox();
        bottom = Box.createHorizontalBox();
        headLine = Box.createHorizontalBox();
        infoLine = Box.createHorizontalBox();
        headLine.add(Box.createHorizontalGlue());
        infoLine.add(Box.createHorizontalGlue());
        headLine.add(title);
        infoLine.add(author);
        headLine.add(Box.createHorizontalGlue());
        infoLine.add(Box.createHorizontalGlue());
        top.add(headLine);
        top.add(infoLine);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(save);
        bottom.add(Box.createHorizontalStrut(save.getPreferredSize().width));
        bottom.add(goBack);
        bottom.add(Box.createHorizontalGlue());
        settings.add(top, BorderLayout.NORTH);
        settings.add(middle, BorderLayout.CENTER);
        settings.add(bottom, BorderLayout.SOUTH);
        update.setFocusPainted(false);
        query.setFocusPainted(false);
        close.setFocusPainted(false);
        head.setBorder(BorderFactory.createEmptyBorder(0, Config.CORNER_SIZE, 0, 0));
        left.add(update);
        left.add(Box.createHorizontalStrut(6));
        left.add(query);
        center.add(practice);
        center.add(setting);
        head.add(left, BorderLayout.WEST);
        head.add(center, BorderLayout.CENTER);
        head.add(close, BorderLayout.EAST);
        winPane.add(head, BorderLayout.NORTH);
        winPane.add(scroll, BorderLayout.CENTER);
        win.add(winPane);
        //自动更新计划
        autoUpdate[0] = () -> {
            timer[0] = Executors.newScheduledThreadPool(1);
            timer[0].scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Date start, end;
                    Calendar cal;
                    cal = Calendar.getInstance();
                    cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 8, 59, 58);
                    start = cal.getTime();
                    cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 21, 0, 1);
                    end = cal.getTime();
                    try {
                        int hour, min, sec;
                        Date now = Calendar.getInstance().getTime();
                        hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                        min = Calendar.getInstance().get(Calendar.MINUTE);
                        sec = Calendar.getInstance().get(Calendar.SECOND);
                        //判断是否在网站运行时段内
                        if (now.compareTo(start) >= 0 & now.compareTo(end) <= 0) {
                            //每三个小时的整点更新一次
                            if (hour % 3 == 0 & min == 0 & sec == 0) {
                                //任务
                                update.doClick();
                                updatePracticeEnrol().execute();
                            }
                        }
                    } catch (Exception ignored) {
                        createLog(ignored);
                        timer[0].shutdown();
                        if (null != autoUpdate[0]) {
                            autoUpdate[0].run();
                        }
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        };
        //美化, 设置opaque为false时(即透明), 可以解决透明组件重影问题
        {
            //主页
            query.setFont(Config.SongTi);
            query.setUI(new MyButtonUI());
            query.setOpaque(false);
            query.setBackground(Config.TRANSPARENT);
            query.setForeground(Color.white);
            query.setBorder(BorderFactory.createEmptyBorder(9, 4, 5, 4));
            update.setFont(Config.SongTi);
            update.setUI(new MyButtonUI());
            update.setOpaque(false);
            update.setBackground(Config.TRANSPARENT);
            update.setForeground(Color.white);
            update.setBorder(BorderFactory.createEmptyBorder(9, 4, 5, 4));
            practice.setFont(Config.HeiTiSmaller);
            practice.setForeground(Color.white);
            setting.setFont(createUnderlinedFont(Config.HeiTi));
            setting.setForeground(Config.BLUE);
            setting.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
            setting.setCursor(new Cursor(Cursor.HAND_CURSOR));
            center.setBorder(BorderFactory.createEmptyBorder(9, 15, 5, 8));
            close.setFont(new Font(close.getFont().getName(), Font.BOLD, close.getFont().getSize()));
            close.setUI(new MyButtonUI());
            close.setOpaque(false);
            close.setBackground(Config.TRANSPARENT);
            close.setForeground(Color.gray);
            close.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            head.setOpaque(false);
            head.setBackground(Config.TRANSPARENT);
            scroll.setOpaque(false);
            scroll.setBackground(Config.TRANSPARENT);
            scroll.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));//解决鼠标经过panel窗口最底下条目时超出区域显示的问题
            scroll.getViewport().setBackground(Config.TRANSPARENT);
            home.setOpaque(false);
            home.setForeground(Color.white);
            home.setBackground(Config.TRANSPARENT);
            winPane.setOpaque(true);
            winPane.setBackground(Color.black);
            win.setUndecorated(true);//取消默认窗口样式
            win.setBackground(Config.TRANSPARENT); // 背景透明, 解决窗口黑边问题
            win.setMinimumSize(new Dimension((int) (Config.SCREEN_WIDTH * 0.1), (int) (Config.SCREEN_HEIGHT * 0.1)));
            win.setMaximumSize(new Dimension(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT));
            win.setType(Window.Type.UTILITY);//隐藏任务栏图标
            //配置页
            settings.setBorder(BorderFactory.createEmptyBorder(1, 6, 15, 6));
            settings.setOpaque(true);
            settings.setBackground(Color.black);
            title.setForeground(Color.white);
            title.setFont(Config.SongTiLarger);
            author.setForeground(Config.BLUE);
            author.setFont(createUnderlinedFont(Config.SongTi));
            save.setUI(new MyButtonUI());
            save.setFocusPainted(false);
            save.setOpaque(false);
            save.setFont(Config.SongTi);
            save.setBackground(Config.TRANSPARENT);
            save.setForeground(Color.white);
            save.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.white, 1), BorderFactory.createEmptyBorder(6, 6, 6, 6)));
            goBack.setFocusPainted(false);
            goBack.setUI(new MyButtonUI());
            goBack.setOpaque(false);
            goBack.setFont(Config.SongTi);
            goBack.setBackground(Config.TRANSPARENT);
            goBack.setForeground(Color.white);
            goBack.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.white, 1), BorderFactory.createEmptyBorder(6, 6, 6, 6)));
            top.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
            bottom.setBorder(BorderFactory.createEmptyBorder(10, 4, 0, 4));
        }
        //系统托盘
        {


            if (null == Config.icon) {
                JOptionPane.showMessageDialog(win, Config.icon.getPath() + " 不存在", "任务栏图标加载失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // System.out.println(Config.icon.getPath());
            ImageIcon icon = new ImageIcon(Config.icon);
            trayIcon = new TrayIcon(icon.getImage(), "自考资讯");

        }
        // //方案一
        // PopupMenu menu = new PopupMenu();
        // if (false) {
        //     CheckboxMenuItem showItem = new CheckboxMenuItem("显示      ", true);
        //     CheckboxMenuItem onTopItem = new CheckboxMenuItem("置顶      ", false);
        //     MenuItem exitItem = new MenuItem("退出       ");
        //     showItem.setFont(com.echo.examinfo.Config.SongTi);
        //     onTopItem.setFont(com.echo.examinfo.Config.SongTi);
        //     exitItem.setFont(com.echo.examinfo.Config.SongTi);
        //     try {
        //         if (SystemTray.isSupported()) {
        //             menu.add(showItem);
        //             menu.add(onTopItem);
        //             menu.addSeparator();
        //             menu.add(exitItem);
        //             trayIcon.setImageAutoSize(true);
        //             systemTray.add(trayIcon);
        //             trayIcon.addMouseListener(new MouseAdapter() {
        //                 @Override
        //                 public void mouseClicked(MouseEvent e) {
        //                     if (timer[0] != null && timer[0].isShutdown()) {
        //                         autoUpdate[0].run();
        //                     }
        //                     if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
        //                         showItem.setState(true);
        //                         restoreWinAppearance();
        //                         win.setVisible(true);
        //                     }
        //                 }
        //             });
        //
        //             exitItem.addActionListener(e -> System.exit(0));
        //
        //             showItem.addItemListener(e -> {
        //                 if (timer[0] != null && timer[0].isShutdown()) {
        //                     autoUpdate[0].run();
        //                 }
        //                 restoreWinAppearance();
        //                 win.setVisible(!win.isVisible());
        //             });
        //
        //             onTopItem.addItemListener(e -> {
        //                 win.setAlwaysOnTop(!win.isAlwaysOnTop());
        //             });
        //         }
        //     } catch (AWTException e) {
        //         createLog(e);
        //     }
        // }
        //方案二
        jMenu = new JPopupMenu();
        showItem = new JCheckBoxMenuItem("显示      ", true);
        onTopItem = new JCheckBoxMenuItem("置顶      ", false);
        autoStart = new JCheckBoxMenuItem("开机自启    ", false);
        settingItem = new JMenuItem("设置      ");
        // author = new JMenuItem("Echoforwork2021@qq.com");
        exitItem = new JMenuItem("退出       ");
        //author.setEnabled(false);
        trayCover.setType(Window.Type.UTILITY);
        trayCover.setBounds(0, 0, 0, 0);
        trayCover.setUndecorated(true);
        trayCover.getContentPane().setBackground(Config.TRANSPARENT);
        trayCover.setOpacity(0f);
        trayCover.setVisible(true);
        // author.setFont(Config.SongTi);
        showItem.setFont(Config.SongTi);
        autoStart.setFont(Config.SongTi);
        onTopItem.setFont(Config.SongTi);
        settingItem.setFont(Config.SongTi);
        exitItem.setFont(Config.SongTi);
        if (SystemTray.isSupported()) {
            try {
                jMenu.add(showItem);
                jMenu.add(onTopItem);
                jMenu.addSeparator();
                jMenu.add(autoStart);
                // jMenu.addSeparator();
                // jMenu.add(author);
                jMenu.addSeparator();
                jMenu.add(settingItem);
                jMenu.addSeparator();
                jMenu.add(exitItem);
                jMenu.revalidate();
                jMenu.repaint();
                trayIcon.setImageAutoSize(true);
                systemTray.add(trayIcon);
                //任务栏监听
                trayIcon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (timer[0] != null && timer[0].isShutdown()) {
                            autoUpdate[0].run();
                        }
                        if (e.getClickCount() == 2 && !jMenu.isVisible()) {
                            showItem.setState(true);
                            restoreWinAppearance();
                            win.setVisible(true);
                        }
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            int sx = (int) (e.getXOnScreen() / Config.DPI);
                            int sy = (int) (e.getYOnScreen() / Config.DPI);
                            SwingUtilities.invokeLater(() -> {
                                trayCover.setLocation(sx, sy);
                                //判断显示空间是否足够, 调整显示位置
                                int mw = jMenu.getWidth();
                                int mh = jMenu.getHeight();
                                int fx = 0;
                                int fy = 0;
                                if (sy + mh > Config.SCREEN_HEIGHT) fy = -mh;
                                if (sx + mw > Config.SCREEN_WIDTH) fx = -mw;
                                jMenu.show(trayCover, fx, fy);
                            });
                        }
                    }
                });
                //退出
                exitItem.addActionListener(e -> System.exit(0));
                //显示
                showItem.addItemListener(e -> {
                    if (timer[0] != null && timer[0].isShutdown()) {
                        autoUpdate[0].run();
                    }
                    restoreWinAppearance();
                    win.setVisible(!win.isVisible());
                });
                //置顶
                onTopItem.addItemListener(e -> {
                    win.setAlwaysOnTop(!win.isAlwaysOnTop());
                    Config.iniMap.put("alwaysOnTop", "" + (win.isAlwaysOnTop() ? 1 : 0));
                    Config.syncMyPreference();
                });
                //设置
                settingItem.addActionListener(e -> {
                            update.setEnabled(false);
                            query.setEnabled(false);
                            center.setVisible(false);
                            createSettingsForm();
                            scroll.setViewportView(settings);
                            win.setVisible(true);
                        }
                );
                //开机自启
                autoStart.addItemListener(e -> {
                    if (autoStart.isSelected()) {
                        if (!AutoStart.enableAutoStart())
                            autoStart.setSelected(false);
                    } else {
                        if (!AutoStart.disableAutoStart())
                            autoStart.setSelected(true);
                    }
                    Config.iniMap.put("autoStart", "" + (autoStart.isSelected() ? 1 : 0));
                    Config.syncMyPreference();
                });
            } catch (AWTException e) {
                createLog(e);
            }
        }
        //监听
        //鼠标经过监听
        update.addMouseListener(HOVER_EVENT);
        query.addMouseListener(HOVER_EVENT);
        close.addMouseListener(HOVER_EVENT);
        save.addMouseListener(HOVER_EVENT);
        goBack.addMouseListener(HOVER_EVENT);
        MouseAdapter blueHover = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                if (!isDragging[0]) {
                    e.getComponent().setForeground(Config.DEEP_BLUE);
                    e.getComponent().setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                if (!isDragging[0]) {
                    e.getComponent().setForeground(Config.BLUE);
                    e.getComponent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };
        setting.addMouseListener(blueHover);
        author.addMouseListener(blueHover);
        author.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/echopan"));
                } catch (IOException | URISyntaxException ex) {
                    createLog(ex);
                }
            }
        });
        practice.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(practiceUrl));
                } catch (IOException | URISyntaxException ex) {
                    createLog(ex);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                if (!isDragging[0]) {
                    practice.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    practice.setToolTipText(practiceUrl);
                } else {
                    practice.setToolTipText(null);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                if (!isDragging[0]) practice.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        final int[] ox = new int[1];//默认x坐标
        final int[] oy = new int[1];//默认y坐标
        final int[] nx = new int[1];//移动后x坐标
        final int[] ny = new int[1];//移动后y坐标
        // 组合所有可能的事件掩码
        // long allEventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK
        //         | AWTEvent.MOUSE_EVENT_MASK
        //         | AWTEvent.MOUSE_WHEEL_EVENT_MASK
        //         | AWTEvent.KEY_EVENT_MASK
        //         | AWTEvent.WINDOW_EVENT_MASK
        //         | AWTEvent.ACTION_EVENT_MASK
        //         | AWTEvent.ADJUSTMENT_EVENT_MASK
        //         | AWTEvent.ITEM_EVENT_MASK
        //         | AWTEvent.TEXT_EVENT_MASK
        //         | AWTEvent.INPUT_METHOD_EVENT_MASK
        //         | AWTEvent.COMPONENT_EVENT_MASK
        //         | AWTEvent.CONTAINER_EVENT_MASK
        //         | AWTEvent.FOCUS_EVENT_MASK;
        // 添加全局 AWT 事件监听器, 监听鼠标移动, 鼠标滚轮, 动作, 增删子组件
        // Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
        //     //System.out.println("Mouse moved: " + e.getPoint() + " on " + e.getComponent());
        //     win.revalidate();//重新布局
        //     win.repaint();//重新绘制
        // }, allEventMask);

        //窗口监听
        win.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //缩放检测
                super.componentResized(e);
                Config.iniMap.put("winWidth", "" + win.getWidth());
                Config.iniMap.put("winHeight", "" + win.getHeight());
                Config.syncMyPreference();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                //移动检测
                super.componentMoved(e);
                Config.iniMap.put("winX", "" + win.getX());
                Config.iniMap.put("winY", "" + win.getY());
                Config.syncMyPreference();
            }
        });
        //滚动窗口监听
        final int[] thumbPos = {0};//滑块位置
        //滚动监听
        bar.addAdjustmentListener(e -> {
            //修改尺寸时阻止滚动条位置变化
            if (resizeAble[0]) {
                bar.setValue(thumbPos[0]);
            }
        });
        //滚动条鼠标监听(进入 按下 释放 点击 离去)
        bar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                nx[0] = e.getXOnScreen();
                ny[0] = e.getYOnScreen();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (isDragging[0]) {
                    isDragging[0] = false;
                    Dimension size = bar.getSize();
                    int w = size.width;
                    int h = size.height;
                    int x = e.getX();
                    int y = e.getY();
                    if (!(Math.abs(w - x) <= Config.CORNER_SIZE && Math.abs(h - y) <= Config.CORNER_SIZE)) {
                        resizeAble[0] = false;
                        scroll.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                if (!isDragging[0]) {
                    resizeAble[0] = false;
                    scroll.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        //滚动条角标缩放窗口大小
        bar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                Dimension size = bar.getSize();
                int w = size.width;
                int h = size.height;
                int x = e.getX();
                int y = e.getY();
                //角标区域判断(右下)
                if (Math.abs(w - x) <= Config.CORNER_SIZE && Math.abs(h - y) <= Config.CORNER_SIZE) {
                    thumbPos[0] = bar.getModel().getValue();
                    scroll.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
                    resizeAble[0] = true;
                } else {
                    scroll.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    resizeAble[0] = false;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (resizeAble[0]) {
                    isDragging[0] = true;
                    Dimension size = bar.getSize();
                    int w = size.width;
                    int h = size.height;
                    int x = e.getX();
                    int y = e.getY();
                    ox[0] = nx[0];
                    oy[0] = ny[0];
                    nx[0] = e.getXOnScreen();
                    ny[0] = e.getYOnScreen();
                    int fw = nx[0] - ox[0] + win.getWidth();
                    int fh = ny[0] - oy[0] + win.getHeight();
                    if ((win.getWidth() == win.getMinimumSize().width && (nx[0] <= ox[0] || Math.abs(w - x) > Config.CORNER_SIZE)) || (win.getWidth() >= win.getMaximumSize().width && (nx[0] >= ox[0] || Math.abs(w - x) > Config.CORNER_SIZE))) {
                        fw = win.getWidth() == win.getMinimumSize().width ? win.getWidth() : win.getMaximumSize().width;
                    }
                    if ((win.getHeight() == win.getMinimumSize().height && (ny[0] <= oy[0] || Math.abs(h - y) > Config.CORNER_SIZE)) || (win.getHeight() >= win.getMaximumSize().height && (ny[0] >= oy[0] || Math.abs(h - y) > Config.CORNER_SIZE))) {
                        fh = win.getHeight() == win.getMinimumSize().height ? win.getHeight() : win.getMaximumSize().height;
                    }
                    win.setSize(fw, fh);
                    win.revalidate();//重新布局
                    win.repaint();//重新绘制
                }
            }
        });
        //滚动列表监听
        MouseAdapter scrollViewPointMouseAdatper = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (isDragging[0]) {
                    isDragging[0] = false;
                    Dimension size = scroll.getSize();
                    int w = size.width;
                    int h = size.height;
                    int x = e.getX();
                    int y = e.getY();
                    if (!(Math.abs(w - x) <= Config.CORNER_SIZE && Math.abs(h - y) <= Config.CORNER_SIZE)) {
                        resizeAble[0] = false;
                        scroll.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                nx[0] = e.getXOnScreen();
                ny[0] = e.getYOnScreen();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                if (!isDragging[0]) {
                    resizeAble[0] = false;
                    scroll.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };
        //滚动列表角标缩放窗口大小
        MouseMotionAdapter scrollViewPointMouseMotionAdatper = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                //如果滚动条存在,则不触发
                if (bar.isVisible()) {
                    scroll.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
                Dimension size = scroll.getSize();//panel溢出部分隐藏,故不使用panel作为参考
                int w = size.width;
                int h = size.height;
                int x = e.getX();
                int y = e.getY();
                if (Math.abs(w - x) <= Config.CORNER_SIZE && Math.abs(h - y) <= Config.CORNER_SIZE) {
                    scroll.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
                    resizeAble[0] = true;
                } else {
                    scroll.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    resizeAble[0] = false;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (resizeAble[0]) {
                    isDragging[0] = true;
                    Dimension size = scroll.getSize();
                    int w = size.width;
                    int h = size.height;
                    int x = e.getX();
                    int y = e.getY();
                    ox[0] = nx[0];
                    oy[0] = ny[0];
                    nx[0] = e.getXOnScreen();
                    ny[0] = e.getYOnScreen();
                    int fw = nx[0] - ox[0] + win.getWidth();
                    int fh = ny[0] - oy[0] + win.getHeight();
                    if ((win.getWidth() == win.getMinimumSize().width && (nx[0] <= ox[0] || Math.abs(w - x) > Config.CORNER_SIZE)) || (win.getWidth() >= win.getMaximumSize().width && (nx[0] >= ox[0] || Math.abs(w - x) > Config.CORNER_SIZE))) {
                        fw = win.getWidth() == win.getMinimumSize().width ? win.getWidth() : win.getMaximumSize().width;
                    }
                    if ((win.getHeight() == win.getMinimumSize().height && (ny[0] <= oy[0] || Math.abs(h - y) > Config.CORNER_SIZE)) || (win.getHeight() >= win.getMaximumSize().height && (ny[0] >= oy[0] || Math.abs(h - y) > Config.CORNER_SIZE))) {
                        fh = win.getHeight() == win.getMinimumSize().height ? win.getHeight() : win.getMaximumSize().height;
                    }
                    win.setSize(fw, fh);
                    win.revalidate();//重新布局
                    win.repaint();//重新绘制
                }
            }
        };
        //滚动列表缩放监听
        home.addMouseListener(scrollViewPointMouseAdatper);
        settings.addMouseListener(scrollViewPointMouseAdatper);
        home.addMouseMotionListener(scrollViewPointMouseMotionAdatper);
        settings.addMouseMotionListener(scrollViewPointMouseMotionAdatper);
        //标题栏鼠标动作监听
        head.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                //左上角角标判断
                int x = e.getX();
                int y = e.getY();
                if (Math.abs(x) <= Config.CORNER_SIZE && Math.abs(y) <= Config.CORNER_SIZE) {
                    win.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
                    resizeAbleOnLeft[0] = true;
                } else {
                    win.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    resizeAbleOnLeft[0] = false;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                ox[0] = nx[0];
                oy[0] = ny[0];
                nx[0] = e.getXOnScreen();
                ny[0] = e.getYOnScreen();
                int fx = nx[0] - ox[0] + win.getX();
                int fy = ny[0] - oy[0] + win.getY();
                if (resizeAbleOnLeft[0]) {
                    //缩放窗口(左上角)
                    isDragging[0] = true;
                    int fw = ox[0] - nx[0] + win.getWidth();
                    int fh = oy[0] - ny[0] + win.getHeight();
                    if ((win.getWidth() == win.getMinimumSize().width && (nx[0] >= ox[0] || Math.abs(e.getX()) > Config.CORNER_SIZE)) || (win.getWidth() >= win.getMaximumSize().width && (nx[0] <= ox[0] || Math.abs(e.getX()) > Config.CORNER_SIZE))) {
                        fx = win.getX();
                        fw = win.getWidth() == win.getMinimumSize().width ? win.getWidth() : win.getMaximumSize().width;//bug:无法自动限制为MaximumSize
                    }
                    if ((win.getHeight() == win.getMinimumSize().height && (ny[0] >= oy[0] || Math.abs(e.getY()) > Config.CORNER_SIZE)) || (win.getHeight() >= win.getMaximumSize().height && (ny[0] <= oy[0] || Math.abs(e.getY()) > Config.CORNER_SIZE))) {
                        fy = win.getY();
                        fh = win.getHeight() == win.getMinimumSize().height ? win.getHeight() : win.getMaximumSize().height;
                    }
                    win.setSize(fw, fh);
                }
                win.revalidate();//重新布局
                win.repaint();//重新绘制
                if (resizeAbleOnLeft[0] || e.getX() > Config.CORNER_SIZE) {
                    //移动窗口
                    win.setLocation(fx, fy);
                }
            }
        });
        head.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (isDragging[0]) {
                    isDragging[0] = false;
                    int x = e.getX();
                    int y = e.getY();
                    //分情况: 1.在源组件上释放;2.在其他组件上释放
                    if (!(Math.abs(x) <= Config.CORNER_SIZE && Math.abs(y) <= Config.CORNER_SIZE)) {
                        resizeAbleOnLeft[0] = false;
                        win.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                nx[0] = e.getXOnScreen();
                ny[0] = e.getYOnScreen();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                if (!isDragging[0]) {
                    resizeAbleOnLeft[0] = false;
                    win.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        //最小化监听
        close.addActionListener(e -> {
            if (timer[0] != null && timer[0].isShutdown()) {
                autoUpdate[0].run();
            }
            showItem.setState(false);
            restoreWinAppearance();
            win.setVisible(false);
        });
        //查询监听
        final SwingWorker[] queryWorker = new SwingWorker[1];
        final SwingWorker[] updateWorker = new SwingWorker[1];
        final boolean[] isQuerying = {false};
        final boolean[] isUpdating = {false};
        query.addActionListener(e -> {
            if (!isQuerying[0]) isQuerying[0] = true;
            else return;
            update.setEnabled(false);
            queryWorker[0] = new SwingWorker() {
                @Override
                protected Object doInBackground() {
                    //检查自动更新
                    if (timer[0] != null && timer[0].isShutdown()) {
                        autoUpdate[0].run();
                    }
                    //查询
                    Vector<Score> scoreList = MyContentAnalysis.getScore(MyHttpRequest.doPost(post, Config.getScoreQueryParams(), null).getContent());
                    if (null == scoreList) {
                        showErrorMsg("连接超时", "成绩查询超时, 请白天或稍后再试");
                        return null;
                    }
                    //展示信息
                    scroll.getViewport().setViewPosition(new Point(0, 0));//滚动到顶部
                    home.removeAll();
                    GridBagLayout grid = new GridBagLayout();
                    JPanel gridPanel = new JPanel(grid);//网格表
                    GridBagConstraints set = new GridBagConstraints();//网格配置
                    home.add(gridPanel);
                    gridPanel.setBackground(Config.TRANSPARENT);
                    gridPanel.setBorder(BorderFactory.createEmptyBorder(1, 6, 15, 6));
                    set.fill = GridBagConstraints.HORIZONTAL;
                    set.weighty = 0;//y轴方向不扩展
                    set.weightx = 1;
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < Objects.requireNonNull(scoreList).size(); i++) {
                        for (int j = 0; j < 6; j++) {
                            JLabel label = new JLabel(scoreList.get(i).get(j));//网格条目
                            label.setForeground(Color.white);
                            label.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 3));
                            label.setFont(Config.HeiTi);
                            if (j == 5) {
                                set.gridwidth = GridBagConstraints.REMAINDER;
                            } else {
                                set.gridwidth = 1;
                            }
                            if (j != 0 & j != 1) {
                                gridPanel.add(label, set);
                            }
                            sb.append(label.getText()).append("\t");
                        }
                        sb.append("\n");
                    }
                    if (scoreList.size() == 1) {
                        JLabel label = new JLabel("未找到成绩信息, 请检查个人信息是否设置有误(右击任务栏图标-设置) *仅限江苏自考可查*");
                        label.setForeground(Color.white);
                        label.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 3));
                        label.setFont(Config.HeiTi);
                        gridPanel.add(label, set);
                    }
                    gridPanel.setMaximumSize(new Dimension(Config.SCREEN_WIDTH, gridPanel.getPreferredSize().height));
                    win.revalidate();//重新布局
                    win.repaint();//重新绘制
                    PrintWriter out;
                    File file = Config.txtScore;
                    //保存文件
                    try {
                        Files.createDirectories(Paths.get(Config.txtScore.getParentFile().getPath()));
                        out = new PrintWriter(file, StandardCharsets.UTF_8);
                        out.print(sb);
                        out.flush();
                        out.close();
                    } catch (IOException ex) {
                        createLog(ex);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    isQuerying[0] = false;
                    update.setEnabled(true);
                }
            };
            queryWorker[0].execute();
        });
        //更新监听
        final boolean[] isFstTm = {true};//是否为首次加载
        update.addActionListener(e -> {
            if (resizeAbleOnLeft[0]) return;
            else if (!isUpdating[0]) isUpdating[0] = true;
            else return;
            query.setEnabled(false);
            Box doing = Box.createVerticalBox();
            JLabel doingTxt = new JLabel();
            if (isFstTm[0]) {
                home.removeAll();
                doingTxt.setText("资讯正在更新中, 请稍后..");
                doingTxt.setFont(Config.HeiTi);
                doingTxt.setForeground(Color.white);
                doingTxt.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
                doing.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
                doing.add(doingTxt);
                //doing.add(Box.createHorizontalGlue());
                home.add(doing);
                win.revalidate();
                win.repaint();
            }
            updateWorker[0] = new SwingWorker() {
                @Override
                protected Object doInBackground() {
                    win.repaint();
                    //重置计时器
                    if (timer[0] != null && timer[0].isShutdown()) {
                        autoUpdate[0].run();
                    }
                    scroll.getViewport().setViewPosition(new Point(0, 0));//位置还原

                    Vector<Info> infoList;//消息列表
                    Vector<Info> oldList = new Vector<>();//旧的消息
                    int page = 2;//第二页
                    StringBuffer sb = new StringBuffer();
                    //考试消息查询
                    infoList = MyContentAnalysis.getList(MyHttpRequest.doGet(get, null, null).getContent());//获取第一页内容
                    if (null == infoList) {
                        if (isFstTm[0] && Config.txtLog.exists()) {
                            doingTxt.setText("资讯更新失败, 请白天或稍后再试");
                            JButton chkLog = new JButton("查看日志");
                            chkLog.setFocusPainted(false);
                            chkLog.setUI(new MyButtonUI());
                            chkLog.setFont(Config.SongTi);
                            chkLog.setOpaque(false);
                            chkLog.setBackground(Config.TRANSPARENT);
                            chkLog.setForeground(Color.white);
                            chkLog.setBorder(BorderFactory.createEmptyBorder(9, 4, 5, 4));
                            chkLog.addMouseListener(HOVER_EVENT);
                            chkLog.addActionListener(e -> {
                                try {
                                    Desktop.getDesktop().open(Config.txtLog);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                            doing.add(chkLog);
                            doing.add(Box.createVerticalGlue());
                            win.revalidate();
                            win.repaint();
                        } else trayIcon.displayMessage("自考|更新出错", "自考资讯更新失败, 请白天或稍后再试", TrayIcon.MessageType.ERROR);
                        return null;
                    }
                    isFstTm[0] = false;
                    //招考信息(MAX_PAGES: 最高页数, ITEM_MAX_NUM: 最多项目数)
                    while (infoList.size() < ITEM_MAX_NUM && page <= MAX_PAGES) {
                        Vector<Info> temp = MyContentAnalysis.getList(MyHttpRequest.doGet(get2 + (page++) + ".html", null, null).getContent());
                        if (temp != null) {
                            infoList.addAll(temp);
                        }
                    }
                    //考试日程表查询
                    try {
                        Vector<Info> tmp = MyContentAnalysis.getList(MyHttpRequest.doGet(getPolicy, null, null).getContent());
                        if (null != tmp) {
                            page = 2;
                            while (tmp.size() < ITEM_MAX_NUM / 2 && page <= MAX_PAGES) {
                                Vector<Info> temp = MyContentAnalysis.getList(MyHttpRequest.doGet(getPolicy2 + (page++) + ".html", null, null).getContent());
                                if (temp != null) {
                                    tmp.addAll(temp);
                                    Info.dedupInfo(tmp);
                                }
                            }
                            //消息按日期降序插入
                            for (Info value : tmp) {
                                for (Info info : infoList) {
                                    Calendar ori = Calendar.getInstance();//初始消息的日期
                                    Calendar tab = Calendar.getInstance();//待插入消息的日期
                                    ori.setTime(Config.sdf.parse(info.date + " 00:00:00"));
                                    tab.setTime(Config.sdf.parse(value.date + " 00:00:00"));
                                    if (tab.compareTo(ori) >= 0) {
                                        infoList.add(infoList.indexOf(info), value);
                                        break;
                                    } else if (infoList.indexOf(info) == (infoList.size() - 1)) {
                                        infoList.add(value);
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    //导出
                    Vector<Info> tp = new Vector<>();
                    for (Info value : infoList) {
                        //限制项目数
                        if (infoList.indexOf(value) < ITEM_MAX_NUM) {
                            sb.append(value).append("\n");
                            tp.add(value);
                        }
                    }
                    infoList.removeAllElements();
                    infoList.addAll(tp);
                    PrintWriter out;
                    BufferedReader br;
                    String line;
                    String[] arr;
                    int count = 0;
                    File file = Config.txtInfoList;
                    File old = Config.txtInfoListOld;
                    //保存文件
                    try {
                        Files.createDirectories(Paths.get(Config.txtInfoList.getParentFile().getPath()));
                        if (file.exists()) {
                            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                            StringBuffer temp = new StringBuffer();
                            while ((line = br.readLine()) != null) {
                                temp.append(line).append("\n");
                            }
                            out = new PrintWriter(old, StandardCharsets.UTF_8);
                            out.print(temp);
                            out.flush();
                            out.close();
                            br.close();
                        }
                        out = new PrintWriter(file, StandardCharsets.UTF_8);
                        out.print(sb);
                        out.flush();
                        out.close();
                        if (old.exists()) {
                            br = new BufferedReader(new InputStreamReader(new FileInputStream(old), StandardCharsets.UTF_8));
                            Info info;
                            while ((line = br.readLine()) != null) {
                                arr = line.split("\\t");
                                if (arr.length > 1) {
                                    info = new Info(arr[0].trim(), arr[1].trim());
                                    oldList.add(info);
                                }
                            }
                            br.close();
                            //寻找旧列表第一项在新列表中的排名
                            while (count < infoList.size() && (!oldList.firstElement().content.equals(infoList.get(count).content))) {
                                count++;
                            }
                            if (count != 0) {
                                trayIcon.displayMessage("自考|新消息!", infoList.get(0).content, TrayIcon.MessageType.INFO);//win10消息提醒
                            }
                        }
                    } catch (NoSuchElementException ex) {
                        System.out.println("删除旧列表文件");
                        System.out.println(count);
                        old.delete();
                    } catch (IOException ex) {
                        createLog(ex);
                    }
                    //展示信息
                    home.removeAll();
                    //panel.add(Box.createVerticalStrut(6));
                    JLabel content, date, newTag; //内容 日期 新增
                    Box note;//条目
                    for (int i = 0; i < infoList.size(); i++) {
                        note = Box.createHorizontalBox();
                        content = new JLabel(infoList.get(i).content);
                        date = new JLabel(infoList.get(i).date);
                        newTag = new JLabel("new");
                        newTag.setForeground(Color.RED);
                        content.setForeground(Color.white);
                        content.setFont(Config.HeiTi);
                        date.setFont(Config.HeiTi);
                        newTag.setFont(Config.HeiTi);
                        date.setForeground(Color.white);
                        note.setBorder(BorderFactory.createEmptyBorder(10, 6, 8, 8));
                        note.add(content);
                        //新闻不超2天或者有更新时
                        int space = 0;
                        try {
                            Calendar past = Calendar.getInstance();
                            Calendar now = Calendar.getInstance();
                            past.setTime(Config.sdf.parse(date.getText() + " 00:00:00"));
                            now.setTime(Config.sdf.parse(Config.sdf.format(new Date())));
                            space = getSpaceInDays(past, now);
                        } catch (ParseException ex) {
                            createLog(ex);
                        }
                        if (i < count || space <= 2) {
                            note.add(newTag);
                        }
                        note.add(Box.createHorizontalGlue());
                        note.add(date);
                        home.add(note);
                        content.setOpaque(false);
                        date.setOpaque(false);
                        note.setOpaque(false);
                        note.addMouseListener(HOVER_EVENT);
                        URI link;
                        try {
                            link = new URI(infoList.get(i).link);
                        } catch (URISyntaxException ex) {
                            createLog(ex);
                            link = null;
                        }

                        //跳转超链接
                        URI uri = link;
                        note.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                super.mouseClicked(e);
                                try {
                                    if (uri != null) {
                                        Desktop.getDesktop().browse(uri);
                                    } else {
                                        Desktop.getDesktop().browse(new URI("https://www.jseea.cn/webfile/examination/selflearning/"));
                                    }
                                } catch (IOException | URISyntaxException ex) {
                                    createLog(ex);
                                }
                            }
                        });
                    }
                    win.revalidate();//重新布局
                    win.repaint();//重新绘制
                    return null;
                }

                @Override
                protected void done() {
                    isUpdating[0] = false;
                    query.setEnabled(allowQuery);
                }
            };
            updateWorker[0].execute();
        });
        //监听配置按钮
        setting.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                update.setEnabled(false);
                query.setEnabled(false);
                center.setVisible(false);
                createSettingsForm();
                scroll.setViewportView(settings);
            }
        });
        //配置返回
        goBack.addActionListener(e -> restoreWinAppearance());
        //配置保存
        save.addActionListener(e -> {
                    for (String key : settingsMap.keySet()) {
                        Config.iniSecretMap.put(key, settingsMap.get(key).getText());
                    }
                    Config.syncMySecret();
                    restoreWinAppearance();
                    updatePracticeEnrol().execute();
                }
        );
        //初始化并加载配置
        {
            //检查日志
            if (Config.txtLog.exists()) {
                if (Config.txtLogOld.exists())
                    Config.txtLogOld.delete();
                Config.txtLog.renameTo(Config.txtLogOld);
            }
            //加载外观
            Config.loadMyPreference();
            //更新实践信息
            updatePracticeEnrol().execute();
            //更新窗口属性
            int iw = Integer.parseInt(Config.iniMap.get("winWidth"));
            int ih = Integer.parseInt(Config.iniMap.get("winHeight"));
            int minW = win.getMinimumSize().width;
            int maxW = win.getMaximumSize().width;
            int minH = win.getMinimumSize().height;
            int maxH = win.getMaximumSize().height;
            win.setLocation(Integer.parseInt(Config.iniMap.get("winX")), Integer.parseInt(Config.iniMap.get("winY")));
            win.setSize(Math.max(Math.min(iw, maxW), minW), Math.max(Math.min(ih, maxH), minH));
            win.setOpacity(Float.parseFloat(Config.iniMap.get("winOpacity")));// 窗口透明
            update.doClick();
            autoUpdate[0].run();
            //绘制窗口
            restoreWinAppearance();
            //更新右键菜单选项
            onTopItem.setSelected(0 != Integer.parseInt(Config.iniMap.get("alwaysOnTop")));
            autoStart.setSelected(0 != Integer.parseInt(Config.iniMap.get("autoStart")));
            //显示界面
            win.setVisible(true);
        }
    }

    /**
     * 更新实践报名时间信息
     */
    private static SwingWorker updatePracticeEnrol() {
        final String[] tips = {"暂无实践信息, 请先完成"};
        final boolean[] ifSearch = {false};
        allowSetting = false;
        allowQuery = false;
        allowCenter = false;
        settingItem.setEnabled(false);
        center.setVisible(false);
        practice.setForeground(Color.white);
        int rsCode = Config.loadMySecret();
        System.out.println("配置载入代码:" + rsCode);
        //判断是否满足查询成绩和实践信息的条件
        if (rsCode == Config.PRACTICE_TOKEN_NOT_FOUND) allowQuery = true;
        if (rsCode == Config.SCORE_TOKEN_NOT_FOUND) ifSearch[0] = true;
        else if (rsCode == Config.ALL_PERFECT) {
            allowQuery = true;
            ifSearch[0] = true;
        }
        //是否可查成绩
        query.setEnabled(allowQuery);
        return new SwingWorker() {
            @Override
            protected Object doInBackground() {
                //是否可查实践
                if (ifSearch[0]) {
                    Config.loadPracticeEnrol();
                    String old = "".equals(Config.practiceMap.get("school")) ? null : createPracticeNote(Config.practiceMap);
                    String fresh = null;
                    int status = 0;
                    practiceUrl = getPractice;
                    WebPage site = MyHttpRequest.doGet(getPractice, null, null);
                    String id = MyContentAnalysis.getSchollId(site.getContent(), Config.iniSecretMap.get("school"));
                    status = site.getNote();
                    if (null != id) {
                        practiceUrl = getPractice + "?khxx=" + id;
                        site = MyHttpRequest.doGet(practiceUrl, null, null);
                        Map<String, String> practiceMap = MyContentAnalysis.getPracticeRegiInfo(site.getContent());
                        status = status == 0 ? site.getNote() : status;
                        if (null != practiceMap) {
                            Config.practiceMap.putAll(practiceMap);
                            Config.syncPracticeEnrol();
                            tips[0] = createPracticeNote(Config.practiceMap);
                            fresh = tips[0];
                            allowSetting = false;
                        } else {
                            tips[0] = "暂无报名";
                            if (null != old) tips[0] += ", 上次为: " + old;
                            allowSetting = true;
                        }
                    } else {
                        tips[0] = "暂无该校";
                        if (null != old) tips[0] += ", 上次为: " + old;
                        allowSetting = true;
                    }
                    if (status == WebPage.TIMEOUT) {
                        tips[0] = "网络超时";
                        if (null != old) tips[0] += ", 上次为: " + old;
                        allowSetting = true;
                    }
                    if (null != fresh) {
                        allowSetting = false;
                        try {
                            Calendar now = Calendar.getInstance();
                            Calendar head = Calendar.getInstance();
                            Calendar tail = Calendar.getInstance();
                            now.setTime(Config.sdf.parse(Config.sdf.format(new Date())));
                            head.setTime(Config.sdf.parse(Config.practiceMap.get("start") + " 00:00:00"));
                            tail.setTime(Config.sdf.parse(Config.practiceMap.get("end") + " 00:00:00"));
                            //提前一天高亮报名时间
                            if (getSpaceInDays(head, now) >= -1 && getSpaceInDays(now, tail) >= 0) {
                                practice.setForeground(Color.red);
                            } else {
                                practice.setForeground(Color.white);
                            }
                        } catch (ParseException e) {
                            createLog(e);
                        }
                    }
                } else {
                    allowSetting = true;
                }
                return null;
            }

            @Override
            protected void done() {
                super.done();
                practice.setText(tips[0]);
                setting.setVisible(allowSetting);
                settingItem.setEnabled(true);
                allowCenter = true;
                center.setVisible(true);
            }
        };
    }

    /**
     * 恢复窗口默认外观
     */
    private static void restoreWinAppearance() {
        update.setEnabled(allowUpdate);
        query.setEnabled(allowQuery);
        center.setVisible(allowCenter);
        scroll.setViewportView(home);
        win.revalidate();//重新布局
        win.repaint();//重新绘制
    }

    /**
     * 生成配置页表单
     */
    private static void createSettingsForm() {
        middle.removeAll();
        settingsMap.clear();
        for (String key : Config.iniSecretMap.keySet()) {
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel item = new JPanel(new GridBagLayout());
            JLabel attr = new JLabel(Config.aliasMap.get(key) + ": ");
            JTextField value = new JTextField(30);
            settingsMap.put(key, value);
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.weighty = 0;
            value.setFont(Config.HeiTi);
            value.setForeground(Color.white);
            value.setOpaque(false);
            value.setBackground(Config.TRANSPARENT);
            value.setText(Config.iniSecretMap.get(key));
            value.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));
            value.setCaretColor(Color.white);
            attr.setFont(Config.HeiTi);
            attr.setForeground(Color.white);
            attr.setHorizontalAlignment(JLabel.RIGHT);
            attr.setPreferredSize(new Dimension(save.getPreferredSize().width * 2, attr.getPreferredSize().height));
            item.setOpaque(false);
            item.setBackground(Config.TRANSPARENT);
            item.add(attr, gbc);
            gbc.gridx = 1;
            item.add(value, gbc);
            item.setBorder(BorderFactory.createEmptyBorder(2, 4, 6, 4));
            middle.add(item);
            item.setMaximumSize(new Dimension(item.getPreferredSize().width, save.getPreferredSize().height * 2));
        }
        middle.add(Box.createHorizontalGlue());
    }

    /**
     * 显示查询出错提示
     */
    private static void showErrorMsg(String title, String msg) {
        JOptionPane.showMessageDialog(win, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 生成实践提示文本
     */
    public static String createPracticeNote(Map<String, String> note) {
        return note.get("school") + " 实践报名时间: " + note.get("start") + "~" + note.get("end");
    }

    /**
     * 获取带下划线的字体
     */
    public static Font createUnderlinedFont(Font font) {
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        return font.deriveFont(attributes);
    }

    /**
     * 获取两日期相差天数
     */
    public static int getSpaceInDays(Calendar from, Calendar to) {
        int space = 0;
        boolean reverse = false;
        if (from.get(Calendar.YEAR) == to.get(Calendar.YEAR)) {
            space = to.get(Calendar.DAY_OF_YEAR) - from.get(Calendar.DAY_OF_YEAR);
        } else {
            if (from.get(Calendar.YEAR) > to.get(Calendar.YEAR)) {
                reverse = true;
                Calendar temp = from;
                from = to;
                to = temp;
            }
            space = from.getActualMaximum(Calendar.DAY_OF_YEAR) - from.get(Calendar.DAY_OF_YEAR);

            for (int year = from.get(Calendar.YEAR) + 1; year < to.get(Calendar.YEAR); year++) {
                Calendar temp = Calendar.getInstance();
                temp.set(year, Calendar.JANUARY, 1);
                space += temp.getActualMaximum(Calendar.DAY_OF_YEAR);
            }
            space += to.get(Calendar.DAY_OF_YEAR);
        }
        if (reverse) {
            space = -space;
        }
        return space;
    }

    public static void createLog(Exception e) {
        e.printStackTrace();
        File log;
        String str;
        StringWriter sw;
        BufferedWriter bw;
        try {
            Files.createDirectories(Paths.get(Config.txtLog.getParentFile().getPath()));
            log = Config.txtLog;
            sw = new StringWriter();
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log, true), StandardCharsets.UTF_8));
            e.printStackTrace(new PrintWriter(sw, true));
            str = sw.toString();
            bw.write("======" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "======\n");
            bw.write(str);
            bw.flush();
            bw.close();
            //trayIcon.displayMessage("查询遇到问题, 具体请查看日志文件.", str.replaceAll("[\\r\\n]", " "), TrayIcon.MessageType.ERROR);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // public static void extra() {
    //     System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    //     String username = "***";
    //     String password = "***";
    //     int enter = 2;
    //     //方法一: 无头浏览器, 模拟人工
    //     if (enter == 1) {
    //         com.echo.examinfo.WebPage fakeLogin = com.echo.examinfo.Encrypt4RSA.fakeLogin(username, password);
    //         System.out.println("test fakelogin cookie:" + fakeLogin.getCookies());
    //         System.out.println(com.echo.examinfo.MyHttpRequest.doPost("https://sdata.jseea.cn/tpl_front/shzk/sjlwbk/kbkList.html", null, fakeLogin.getCooStrMap()).getContent());
    //         System.exit(0);
    //     }
    //     //方法二: 虚构浏览器对象, 纯数据交互
    //     if (enter == 2) {
    //         try {
    //             String loginUrl = "https://sdata.jseea.cn/tpl_front/login.html";
    //             String imageUrl = "https://sdata.jseea.cn/codeMsg.html?t=";
    //             String formUrl = "https://sdata.jseea.cn/j_spring_security_check";
    //             String oriCode = "./temp/codeMsg.png";
    //             String denoCode = "./temp/denoise.png";
    //             com.echo.examinfo.WebPage loginPage, imgPage;
    //             Map<String, String> token;
    //             //访问登录页
    //             System.out.println("\n===访问登录页");
    //             loginPage = com.echo.examinfo.MyHttpRequest.doGet(loginUrl, null, null);
    //             token = loginPage.getCooStrMap();
    //             Thread.sleep(1000);
    //             //下载图片
    //             System.out.println("===开始下载图片");
    //             imgPage = com.echo.examinfo.MyHttpRequest.doGet(imageUrl + System.currentTimeMillis(), token, "file");
    //             token = loginPage.setCookies(imgPage.getCookies()).getCooStrMap();
    //             imgPage.saveFile(oriCode);
    //             System.out.println("===图片下载完成");
    //             // 进行 OCR 识别
    //             String code = com.echo.examinfo.ImageAnalysis.recognizeImage(oriCode, denoCode);
    //             //加密数据
    //             String encrypted = com.echo.examinfo.Encrypt4RSA.encrypt4RSA2(username, password, token);
    //             if (null == encrypted) System.exit(0);
    //             encrypted += "&u_securitycode=" + "undefined" + "%2C" + code;//%2C 逗号
    //             System.out.println("表单参数: " + encrypted);
    //             Thread.sleep(2000);
    //             //设置请求头
    //             Map<String, String> headers = new HashMap<>(8);
    //             headers.putAll(token);
    //             headers.put("Referer", loginUrl);
    //             //提交表单
    //             System.out.println("===提交表单");
    //             com.echo.examinfo.WebPage submitPage = com.echo.examinfo.MyHttpRequest.doPost(formUrl, encrypted, headers);
    //             System.out.println("===返回结果");
    //             //System.out.println(submitPage.getContent());
    //             System.out.println("======判断是否登录成功=======");
    //             Boolean isLogged = submitPage.getContent().contains("退出登录");
    //             System.out.println(isLogged ? "登录成功" : "登录失败");
    //             if (isLogged) {
    //                 System.out.println("===报考列表");
    //                 System.out.println(com.echo.examinfo.MyHttpRequest.doGet("https://sdata.jseea.cn/tpl_front/shzk/sjlwbk/kbkList.html", submitPage.getCooStrMap(), null).getContent());
    //
    //             }
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //         }
    //         System.exit(0);
    //     }
    // }
}

