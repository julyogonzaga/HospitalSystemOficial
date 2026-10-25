package tela;

import dao.MedicoDAO;
import estrutura.Medico;
import estrutura.Pacientes;
import dao.PacienteDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tela de escolha de perfil do Sistema Hospitalar.
 * Mantém o mesmo padrão visual refinado e componentes personalizados (Swing puro).
 */
public class TelaEscolhaPerfil extends JFrame {

    // ---------- Paleta de cores ----------
    private static final Color COR_PRIMARIA        = new Color(13, 110, 130);
    private static final Color COR_PRIMARIA_ESCURA = new Color(9, 78, 99);
    private static final Color COR_FUNDO           = new Color(241, 245, 249);
    private static final Color COR_TEXTO           = new Color(30, 41, 59);

    // Cores específicas para cada perfil
    private static final Color COR_MEDICO          = new Color(13, 110, 130);   // Azul clínico
    private static final Color COR_PACIENTE        = new Color(22, 163, 74);   // Verde acolhedor
    private static final Color COR_ADM             = new Color(220, 38, 38);   // Vermelho de gestão

    public TelaEscolhaPerfil() {
        setTitle("Sistema Hospitalar — Seleção de Perfil");
        setSize(750, 520);
        setMinimumSize(new Dimension(680, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(COR_FUNDO);
        setContentPane(raiz);

        raiz.add(criarCabecalho(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new GridBagLayout());
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(24, 24, 24, 24));

        PainelCartao cartaoCentral = criarCartaoSelecao();
        corpo.add(cartaoCentral);

        raiz.add(corpo, BorderLayout.CENTER);
    }

    // =====================================================================
    //  CONSTRUÇÃO DA INTERFACE
    // =====================================================================

    private JPanel criarCabecalho() {
        JPanel painel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COR_PRIMARIA_ESCURA, getWidth(), 0, COR_PRIMARIA));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(18, 28, 18, 28));

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        esquerda.setOpaque(false);
        esquerda.add(new IconeCruz(44));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        JLabel titulo = new JLabel("Sistema Hospitalar");
        titulo.setFont(fonte(Font.BOLD, 22));
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("Selecione o seu perfil de acesso para continuar");
        subtitulo.setFont(fonte(Font.PLAIN, 13));
        subtitulo.setForeground(new Color(186, 230, 253));
        textos.add(titulo);
        textos.add(subtitulo);
        esquerda.add(textos);
        painel.add(esquerda, BorderLayout.WEST);

        return painel;
    }

    private PainelCartao criarCartaoSelecao() {
        PainelCartao cartao = new PainelCartao(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel instrucao = new JLabel("Como você deseja entrar?", SwingConstants.CENTER);
        instrucao.setFont(fonte(Font.BOLD, 18));
        instrucao.setForeground(COR_TEXTO);
        gbc.insets = new Insets(0, 0, 20, 0);
        cartao.add(instrucao, gbc);

        // Botões de Perfis
        BotaoArredondado btnMedico = new BotaoArredondado("👨‍⚕️  Entrar como Médico", COR_MEDICO);
        BotaoArredondado btnPaciente = new BotaoArredondado("🏥  Entrar como Paciente", COR_PACIENTE);
        BotaoArredondado btnAdm = new BotaoArredondado("⚙️  Entrar como Administrador", COR_ADM);

        // Ações de clique (Transição de telas)
        btnMedico.addActionListener(e -> {
            new TelaMedico().setVisible(true);
            dispose();
        });

        // Abertura integrada da tela/painel de pacientes
        btnPaciente.addActionListener(e -> {
            JFrame janelaPaciente = new JFrame("Portal do Paciente — Fila de Atendimento");
            janelaPaciente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            janelaPaciente.setSize(950, 600);
            janelaPaciente.setLocationRelativeTo(this);
            janelaPaciente.add(new PainelPaciente());
            janelaPaciente.setVisible(true);
            dispose();
        });

        // Integração correta abrindo a Tela de Login do Administrador
        btnAdm.addActionListener(e -> {
            new TelaLoginAdm().setVisible(true);
            dispose();
        });

        gbc.insets = new Insets(8, 0, 8, 0);

        gbc.gridy = 1;
        cartao.add(btnMedico, gbc);

        gbc.gridy = 2;
        cartao.add(btnPaciente, gbc);

        gbc.gridy = 3;
        cartao.add(btnAdm, gbc);

        return cartao;
    }

    // =====================================================================
    //  APOIO E COMPONENTES PERSONALIZADOS
    // =====================================================================

    private static Font fonte(int estilo, int tamanho) {
        return new Font("Segoe UI", estilo, tamanho);
    }

    private static Color misturar(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    /** Botão arredondado com efeito interativo. */
    private static class BotaoArredondado extends JButton {
        private final Color corBase;
        private boolean sobre, pressionado;

        BotaoArredondado(String texto, Color corBase) {
            super(texto);
            this.corBase = corBase;
            setFont(fonte(Font.BOLD, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { sobre = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)   { sobre = false; pressionado = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  { pressionado = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressionado = false; repaint(); }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(340, 48);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color fundo;
            if (!isEnabled()) {
                fundo = new Color(226, 232, 240);
            } else if (pressionado) {
                fundo = corBase.darker();
            } else if (sobre) {
                fundo = misturar(corBase, Color.WHITE, 0.15f);
            } else {
                fundo = corBase;
            }

            g2.setColor(fundo);
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

    /** Painel central com efeito de cartão flutuante e sombra corrigido. */
    private static class PainelCartao extends JPanel {
        private static final int SOMBRA = 6;
        private static final int RAIO = 22;

        PainelCartao(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(SOMBRA + 16, SOMBRA + 24, SOMBRA + 18, SOMBRA + 24));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = SOMBRA; i >= 1; i--) {
                int recuo = SOMBRA - i;
                g2.setColor(new Color(15, 23, 42, (recuo + 1) * 2));
                g2.fillRoundRect(
                        recuo,
                        recuo + 2,
                        getWidth() - (2 * recuo),
                        getHeight() - (2 * recuo) - 2,
                        RAIO + (SOMBRA - recuo) * 2,
                        RAIO + (SOMBRA - recuo) * 2
                );
            }

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(
                    SOMBRA,
                    SOMBRA,
                    getWidth() - (2 * SOMBRA),
                    getHeight() - (2 * SOMBRA) - 2,
                    RAIO,
                    RAIO
            );
            g2.dispose();
        }
    }

    private static class IconeCruz extends JComponent {
        private final int tam;

        IconeCruz(int tam) {
            this.tam = tam;
            setPreferredSize(new Dimension(tam, tam));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 45));
            g2.fillOval(0, 0, tam, tam);
            g2.setColor(Color.WHITE);
            int espessura = tam / 6;
            int comprimento = tam / 2;
            int c = tam / 2;
            g2.fillRoundRect(c - comprimento / 2, c - espessura / 2, comprimento, espessura, espessura, espessura);
            g2.fillRoundRect(c - espessura / 2, c - comprimento / 2, espessura, comprimento, espessura, espessura);
            g2.dispose();
        }
    }

    // Método main: inicializa o sistema pela tela de escolha de perfil
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TelaEscolhaPerfil().setVisible(true);
        });
    }
}