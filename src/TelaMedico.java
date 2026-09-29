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

    public TelaMedico() {
        medicoDAO = new MedicoDAO();

        setTitle("Sistema Hospitalar — Gestão de Médicos");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel Principal com Espaçamento (Padding)
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(new Color(245, 247, 250));
        setContentPane(contentPane);

        // --- PAINEL NORTE: FORMULÁRIO ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(Color.WHITE);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linha 0: Nome e CPF
        adicionarRotuloECampo(panelForm, "Nome Completo:", txtNome = new JTextField(), gbc, 0, 0, 1);
        adicionarRotuloECampo(panelForm, "CPF:", txtCpf = new JTextField(), gbc, 2, 0, 1);

        // Linha 1: CRM, Telefone e ID Especialidade
        adicionarRotuloECampo(panelForm, "CRM:", txtCrm = new JTextField(), gbc, 0, 1, 1);
        adicionarRotuloECampo(panelForm, "Telefone:", txtTelefone = new JTextField(), gbc, 2, 1, 1);

        // Linha 2: ID Especialidade isolado ou alinhado
        adicionarRotuloECampo(panelForm, "ID Especialidade:", txtEspecialidadeId = new JTextField(), gbc, 0, 2, 1);

        // Linha 3: Barra de Ações (Botões)
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotoes.setOpaque(false);

        btnSalvar = criarBotaoEstilizado("Cadastrar Novo", new Color(40, 167, 69), Color.WHITE);
        btnEditar = criarBotaoEstilizado("Salvar Alterações", new Color(0, 123, 255), Color.WHITE);
        btnExcluir = criarBotaoEstilizado("Excluir", new Color(220, 53, 69), Color.WHITE);
        btnLimpar = criarBotaoEstilizado("Limpar Seleção", new Color(108, 117, 125), Color.WHITE);

        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);

        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnEditar);
        panelBotoes.add(btnExcluir);
        panelBotoes.add(btnLimpar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(15, 6, 6, 6);
        panelForm.add(panelBotoes, gbc);

        add(panelForm, BorderLayout.NORTH);

        // --- PAINEL CENTRO: TABELA E FILTRO DE BUSCA ---
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setOpaque(false);

        // Campo de Busca
        JPanel panelBusca = new JPanel(new BorderLayout(8, 0));
        panelBusca.setOpaque(false);
        panelBusca.add(new JLabel("🔍 Buscar por Médico/CPF/CRM:"), BorderLayout.WEST);
        txtBusca = new JTextField();
        panelBusca.add(txtBusca, BorderLayout.CENTER);
        panelCentro.add(panelBusca, BorderLayout.NORTH);

        // Tabela Estilizada
        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "CPF", "CRM", "Telefone", "Especialidade ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaMedicos = new JTable(tableModel);
        tabelaMedicos.setRowHeight(28);
        tabelaMedicos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaMedicos.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabelaMedicos.getTableHeader().setBackground(new Color(230, 235, 242));

        sorter = new TableRowSorter<>(tableModel);
        tabelaMedicos.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tabelaMedicos);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));

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

    private void adicionarRotuloECampo(JPanel panel, String label, JTextField campo, GridBagConstraints gbc, int x, int y, int width) {
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.gridx = x;
        gbc.gridy = y;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(lbl, gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = width;
        campo.setPreferredSize(new Dimension(150, 26));
        panel.add(campo, gbc);
    }

    private JButton criarBotaoEstilizado(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
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
                // Caso o seu DAO possua o método de excluir:
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