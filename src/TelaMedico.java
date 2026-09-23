import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TelaMedico extends JFrame {

    private JTextField txtNome, txtCpf, txtCrm, txtTelefone, txtEspecialidadeId;
    private JTable tabelaMedicos;
    private DefaultTableModel tableModel;
    private MedicoDAO medicoDAO;



    // Guarda o ID do médico selecionado na tabela (-1 indica que nenhum médico está selecionado para edição)
    private int idMedicoSelecionado = -1;

    public TelaMedico() {
        medicoDAO = new MedicoDAO();

        setTitle("Sistema de Gerenciamento Hospitalar");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL DE FORMULÁRIO (ENTRADA DE DADOS) ---
        JPanel panelForm = new JPanel(new GridLayout(7, 2, 8, 8));
        panelForm.setBorder(BorderFactory.createTitledBorder("Dados do Médico"));

        panelForm.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        panelForm.add(txtNome);

        panelForm.add(new JLabel("CPF:"));
        txtCpf = new JTextField();
        panelForm.add(txtCpf);

        panelForm.add(new JLabel("CRM:"));
        txtCrm = new JTextField();
        panelForm.add(txtCrm);

        panelForm.add(new JLabel("Telefone:"));
        txtTelefone = new JTextField();
        panelForm.add(txtTelefone);

        panelForm.add(new JLabel("ID Especialidade:"));
        txtEspecialidadeId = new JTextField();
        panelForm.add(txtEspecialidadeId);

        // Botões de Ação
        JButton btnSalvar = new JButton("Cadastrar Novo");
        btnSalvar.setBackground(new Color(46, 139, 87));
        btnSalvar.setForeground(Color.WHITE);
        panelForm.add(btnSalvar);

        JButton btnEditar = new JButton("Salvar Alterações (Editar)");
        btnEditar.setBackground(new Color(70, 130, 180));
        btnEditar.setForeground(Color.WHITE);
        panelForm.add(btnEditar);

        JButton btnLimpar = new JButton("Limpar Seleção");
        panelForm.add(btnLimpar);

        add(panelForm, BorderLayout.NORTH);

        // --- PAINEL DE TABELA (LISTAGEM) ---
        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "CPF", "CRM", "Telefone", "Especialidade ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Impede edição direta nas células da tabela
            }
        };

        tabelaMedicos = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tabelaMedicos);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Médicos Cadastrados (Clique em uma linha para editar)"));

        add(scrollPane, BorderLayout.CENTER);

        // --- EVENTOS DOS BOTÕES E TABELA ---
        btnSalvar.addActionListener(e -> cadastrarMedico());
        btnEditar.addActionListener(e -> editarMedico());
        btnLimpar.addActionListener(e -> limparFormulario());

        // Evento ao clicar em uma linha da tabela: carrega os dados nos campos
        tabelaMedicos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                preencherCamposComLinhaSelecionada();
            }
        });

        atualizarTabela();
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

            JOptionPane.showMessageDialog(this, "Médico cadastrado com sucesso!");
            limparFormulario();
            atualizarTabela();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "O ID da Especialidade deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarMedico() {
        if (idMedicoSelecionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um médico na tabela abaixo para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validarCampos()) return;

        try {
            int especialidadeId = Integer.parseInt(txtEspecialidadeId.getText().trim());

            // Cria o objeto Medico mantendo o ID original selecionado
            Medico medicoAtualizado = new Medico(
                    idMedicoSelecionado,
                    txtNome.getText().trim(),
                    txtCpf.getText().trim(),
                    txtCrm.getText().trim(),
                    txtTelefone.getText().trim(),
                    especialidadeId
            );

            // Chama o método de UPDATE do seu DAO
            medicoDAO.atualizarMedico(medicoAtualizado);

            JOptionPane.showMessageDialog(this, "Dados do médico atualizados com sucesso!");
            limparFormulario();
            atualizarTabela();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "O ID da Especialidade deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherCamposComLinhaSelecionada() {
        int linhaSelecionada = tabelaMedicos.getSelectedRow();
        if (linhaSelecionada != -1) {
            idMedicoSelecionado = (int) tableModel.getValueAt(linhaSelecionada, 0);
            txtNome.setText(tableModel.getValueAt(linhaSelecionada, 1).toString());
            txtCpf.setText(tableModel.getValueAt(linhaSelecionada, 2).toString());
            txtCrm.setText(tableModel.getValueAt(linhaSelecionada, 3).toString());
            txtTelefone.setText(tableModel.getValueAt(linhaSelecionada, 4).toString());
            txtEspecialidadeId.setText(tableModel.getValueAt(linhaSelecionada, 5).toString());
        }
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty() ||
                txtCpf.getText().trim().isEmpty() ||
                txtCrm.getText().trim().isEmpty() ||
                txtTelefone.getText().trim().isEmpty() ||
                txtEspecialidadeId.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Aviso", JOptionPane.WARNING_MESSAGE);
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
        tabelaMedicos.clearSelection();
    }
}