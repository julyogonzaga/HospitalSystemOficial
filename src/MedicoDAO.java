import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicoDAO {

    // 1. CADASTRAR MÉDICO (CREATE)
    public void cadastrarMedico(Medico medico) {
        String sql = "INSERT INTO medicos (nome, cpf, crm, telefone, id_especialidade) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, medico.getNome());
            stmt.setString(2, medico.getCpf());
            stmt.setString(3, medico.getCrm());
            stmt.setString(4, medico.getTelefone());
            stmt.setInt(5, medico.getIdEspecialidade());

            stmt.executeUpdate();
            System.out.println("Médico " + medico.getNome() + " cadastrado com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar médico: " + e.getMessage());
        }
    }

    // 2. LISTAR MÉDICOS (READ) - Atualizado para retornar List<Medico>
    public List<Medico> listarMedicos() {
        List<Medico> medicos = new ArrayList<>();
        String sql = "SELECT id_medico, nome, cpf, crm, telefone, id_especialidade FROM medicos";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Medico m = new Medico(
                        rs.getInt("id_medico"),
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getString("crm"),
                        rs.getString("telefone"),
                        rs.getInt("id_especialidade")
                );
                medicos.add(m);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar médicos: " + e.getMessage());
        }
        return medicos;
    }

    // 3. ATUALIZAR MÉDICO (UPDATE)
    public void atualizarMedico(Medico medico) {
        String sql = "UPDATE medicos SET nome = ?, cpf = ?, crm = ?, telefone = ?, id_especialidade = ? WHERE id_medico = ?";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, medico.getNome());
            stmt.setString(2, medico.getCpf());
            stmt.setString(3, medico.getCrm());
            stmt.setString(4, medico.getTelefone());
            stmt.setInt(5, medico.getIdEspecialidade());
            stmt.setInt(6, medico.getIdMedico());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Dados do médico (ID: " + medico.getIdMedico() + ") atualizados com sucesso!");
            } else {
                System.out.println("Nenhum médico encontrado com o ID fornecido.");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar médico: " + e.getMessage());
        }
    }

    // 4. EXCLUIR MÉDICO (DELETE)
    public void deletarMedico(int idMedico) {
        String sql = "DELETE FROM medicos WHERE id_medico = ?";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMedico);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Médico ID " + idMedico + " excluído com sucesso!");
            } else {
                System.out.println("Nenhum médico encontrado com o ID: " + idMedico);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao excluir médico: " + e.getMessage());
        }
    }
}