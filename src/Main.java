import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Define o visual padrão do sistema operacional
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Inicializa a interface gráfica
        SwingUtilities.invokeLater(() -> {
            TelaMedico tela = new TelaMedico();
            tela.setVisible(true);
        });
    }
}
