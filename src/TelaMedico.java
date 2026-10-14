import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.Pattern;

/*
 * Tela de gestão de médicos.
 * Visual feito 100% com Swing puro (sem bibliotecas extras): botões e campos
 * arredondados, cartões com sombra, tabela com linhas zebradas e cabeçalho.
 */
public class TelaMedico extends JFrame {

    // ---------- Paleta de cores ----------
    private static final Color COR_PRIMARIA        = new Color(13, 110, 130);
    private static final Color COR_PRIMARIA_ESCURA = new Color(9, 78, 99);
    private static final Color COR_FUNDO           = new Color(241, 245, 249);
    private static final Color COR_TEXTO           = new Color(30, 41, 59);
    private static final Color COR_TEXTO_SUAVE     = new Color(100, 116, 139);
    private static final Color COR_BORDA           = new Color(203, 213, 225);
    private static final Color COR_VERDE           = new Color(22, 163, 74);
    private static final Color COR_AZUL            = new Color(37, 99, 235);
    private static final Color COR_VERMELHO        = new Color(220, 38, 38);
    private static final Color COR_CINZA           = new Color(100, 116, 139);

    // ---------- Componentes ----------
    private JTextField txtNome, txtCpf, txtCrm, txtTelefone, txtEspecialidadeId, txtBusca;
    private JTable tabelaMedicos;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnSalvar, btnEditar, btnExcluir, btnLimpar;
    private JLabel lblModo, lblStatus, lblTotal;
    private Timer timerStatus;

    private final MedicoDAO medicoDAO;
    private int idMedicoSelecionado = -1;

    public TelaMedico() {
        medicoDAO = new MedicoDAO();

        setTitle("Sistema Hospitalar — Gestão de Médicos");
        setSize(1050, 740);
        setMinimumSize(new Dimension(900, 660));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(COR_FUNDO);
        setContentPane(raiz);

        raiz.add(criarCabecalho(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(0, 10));
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(10, 16, 16, 16));
        corpo.add(criarCartaoFormulario(), BorderLayout.NORTH);
        corpo.add(criarCartaoTabela(), BorderLayout.CENTER);
        raiz.add(corpo, BorderLayout.CENTER);

        configurarEventos();
        atualizarTabela();
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
        esquerda.add(new IconeCruz(48));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        JLabel titulo = new JLabel("Sistema Hospitalar");
        titulo.setFont(fonte(Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("Gestão de Médicos");
        subtitulo.setFont(fonte(Font.PLAIN, 14));
        subtitulo.setForeground(new Color(186, 230, 253));
        textos.add(titulo);
        textos.add(subtitulo);
        esquerda.add(textos);
        painel.add(esquerda, BorderLayout.WEST);

        lblTotal = new JLabel("0 médicos cadastrados", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 38));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblTotal.setOpaque(false);
        lblTotal.setFont(fonte(Font.BOLD, 13));
        lblTotal.setForeground(Color.WHITE);
        lblTotal.setBorder(new EmptyBorder(9, 20, 9, 20));

        JPanel direita = new JPanel(new GridBagLayout());
        direita.setOpaque(false);
        direita.add(lblTotal);
        painel.add(direita, BorderLayout.EAST);

        return painel;
    }

    private JPanel criarCartaoFormulario() {
        PainelCartao cartao = new PainelCartao(new GridBagLayout());

        JLabel titulo = new JLabel("Dados do Médico");
        titulo.setFont(fonte(Font.BOLD, 17));
        titulo.setForeground(COR_TEXTO);

        lblModo = new JLabel("Novo cadastro");
        lblModo.setFont(fonte(Font.BOLD, 12));
        lblModo.setForeground(COR_VERDE);

        JPanel linhaTitulo = new JPanel(new BorderLayout());
        linhaTitulo.setOpaque(false);
        linhaTitulo.add(titulo, BorderLayout.WEST);
        linhaTitulo.add(lblModo, BorderLayout.EAST);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 8, 4, 8);
        cartao.add(linhaTitulo, gbc);

        txtNome            = new CampoArredondado("Ex.: Dra. Maria Souza");
        txtCpf             = new CampoArredondado("000.000.000-00");
        txtCrm             = new CampoArredondado("Ex.: 123456-PE");
        txtTelefone        = new CampoArredondado("(81) 90000-0000");
        txtEspecialidadeId = new CampoArredondado("Ex.: 1");
        txtBusca           = new CampoArredondado("Buscar por nome, CPF, CRM ou telefone...");

        adicionarCampo(cartao, "Nome completo",       txtNome,            0, 1, 2);
        adicionarCampo(cartao, "CPF",                 txtCpf,             2, 1, 1);
        adicionarCampo(cartao, "CRM",                 txtCrm,             0, 3, 1);
        adicionarCampo(cartao, "Telefone",            txtTelefone,        1, 3, 1);
        adicionarCampo(cartao, "ID da especialidade", txtEspecialidadeId, 2, 3, 1);

        btnSalvar  = new BotaoArredondado("Cadastrar",         COR_VERDE);
        btnEditar  = new BotaoArredondado("Salvar alterações", COR_AZUL);
        btnExcluir = new BotaoArredondado("Excluir",           COR_VERMELHO);
        btnLimpar  = new BotaoArredondado("Limpar",            COR_CINZA);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        botoes.setOpaque(false);
        botoes.add(btnLimpar);
        botoes.add(btnExcluir);
        botoes.add(btnEditar);
        botoes.add(btnSalvar);

        gbc.gridy = 5;
        gbc.insets = new Insets(14, 8, 0, 8);
        cartao.add(botoes, gbc);

        return cartao;
    }

    private JPanel criarCartaoTabela() {
        PainelCartao cartao = new PainelCartao(new BorderLayout(0, 12));

        JLabel titulo = new JLabel("Médicos cadastrados");
        titulo.setFont(fonte(Font.BOLD, 17));
        titulo.setForeground(COR_TEXTO);

        txtBusca.setPreferredSize(new Dimension(360, 40));
        JPanel topo = new JPanel(new BorderLayout(14, 0));
        topo.setOpaque(false);
        topo.add(titulo, BorderLayout.WEST);
        topo.add(txtBusca, BorderLayout.EAST);
        cartao.add(topo, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Nome", "CPF", "CRM", "Telefone", "Especialidade"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaMedicos = new JTable(tableModel);
        tabelaMedicos.setRowHeight(38);
        tabelaMedicos.setShowGrid(false);
        tabelaMedicos.setIntercellSpacing(new Dimension(0, 0));
        tabelaMedicos.setFillsViewportHeight(true);
        tabelaMedicos.setFont(fonte(Font.PLAIN, 13));
        tabelaMedicos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaMedicos.setDefaultRenderer(Object.class, new RenderizadorCelula());

        JTableHeader cabecalho = tabelaMedicos.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new Dimension(0, 40));
        cabecalho.setDefaultRenderer(new RenderizadorCabecalho());

        tabelaMedicos.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabelaMedicos.getColumnModel().getColumn(1).setPreferredWidth(280);
        tabelaMedicos.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabelaMedicos.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelaMedicos.getColumnModel().getColumn(4).setPreferredWidth(140);
        tabelaMedicos.getColumnModel().getColumn(5).setPreferredWidth(110);

        sorter = new TableRowSorter<>(tableModel);
        tabelaMedicos.setRowSorter(sorter);

        JScrollPane rolagem = new JScrollPane(tabelaMedicos);
        rolagem.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        rolagem.getViewport().setBackground(Color.WHITE);
        estilizarBarra(rolagem.getVerticalScrollBar());
        estilizarBarra(rolagem.getHorizontalScrollBar());
        cartao.add(rolagem, BorderLayout.CENTER);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(fonte(Font.BOLD, 13));
        cartao.add(lblStatus, BorderLayout.SOUTH);

        return cartao;
    }

    private void adicionarCampo(JPanel painel, String rotulo, JTextField campo,
                                int x, int y, int largura) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = largura;
        gbc.gridx = x;
        gbc.gridwidth = largura;

        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(fonte(Font.BOLD, 12));
        lbl.setForeground(COR_TEXTO_SUAVE);
        gbc.gridy = y;
        gbc.insets = new Insets(6, 8, 2, 8);
        painel.add(lbl, gbc);

        gbc.gridy = y + 1;
        gbc.insets = new Insets(0, 8, 2, 8);
        painel.add(campo, gbc);
    }

    // =====================================================================
    //  EVENTOS
    // =====================================================================

    private void configurarEventos() {
        btnSalvar.addActionListener(e -> cadastrarMedico());
        btnEditar.addActionListener(e -> editarMedico());
        btnExcluir.addActionListener(e -> excluirMedico());
        btnLimpar.addActionListener(e -> limparFormulario());

        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        tabelaMedicos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherCamposComLinhaSelecionada();
            }
        });
    }

    private void filtrar() {
        String termo = txtBusca.getText().trim();
        if (termo.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(termo)));
        }
    }

    // =====================================================================
    //  AÇÕES (CRUD)
    // =====================================================================

    private void cadastrarMedico() {
        if (!validarCampos()) return;

        try {
            int especialidadeId = Integer.parseInt(txtEspecialidadeId.getText().trim());

            Medico medico = new Medico(
                    txtNome.getText().trim(),
                    txtCpf.getText().trim(),
                    txtCrm.getText().trim(),
                    txtTelefone.getText().trim(),
                    especialidadeId
            );

            medicoDAO.cadastrarMedico(medico);

            limparFormulario();
            atualizarTabela();
            mostrarStatus("Médico cadastrado com sucesso.", COR_VERDE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "O ID da especialidade deve ser um número inteiro.",
                    "Erro de validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarMedico() {
        if (idMedicoSelecionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um médico na tabela para editar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validarCampos()) return;

        try {
            int especialidadeId = Integer.parseInt(txtEspecialidadeId.getText().trim());

            Medico atualizado = new Medico(
                    idMedicoSelecionado,
                    txtNome.getText().trim(),
                    txtCpf.getText().trim(),
                    txtCrm.getText().trim(),
                    txtTelefone.getText().trim(),
                    especialidadeId
            );

            medicoDAO.atualizarMedico(atualizado);

            limparFormulario();
            atualizarTabela();
            mostrarStatus("Dados do médico atualizados.", COR_AZUL);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "O ID da especialidade deve ser um número inteiro.",
                    "Erro de validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirMedico() {
        if (idMedicoSelecionado == -1) return;

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja excluir o médico selecionado?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacao == JOptionPane.YES_OPTION) {
            medicoDAO.deletarMedico(idMedicoSelecionado);

            limparFormulario();
            atualizarTabela();
            mostrarStatus("Médico excluído.", COR_VERMELHO);
        }
    }

    // =====================================================================
    //  APOIO
    // =====================================================================

    private void preencherCamposComLinhaSelecionada() {
        int linhaView = tabelaMedicos.getSelectedRow();
        if (linhaView == -1) return;

        int linha = tabelaMedicos.convertRowIndexToModel(linhaView);
        idMedicoSelecionado = (int) tableModel.getValueAt(linha, 0);
        txtNome.setText(String.valueOf(tableModel.getValueAt(linha, 1)));
        txtCpf.setText(String.valueOf(tableModel.getValueAt(linha, 2)));
        txtCrm.setText(String.valueOf(tableModel.getValueAt(linha, 3)));
        txtTelefone.setText(String.valueOf(tableModel.getValueAt(linha, 4)));
        txtEspecialidadeId.setText(String.valueOf(tableModel.getValueAt(linha, 5)));

        btnSalvar.setEnabled(false);
        btnEditar.setEnabled(true);
        btnExcluir.setEnabled(true);

        lblModo.setText("Editando médico #" + idMedicoSelecionado);
        lblModo.setForeground(COR_AZUL);
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty()
                || txtCpf.getText().trim().isEmpty()
                || txtCrm.getText().trim().isEmpty()
                || txtTelefone.getText().trim().isEmpty()
                || txtEspecialidadeId.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Preencha todos os campos obrigatórios!",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void atualizarTabela() {
        tableModel.setRowCount(0);
        try {
            List<Medico> medicos = medicoDAO.listarMedicos();
            for (Medico m : medicos) {
                tableModel.addRow(new Object[]{
                        m.getIdMedico(),
                        m.getNome(),
                        m.getCpf(),
                        m.getCrm(),
                        m.getTelefone(),
                        m.getIdEspecialidade()
                });
            }
            int total = medicos.size();
            lblTotal.setText(total + (total == 1 ? " médico cadastrado" : " médicos cadastrados"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar lista de médicos:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        idMedicoSelecionado = -1;
        txtNome.setText("");
        txtCpf.setText("");
        txtCrm.setText("");
        txtTelefone.setText("");
        txtEspecialidadeId.setText("");
        tabelaMedicos.clearSelection();

        btnSalvar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);

        lblModo.setText("Novo cadastro");
        lblModo.setForeground(COR_VERDE);
    }

    private void mostrarStatus(String mensagem, Color cor) {
        lblStatus.setForeground(cor);
        lblStatus.setText("●  " + mensagem);
        if (timerStatus != null) timerStatus.stop();
        timerStatus = new Timer(4500, e -> lblStatus.setText(" "));
        timerStatus.setRepeats(false);
        timerStatus.start();
    }

    private static Font fonte(int estilo, int tamanho) {
        return new Font("Segoe UI", estilo, tamanho);
    }

    private static Color misturar(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    private static JButton botaoInvisivel() {
        JButton b = new JButton();
        Dimension zero = new Dimension(0, 0);
        b.setPreferredSize(zero);
        b.setMinimumSize(zero);
        b.setMaximumSize(zero);
        return b;
    }

    private static void estilizarBarra(JScrollBar barra) {
        barra.setPreferredSize(barra.getOrientation() == Adjustable.VERTICAL
                ? new Dimension(12, 0) : new Dimension(0, 12));
        barra.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(148, 163, 184);
                trackColor = Color.WHITE;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return botaoInvisivel(); }
            @Override protected JButton createIncreaseButton(int orientation) { return botaoInvisivel(); }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(Color.WHITE);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
                g2.dispose();
            }
        });
    }

    // ====================================================================
    //  COMPONENTES VISUAIS PERSONALIZADOS
    // ====================================================================

    private static class BotaoArredondado extends JButton {
        private final Color corBase;
        private boolean sobre, pressionado;

        BotaoArredondado(String texto, Color corBase) {
            super(texto);
            this.corBase = corBase;
            setFont(fonte(Font.BOLD, 13));
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
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 48, 42);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color fundo, texto = Color.WHITE;
            if (!isEnabled()) {
                fundo = new Color(226, 232, 240);
                texto = new Color(148, 163, 184);
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
            g2.setColor(texto);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    private static class CampoArredondado extends JTextField {
        private final String dica;

        CampoArredondado(String dica) {
            this.dica = dica;
            setOpaque(false);
            setFont(fonte(Font.PLAIN, 14));
            setForeground(COR_TEXTO);
            setCaretColor(COR_PRIMARIA);
            setSelectionColor(new Color(186, 230, 253));
            setBorder(new EmptyBorder(9, 16, 9, 16));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e)   { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(248, 250, 252));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            boolean foco = hasFocus();
            g2.setStroke(new BasicStroke(foco ? 2f : 1.2f));
            g2.setColor(foco ? COR_PRIMARIA : COR_BORDA);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            g2.dispose();

            super.paintComponent(g);

            if (getText().isEmpty()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g3.setColor(new Color(148, 163, 184));
                g3.setFont(getFont());
                FontMetrics fm = g3.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g3.drawString(dica, getInsets().left, y);
                g3.dispose();
            }
        }
    }

    private static class PainelCartao extends JPanel {
        private static final int SOMBRA = 6;
        private static final int RAIO = 22;

        PainelCartao(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(SOMBRA + 14, SOMBRA + 18, SOMBRA + 16, SOMBRA + 18));
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
                        getWidth() - 2 * recuo,
                        getHeight() - 2 * recuo - 2,
                        RAIO + (SOMBRA - recuo) * 2,
                        RAIO + (SOMBRA - recuo) * 2
                );
            }

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(
                    SOMBRA,
                    SOMBRA,
                    getWidth() - 2 * SOMBRA,
                    getHeight() - 2 * SOMBRA - 2,
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

    private static class RenderizadorCelula extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor,
                                                       boolean selecionada, boolean foco,
                                                       int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, false, linha, coluna);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setForeground(COR_TEXTO);
            if (selecionada) {
                setBackground(new Color(207, 236, 242));
            } else {
                setBackground(linha % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            setHorizontalAlignment(coluna == 0 || coluna == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
            return this;
        }
    }

    private static class RenderizadorCabecalho extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor,
                                                       boolean selecionada, boolean foco,
                                                       int linha, int coluna) {
            String texto = String.valueOf(valor);
            RowSorter<?> rs = tabela.getRowSorter();
            if (rs != null && !rs.getSortKeys().isEmpty()
                    && rs.getSortKeys().get(0).getColumn() == tabela.convertColumnIndexToModel(coluna)) {
                texto += rs.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING ? "  ▲" : "  ▼";
            }
            super.getTableCellRendererComponent(tabela, texto, false, false, linha, coluna);
            setOpaque(true);
            setBackground(new Color(241, 245, 249));
            setForeground(COR_TEXTO_SUAVE);
            setFont(fonte(Font.BOLD, 12));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA),
                    new EmptyBorder(0, 14, 0, 14)));
            setHorizontalAlignment(coluna == 0 || coluna == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
            return this;
        }
    }

    // Metodo main para inicialização direta da tela
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TelaMedico().setVisible(true);
        });
    }
}