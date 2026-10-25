package tela;

import dao.PacienteDAO;
import estrutura.Pacientes;
import util.Validador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class PainelPaciente extends JPanel {

    // ---------- Paleta de Cores Padrão ----------
    private static final Color COR_PRIMARIA     = new Color(13, 110, 130);
    private static final Color COR_FUNDO        = new Color(241, 245, 249);
    private static final Color COR_TEXTO        = new Color(30, 41, 59);
    private static final Color COR_TEXTO_SUAVE  = new Color(100, 116, 139);
    private static final Color COR_BORDA        = new Color(203, 213, 225);
    private static final Color COR_VERDE        = new Color(22, 163, 74);
    private static final Color COR_AZUL         = new Color(37, 99, 235);
    private static final Color COR_VERMELHO     = new Color(220, 38, 38);
    private static final Color COR_CINZA        = new Color(100, 116, 139);

    private static final int INTERVALO_ATUALIZACAO_MS = 5000;
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PacienteDAO dao = new PacienteDAO();

    private final DefaultTableModel modeloTabela;
    private final JTable tabela;

    private final CampoArredondado campoNome;
    private final CampoArredondado campoNascimento;
    private final CampoArredondado campoEmail;
    private final CampoArredondado campoCpf;

    private final BotaoArredondado btnCadastrar;
    private final BotaoArredondado btnAtualizar;
    private final BotaoArredondado btnRemover;
    private final BotaoArredondado btnLimpar;
    private final BotaoArredondado btnAtender;
    private final JLabel lblStatus;

    private Integer idSelecionado = null;
    private boolean escritaEmAndamento = false;

    public PainelPaciente() {
        setLayout(new BorderLayout(0, 12));
        setBackground(COR_FUNDO);
        setBorder(new EmptyBorder(12, 16, 16, 16));

        // =================================================================
        //  FORMULÁRIO (TOPO) - Alinhamento Proporcional
        // =================================================================
        PainelCartao cartaoForm = new PainelCartao(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        campoNome = new CampoArredondado("Ex.: Maria Silva");
        campoNascimento = new CampoArredondado("dd/mm/aaaa");
        campoEmail = new CampoArredondado("exemplo@email.com");
        campoCpf = new CampoArredondado("000.000.000-00");

        // Linha 0: Nome completo e Nascimento
        adicionarCampo(cartaoForm, gbc, "Nome completo:", campoNome, 0, 0, 3, 0.7);
        adicionarCampo(cartaoForm, gbc, "Data Nasc.:", campoNascimento, 3, 0, 1, 0.3);

        // Linha 1: E-mail e CPF
        adicionarCampo(cartaoForm, gbc, "E-mail:", campoEmail, 0, 1, 2, 0.5);
        adicionarCampo(cartaoForm, gbc, "CPF:", campoCpf, 2, 1, 2, 0.5);

        // Botões de Ação
        btnCadastrar = new BotaoArredondado("Cadastrar", COR_VERDE);
        btnAtualizar = new BotaoArredondado("Salvar Alteração", COR_AZUL);
        btnRemover   = new BotaoArredondado("Remover", COR_VERMELHO);
        btnLimpar    = new BotaoArredondado("Limpar", COR_CINZA);

        btnAtualizar.setEnabled(false);
        btnRemover.setEnabled(false);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelBotoes.setOpaque(false);
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnRemover);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnCadastrar);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(14, 8, 4, 8);
        cartaoForm.add(painelBotoes, gbc);

        // =================================================================
        //  TABELA DA FILA (CENTRO)
        // =================================================================
        PainelCartao cartaoTabela = new PainelCartao(new BorderLayout(0, 10));

        modeloTabela = new DefaultTableModel(
                new String[]{"Posição", "ID", "Nome", "Nascimento", "Email", "CPF", "Chegada"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabela = new JTable(modeloTabela);
        tabela.setRowHeight(38);
        tabela.setShowGrid(false);
        tabela.setIntercellSpacing(new Dimension(0, 0));
        tabela.setFont(fonte(Font.PLAIN, 13));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setDefaultRenderer(Object.class, new RenderizadorCelula());

        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new Dimension(0, 40));
        cabecalho.setDefaultRenderer(new RenderizadorCabecalho());

        // Oculta a coluna ID (uso interno)
        tabela.getColumnModel().getColumn(1).setMinWidth(0);
        tabela.getColumnModel().getColumn(1).setMaxWidth(0);

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        rolagem.getViewport().setBackground(Color.WHITE);
        cartaoTabela.add(rolagem, BorderLayout.CENTER);

        // Rodapé / Botão de Atendimento
        lblStatus = new JLabel("Carregando fila...", SwingConstants.LEFT);
        lblStatus.setFont(fonte(Font.BOLD, 13));
        lblStatus.setForeground(COR_PRIMARIA);

        btnAtender = new BotaoArredondado("📢  Chamar Próximo Paciente", COR_PRIMARIA);

        JPanel rodape = new JPanel(new BorderLayout(10, 0));
        rodape.setOpaque(false);
        rodape.add(lblStatus, BorderLayout.WEST);
        rodape.add(btnAtender, BorderLayout.EAST);

        cartaoTabela.add(rodape, BorderLayout.SOUTH);

        add(cartaoForm, BorderLayout.NORTH);
        add(cartaoTabela, BorderLayout.CENTER);

        // =================================================================
        //  EVENTOS
        // =================================================================
        btnCadastrar.addActionListener(e -> cadastrarPaciente());
        btnAtualizar.addActionListener(e -> atualizarPaciente());
        btnRemover.addActionListener(e -> removerPaciente());
        btnLimpar.addActionListener(e -> limparFormulario());
        btnAtender.addActionListener(e -> atenderProximo());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) carregarSelecaoNoFormulario();
        });

        atualizarTabela();
        new Timer(INTERVALO_ATUALIZACAO_MS, e -> atualizarAutomaticamente()).start();
    }

    // Método auxiliar corrigido para alinhar os campos do formulário proporcionalmente
    private void adicionarCampo(JPanel painel, GridBagConstraints gbc, String rotulo, JComponent campo, int x, int y, int largura, double peso) {
        gbc.gridx = x;
        gbc.gridy = y * 2;
        gbc.gridwidth = largura;
        gbc.weightx = peso;

        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(fonte(Font.BOLD, 12));
        lbl.setForeground(COR_TEXTO_SUAVE);
        painel.add(lbl, gbc);

        gbc.gridy = (y * 2) + 1;
        painel.add(campo, gbc);
    }

    // =================================================================
    //  LÓGICA E BANCO DE DADOS
    // =================================================================

    private <T> void executar(Callable<T> tarefa, Consumer<T> aoConcluir, Consumer<Exception> aoFalhar, boolean escrita) {
        if (escrita) {
            if (escritaEmAndamento) return;
            escritaEmAndamento = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return tarefa.call(); }
            @Override protected void done() {
                if (escrita) {
                    escritaEmAndamento = false;
                    setCursor(Cursor.getDefaultCursor());
                }
                try {
                    aoConcluir.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause();
                    aoFalhar.accept(causa instanceof Exception ? (Exception) causa : ex);
                }
            }
        }.execute();
    }

    private <T> void executar(Callable<T> tarefa, Consumer<T> aoConcluir, boolean escrita) {
        executar(tarefa, aoConcluir, this::tratarErro, escrita);
    }

    private void cadastrarPaciente() {
        DadosFormulario dados = lerFormulario();
        if (dados == null) return;

        executar(() -> {
            Pacientes p = dao.cadastrar(dados.nome, dados.nascimento, dados.email, dados.cpf);
            int posicao = dao.posicaoNaFila(p.getId());
            return p.getNome() + " cadastrado(a) com sucesso!\nPosição na fila: " + posicao + "º";
        }, mensagem -> {
            JOptionPane.showMessageDialog(this, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparFormulario();
            atualizarTabela();
        }, true);
    }

    private void atualizarPaciente() {
        if (idSelecionado == null) return;
        DadosFormulario dados = lerFormulario();
        if (dados == null) return;

        final int id = idSelecionado;
        executar(() -> {
            dao.atualizar(id, dados.nome, dados.nascimento, dados.email, dados.cpf);
            return null;
        }, resultado -> {
            limparFormulario();
            atualizarTabela();
        }, true);
    }

    private void removerPaciente() {
        if (idSelecionado == null) return;
        int confirmacao = JOptionPane.showConfirmDialog(
                this, "Deseja remover este paciente da fila?", "Confirmar remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacao != JOptionPane.YES_OPTION) return;

        final int id = idSelecionado;
        executar(() -> {
            dao.remover(id);
            return null;
        }, resultado -> {
            limparFormulario();
            atualizarTabela();
        }, true);
    }

    private void atenderProximo() {
        executar(dao::atenderProximo, atendido -> {
            if (atendido == null) {
                JOptionPane.showMessageDialog(this, "Não há pacientes na fila de espera.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "📢 Chamando Paciente: " + atendido.getNome(), "Atendimento", JOptionPane.INFORMATION_MESSAGE);
            }
            limparFormulario();
            atualizarTabela();
        }, true);
    }

    private void atualizarTabela() {
        executar(dao::listarFila, this::preencherTabela, false);
    }

    private void atualizarAutomaticamente() {
        if (idSelecionado != null || escritaEmAndamento) return;
        executar(dao::listarFila,
                fila -> { if (idSelecionado == null) preencherTabela(fila); },
                ex -> lblStatus.setText("Tentando reconectar ao banco..."),
                false);
    }

    private void preencherTabela(List<Pacientes> fila) {
        modeloTabela.setRowCount(0);
        int posicao = 1;
        for (Pacientes p : fila) {
            modeloTabela.addRow(new Object[]{
                    posicao++,
                    p.getId(),
                    p.getNome(),
                    p.getNascimento().format(FORMATO_DATA),
                    p.getEmail(),
                    Validador.formatarCpf(p.getCpf()),
                    p.getHoraChegada().format(FORMATO_HORA)
            });
        }
        lblStatus.setText("Total na fila de espera: " + fila.size() + " pacientes");
    }

    private void carregarSelecaoNoFormulario() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return;

        idSelecionado = (Integer) modeloTabela.getValueAt(linha, 1);
        campoNome.setText((String) modeloTabela.getValueAt(linha, 2));
        campoNascimento.setText((String) modeloTabela.getValueAt(linha, 3));
        campoEmail.setText((String) modeloTabela.getValueAt(linha, 4));
        campoCpf.setText((String) modeloTabela.getValueAt(linha, 5));

        btnCadastrar.setEnabled(false);
        btnAtualizar.setEnabled(true);
        btnRemover.setEnabled(true);
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoNascimento.setText("");
        campoEmail.setText("");
        campoCpf.setText("");
        idSelecionado = null;
        tabela.clearSelection();

        btnCadastrar.setEnabled(true);
        btnAtualizar.setEnabled(false);
        btnRemover.setEnabled(false);
    }

    private DadosFormulario lerFormulario() {
        String nome = campoNome.getText().trim();
        String nascimentoTexto = campoNascimento.getText().trim();
        String email = campoEmail.getText().trim();
        String cpf = Validador.somenteDigitos(campoCpf.getText());

        if (nome.isEmpty()) {
            avisar("Informe o nome completo.");
            return null;
        }

        LocalDate nascimento;
        try {
            nascimento = LocalDate.parse(nascimentoTexto, FORMATO_DATA);
        } catch (DateTimeParseException ex) {
            avisar("Data de nascimento inválida! Use o formato dd/mm/aaaa.");
            return null;
        }

        if (!Validador.emailValido(email)) {
            avisar("Informe um e-mail válido.");
            return null;
        }

        if (!Validador.cpfValido(cpf)) {
            avisar("CPF inválido! Verifique os dígitos.");
            return null;
        }

        return new DadosFormulario(nome, nascimento, email, cpf);
    }

    private void avisar(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Validação", JOptionPane.WARNING_MESSAGE);
    }

    private void tratarErro(Exception ex) {
        ex.printStackTrace();
        String mensagem = (ex instanceof SQLException && ((SQLException) ex).getErrorCode() == 1062) ?
                "Já existe um paciente cadastrado com este CPF." : "Erro:\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // =================================================================
    //  COMPONENTES VISUAIS PERSONALIZADOS (Padrão Arredondado)
    // =================================================================

    private static Font fonte(int estilo, int tamanho) {
        return new Font("Segoe UI", estilo, tamanho);
    }

    private static class CampoArredondado extends JTextField {
        private final String dica;

        CampoArredondado(String dica) {
            this.dica = dica;
            setOpaque(false);
            setFont(fonte(Font.PLAIN, 13));
            setForeground(COR_TEXTO);
            setBorder(new EmptyBorder(8, 12, 8, 12));
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
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            boolean foco = hasFocus();
            g2.setStroke(new BasicStroke(foco ? 1.8f : 1.2f));
            g2.setColor(foco ? COR_PRIMARIA : COR_BORDA);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
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
            return new Dimension(fm.stringWidth(getText()) + 32, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fundo = !isEnabled() ? new Color(226, 232, 240) : (pressionado ? corBase.darker() : (sobre ? corBase.brighter() : corBase));
            g2.setColor(fundo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

            g2.setFont(getFont());
            g2.setColor(isEnabled() ? Color.WHITE : new Color(148, 163, 184));
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
            setBorder(new EmptyBorder(14, 18, 14, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
        }
    }

    private static class RenderizadorCelula extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, false, linha, coluna);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setForeground(COR_TEXTO);
            setBackground(selecionada ? new Color(207, 236, 242) : (linha % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            setHorizontalAlignment(coluna == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            return this;
        }
    }

    private static class RenderizadorCabecalho extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, false, false, linha, coluna);
            setOpaque(true);
            setBackground(new Color(241, 245, 249));
            setForeground(COR_TEXTO_SUAVE);
            setFont(fonte(Font.BOLD, 12));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA),
                    new EmptyBorder(0, 10, 0, 10)));
            setHorizontalAlignment(coluna == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            return this;
        }
    }

    private static class DadosFormulario {
        final String nome;
        final LocalDate nascimento;
        final String email;
        final String cpf;

        DadosFormulario(String nome, LocalDate nascimento, String email, String cpf) {
            this.nome = nome;
            this.nascimento = nascimento;
            this.email = email;
            this.cpf = cpf;
        }
    }
}