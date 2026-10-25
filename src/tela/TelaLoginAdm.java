import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TelaLoginAdm extends JFrame {

    private static final Color COR_PRIMARIA        = new Color(13, 110, 130);
    private static final Color COR_PRIMARIA_ESCURA = new Color(9, 78, 99);
    private static final Color COR_FUNDO           = new Color(241, 245, 249);
    private static final Color COR_TEXTO           = new Color(30, 41, 59);
    private static final Color COR_TEXTO_SUAVE     = new Color(100, 116, 139);
    private static final Color COR_BORDA           = new Color(203, 213, 225);
    private static final Color COR_VERDE           = new Color(22, 163, 74);
    private static final Color COR_AZUL            = new Color(37, 99, 235);

    private JTextField txtUsuario;
    private JPasswordField txtSenha;
    private JButton btnEntrar, btnCadastrar;

    public TelaLoginAdm() {
        setTitle("Sistema Hospitalar — Login do Administrador");
        setSize(450, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(COR_FUNDO);
        setContentPane(raiz);

        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(criarFormulario(), BorderLayout.CENTER);

        configurarEventos();
    }

    private JPanel criarCabecalho() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COR_PRIMARIA_ESCURA, getWidth(), 0, COR_PRIMARIA));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        painel.setBorder(new EmptyBorder(25, 20, 25, 20));
        JLabel titulo = new JLabel("Área do Administrador");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        painel.add(titulo);
        return painel;
    }

    private JPanel criarFormulario() {
        JPanel cartao = new PainelCartao(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 12, 4, 12);
        gbc.weightx = 1.0;

        JLabel lblUser = new JLabel("Usuário");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(COR_TEXTO_SUAVE);
        cartao.add(lblUser, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 12, 12, 12);
        txtUsuario = new CampoArredondado("Digite seu usuário...");
        cartao.add(txtUsuario, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(8, 12, 4, 12);
        JLabel lblPass = new JLabel("Senha");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(COR_TEXTO_SUAVE);
        cartao.add(lblPass, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 12, 20, 12);
        txtSenha = new SenhaArredondada("Digite sua senha...");
        cartao.add(txtSenha, gbc);

        btnEntrar = new BotaoArredondado("Entrar no Sistema", COR_AZUL);
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 12, 8, 12);
        cartao.add(btnEntrar, gbc);

        btnCadastrar = new BotaoArredondado("Não tem conta? Cadastre-se", COR_VERDE);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 12, 8, 12);
        cartao.add(btnCadastrar, gbc);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 25, 20, 25));
        wrapper.add(cartao, gbc);
        return wrapper;
    }

    private void configurarEventos() {
        btnEntrar.addActionListener(e -> realizarLogin());
        btnCadastrar.addActionListener(e -> abrirCadastroAdmin());
    }

    private void realizarLogin() {
        String usuario = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword()).trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT * FROM administradores WHERE usuario = ? AND senha = ?";
        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new TelaPainelAdm().setVisible(true); // Abre o painel principal do admin
            } else {
                JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao banco:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirCadastroAdmin() {
        String usuario = JOptionPane.showInputDialog(this, "Escolha um nome de usuário:");
        if (usuario == null || usuario.trim().isEmpty()) return;

        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(this, pf, "Digite a senha para o novo admin:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (ok == JOptionPane.OK_OPTION) {
            String senha = new String(pf.getPassword()).trim();
            if (senha.isEmpty()) {
                JOptionPane.showMessageDialog(this, "A senha não pode ser vazia!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "INSERT INTO administradores (usuario, senha) VALUES (?, ?)";
            try (Connection conn = ConexaoBanco.conectar();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, usuario.trim());
                stmt.setString(2, senha);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Administrador cadastrado com sucesso! Faça o login.");

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar (Usuário já pode existir):\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Componentes visuais personalizados padronizados
    private static class CampoArredondado extends JTextField {
        CampoArredondado(String dica) {
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(COR_TEXTO);
            setBorder(new EmptyBorder(9, 16, 9, 16));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(248, 250, 252));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(hasFocus() ? COR_PRIMARIA : COR_BORDA);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SenhaArredondada extends JPasswordField {
        SenhaArredondada(String dica) {
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(COR_TEXTO);
            setBorder(new EmptyBorder(9, 16, 9, 16));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(248, 250, 252));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(hasFocus() ? COR_PRIMARIA : COR_BORDA);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class BotaoArredondado extends JButton {
        BotaoArredondado(String texto, Color corBase) {
            super(texto);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            putClientProperty("cor", corBase);
        }
        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 48, 40);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color cor = (Color) getClientProperty("cor");
            g2.setColor(cor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setFont(getFont());
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    private static class PainelCartao extends JPanel {
        PainelCartao(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            g2.dispose();
        }
    }
}