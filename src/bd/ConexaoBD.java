package bd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexaoBD {

    // Arquivo procurado na pasta raiz do projeto (diretório de execução)
    private static final String ARQUIVO_CONFIG = "config.properties";

    private static String url;
    private static String usuario;
    private static String senha;

    private ConexaoBD() {
    }

    // Cada chamada abre uma conexão nova; quem chama deve fechar (try-with-resources)
    public static Connection getConexao() throws SQLException {
        carregarConfiguracao();
        return DriverManager.getConnection(url, usuario, senha);
    }

    // Lê o arquivo uma única vez e guarda os valores em memória
    private static synchronized void carregarConfiguracao() throws SQLException {
        if (url != null) {
            return;
        }

        File arquivo = new File(ARQUIVO_CONFIG);
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(arquivo)) {
            props.load(in);
        } catch (IOException e) {
            throw new SQLException(
                    "Arquivo de configuração não encontrado ou ilegível:\n" + arquivo.getAbsolutePath(), e);
        }

        String host = props.getProperty("db.host", "localhost");
        String porta = props.getProperty("db.porta", "3306");
        String banco = props.getProperty("db.nome", "clinica");
        String fuso = props.getProperty("db.timezone", "America/Sao_Paulo");

        // connectTimeout/socketTimeout evitam que a tela fique "pendurada" se o MySQL não responder
        url = "jdbc:mysql://" + host + ":" + porta + "/" + banco
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=" + fuso
                + "&connectTimeout=5000"
                + "&socketTimeout=15000";

        usuario = props.getProperty("db.usuario", "root");
        senha = props.getProperty("db.senha", "");
    }
}