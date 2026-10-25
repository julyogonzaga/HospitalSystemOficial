package dao;

import bd.ConexaoBanco;
import estrutura.Pacientes;
import tela.PainelPaciente;
import util.Validador;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    private static final String COLUNAS =
            "id, nome, nascimento, email, cpf, hora_chegada, atendido";

    // CREATE
    public Pacientes cadastrar(String nome, LocalDate nascimento, String email, String cpf)
            throws SQLException {

        String sql = "INSERT INTO pacientes (nome, nascimento, email, cpf, hora_chegada, atendido) "
                + "VALUES (?, ?, ?, ?, ?, FALSE)";

        Pacientes p = new Pacientes(nome, nascimento, email, cpf);

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNome());
            ps.setDate(2, Date.valueOf(p.getNascimento()));
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getCpf());
            ps.setTimestamp(5, Timestamp.valueOf(p.getHoraChegada()));
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    p.setId(chaves.getInt(1));
                }
            }
        }
        return p;
    }

    // READ (todos)
    public List<Pacientes> listarTodos() throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM pacientes ORDER BY id";
        List<Pacientes> lista = new ArrayList<>();

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // READ (por id)
    public Pacientes buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM pacientes WHERE id = ?";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // UPDATE
    public boolean atualizar(int id, String nome, LocalDate nascimento, String email, String cpf)
            throws SQLException {

        String sql = "UPDATE pacientes SET nome = ?, nascimento = ?, email = ?, cpf = ? WHERE id = ?";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setDate(2, Date.valueOf(nascimento));
            ps.setString(3, email);
            ps.setString(4, cpf);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean remover(int id) throws SQLException {
        String sql = "DELETE FROM pacientes WHERE id = ?";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Fila: não atendidos, ordenados por chegada
    public List<Pacientes> listarFila() throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM pacientes "
                + "WHERE atendido = FALSE ORDER BY hora_chegada, id";
        List<Pacientes> fila = new ArrayList<>();

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                fila.add(mapear(rs));
            }
        }
        return fila;
    }

    // Posição de um paciente na fila
    public int posicaoNaFila(int id) throws SQLException {
        Pacientes p = buscarPorId(id);
        if (p == null || p.isAtendido()) {
            return -1;
        }

        String sql = "SELECT COUNT(*) FROM pacientes WHERE atendido = FALSE "
                + "AND (hora_chegada < ? OR (hora_chegada = ? AND id <= ?))";
        Timestamp chegada = Timestamp.valueOf(p.getHoraChegada());

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, chegada);
            ps.setTimestamp(2, chegada);
            ps.setInt(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    // Marca o primeiro da fila como atendido
    public Pacientes atenderProximo() throws SQLException {
        String selecionar = "SELECT " + COLUNAS + " FROM pacientes "
                + "WHERE atendido = FALSE ORDER BY hora_chegada, id LIMIT 1 FOR UPDATE";
        String atualizar = "UPDATE pacientes SET atendido = TRUE WHERE id = ?";

        try (Connection con = ConexaoBanco.getConexao()) {
            con.setAutoCommit(false);
            try {
                Pacientes proximo;
                try (PreparedStatement ps = con.prepareStatement(selecionar);
                     ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return null;
                    }
                    proximo = mapear(rs);
                }

                try (PreparedStatement ps = con.prepareStatement(atualizar)) {
                    ps.setInt(1, proximo.getId());
                    ps.executeUpdate();
                }

                con.commit();
                proximo.setAtendido(true);
                return proximo;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // Converte uma linha do ResultSet em objeto Pacientes
    private Pacientes mapear(ResultSet rs) throws SQLException {
        Pacientes p = new Pacientes();
        p.setId(rs.getInt("id"));
        p.setNome(rs.getString("nome"));
        p.setNascimento(rs.getDate("nascimento").toLocalDate());
        p.setEmail(rs.getString("email"));
        p.setCpf(rs.getString("cpf"));

        Timestamp chegada = rs.getTimestamp("hora_chegada");
        p.setHoraChegada(chegada != null ? chegada.toLocalDateTime() : LocalDateTime.now());

        p.setAtendido(rs.getBoolean("atendido"));
        return p;
    }
}