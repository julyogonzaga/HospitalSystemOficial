import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EspecialidadeDAO {

    // Método para CADASTRAR uma nova especialidade
    public void cadastrarEspecialidade(String nomeEspecialidade) {
        String sql = "INSERT INTO especialidades (nome_especialidade) VALUES (?)";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeEspecialidade);
            stmt.executeUpdate();
            System.out.println("specialidade '" + nomeEspecialidade + "' cadastrada com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar especialidade: " + e.getMessage());
        }
    }

    // Método para LISTAR todas as especialidades salvas no banco
    public void listarEspecialidades() {
        String sql = "SELECT id_especialidade, nome_especialidade FROM especialidades";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- ESPECIALIDADES DISPONÍVEIS ---");
            while (rs.next()) {
                int id = rs.getInt("id_especialidade");
                String nome = rs.getString("nome_especialidade");
                System.out.println("ID: " + id + " | Nome: " + nome);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar especialidades: " + e.getMessage());
        }
    }
}