package dao;

import bd.ConexaoBanco;
import estrutura.Pacientes;
import tela.PainelPaciente;
import util.Validador;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    public Pacientes cadastrar(String nome, java.time.LocalDate nascimento, String email, String cpf, String tipoExame, LocalDateTime horaChegada) throws SQLException {
        String sql = "INSERT INTO pacientes (nome, nascimento, email, cpf, tipo_exame, hora_chegada, atendido) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nome);
            ps.setDate(2, Date.valueOf(nascimento));
            ps.setString(3, email);
            ps.setString(4, cpf);
            ps.setString(5, tipoExame);
            ps.setTimestamp(6, Timestamp.valueOf(horaChegada));
            ps.setBoolean(7, false);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Pacientes(id, nome, nascimento, email, cpf, tipoExame, horaChegada, false);
                }
            }
        }
        throw new SQLException("Erro ao cadastrar paciente, ID não gerado.");
    }

    public List<Pacientes> listarFila() throws SQLException {
        String sql = "SELECT * FROM pacientes WHERE atendido = 0 ORDER BY hora_chegada ASC";
        List<Pacientes> lista = new ArrayList<>();

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pacientes p = new Pacientes(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDate("nascimento").toLocalDate(),
                        rs.getString("email"),
                        rs.getString("cpf"),
                        rs.getString("tipo_exame"),
                        rs.getTimestamp("hora_chegada").toLocalDateTime(),
                        rs.getBoolean("atendido")
                );
                lista.add(p);
            }
        }
        return lista;
    }

    public void atualizar(int id, String nome, java.time.LocalDate nascimento, String email, String cpf, String tipoExame) throws SQLException {
        String sql = "UPDATE pacientes SET nome = ?, nascimento = ?, email = ?, cpf = ?, tipo_exame = ? WHERE id = ?";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setDate(2, Date.valueOf(nascimento));
            ps.setString(3, email);
            ps.setString(4, cpf);
            ps.setString(5, tipoExame);
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    public void remover(int id) throws SQLException {
        String sql = "DELETE FROM pacientes WHERE id = ?";
        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Pacientes atenderProximo() throws SQLException {
        List<Pacientes> fila = listarFila();
        if (fila.isEmpty()) {
            return null;
        }
        Pacientes proximo = fila.get(0);
        String sql = "UPDATE pacientes SET atendido = 1 WHERE id = ?";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, proximo.getId());
            ps.executeUpdate();
        }
        return proximo;
    }

    public int posicaoNaFila(int idPaciente) throws SQLException {
        List<Pacientes> fila = listarFila();
        for (int i = 0; i < fila.size(); i++) {
            if (fila.get(i).getId() == idPaciente) {
                return i + 1;
            }
        }
        return -1;
    }
}