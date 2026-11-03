package edu.sustech.xiangqi.view;

import edu.sustech.xiangqi.storage.UserSession;
import edu.sustech.xiangqi.storage.UserStore;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

// 登录 / 注册对话框。返回 UserSession；游客返回 username==null；关闭窗口返回 null。
public final class LoginDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private UserSession result;
    private final JTextField userField = new JTextField(16);
    private final JPasswordField passField = new JPasswordField(16);

    private LoginDialog(Component parent) {
        super(parent instanceof java.awt.Frame ? (java.awt.Frame) parent : null,
                "登录 / 注册", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("用户名："));
        form.add(userField);
        form.add(new JLabel("密码："));
        form.add(passField);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 8));
        buttons.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        JButton login = new JButton("登录");
        JButton register = new JButton("注册");
        JButton guest = new JButton("游客进入");
        buttons.add(login);
        buttons.add(register);
        buttons.add(guest);

        login.addActionListener(e -> doLogin());
        register.addActionListener(e -> doRegister());
        guest.addActionListener(e -> {
            result = new UserSession(null);
            dispose();
        });

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    public static UserSession prompt(Component parent) {
        LoginDialog dialog = new LoginDialog(parent);
        dialog.setVisible(true);
        return dialog.result;
    }

    private String username() {
        return userField.getText().trim();
    }

    private String password() {
        return new String(passField.getPassword());
    }

    private void doLogin() {
        String u = username();
        String p = password();
        if (u.isEmpty()) {
            warn("请输入用户名。");
            return;
        }
        if (UserStore.login(u, p)) {
            result = new UserSession(u);
            dispose();
        } else {
            warn("用户名或密码错误。");
        }
    }

    private void doRegister() {
        String u = username();
        String p = password();
        if (!UserStore.isValidUsername(u)) {
            warn("用户名不合法（不能为空，不能包含制表符或换行）。");
            return;
        }
        if (p.isEmpty()) {
            warn("密码不能为空。");
            return;
        }
        if (UserStore.register(u, p)) {
            warn("注册成功，请点击“登录”。");
        } else {
            warn("用户名已存在，注册失败。");
        }
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }
}
