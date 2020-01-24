package br.com.cod3r.cm.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


import java.util.function.Consumer;
public class Tabuleiro implements CampoObservador {

    private int linhas;
    private int colunas;
    private int minas;

    private final List<Campo> campos = new ArrayList<>();
    private final List<Consumer<Boolean>> observadores =
            new ArrayList<>();

    public Tabuleiro(int linhas, int colunas, int minas) {
        this.linhas = linhas;
        this.colunas = colunas;
        this.minas = minas;

        gerarCampos();
        associarVizinhos();
        sortearMinas();
    }

    public void registrarObservador(Consumer<Boolean> observador) {
        observadores.add(observador);
    }

    public void notificarObservadores(boolean resultado) {
        observadores.stream()
                .forEach(o -> o.accept(resultado));
    }


    private void gerarCampos() {
        for (int linha = 0; linha < linhas; linha++) {
            for (int coluna = 0; coluna < colunas; coluna++) {
                Campo campo = new Campo(linha, coluna);
                campo.registrarObservador(this);
                campos.add(campo);
            }
        }
    }

    private void associarVizinhos() {
        for (Campo c1 : campos) {
            for (Campo c2 : campos) {
                c1.adicionarVizinho(c2);
            }
        }
    }

    private void sortearMinas() {
        long minasArmadas = 0;
        Predicate<Campo> minado = Campo::isMinado;

        do {
            int aleatorio = (int) (Math.random() * campos.size());
            campos.get(aleatorio).minar();
            minasArmadas = campos.stream().filter(minado).count();
        } while (minasArmadas < minas);
    }

    public boolean objetivoAlcancado() {
        return campos.stream().allMatch(Campo::objetivoAlcancado);
    }

    public void reiniciar() {
        campos.stream().forEach(Campo::reiniciar);
        sortearMinas();
    }

    public void abrir(int linha, int coluna) {
        try {
            campos.parallelStream()
                    .filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
                    .findFirst()
                    .ifPresent(Campo::abrir);
        } catch (Exception e) {
            // FIXME ajustar a implementacao do metodo abrir
            campos.forEach(c -> c.setAberto(true));
            throw e;
        }
    }

    private void mostrarMinas () {
        campos.stream()
                .filter(c -> c.isMinado())
                .forEach(c -> c.setAberto(true));
    }

    public void alternarMarcacao(int linha, int coluna) {
        campos.parallelStream()
                .filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
                .findFirst()
                .ifPresent(Campo::alternarMarcacao);
    }


    @Override
    public void eventoOcorreu(Campo campo, CampoEvento evento) {
        if (evento == CampoEvento.EXPLODIR) {
            System.out.println("PERDEU... :(");
            mostrarMinas();
            notificarObservadores(false);
        } else if (objetivoAlcancado()) {
            System.out.println("Ganhou... :)");
            notificarObservadores(true);
        }

    }
}
