package com.echo.examinfo;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2023/12/2 4:31
 * @description
 */
public class MyScrollBarUI extends BasicScrollBarUI {

    public final static Color SCROLL_TRACK = Color.BLACK;//滑道颜色
    public final static Color SCROLL_THUMB = Color.GRAY;//滑块颜色
        @Override
        protected void configureScrollBarColors() {
            /*滑块*/
            this.thumbColor = SCROLL_THUMB;
            this.thumbHighlightColor = SCROLL_THUMB;
            this.thumbDarkShadowColor = SCROLL_TRACK;
            /*轨道*/
            this.trackColor = SCROLL_TRACK;
            this.trackHighlightColor = SCROLL_TRACK;
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            //滚动条尺寸
            c.setPreferredSize(new Dimension(Config.SCROLL_BAR_WIDTH, 0));
            c.setBorder(BorderFactory.createEmptyBorder());
            return super.getPreferredSize(c);
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            //隐藏滚动条箭头
            JButton button = new JButton();
            Dimension dimension = new Dimension(0, 0);
            button.setPreferredSize(dimension);
            button.setMinimumSize(dimension);
            button.setMaximumSize(dimension);
            button.setBorder(BorderFactory.createEmptyBorder());
            return button;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createIncreaseButton(orientation);
        }
    }
