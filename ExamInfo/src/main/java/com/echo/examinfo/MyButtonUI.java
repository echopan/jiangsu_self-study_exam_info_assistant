package com.echo.examinfo;

import javax.swing.*;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.*;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2024/12/4 3:28
 * @description
 */
public class MyButtonUI extends MetalButtonUI {
    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
    }

    @Override
    protected Color getDisabledTextColor() {
        return Color.GRAY;
    }
}
