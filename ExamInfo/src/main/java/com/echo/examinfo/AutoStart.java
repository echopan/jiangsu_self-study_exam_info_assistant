package com.echo.examinfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2024/12/6 23:09
 * @description 控制开机自启
 */
class AutoStart {
    private static final String LAUNCHER = "ExamInfo_start.bat";
    private static final String SHORTCUT = "ExamInfo.lnk";

    /**
     * 获取启动文件夹路径 (针对当前用户)
     */
    private static Path getStartupFolderPath() {
        return Paths.get(System.getenv("APPDATA"), "Microsoft", "Windows", "Start Menu", "Programs", "Startup");
    }

    /**
     * 创建快捷方式并放置到启动文件夹
     */
    public static boolean enableAutoStart() {
        Path startupFolder = getStartupFolderPath();
        Path shortcutPath = startupFolder.resolve(SHORTCUT);
        Path launcherPath = Paths.get(LAUNCHER).toAbsolutePath();
        Path workingDir = launcherPath.getParent();
        String command = "powershell.exe -Command " +
                "\"$WshShell = New-Object -comObject WScript.Shell; " +
                "$Shortcut = $WshShell.CreateShortcut('" + shortcutPath.toString().replace("\\", "\\\\") + "'); " +
                "$Shortcut.TargetPath = '" + launcherPath.toString().replace("\\", "\\\\") + "'; " +
                "$Shortcut.WorkingDirectory = '" + workingDir.toString() + "'; " +
                "$Shortcut.Save()\"";
        // 使用 PowerShell 创建快捷方式
        Process process = null;
        try {
            System.out.println("powershell: " + command);
            process = Runtime.getRuntime().exec(command);

            // 捕获标准输出和错误输出
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // 打印标准输出
            String s;
            StringBuffer records = new StringBuffer("::powershell start\n");
            System.out.println();
            while ((s = stdInput.readLine()) != null) {
                records.append(s + "\n");
            }

            // 打印错误输出
            while ((s = stdError.readLine()) != null) {
                records.append(s + "\n");
            }
            records.append("::powershell end");
            System.out.println(records);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                try {
                    throw new Exception("启用开机自启失败，详情:" + records + "退出码:" + exitCode);
                } catch (Exception e) {
                    ExamInfo.createLog(e);
                    return false;
                }
            }
        } catch (Exception e) {
            ExamInfo.createLog(e);
            return false;
        }
        return true;
    }

    /**
     * 从启动文件夹中移除快捷方式
     */
    public static boolean disableAutoStart() {
        Path startupFolder = getStartupFolderPath();
        Path shortcutPath = startupFolder.resolve(SHORTCUT);
        try {
            if (Files.exists(shortcutPath))
                Files.delete(shortcutPath);
        } catch (Exception e) {
            ExamInfo.createLog(e);
            return false;
        }
        return true;
    }
}
