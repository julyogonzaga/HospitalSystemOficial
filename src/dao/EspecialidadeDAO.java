package dao;
import bd.ConexaoBanco;
import estrutura.Medico;
import tela.TelaMedico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadeDAO {

    // Método para CADASTRAR uma nova especialidade
    public void cadastrarEspecialidade(String nomeEspecialidade) {
        String sql = "INSERT INTO especialidades (nome_especialidade) VALUES (?)";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeEspecialidade);
            stmt.executeUpdate();
            System.out.println("Especialidade '" + nomeEspecialidade + "' cadastrada com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar especialidade: " + e.getMessage());
        }
    }

    // Método para LISTAR e retornar as especialidades para o ComboBox da Tela
    public List<TelaMedico.ItemEspecialidade> listarEspecialidadesParaCombo() {
        List<TelaMedico.ItemEspecialidade> lista = new ArrayList<>();
        String sql = "SELECT id_especialidade, nome_especialidade FROM especialidades";

        try (Connection conn = ConexaoBanco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_especialidade");
                String nome = rs.getString("nome_especialidade");
                lista.add(new TelaMedico.ItemEspecialidade(id, nome));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar especialidades: " + e.getMessage());
        }

        return lista;
    }
}