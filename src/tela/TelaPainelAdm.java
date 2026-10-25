package tela;

import dao.PacienteDAO;
import estrutura.Pacientes;
import bd.ConexaoBD;
import bd.ConexaoBanco;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class TelaPainelAdm extends JFrame {

    private static final Color COR_PRIMARIA = new Color(13, 110, 130);
    private static final Color COR_FUNDO = new Color(241, 245, 249);
    private static final Color COR_VERDE = new Color(22, 163, 74);
    private static final Color COR_TEXTO = new Color(30, 41, 59);

    private final PacienteDAO dao = new PacienteDAO();

    private JLabel lblPacienteChamado;
    private JButton btnChamarProximo;

    public TelaPainelAdm() {
        setTitle("Sistema Hospitalar — Painel do Administrador (Chamada de Pacientes)");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(COR_FUNDO);
        setContentPane(raiz);

        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(criarPainelCentral(), BorderLayout.CENTER);
    }

    private JPanel criarCabecalho() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        painel.setBackground(COR_PRIMARIA);
        JLabel titulo = new JLabel("Painel de Chamada — Recepção / Consultório");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        painel.add(titulo);
        return painel;
    }

    private JPanel criarPainelCentral() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 20, 10);

        JLabel lblSub = new JLabel("Paciente Chamado no Momento:");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSub.setForeground(COR_TEXTO);
        painel.add(lblSub, gbc);

        gbc.gridy = 1;
        lblPacienteChamado = new JLabel("Nenhum paciente chamado", SwingConstants.CENTER);
        lblPacienteChamado.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblPacienteChamado.setForeground(COR_PRIMARIA);
        painel.add(lblPacienteChamado, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        btnChamarProximo = new BotaoArredondado("Chamar Próximo Paciente", COR_VERDE);
        painel.add(btnChamarProximo, gbc);

        // Ação real conectada ao PacienteDAO para chamar o próximo da fila
        btnChamarProximo.addActionListener(e -> chamarProximoPaciente());

        return painel;
    }

    private void chamarProximoPaciente() {
        // Executa em segundo plano para não travar a interface gráfica
        new SwingWorker<Pacientes, Void>() {
            @Override
            protected Pacientes doInBackground() throws Exception {
                return dao.atenderProximo(); // Pega o primeiro da fila e marca como atendido no banco
            }

            @Override
            protected void done() {
                try {
                    Pacientes proximo = get();
                    if (proximo == null) {
                        JOptionPane.showMessageDialog(TelaPainelAdm.this,
                                "Não há pacientes aguardando na fila de espera.",
                                "Fila Vazia", JOptionPane.INFORMATION_MESSAGE);
                        lblPacienteChamado.setText("Nenhum paciente na fila");
                    } else {
                        // Exibe o nome e a especialidade/exame escolhido do banco
                        String especialidade = proximo.getTipoExame() != null ? proximo.getTipoExame() : "Clínico Geral";
                        lblPacienteChamado.setText("<html><center>" + proximo.getNome() +
                                "<br><font size='5' color='#16a34a'>Especialidade: " + especialidade + "</font></center></html>");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TelaPainelAdm.this,
                            "Erro ao chamar próximo paciente:\n" + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static class BotaoArredondado extends JButton {
        BotaoArredondado(String texto, Color corBase) {
            super(texto);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
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
            return new Dimension(fm.stringWidth(getText()) + 60, 50);
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
}