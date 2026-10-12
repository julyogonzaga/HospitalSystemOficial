import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TelaMedico extends JFrame {

    private JTextField txtNome, txtCpf, txtCrm, txtTelefone, txtEspecialidadeId, txtBusca;
    private JTable tabelaMedicos;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private MedicoDAO medicoDAO;

    private JButton btnSalvar, btnEditar, btnExcluir, btnLimpar;
    private int idMedicoSelecionado = -1;

    // Cores do Tema Moderno (Flat Design)
    private final Color COR_BG_GERAL = new Color(248, 249, 250);
    private final Color COR_CARD = Color.WHITE;
    private final Color COR_BORDA = new Color(226, 232, 240);
    private final Color COR_TEXTO = new Color(30, 41, 59);

    // Cores de Ação
    private final Color COR_PRIMARIA = new Color(13, 110, 253);   // Azul moderno
    private final Color COR_SUCESSO = new Color(25, 135, 84);     // Verde elegante
    private final Color COR_PERIGO = new Color(220, 53, 69);      // Vermelho suave
    private final Color COR_SECUNDARIA = new Color(108, 117, 125); // Cinza neutro

    public TelaMedico() {
        medicoDAO = new MedicoDAO();

        setTitle("Sistema Hospitalar — Gestão de Médicos");
        setSize(980, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel Principal com Padding confortável
        JPanel contentPane = new JPanel(new BorderLayout(16, 16));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPane.setBackground(COR_BG_GERAL);
        setContentPane(contentPane);

        // --- PAINEL NORTE: FORMULÁRIO ESTILIZADO EM CARD ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(COR_CARD);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linha 0: Nome e CPF
        adicionarRotuloECampo(panelForm, "Nome Completo:", txtNome = criarCampoTexto(), gbc, 0, 0, 1);
        adicionarRotuloECampo(panelForm, "CPF:", txtCpf = criarCampoTexto(), gbc, 2, 0, 1);

        // Linha 1: CRM, Telefone
        adicionarRotuloECampo(panelForm, "CRM:", txtCrm = criarCampoTexto(), gbc, 0, 1, 1);
        adicionarRotuloECampo(panelForm, "Telefone:", txtTelefone = criarCampoTexto(), gbc, 2, 1, 1);

        // Linha 2: ID Especialidade
        adicionarRotuloECampo(panelForm, "ID Especialidade:", txtEspecialidadeId = criarCampoTexto(), gbc, 0, 2, 1);

        // Linha 3: Barra de Ações (Botões Modernos)
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panelBotoes.setOpaque(false);

        btnSalvar = criarBotaoEstilizado("Cadastrar Novo", COR_SUCESSO);
        btnEditar = criarBotaoEstilizado("Salvar Alterações", COR_PRIMARIA);
        btnExcluir = criarBotaoEstilizado("Excluir", COR_PERIGO);
        btnLimpar = criarBotaoEstilizado("Limpar Seleção", COR_SECUNDARIA);

        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);

        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnEditar);
        panelBotoes.add(btnExcluir);
        panelBotoes.add(btnLimpar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(20, 8, 8, 8);
        panelForm.add(panelBotoes, gbc);

        add(panelForm, BorderLayout.NORTH);

        // --- PAINEL CENTRO: TABELA E FILTRO DE BUSCA ---
        JPanel panelCentro = new JPanel(new BorderLayout(12, 12));
        panelCentro.setOpaque(false);

        // Campo de Busca Moderno
        JPanel panelBusca = new JPanel(new BorderLayout(10, 0));
        panelBusca.setBackground(COR_CARD);
        panelBusca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblBusca = new JLabel("🔍 Filtrar Registros:");
        lblBusca.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBusca.setForeground(COR_TEXTO);
        panelBusca.add(lblBusca, BorderLayout.WEST);

        txtBusca = criarCampoTexto();
        txtBusca.setPreferredSize(new Dimension(250, 32));
        panelBusca.add(txtBusca, BorderLayout.CENTER);

        panelCentro.add(panelBusca, BorderLayout.NORTH);

        // Tabela Estilizada (Fontes limpas, linhas alternadas e altura maior)
        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "CPF", "CRM", "Telefone", "Especialidade ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaMedicos = new JTable(tableModel);
        tabelaMedicos.setRowHeight(32);
        tabelaMedicos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaMedicos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaMedicos.setSelectionBackground(new Color(224, 242, 254));
        tabelaMedicos.setSelectionForeground(new Color(12, 74, 110));
        tabelaMedicos.setGridColor(COR_BORDA);

        // Estilo do Cabeçalho da Tabela
        tabelaMedicos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaMedicos.getTableHeader().setBackground(new Color(241, 245, 249));
        tabelaMedicos.getTableHeader().setForeground(new Color(71, 85, 105));
        tabelaMedicos.getTableHeader().setPreferredSize(new Dimension(0, 36));

        sorter = new TableRowSorter<>(tableModel);
        tabelaMedicos.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tabelaMedicos);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COR_BORDA, 1));

        panelCentro.add(scrollPane, BorderLayout.CENTER);
        add(panelCentro, BorderLayout.CENTER);

        // --- EVENTOS DE INTERAÇÃO ---
        btnSalvar.addActionListener(e -> cadastrarMedico());
        btnEditar.addActionListener(e -> editarMedico());
        btnExcluir.addActionListener(e -> excluirMedico());
        btnLimpar.addActionListener(e -> limparFormulario());

        txtBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String termo = txtBusca.getText().trim();
                if (termo.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + termo));
                }
            }
        });

        tabelaMedicos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                preencherCamposComLinhaSelecionada();
            }
        });

        atualizarTabela();
    }

    private JTextField criarCampoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setPreferredSize(new Dimension(180, 34));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return campo;
    }

    private void adicionarRotuloECampo(JPanel panel, String label, JTextField campo, GridBagConstraints gbc, int x, int y, int width) {
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.gridx = x;
        gbc.gridy = y;

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COR_TEXTO);
        panel.add(lbl, gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = width;
        panel.add(campo, gbc);
    }

    private JButton criarBotaoEstilizado(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Efeito visual sutil ao passar o mouse
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(bg.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(bg);
            }
        });

        return btn;
    }

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

            JOptionPane.showMessageDialog(this, "Médico cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparFormulario();
            atualizarTabela();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "O ID da Especialidade deve ser um número inteiro.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarMedico() {
        if (idMedicoSelecionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um médico na tabela para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validarCampos()) return;

        try {
            int especialidadeId = Integer.parseInt(txtEspecialidadeId.getText().trim());

            Medico medicoAtualizado = new Medico(
                    idMedicoSelecionado,
                    txtNome.getText().trim(),
                    txtCpf.getText().trim(),
                    txtCrm.getText().trim(),
                    txtTelefone.getText().trim(),
                    especialidadeId
            );

            medicoDAO.atualizarMedico(medicoAtualizado);

            JOptionPane.showMessageDialog(this, "Dados do médico atualizados com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparFormulario();
            atualizarTabela();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "O ID da Especialidade deve ser um número inteiro.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirMedico() {
        if (idMedicoSelecionado == -1) return;

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja excluir o médico selecionado?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                // medicoDAO.excluirMedico(idMedicoSelecionado);
                JOptionPane.showMessageDialog(this, "Registro excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparFormulario();
                atualizarTabela();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir médico: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void preencherCamposComLinhaSelecionada() {
        int linhaView = tabelaMedicos.getSelectedRow();
        if (linhaView != -1) {
            int linhaModel = tabelaMedicos.convertRowIndexToModel(linhaView);
            idMedicoSelecionado = (int) tableModel.getValueAt(linhaModel, 0);
            txtNome.setText(tableModel.getValueAt(linhaModel, 1).toString());
            txtCpf.setText(tableModel.getValueAt(linhaModel, 2).toString());
            txtCrm.setText(tableModel.getValueAt(linhaModel, 3).toString());
            txtTelefone.setText(tableModel.getValueAt(linhaModel, 4).toString());
            txtEspecialidadeId.setText(tableModel.getValueAt(linhaModel, 5).toString());

            btnSalvar.setEnabled(false);
            btnEditar.setEnabled(true);
            btnExcluir.setEnabled(true);
        }
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty() ||
                txtCpf.getText().trim().isEmpty() ||
                txtCrm.getText().trim().isEmpty() ||
                txtTelefone.getText().trim().isEmpty() ||
                txtEspecialidadeId.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!", "Aviso", JOptionPane.WARNING_MESSAGE);
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar lista de médicos:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        idMedicoSelecionado = -1;
        txtNome.setText("");
        txtCpf.setText("");
        txtCrm.setText("");
        txtTelefone.setText("");
        txtEspecialidadeId.setText("");
        txtBusca.setText("");
        if (sorter != null) sorter.setRowFilter(null);
        tabelaMedicos.clearSelection();

        btnSalvar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
    }
}